package org.lifeline.emergencyassistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lifeline.emergencyassistant.data.BloodType
import org.lifeline.emergencyassistant.data.MedicalProfile
import org.lifeline.emergencyassistant.data.ProfileRepository

@Composable
fun MedicalDataScreen(repo: ProfileRepository, onBack: () -> Unit) {
    var state = repo.loadState()
    var name by remember { mutableStateOf(state.profile.fullName) }
    var dob by remember { mutableStateOf(state.profile.dateOfBirth) }
    var blood by remember { mutableStateOf(state.profile.bloodType) }
    var allergies by remember { mutableStateOf(state.profile.allergies.joinToString(", ") { it.customText ?: it.lexiconKey ?: "" }) }
    var notes by remember { mutableStateOf(state.profile.notes) }
    var saved by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Medical data", fontSize = 26.sp)
        OutlinedTextField(name, { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(dob, { dob = it }, label = { Text("Date of birth") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(blood, { blood = it }, label = { Text("Blood type (A+, O-, ...)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(allergies, { allergies = it }, label = { Text("Allergies (comma separated)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes (free text)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val entries = allergies.split(",").map { it.trim() }.filter { it.isNotBlank() }
                .map { org.lifeline.emergencyassistant.data.StructuredEntry(customText = it) }
            val updated = state.profile.copy(
                fullName = name, dateOfBirth = dob,
                bloodType = BloodType.fromAscii(blood).ascii,
                allergies = entries, notes = notes
            )
            repo.saveProfile(updated)
            saved = true
        }, modifier = Modifier.fillMaxWidth()) {
            Text(if (saved) "Saved + translated" else "Save (encrypts & pre-translates)", fontSize = 18.sp)
        }
        Button(onClick = { onBack() }, modifier = Modifier.fillMaxWidth()) { Text("Back", fontSize = 18.sp) }
    }
}
