package org.lifeline.emergencyassistant.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.lifeline.emergencyassistant.LifelineApp
import org.lifeline.emergencyassistant.data.ProfileRepository
import org.lifeline.emergencyassistant.geo.CountryDb
import org.lifeline.emergencyassistant.geo.CountryResolver
import org.lifeline.emergencyassistant.geo.NaturalEarthGeodata

class ResponderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SECURE.inv()
        )
        val app = application as LifelineApp
        val repo = ProfileRepository(this, app.store)
        val countryDb = CountryDb.load(this)
        val geodata = NaturalEarthGeodata.load(this)
        val resolver = CountryResolver(geodata, countryDb, object : org.lifeline.emergencyassistant.geo.MccProvider {
            override fun mccCountry(): Pair<String, Int>? = null
            override fun dualSimDisagreement(): Boolean = false
        })
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ResponderScreen(repo, countryDb, resolver, onBack = { finish() })
                }
            }
        }
    }
}
