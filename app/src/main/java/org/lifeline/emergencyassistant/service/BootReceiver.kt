package org.lifeline.emergencyassistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.lifeline.emergencyassistant.LifelineApp
import org.lifeline.emergencyassistant.data.ProfileRepository
import org.lifeline.emergencyassistant.logging.ScrubbedLogger

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as LifelineApp
        val settings = ProfileRepository(context, app.store).settings()
        if (settings.fallDetectionEnabled && intent.action != null &&
            intent.action in listOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_LOCKED_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED)
        ) {
            ScrubbedLogger.d("boot: restarting fall detection if enabled")
            FallDetectionService.start(context)
        }
    }
}
