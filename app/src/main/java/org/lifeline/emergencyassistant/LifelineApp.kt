package org.lifeline.emergencyassistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import org.lifeline.emergencyassistant.crypto.CryptoStore
import org.lifeline.emergencyassistant.data.EncryptedStore
import org.lifeline.emergencyassistant.data.EventLog
import org.lifeline.emergencyassistant.logging.CrashLog
import org.lifeline.emergencyassistant.logging.ScrubbedLogger

class LifelineApp : Application() {
    lateinit var store: EncryptedStore
    lateinit var crypto: CryptoStore
    lateinit var events: EventLog
    lateinit var crashLog: CrashLog

    override fun onCreate() {
        super.onCreate()
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, t ->
            try {
                crashLog.record("uncaught", t)
            } catch (_: Throwable) {
            }
            prev?.uncaughtException(Thread.currentThread(), t)
        }
        crypto = CryptoStore(this)
        store = EncryptedStore(this)
        events = EventLog(store)
        crashLog = CrashLog(this, crypto)
        createChannels()
        ScrubbedLogger.d("app created")
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CH_FALL, "Fall detection", NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = "Persistent fall-detection service status" }
            )
            nm.createNotificationChannel(
                NotificationChannel(CH_EMERGENCY, "Emergency", NotificationManager.IMPORTANCE_HIGH)
                    .apply {
                        description = "Emergency alerts and read-aloud controls"
                        setSound(null, null)
                    }
            )
            nm.createNotificationChannel(
                NotificationChannel(CH_EVENTS, "Event confirmations", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    companion object {
        const val CH_FALL = "fall_detection"
        const val CH_EMERGENCY = "emergency"
        const val CH_EVENTS = "events"
    }
}
