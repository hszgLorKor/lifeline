package org.lifeline.emergencyassistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lifeline.emergencyassistant.data.ProfileRepository
import org.lifeline.emergencyassistant.emergency.EmergencyNumbers
import org.lifeline.emergencyassistant.geo.CountryDb
import org.lifeline.emergencyassistant.geo.CountryResolver
import org.lifeline.emergencyassistant.geo.PlaceIndex

@Composable
fun HomeScreen(
    repo: ProfileRepository,
    countryDb: CountryDb,
    resolver: CountryResolver,
    places: PlaceIndex,
    onNavigate: (String) -> Unit
) {
    var country by remember { mutableStateOf(repo.settings().countryOverride ?: repo.settings().homeCountry) }
    var lastTrigger by remember { mutableStateOf("—") }
    LaunchedEffect(Unit) {
        lastTrigger = (repo.let { r ->
            val app = r; "—"
        })
    }
    val number = EmergencyNumbers.primaryNumber(countryDb, country)
    val lang = repo.settings().activeRegionalLanguage ?: resolver.regionalLanguageFor(country)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Lifeline", fontSize = 28.sp, color = MaterialTheme.colorScheme.onBackground)

        Button(
            onClick = { },
            modifier = Modifier.size(220.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Text("EMERGENCY\nhold 3 s", fontSize = 24.sp, color = Color.White)
        }

        Text("Country: ${countryDb.entry(country)?.name ?: country}   Number: $number", fontSize = 18.sp)
        Text("Language: $lang", fontSize = 18.sp)
        Text("Fall detection: ${if (repo.settings().fallDetectionEnabled) "ON" else "OFF"}   Last trigger: $lastTrigger", fontSize = 14.sp)

        Card(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate("responder") }) {
            Text("Medical information", fontSize = 22.sp, modifier = Modifier.padding(24.dp))
        }

        Button(onClick = { onNavigate("medical") }, modifier = Modifier.fillMaxWidth()) {
            Text("Medical data", fontSize = 20.sp)
        }
        Button(onClick = { onNavigate("settings") }, modifier = Modifier.fillMaxWidth()) {
            Text("Settings", fontSize = 20.sp)
        }
    }
}
