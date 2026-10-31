package org.lifeline.emergencyassistant.emergency

import org.lifeline.emergencyassistant.geo.CountryDb

object EmergencyNumbers {
    fun primaryNumber(countryDb: CountryDb, countryCode: String): String =
        countryDb.entry(countryCode)?.primaryEmergencyNumber ?: "112"

    fun alternates(countryDb: CountryDb, countryCode: String): List<String> =
        countryDb.entry(countryCode)?.alternateNumbers ?: listOf("112")
}
