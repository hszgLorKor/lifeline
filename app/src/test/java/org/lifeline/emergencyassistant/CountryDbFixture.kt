package org.lifeline.emergencyassistant

import org.lifeline.emergencyassistant.geo.CountryDb
import org.lifeline.emergencyassistant.geo.CountryEntry

// Test fixture loader: constructs CountryDb from in-memory entries.
fun CountryDb.Companion.loadFixture(): CountryDb {
    val entries = listOf(
        CountryEntry("US", "United States", listOf("en"), "en", emptyList(), "911", listOf("911"),
            "fixture", "2026-10-31"),
        CountryEntry("DE", "Germany", listOf("de"), "en", emptyList(), "112", listOf("112","110"),
            "fixture", "2026-10-31"),
        CountryEntry("FR", "France", listOf("fr"), "fr", emptyList(), "112", listOf("112","17"),
            "fixture", "2026-10-31"),
        CountryEntry("JP", "Japan", listOf("ja"), "en", emptyList(), "110", listOf("110","118"),
            "fixture", "2026-10-31"),
        CountryEntry("IN", "India", listOf("hi", "en"), "hi", listOf("en"), "112", listOf("112","100"),
            "fixture", "2026-10-31")
    )
    return CountryDb.fromEntries(entries)
}
