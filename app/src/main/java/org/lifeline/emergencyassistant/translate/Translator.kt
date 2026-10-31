package org.lifeline.emergencyassistant.translate

import org.lifeline.emergencyassistant.data.MedicalProfile
import org.lifeline.emergencyassistant.data.StructuredEntry
import org.lifeline.emergencyassistant.data.TranslatedEntry
import org.lifeline.emergencyassistant.data.TranslatedProfile
import org.lifeline.emergencyassistant.data.TranslationStatus
import org.lifeline.emergencyassistant.pack.LanguagePack
import org.lifeline.emergencyassistant.pack.Packs

class Translator(
    private val sourcePack: LanguagePack,
    private val targetPack: LanguagePack
) {
    private val targetLex: Map<String, String> by lazy {
        with(targetPack.lexicon) {
            allergies + conditions + medications + implants + bloodTypes + relations
        }
    }

    private fun translateEntry(e: StructuredEntry, category: String): TranslatedEntry {
        if (e.lexiconKey != null) {
            val target = targetLex[e.lexiconKey]
            return if (target != null) {
                TranslatedEntry(text = target, needsReview = false)
            } else {
                val src = sourceLexiconValue(category, e.lexiconKey)
                TranslatedEntry(
                    text = renderUnknown(src),
                    needsReview = true,
                    transliterated = Transliterator.needsTransliteration(src, targetPack.meta.script)
                )
            }
        }
        val custom = e.customText ?: ""
        return TranslatedEntry(
            text = renderUnknown(custom),
            needsReview = true,
            transliterated = Transliterator.needsTransliteration(custom, targetPack.meta.script)
        )
    }

    private fun sourceLexiconValue(category: String, key: String): String {
        val lex = sourcePack.lexicon
        val map = when (category) {
            "allergies" -> lex.allergies
            "conditions" -> lex.conditions
            "medications" -> lex.medications
            "implants" -> lex.implants
            "blood" -> lex.bloodTypes
            else -> lex.relations
        }
        return map[key] ?: key
    }

    private fun renderUnknown(raw: String): String {
        return if (Transliterator.needsTransliteration(raw, targetPack.meta.script)) {
            Transliterator.transliterate(raw, targetPack.meta.script)
        } else raw
    }

    fun translate(profile: MedicalProfile, profileVersion: Long): TranslatedProfile {
        val t = targetPack.templates
        val unmatched = mutableListOf<String>()
        fun field(key: String, value: String): String {
            if (value.isBlank()) return ""
            val tmpl = t.sentences[key] ?: return value
            return tmpl.replace("{value}", value)
        }
        fun entries(list: List<StructuredEntry>, category: String): List<TranslatedEntry> =
            list.map { e ->
                val te = translateEntry(e, category)
                if (te.needsReview) unmatched.add(te.text)
                te
            }

        val bloodKey = profile.bloodType.lowercase().replace("+", "_pos").replace("-", "_neg")
        val bloodAscii = profile.bloodType
        val bloodTranslated = targetPack.lexicon.bloodTypes[bloodKey]
            ?: targetPack.lexicon.bloodTypes[profile.bloodType]
            ?: bloodAscii

        val fields = mutableMapOf<String, String>()
        fields["name"] = field("name", profile.fullName)
        fields["dob"] = field("dob", profile.dateOfBirth)
        fields["blood_type"] = field("blood_type", bloodTranslated)

        val notesTranslated = translateNotes(profile.notes)
        if (notesTranslated.second.isNotEmpty()) unmatched.addAll(notesTranslated.second)

        return TranslatedProfile(
            language = targetPack.meta.bcp47,
            fields = fields,
            allergies = entries(profile.allergies, "allergies"),
            conditions = entries(profile.conditions, "conditions"),
            medications = entries(profile.medications, "medications"),
            implants = entries(profile.implants, "implants"),
            doctor = if (profile.doctor.name.isNotBlank() || profile.doctor.phoneE164.isNotBlank()) {
                t.sentences["doctor"]?.replace("{name}", profile.doctor.name)
                    ?.replace("{phone}", profile.doctor.phoneE164) ?: ""
            } else "",
            contactsLine = t.sentences["contacts"] ?: "",
            notes = notesTranslated.first,
            unmatchedTerms = unmatched.distinct(),
            status = if (unmatched.isEmpty()) TranslationStatus.UP_TO_DATE else TranslationStatus.NEEDS_REVIEW,
            profileVersion = profileVersion
        )
    }

    private fun translateNotes(notes: String): Pair<String, List<String>> {
        if (notes.isBlank()) return "" to emptyList()
        val sourceWords = sourcePack.lexicon.let {
            (it.allergies + it.conditions + it.medications + it.implants + it.bloodTypes + it.relations)
        }
        val targetWords = targetLex
        val unmatched = mutableListOf<String>()
        val out = notes.split(Regex("(\\s+)")).joinToString("") { token ->
            val key = sourceWords.entries.firstOrNull { it.value.equals(token, ignoreCase = true) }?.key
            if (key != null && targetWords[key] != null) {
                targetWords[key]!!
            } else {
                if (token.isNotBlank() && token.any { it.isLetter() }) unmatched.add(token)
                token
            }
        }
        return out to unmatched.distinct()
    }

    fun renderTtsScript(tp: TranslatedProfile): String {
        val t = targetPack.templates
        val lines = t.ttsScript.mapNotNull { step ->
            when (step) {
                "identity" -> tp.fields["name"]?.takeIf { it.isNotBlank() }
                "blood_type" -> tp.fields["blood_type"]?.takeIf { it.isNotBlank() }
                "allergies" -> tp.allergies.joinToString(", ") { it.text }.takeIf { tp.allergies.isNotEmpty() }
                    ?.let { t.sentences["tts_allergies"]?.replace("{values}", it) }
                "conditions" -> tp.conditions.joinToString(", ") { it.text }.takeIf { tp.conditions.isNotEmpty() }
                    ?.let { t.sentences["tts_conditions"]?.replace("{values}", it) }
                "medications" -> tp.medications.joinToString(", ") { it.text }.takeIf { tp.medications.isNotEmpty() }
                    ?.let { t.sentences["tts_medications"]?.replace("{values}", it) }
                "implants" -> tp.implants.joinToString(", ") { it.text }.takeIf { tp.implants.isNotEmpty() }
                    ?.let { t.sentences["tts_implants"]?.replace("{values}", it) }
                "doctor" -> tp.doctor.takeIf { it.isNotBlank() }
                "contacts" -> tp.contactsLine.takeIf { it.isNotBlank() }
                else -> null
            }
        }
        return lines.joinToString(". ")
    }

    fun renderSms(template: org.lifeline.emergencyassistant.pack.SmsTemplate, tp: TranslatedProfile,
                  location: SmsLocation?): String {
        fun list(items: List<TranslatedEntry>): String =
            if (items.isEmpty()) "" else items.joinToString(", ") { it.text }
        val place = location?.placeName?.takeIf { it.isNotBlank() } ?: ""
        return template.body
            .replace("{name}", tp.fields["name"] ?: "")
            .replace("{dob}", tp.fields["dob"] ?: "")
            .replace("{blood_type}", tp.fields["blood_type"] ?: "")
            .replace("{allergies}", list(tp.allergies))
            .replace("{conditions}", list(tp.conditions))
            .replace("{medications}", list(tp.medications))
            .replace("{implants}", list(tp.implants))
            .replace("{doctor}", tp.doctor)
            .replace("{location}", location?.coords ?: "")
            .replace("{place_name}", place)
            .replace("{timestamp}", location?.timestamp ?: "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

data class SmsLocation(
    val coords: String,
    val placeName: String = "",
    val timestamp: String = "",
    val lastKnown: Boolean = false
)
