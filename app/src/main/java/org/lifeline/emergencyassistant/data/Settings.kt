package org.lifeline.emergencyassistant.data

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val motherTongue: String = "en",
    val homeCountry: String = "US",
    val activeRegionalLanguage: String? = null,
    val countryOverride: String? = null,
    val consentGranted: Boolean = false,
    val appLockEnabled: Boolean = true,
    val appPinHash: String? = null,
    val appPinSalt: String? = null,
    val fallDetectionEnabled: Boolean = true,
    val fallSensitivity: String = "MEDIUM",
    val countdownSeconds: Int = 30,
    val fallStepSms: Boolean = true,
    val fallStepDialer: Boolean = true,
    val fallStepTts: Boolean = true,
    val manualSmsEnabled: Boolean = true,
    val manualTtsReads: Int = 1,
    val summaryInNotification: Boolean = false,
    val lastManualSmsAt: Long = 0L
) {
    companion object {
        const val KEY = "settings"
        const val RATE_LIMIT_MS = 10L * 60 * 1000
    }
}
