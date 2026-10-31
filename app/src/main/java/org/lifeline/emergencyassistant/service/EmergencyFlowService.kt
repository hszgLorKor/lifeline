package org.lifeline.emergencyassistant.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.lifeline.emergencyassistant.LifelineApp
import org.lifeline.emergencyassistant.data.AppSettings
import org.lifeline.emergencyassistant.data.EventLogEntry
import org.lifeline.emergencyassistant.data.ProfileRepository
import org.lifeline.emergencyassistant.logging.ScrubbedLogger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class EmergencyFlowService : Service() {

    private var callback: TelephonyCallback? = null
    private var observedOffHook = false
    var onOffHook: (() -> Unit)? = null
    var onIdle: (() -> Unit)? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        ) {
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    when (state) {
                        TelephonyManager.CALL_STATE_OFFHOOK -> {
                            observedOffHook = true
                            onOffHook?.invoke()
                        }
                        TelephonyManager.CALL_STATE_IDLE -> onIdle?.invoke()
                    }
                }
            }
            tm.registerTelephonyCallback(Executors.newSingleThreadExecutor(), callback!!)
        } else {
            ScrubbedLogger.d("call state unobservable: READ_PHONE_STATE not granted")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundWithType()
        return START_STICKY
    }

    private fun startForegroundWithType() {
        val notif: Notification = NotificationCompat.Builder(this, LifelineApp.CH_EMERGENCY)
            .setContentTitle("Emergency flow active")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(FGS_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(FGS_ID, notif)
        }
    }

    fun callStateObservable(): Boolean = callback != null

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            callback?.let { tm.unregisterTelephonyCallback(it) }
        }
        super.onDestroy()
    }

    companion object {
        const val FGS_ID = 1002

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, EmergencyFlowService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, EmergencyFlowService::class.java))
        }
    }
}
