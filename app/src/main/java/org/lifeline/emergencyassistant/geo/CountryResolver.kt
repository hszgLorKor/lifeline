package org.lifeline.emergencyassistant.geo


data class GeoPoint(val lat: Double, val lon: Double)

data class ResolutionResult(
    val countryCode: String,
    val source: Source,
    val confidence: Confidence
)

enum class Source { GPS_FRESH, GPS_LAST_KNOWN, MCC, MANUAL }
enum class Confidence { HIGH, LOW }

interface CountryGeodata {
    fun countryAt(point: GeoPoint): String?
}

interface MccProvider {
    fun mccCountry(): Pair<String, Int>? // iso code, mcc
    fun dualSimDisagreement(): Boolean
}

class CountryResolver(
    private val geodata: CountryGeodata,
    private val countryDb: CountryDb,
    private val mccProvider: MccProvider
) {
    fun resolve(
        freshGps: GeoPoint?,
        lastKnownGps: Pair<GeoPoint, Long>?,
        nowMillis: Long = System.currentTimeMillis(),
        manualOverride: String? = null
    ): ResolutionResult {
        manualOverride?.let { return ResolutionResult(it, Source.MANUAL, Confidence.HIGH) }
        freshGps?.let { p ->
            geodata.countryAt(p)?.let { return ResolutionResult(it, Source.GPS_FRESH, Confidence.HIGH) }
        }
        lastKnownGps?.let { (p, ts) ->
            if (nowMillis - ts < 24L * 3600 * 1000) {
                geodata.countryAt(p)?.let { return ResolutionResult(it, Source.GPS_LAST_KNOWN, Confidence.HIGH) }
            }
        }
        mccProvider.mccCountry()?.let { (iso, mcc) ->
            val conf = if (mccProvider.dualSimDisagreement()) Confidence.LOW else Confidence.HIGH
            if (conf == Confidence.HIGH) return ResolutionResult(iso, Source.MCC, conf)
            return ResolutionResult(iso, Source.MCC, Confidence.LOW)
        }
        return ResolutionResult(countryDb.defaultHomeCountry, Source.MANUAL, Confidence.LOW)
    }

    fun regionalLanguageFor(countryCode: String): String =
        countryDb.entry(countryCode)?.defaultRegionalLanguage ?: "en"
}
