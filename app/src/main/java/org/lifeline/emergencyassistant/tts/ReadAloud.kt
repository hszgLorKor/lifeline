package org.lifeline.emergencyassistant.tts

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import org.lifeline.emergencyassistant.LifelineApp
import org.lifeline.emergencyassistant.logging.ScrubbedLogger
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class ReadAloud(private val context: Context) {

    fun onInit(status: Int) {}


    private var tts: TextToSpeech? = null
    private val ready = AtomicBoolean(false)
    private var onDone: (() -> Unit)? = null
    private var onError: ((String) -> Unit)? = null
    private var savedAlarmVolume: Int? = null
    private var currentId: String = ""

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val nm: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun init(onReady: (Boolean) -> Unit) {
        tts = TextToSpeech(context, { status ->
            ready.set(status == TextToSpeech.SUCCESS)
            if (status == TextToSpeech.SUCCESS) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String) {}
                    override fun onDone(utteranceId: String) {
                        if (utteranceId == currentId) onDone?.invoke()
                    }
                    override fun onError(utteranceId: String) {
                        if (utteranceId == currentId) onError?.invoke("tts_error")
                    }
                })
            }
            onReady(status == TextToSpeech.SUCCESS)
        })
    }

    fun setLocale(locale: Locale) {
        tts?.language = locale
    }

    fun voiceAvailable(locale: Locale): Boolean {
        val t = tts ?: return false
        val r = t.isLanguageAvailable(locale)
        return r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun speak(text: String, volumeFloorPercent: Int, flow: String,
              onDoneCb: (() -> Unit)? = null, onErrorCb: ((String) -> Unit)? = null) {
        val t = tts ?: run { onErrorCb?.invoke("tts_not_initialized"); return }
        if (text.isBlank()) { onDoneCb?.invoke(); return }
        currentId = UUID.randomUUID().toString()
        onDone = onDoneCb
        onError = onErrorCb
        applyVolumeFloor(volumeFloorPercent)
        showControlNotification(flow)
        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM.toString())
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        t.speak(text, TextToSpeech.QUEUE_FLUSH, params, currentId)
    }

    fun stop() {
        tts?.stop()
        restoreVolume()
        nm.cancel(NOTIF_ID)
    }

    fun applyVolumeFloor(percent: Int) {
        if (savedAlarmVolume == null) savedAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val floor = (max * percent / 100.0).toInt().coerceAtLeast(1)
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) < floor) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, floor, 0)
        }
    }

    fun restoreVolume() {
        savedAlarmVolume?.let {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, it, 0)
            savedAlarmVolume = null
        }
    }

    private fun showControlNotification(flow: String) {
        val stopIntent = Intent(context, ReadAloudReceiver::class.java).apply {
            action = ReadAloudReceiver.ACTION_STOP
        }
        val replayIntent = Intent(context, ReadAloudReceiver::class.java).apply {
            action = ReadAloudReceiver.ACTION_REPLAY
        }
        val stopPi = PendingIntent.getBroadcast(context, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val replayPi = PendingIntent.getBroadcast(context, 2, replayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n: Notification = androidx.core.app.NotificationCompat.Builder(context, LifelineApp.CH_EMERGENCY)
            .setContentTitle("Read-aloud active ($flow)")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Stop read-aloud", stopPi)
            .addAction(android.R.drawable.ic_media_play, "Replay", replayPi)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_CALL)
            .build()
        nm.notify(NOTIF_ID, n)
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }

    companion object {
        const val NOTIF_ID = 2001
    }
}

class ReadAloudReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ScrubbedLogger.d("read-aloud control: ${intent.action}")
    }

    companion object {
        const val ACTION_STOP = "org.lifeline.emergencyassistant.action.STOP_TTS"
        const val ACTION_REPLAY = "org.lifeline.emergencyassistant.action.REPLAY_TTS"
    }
}
