package org.lifeline.emergencyassistant.flow

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.lifeline.emergencyassistant.LifelineApp
import org.lifeline.emergencyassistant.R
import org.lifeline.emergencyassistant.data.AppSettings
import org.lifeline.emergencyassistant.data.EmergencyContact
import org.lifeline.emergencyassistant.data.EventLogEntry
import org.lifeline.emergencyassistant.data.ProfileRepository
import org.lifeline.emergencyassistant.emergency.EmergencyNumbers
import org.lifeline.emergencyassistant.geo.CountryDb
import org.lifeline.emergencyassistant.geo.CountryResolver
import org.lifeline.emergencyassistant.geo.PlaceIndex
import org.lifeline.emergencyassistant.logging.ScrubbedLogger
import org.lifeline.emergencyassistant.pack.PackLoader
import org.lifeline.emergencyassistant.pack.Packs
import org.lifeline.emergencyassistant.service.EmergencyFlowService
import org.lifeline.emergencyassistant.sms.EmergencySmsSender
import org.lifeline.emergencyassistant.translate.SmsLocation
import org.lifeline.emergencyassistant.translate.Translator
import org.lifeline.emergencyassistant.tts.ReadAloud
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmergencyFlowController(
    private val context: Context,
    private val repo: ProfileRepository,
    private val countryDb: CountryDb,
    private val resolver: CountryResolver,
    private val places: PlaceIndex
) {
    private val app get() = context.applicationContext as LifelineApp

    data class FlowContext(
        val isFall: Boolean,
        val countryCode: String,
        val number: String,
        val regionalLanguage: String
    )

    fun resolveFlowContext(manualOverride: String? = null): FlowContext {
        val settings = repo.settings()
        val res = resolver.resolve(
            freshGps = null,
            lastKnownGps = null,
            manualOverride = settings.countryOverride ?: manualOverride
        )
        val lang = settings.activeRegionalLanguage
            ?: resolver.regionalLanguageFor(res.countryCode)
        val effectiveLang = if (PackLoader.listInstalled(context).any { it.bcp47 == lang }) lang else Packs.ENGLISH
        return FlowContext(
            isFall = false,
            countryCode = res.countryCode,
            number = EmergencyNumbers.primaryNumber(countryDb, res.countryCode),
            regionalLanguage = effectiveLang
        )
    }

    fun openDialer(number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun manualFlowSmsAllowed(now: Long = System.currentTimeMillis()): Boolean {
        val s = repo.settings()
        if (!s.manualSmsEnabled) return false
        return now - s.lastManualSmsAt >= AppSettings.RATE_LIMIT_MS
    }

    fun markManualSmsSent(now: Long = System.currentTimeMillis()) {
        repo.saveSettings(repo.settings().copy(lastManualSmsAt = now))
    }

    fun sendContactSms(flow: FlowContext, location: SmsLocation?): Map<String, String> {
        val state = repo.loadState()
        val sourcePack = PackLoader.load(context, repo.motherTongue())
        val contacts = state.profile.emergencyContacts
        val sender = EmergencySmsSender(context)
        val results = sender.sendToAll(contacts) { contact ->
            val lang = contact.smsLanguage
            val targetPack = runCatching { PackLoader.load(context, lang) }.getOrNull()
                ?: PackLoader.load(context, Packs.ENGLISH)
            val tp = Translator(sourcePack, targetPack).translate(state.profile, state.version)
            val tmpl = targetPack.templates.smsTemplates.first()
            val placeName = location?.placeName ?: ""
            Translator(sourcePack, targetPack).renderSms(tmpl, tp, location?.copy(placeName = placeName))
        }
        val nm = context.getSystemService(NotificationManager::class.java)
        val sentCount = results.count { it.status.startsWith("sent") }
        if (sentCount > 0) {
            val n = NotificationCompat.Builder(context, LifelineApp.CH_EVENTS)
                .setContentTitle("SMS sent")
                .setContentText("$sentCount contact(s) notified")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()
            nm.notify(3001, n)
        }
        return results.associate { it.contactId to it.status }
    }

    fun logEvent(trigger: String, flow: FlowContext, actions: List<String>, smsStatus: Map<String, String> = emptyMap()) {
        app.events.add(EventLogEntry(
            timestampIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date()),
            triggerType = trigger,
            country = flow.countryCode,
            emergencyNumber = flow.number,
            actions = actions,
            smsStatus = smsStatus
        ))
    }

    fun startShortLivedService() = EmergencyFlowService.start(context)
    fun stopShortLivedService() = EmergencyFlowService.stop(context)

    fun stageResponderOverLock(flow: FlowContext) {
        val intent = Intent(context, org.lifeline.emergencyassistant.ui.ResponderActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pi = PendingIntent.getActivity(context, 10, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val nm = context.getSystemService(NotificationManager::class.java)
        val settings = repo.settings()
        val body = if (settings.summaryInNotification) {
            "EMERGENCY — medical summary: " + buildSummary(flow)
        } else {
            "EMERGENCY — Tap to read this person's medical information"
        }
        val n = NotificationCompat.Builder(context, LifelineApp.CH_EMERGENCY)
            .setContentTitle("EMERGENCY")
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setFullScreenIntent(pi, true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .build()
        nm.notify(4001, n)
    }

    private fun buildSummary(flow: FlowContext): String {
        val tp = repo.translatedFor(flow.regionalLanguage)
        if (tp == null) return "(profile unavailable)"
        return buildString {
            tp.fields["name"]?.let { append(it).append("; ") }
            tp.fields["blood_type"]?.let { append(it).append("; ") }
            if (tp.allergies.isNotEmpty()) append("Allergies: ").append(tp.allergies.joinToString(", ") { it.text })
        }
    }
}
