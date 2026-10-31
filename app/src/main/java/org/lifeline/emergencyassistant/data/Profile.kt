package org.lifeline.emergencyassistant.data

import kotlinx.serialization.Serializable

enum class BloodType(val ascii: String) {
    A_POS("A+"), A_NEG("A-"), B_POS("B+"), B_NEG("B-"),
    AB_POS("AB+"), AB_NEG("AB-"), O_POS("O+"), O_NEG("O-"),
    UNKNOWN("Unknown");

    companion object {
        fun fromAscii(s: String): BloodType =
            entries.firstOrNull { it.ascii.equals(s, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class Relation(val key: String) {
    SPOUSE("spouse"), PARENT("parent"), CHILD("child"), SIBLING("sibling"),
    OTHER_RELATIVE("other_relative"), FRIEND("friend"), COLLEAGUE("colleague");

    companion object {
        fun fromKey(k: String): Relation = entries.firstOrNull { it.key == k } ?: FRIEND
    }
}

@Serializable
data class StructuredEntry(
    val lexiconKey: String? = null,
    val customText: String? = null
) {
    val isCustom: Boolean get() = customText != null && lexiconKey == null
    fun displayRaw(sourcePackDefault: String): String = customText ?: lexiconKey ?: ""
}

@Serializable
data class EmergencyContact(
    val id: String,
    val name: String,
    val phoneE164: String,
    val relation: String = Relation.FRIEND.key,
    val relationCustom: String? = null,
    val smsLanguage: String
)

@Serializable
data class DoctorContact(
    val name: String = "",
    val phoneE164: String = ""
)

@Serializable
data class MedicalProfile(
    val fullName: String = "",
    val dateOfBirth: String = "",
    val bloodType: String = BloodType.UNKNOWN.ascii,
    val allergies: List<StructuredEntry> = emptyList(),
    val conditions: List<StructuredEntry> = emptyList(),
    val medications: List<StructuredEntry> = emptyList(),
    val implants: List<StructuredEntry> = emptyList(),
    val doctor: DoctorContact = DoctorContact(),
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val notes: String = "",
    val schemaVersion: Int = 1
)

@Serializable
data class TranslatedProfile(
    val language: String,
    val fields: Map<String, String>,
    val allergies: List<TranslatedEntry> = emptyList(),
    val conditions: List<TranslatedEntry> = emptyList(),
    val medications: List<TranslatedEntry> = emptyList(),
    val implants: List<TranslatedEntry> = emptyList(),
    val doctor: String = "",
    val contactsLine: String = "",
    val notes: String = "",
    val unmatchedTerms: List<String> = emptyList(),
    val status: TranslationStatus = TranslationStatus.UP_TO_DATE,
    val profileVersion: Long = 0L
)

@Serializable
data class TranslatedEntry(
    val text: String,
    val needsReview: Boolean = false,
    val transliterated: Boolean = false
)

enum class TranslationStatus { UP_TO_DATE, STALE, NEEDS_REVIEW, PACK_NOT_INSTALLED }

@Serializable
data class ProfileState(
    val profile: MedicalProfile = MedicalProfile(),
    val version: Long = 0L,
    val translations: Map<String, TranslatedProfile> = emptyMap()
)
