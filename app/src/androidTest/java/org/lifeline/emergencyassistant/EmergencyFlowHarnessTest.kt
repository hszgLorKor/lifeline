package org.lifeline.emergencyassistant

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.lifeline.emergencyassistant.data.AppSettings
import org.lifeline.emergencyassistant.data.EncryptedStore
import org.lifeline.emergencyassistant.data.ProfileRepository
import org.lifeline.emergencyassistant.flow.EmergencyFlowController
import org.lifeline.emergencyassistant.geo.CountryDb
import org.lifeline.emergencyassistant.geo.CountryResolver
import org.lifeline.emergencyassistant.geo.NaturalEarthGeodata
import org.lifeline.emergencyassistant.geo.PlaceIndex

@RunWith(AndroidJUnit4::class)
class EmergencyFlowHarnessTest {
    @kotlinx.serialization.Serializable
    data class Probe(val value: String)

    private fun ctx() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun controller(): EmergencyFlowController {
        val app = ctx().applicationContext as LifelineApp
        val repo = ProfileRepository(ctx(), app.store)
        val db = CountryDb.load(ctx())
        val geo = NaturalEarthGeodata.load(ctx())
        val places = PlaceIndex.load(ctx())
        val resolver = CountryResolver(geo, db, object : org.lifeline.emergencyassistant.geo.MccProvider {
            override fun mccCountry(): Pair<String, Int>? = null
            override fun dualSimDisagreement() = false
        })
        return EmergencyFlowController(ctx(), repo, db, resolver, places)
    }

    @Test
    fun manualSmsRateLimit_enforced() {
        val app = ctx().applicationContext as LifelineApp
        val repo = ProfileRepository(ctx(), app.store)
        repo.saveSettings(repo.settings().copy(manualSmsEnabled = true, lastManualSmsAt = 0L))
        val c = controller()
        val now = 1_000_000L
        assertTrue(c.manualFlowSmsAllowed(now))
        c.markManualSmsSent(now)
        assertFalse(c.manualFlowSmsAllowed(now + 5 * 60 * 1000))
        assertTrue(c.manualFlowSmsAllowed(now + 10 * 60 * 1000 + 1))
    }

    @Test
    fun manualSmsToggleOff_blocks() {
        val app = ctx().applicationContext as LifelineApp
        val repo = ProfileRepository(ctx(), app.store)
        repo.saveSettings(repo.settings().copy(manualSmsEnabled = false))
        val c = controller()
        assertFalse(c.manualFlowSmsAllowed())
        repo.saveSettings(repo.settings().copy(manualSmsEnabled = true))
    }

    @Test
    fun dialerPrefill_usesResolvedNumber() {
        val c = controller()
        val flow = c.resolveFlowContext(manualOverride = "US")
        assertEquals("911", flow.number)
        val flowDe = c.resolveFlowContext(manualOverride = "DE")
        assertEquals("112", flowDe.number)
    }

    @Test
    fun encryptionRoundTrip_noPlaintextAtRest() {
        val store = EncryptedStore(ctx())
        store.putJson("probe", Probe("Aisha-Rahman-1985-O-neg"), Probe.serializer())
        val dir = java.io.File(ctx().filesDir, "kv")
        val raw = dir.listFiles()!!.first().readBytes().toString(Charsets.UTF_8)
        assertFalse(raw.contains("Aisha"))
        assertFalse(raw.contains("1985"))
        assertEquals("Aisha-Rahman-1985-O-neg", store.getJson<Probe>("probe", Probe("")).value)
        store.deleteAll()
    }

    @Test
    fun geoLookup_knownCities() {
        val geo = NaturalEarthGeodata.load(ctx())
        assertEquals("FR", geo.countryAt(org.lifeline.emergencyassistant.geo.GeoPoint(48.8566, 2.3522)))
        assertEquals("US", geo.countryAt(org.lifeline.emergencyassistant.geo.GeoPoint(41.8781, -87.6298)))
        assertEquals("JP", geo.countryAt(org.lifeline.emergencyassistant.geo.GeoPoint(35.6762, 139.6503)))
        assertEquals("EG", geo.countryAt(org.lifeline.emergencyassistant.geo.GeoPoint(30.0444, 31.2357)))
    }

    @Test
    fun placeIndex_findsCairo() {
        val places = PlaceIndex.load(ctx())
        val p = places.nearest(30.0444, 31.2357)!!
        assertEquals("EG", p.cc)
    }
}
