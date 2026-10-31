package org.lifeline.emergencyassistant.sms

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import org.lifeline.emergencyassistant.data.EmergencyContact
import org.lifeline.emergencyassistant.data.EventLogEntry
import org.lifeline.emergencyassistant.logging.ScrubbedLogger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmergencySmsSender(private val context: Context) {

    data class Result(val contactId: String, val status: String)

    fun sendToAll(
        contacts: List<EmergencyContact>,
        render: (EmergencyContact) -> String
    ): List<Result> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ScrubbedLogger.d("sms skipped: SEND_SMS not granted")
            return contacts.map { Result(it.id, "skipped_permission_denied") }
        }
        val sm = context.getSystemService(SmsManager::class.java)
            ?: SmsManager.getDefault()
        return contacts.map { contact ->
            val text = render(contact)
            if (text.isBlank()) return@map Result(contact.id, "skipped_empty")
            try {
                val parts = sm.divideMessage(text)
                val sentIntents = ArrayList(parts.map {
                    PendingIntent.getBroadcast(context, 0,
                        Intent(SMS_SENT_ACTION).putExtra("contactId", contact.id),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                })
                sm.sendMultipartTextMessage(contact.phoneE164, null, ArrayList(parts), sentIntents, null)
                Result(contact.id, "sent")
            } catch (e: Exception) {
                ScrubbedLogger.e("sms send failed")
                try {
                    sm.sendTextMessage(contact.phoneE164, null, text, null, null)
                    Result(contact.id, "sent_retry")
                } catch (e2: Exception) {
                    Result(contact.id, "failed")
                }
            }
        }
    }

    companion object {
        const val SMS_SENT_ACTION = "org.lifeline.emergencyassistant.action.SMS_SENT"
    }
}
