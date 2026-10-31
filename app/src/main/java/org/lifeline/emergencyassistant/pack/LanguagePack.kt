package org.lifeline.emergencyassistant.pack

import kotlinx.serialization.Serializable

@Serializable
data class PackMeta(
    val bcp47: String,
    val displayName: String,
    val script: String,
    val ttsLocale: String,
    val rtl: Boolean,
    val packSchemaVersion: Int,
    val reviewStatus: String = "UNREVIEWED"
)

@Serializable
data class LexiconEntry(
    val key: String,
    val terms: Map<String, String> = emptyMap()
)

@Serializable
data class Lexicon(
    val allergies: Map<String, String> = emptyMap(),
    val conditions: Map<String, String> = emptyMap(),
    val medications: Map<String, String> = emptyMap(),
    val implants: Map<String, String> = emptyMap(),
    val bloodTypes: Map<String, String> = emptyMap(),
    val relations: Map<String, String> = emptyMap()
)

@Serializable
data class Templates(
    val fieldLabels: Map<String, String> = emptyMap(),
    val sentences: Map<String, String> = emptyMap(),
    val ttsScript: List<String> = emptyList(),
    val smsTemplates: List<SmsTemplate> = emptyList(),
    val ui: Map<String, String> = emptyMap(),
    val warnings: Map<String, String> = emptyMap()
)

@Serializable
data class SmsTemplate(
    val id: String,
    val body: String,
    val maxSegments: Int = 2
)

@Serializable
data class PlaceNames(
    val endonyms: Map<String, String> = emptyMap(),
    val transliterations: Map<String, String> = emptyMap()
)

@Serializable
data class LanguagePack(
    val meta: PackMeta,
    val lexicon: Lexicon,
    val templates: Templates,
    val placeNames: PlaceNames = PlaceNames()
)

object Packs {
    const val SUPPORTED_SCHEMA_VERSION = 1
    const val ENGLISH = "en"

    fun validateSchema(pack: LanguagePack): Result<LanguagePack> {
        return if (pack.meta.packSchemaVersion > SUPPORTED_SCHEMA_VERSION) {
            Result.failure(UnsupportedPackSchemaException(pack.meta.bcp47, pack.meta.packSchemaVersion))
        } else {
            Result.success(pack)
        }
    }
}

class UnsupportedPackSchemaException(val bcp47: String, val version: Int) :
    Exception("Language pack '$bcp47' schema version $version exceeds supported ${Packs.SUPPORTED_SCHEMA_VERSION}; rejected, never partially loaded")
