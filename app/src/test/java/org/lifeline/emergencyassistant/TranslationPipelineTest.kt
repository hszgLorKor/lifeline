package org.lifeline.emergencyassistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.lifeline.emergencyassistant.data.BloodType
import org.lifeline.emergencyassistant.data.MedicalProfile
import org.lifeline.emergencyassistant.data.StructuredEntry
import org.lifeline.emergencyassistant.data.TranslationStatus
import org.lifeline.emergencyassistant.pack.LanguagePack
import org.lifeline.emergencyassistant.pack.Lexicon
import org.lifeline.emergencyassistant.pack.PackMeta
import org.lifeline.emergencyassistant.pack.Packs
import org.lifeline.emergencyassistant.pack.SmsTemplate
import org.lifeline.emergencyassistant.pack.Templates
import org.lifeline.emergencyassistant.translate.Translator

class TranslationPipelineTest {

    private fun enPack(): LanguagePack = LanguagePack(
        meta = PackMeta("en", "English", "Latin", "en-US", false, 1),
        lexicon = Lexicon(
            allergies = mapOf("allergy_peanut" to "Peanut", "allergy_penicillin" to "Penicillin"),
            conditions = mapOf("cond_asthma" to "Asthma"),
            medications = mapOf("med_metformin" to "Metformin"),
            implants = mapOf("imp_pacemaker" to "Pacemaker"),
            bloodTypes = mapOf("a_pos" to "A positive", "A+" to "A+"),
            relations = mapOf("friend" to "Friend")
        ),
        templates = Templates(
            sentences = mapOf(
                "name" to "Name: {value}", "dob" to "DOB: {value}", "blood_type" to "Blood type: {value}",
                "tts_allergies" to "Allergies: {values}"
            ),
            ttsScript = listOf("identity", "blood_type", "allergies"),
            smsTemplates = listOf(SmsTemplate("default", "EMERGENCY {name} blood {blood_type} allergies {allergies} loc {location} {place_name} {timestamp}", 2))
        )
    )

    private fun zhPack(): LanguagePack = LanguagePack(
        meta = PackMeta("zh-CN", "简体中文", "Han", "zh-CN", false, 1),
        lexicon = Lexicon(
            allergies = mapOf("allergy_peanut" to "花生"),
            conditions = mapOf("cond_asthma" to "哮喘"),
            medications = emptyMap(),
            implants = emptyMap(),
            bloodTypes = mapOf("a_pos" to "A阳性"),
            relations = emptyMap()
        ),
        templates = Templates(
            sentences = mapOf("name" to "姓名: {value}", "blood_type" to "血型: {value}", "tts_allergies" to "过敏: {values}"),
            ttsScript = listOf("identity", "blood_type", "allergies"),
            smsTemplates = listOf(SmsTemplate("default", "紧急 {name} 血型 {blood_type} 过敏 {allergies} 位置 {place_name} {location} {timestamp}", 2))
        )
    )

    private fun profile() = MedicalProfile(
        fullName = "Test Person",
        dateOfBirth = "1990-01-01",
        bloodType = BloodType.A_POS.ascii,
        allergies = listOf(StructuredEntry(lexiconKey = "allergy_peanut")),
        conditions = listOf(StructuredEntry(lexiconKey = "cond_asthma")),
        medications = listOf(StructuredEntry(customText = "SomeBrandXYZ"))
    )

    @Test
    fun `lexicon-selected values translate with template coverage`() {
        val tp = Translator(enPack(), zhPack()).translate(profile(), 1)
        assertEquals("姓名: Test Person", tp.fields["name"])
        assertEquals("血型: A阳性", tp.fields["blood_type"])
        assertEquals("花生", tp.allergies[0].text)
        assertFalse(tp.allergies[0].needsReview)
        assertEquals("哮喘", tp.conditions[0].text)
    }

    @Test
    fun `custom entries flagged needs review and never dropped`() {
        val tp = Translator(enPack(), zhPack()).translate(profile(), 1)
        val med = tp.medications[0]
        assertTrue(med.needsReview)
        assertTrue(med.text.contains("SomeBrandXYZ"))
        assertEquals(TranslationStatus.NEEDS_REVIEW, tp.status)
        assertTrue(tp.unmatchedTerms.isNotEmpty())
    }

    @Test
    fun `tts script renders only non-empty structured fields`() {
        val tp = Translator(enPack(), zhPack()).translate(profile(), 1)
        val script = Translator(enPack(), zhPack()).renderTtsScript(tp)
        assertTrue(script.contains("姓名: Test Person"))
        assertTrue(script.contains("过敏: 花生"))
        assertFalse(script.contains("notes"))
    }

    @Test
    fun `sms template renders with placeholders filled and empties dropped`() {
        val tp = Translator(enPack(), zhPack()).translate(profile(), 1)
        val t = Translator(enPack(), zhPack())
        val text = t.renderSms(
            zhPack().templates.smsTemplates[0], tp,
            org.lifeline.emergencyassistant.translate.SmsLocation("31.23000,121.47000", "上海", "2026-10-31T10:00:00+0800")
        )
        assertTrue(text.contains("31.23000,121.47000"))
        assertTrue(text.contains("上海"))
        assertFalse(text.contains("{name}"))
    }

    @Test
    fun `higher schema version is rejected, never partially loaded`() {
        val future = zhPack().copy(meta = zhPack().meta.copy(packSchemaVersion = 99))
        val result = Packs.validateSchema(future)
        assertTrue(result.isFailure)
    }

    @Test
    fun `english pack is identity for english source`() {
        val tp = Translator(enPack(), enPack()).translate(profile(), 1)
        assertEquals("Name: Test Person", tp.fields["name"])
        assertEquals(TranslationStatus.NEEDS_REVIEW, tp.status) // custom med flagged
    }
}
