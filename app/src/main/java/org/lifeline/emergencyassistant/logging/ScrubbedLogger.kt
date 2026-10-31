package org.lifeline.emergencyassistant.logging

import android.util.Log
import org.lifeline.emergencyassistant.data.MedicalProfile

object ScrubbedLogger {
    private const val TAG = "Lifeline"
    private val sensitiveMarkers = listOf(
        "name=", "dob=", "blood=", "allerg", "medicat", "condition", "implant",
        "phone", "contact", "note"
    )

    fun d(msg: String) {
        if (isSensitive(msg)) return
        Log.d(TAG, msg)
    }

    fun e(msg: String, t: Throwable? = null) {
        if (isSensitive(msg)) return
        Log.e(TAG, msg, t)
    }

    fun isSensitive(msg: String): Boolean {
        val lower = msg.lowercase()
        return sensitiveMarkers.any { lower.contains(it) }
    }

    fun scrub(msg: String): String =
        if (isSensitive(msg)) "[scrubbed]" else msg

    fun assertNoProfileValues(profile: MedicalProfile, msg: String): Boolean {
        val values = buildList {
            add(profile.fullName); add(profile.dateOfBirth); add(profile.bloodType)
            add(profile.notes)
            profile.allergies.forEach { add(it.customText ?: ""); add(it.lexiconKey ?: "") }
            profile.conditions.forEach { add(it.customText ?: ""); add(it.lexiconKey ?: "") }
            profile.medications.forEach { add(it.customText ?: ""); add(it.lexiconKey ?: "") }
            profile.implants.forEach { add(it.customText ?: ""); add(it.lexiconKey ?: "") }
            add(profile.doctor.name); add(profile.doctor.phoneE164)
            profile.emergencyContacts.forEach { add(it.name); add(it.phoneE164) }
        }.filter { it.length >= 3 }
        return values.none { msg.contains(it, ignoreCase = true) }
    }
}
