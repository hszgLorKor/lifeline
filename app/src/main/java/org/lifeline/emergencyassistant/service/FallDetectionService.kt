package org.lifeline.emergencyassistant.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.lifeline.emergencyassistant.LifelineApp
import org.lifeline.emergencyassistant.R
import org.lifeline.emergencyassistant.data.AppSettings
import org.lifeline.emergencyassistant.data.EventLogEntry
import org.lifeline.emergencyassistant.fall.FallDetector
import org.lifeline.emergencyassistant.fall.Sensitivity
import org.lifeline.emergencyassistant.logging.ScrubbedLogger
import org.lifeline.emergencyassistant.ui.FallOverlayActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FallDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null
    private var detector: FallDetector = FallDetector(Sensitivity.MEDIUM)
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lifeline:falldetect")
        wakeLock?.acquire()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val settings = (application as LifelineApp).let { app ->
            org.lifeline.emergencyassistant.data.ProfileRepository(this, app.store).settings()
        }
        detector = FallDetector(Sensitivity.valueOf(settings.fallSensitivity))
        startForegroundWithType()
        if (accel != null && ContextCompat.checkSelfPermission(this, Manifest.permission.HIGH_SAMPLING_RATE_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME)
        } else {
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        }
        return START_STICKY
    }

    private fun startForegroundWithType() {
        val intent = Intent(this, FallOverlayActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notif: Notification = NotificationCompat.Builder(this, LifelineApp.CH_FALL)
            .setContentTitle("Fall detection active")
            .setContentText("Lifeline is monitoring for falls")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setContentIntent(pi)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(FGS_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH or ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(FGS_ID, notif)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        val sample = org.lifeline.emergencyassistant.fall.AccelSample(
            System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]
        )
        if (detector.onSample(sample)) {
            onFallDetected()
        }
    }

    private fun onFallDetected() {
        val app = application as LifelineApp
        app.events.add(EventLogEntry(
            timestampIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date()),
            triggerType = "fall detected"
        ))
        ScrubbedLogger.d("fall detected")
        val intent = Intent(this, FallOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        wakeLock?.release()
        super.onDestroy()
    }

    companion object {
        const val FGS_ID = 1001

        fun start(context: Context) {
            val i = Intent(context, FallDetectionService::class.java)
            ContextCompat.startForegroundService(context, i)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FallDetectionService::class.java))
        }
    }
}
