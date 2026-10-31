package org.lifeline.emergencyassistant.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.lifeline.emergencyassistant.LifelineApp
import org.lifeline.emergencyassistant.data.EncryptedStore
import org.lifeline.emergencyassistant.data.ProfileRepository
import org.lifeline.emergencyassistant.geo.CountryDb
import org.lifeline.emergencyassistant.geo.CountryResolver
import org.lifeline.emergencyassistant.geo.NaturalEarthGeodata
import org.lifeline.emergencyassistant.geo.PlaceIndex

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        val app = application as LifelineApp
        val repo = ProfileRepository(this, app.store)
        val countryDb = CountryDb.load(this)
        val geodata = NaturalEarthGeodata.load(this)
        val places = PlaceIndex.load(this)
        val resolver = CountryResolver(geodata, countryDb, object : org.lifeline.emergencyassistant.geo.MccProvider {
            override fun mccCountry(): Pair<String, Int>? = null
            override fun dualSimDisagreement(): Boolean = false
        })
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val nav = rememberNavController()
                    NavHost(nav, startDestination = "home") {
                        composable("home") { HomeScreen(repo, countryDb, resolver, places, onNavigate = { nav.navigate(it) }) }
                        composable("medical") { MedicalDataScreen(repo, onBack = { nav.popBackStack() }) }
                        composable("settings") { SettingsScreen(repo, onBack = { nav.popBackStack() }) }
                        composable("responder") { ResponderScreen(repo, countryDb, resolver, onBack = { nav.popBackStack() }) }
                    }
                }
            }
        }
    }
}
