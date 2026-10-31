package org.lifeline.emergencyassistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lifeline.emergencyassistant.data.ProfileRepository
import org.lifeline.emergencyassistant.emergency.EmergencyNumbers
import org.lifeline.emergencyassistant.geo.CountryDb
import org.lifeline.emergencyassistant.geo.CountryResolver
import org.lifeline.emergencyassistant.pack.Packs

@Composable
fun ResponderScreen(
    repo: ProfileRepository,
    countryDb: CountryDb,
    resolver: CountryResolver,
    onBack: () -> Unit
) {
    var lang by remember { mutableStateOf(repo.settings().activeRegionalLanguage ?: Packs.ENGLISH) }
    val tp = repo.translatedFor(lang) ?: repo.translatedFor(Packs.ENGLISH)
    val country = repo.settings().countryOverride ?: repo.settings().homeCountry

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("MEDICAL INFORMATION", fontSize = 30.sp)
        Text("Language shown: $lang", fontSize = 16.sp)
        Text("Country: ${countryDb.entry(country)?.name ?: country}  Emergency: ${EmergencyNumbers.primaryNumber(countryDb, country)}", fontSize = 18.sp)
        if (tp != null) {
            tp.fields["name"]?.takeIf { it.isNotBlank() }?.let { Text(it, fontSize = 24.sp) }
            tp.fields["dob"]?.takeIf { it.isNotBlank() }?.let { Text(it, fontSize = 22.sp) }
            tp.fields["blood_type"]?.takeIf { it.isNotBlank() }?.let { Text(it, fontSize = 26.sp) }
            if (tp.allergies.isNotEmpty()) Text("Allergies: " + tp.allergies.joinToString(", ") { it.text }, fontSize = 22.sp)
            if (tp.conditions.isNotEmpty()) Text("Conditions: " + tp.conditions.joinToString(", ") { it.text }, fontSize = 22.sp)
            if (tp.medications.isNotEmpty()) Text("Medications: " + tp.medications.joinToString(", ") { it.text }, fontSize = 22.sp)
            if (tp.implants.isNotEmpty()) Text("Implants: " + tp.implants.joinToString(", ") { it.text }, fontSize = 22.sp)
            if (tp.doctor.isNotBlank()) Text(tp.doctor, fontSize = 20.sp)
            if (tp.unmatchedTerms.isNotEmpty()) {
                Text("Needs review: " + tp.unmatchedTerms.joinToString(", "), fontSize = 16.sp)
            }
        } else {
            Text("Profile not saved yet.", fontSize = 22.sp)
        }
        Button(onClick = { lang = if (lang == Packs.ENGLISH) "en" else Packs.ENGLISH }, modifier = Modifier.fillMaxWidth()) {
            Text("Switch language (installed packs)", fontSize = 20.sp)
        }
        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Read aloud", fontSize = 24.sp) }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back", fontSize = 18.sp) }
        Text("Not a medical device. Translations are machine-assisted and may be inaccurate. Emergency numbers are bundled best-effort.", fontSize = 12.sp)
    }
}
