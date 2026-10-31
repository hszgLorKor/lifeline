package org.lifeline.emergencyassistant

import org.junit.Assert.assertEquals
import org.junit.Test
import org.lifeline.emergencyassistant.geo.CountryDb
import org.lifeline.emergencyassistant.geo.CountryGeodata
import org.lifeline.emergencyassistant.geo.CountryResolver
import org.lifeline.emergencyassistant.geo.Confidence
import org.lifeline.emergencyassistant.geo.GeoPoint
import org.lifeline.emergencyassistant.geo.MccProvider
import org.lifeline.emergencyassistant.geo.Source
import org.lifeline.emergencyassistant.geo.pointInPolygon

class FakeGeo(private val inside: Set<String>) : CountryGeodata {
    override fun countryAt(point: GeoPoint): String? {
        // grid lookup: lat/lon -> iso if in set (test stub)
        return inside.firstOrNull()
    }
}

class CountryResolverTest {

    private fun db(): CountryDb = CountryDb.loadFixture()

    @Test
    fun `fresh GPS wins over MCC and last-known`() {
        val geo = FakeGeo(setOf("DE"))
        val mcc = object : MccProvider {
            override fun mccCountry() = "US" to 310
            override fun dualSimDisagreement() = false
        }
        val r = CountryResolver(geo, db(), mcc).resolve(
            freshGps = GeoPoint(48.1, 11.6),
            lastKnownGps = GeoPoint(40.7, -74.0) to 0L
        )
        assertEquals("DE", r.countryCode)
        assertEquals(Source.GPS_FRESH, r.source)
    }

    @Test
    fun `stale last-known GPS falls through to MCC`() {
        val geo = FakeGeo(setOf("FR"))
        val mcc = object : MccProvider {
            override fun mccCountry() = "US" to 310
            override fun dualSimDisagreement() = false
        }
        val now = 10_000_000L
        val r = CountryResolver(geo, db(), mcc).resolve(
            freshGps = null,
            lastKnownGps = GeoPoint(48.8, 2.3) to (now - 25L * 3600 * 1000),
            nowMillis = now
        )
        assertEquals("US", r.countryCode)
        assertEquals(Source.MCC, r.source)
    }

    @Test
    fun `fresh last-known GPS within 24h is used`() {
        val geo = FakeGeo(setOf("FR"))
        val mcc = object : MccProvider {
            override fun mccCountry() = "US" to 310
            override fun dualSimDisagreement() = false
        }
        val now = 10_000_000L
        val r = CountryResolver(geo, db(), mcc).resolve(
            freshGps = null,
            lastKnownGps = GeoPoint(48.8, 2.3) to (now - 3600_000L),
            nowMillis = now
        )
        assertEquals("FR", r.countryCode)
        assertEquals(Source.GPS_LAST_KNOWN, r.source)
    }

    @Test
    fun `manual override beats everything`() {
        val geo = FakeGeo(setOf("DE"))
        val mcc = object : MccProvider {
            override fun mccCountry() = "US" to 310
            override fun dualSimDisagreement() = false
        }
        val r = CountryResolver(geo, db(), mcc).resolve(
            freshGps = GeoPoint(48.1, 11.6),
            lastKnownGps = null,
            manualOverride = "JP"
        )
        assertEquals("JP", r.countryCode)
        assertEquals(Source.MANUAL, r.source)
    }

    @Test
    fun `dual-SIM disagreement marks MCC low-confidence`() {
        val geo = FakeGeo(emptySet())
        val mcc = object : MccProvider {
            override fun mccCountry() = "US" to 310
            override fun dualSimDisagreement() = true
        }
        val r = CountryResolver(geo, db(), mcc).resolve(null, null)
        assertEquals(Confidence.LOW, r.confidence)
    }

    @Test
    fun `point in polygon ray casting works for simple square`() {
        val square = listOf(
            GeoPoint(0.0, 0.0), GeoPoint(0.0, 10.0), GeoPoint(10.0, 10.0), GeoPoint(10.0, 0.0)
        )
        assertEquals(true, pointInPolygon(GeoPoint(5.0, 5.0), square))
        assertEquals(false, pointInPolygon(GeoPoint(15.0, 5.0), square))
    }
}
