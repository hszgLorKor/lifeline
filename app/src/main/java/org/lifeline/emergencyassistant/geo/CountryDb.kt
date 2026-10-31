package org.lifeline.emergencyassistant.geo

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CountryEntry(
    val iso2: String,
    val name: String,
    val officialLanguages: List<String>,
    val defaultRegionalLanguage: String,
    val alternates: List<String> = emptyList(),
    val primaryEmergencyNumber: String,
    val alternateNumbers: List<String> = emptyList(),
    val source: String,
    val lastVerified: String
)

class CountryDb private constructor(private val entries: Map<String, CountryEntry>) {
    data class CountryEntry2(val entry: CountryEntry)
    val defaultHomeCountry: String = "US"

    fun entry(iso2: String): CountryEntry? = entries[iso2.uppercase()]
    fun all(): List<CountryEntry> = entries.values.sortedBy { it.name }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromEntries(entries: List<CountryEntry>): CountryDb =
            CountryDb(entries.associateBy { it.iso2.uppercase() })

        fun load(context: Context): CountryDb {
            val raw = context.assets.open("data/countries.json").use {
                it.readBytes().toString(Charsets.UTF_8)
            }
            val list = json.decodeFromString<List<CountryEntry>>(raw)
            return CountryDb(list.associateBy { it.iso2.uppercase() })
        }
    }
}
