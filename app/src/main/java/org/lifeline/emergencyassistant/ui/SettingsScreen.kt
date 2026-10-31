package org.lifeline.emergencyassistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
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

@Composable
fun SettingsScreen(repo: ProfileRepository, onBack: () -> Unit) {
    var s by remember { mutableStateOf(repo.settings()) }
    fun update(n: org.lifeline.emergencyassistant.data.AppSettings) {
        repo.saveSettings(n); s = n
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", fontSize = 26.sp)
        Row("Fall detection", s.fallDetectionEnabled) { update(s.copy(fallDetectionEnabled = it)) }
        Row("Manual-flow SMS (default ON)", s.manualSmsEnabled) { update(s.copy(manualSmsEnabled = it)) }
        Row("SMS summary in alert notification (privacy risk on lock screen)", s.summaryInNotification) { update(s.copy(summaryInNotification = it)) }
        Row("App lock (biometric + PIN)", s.appLockEnabled) { update(s.copy(appLockEnabled = it)) }
        Button(onClick = { repo.cryptoErase() }, modifier = Modifier.fillMaxWidth()) {
            Text("Delete all data (crypto-erase, unrecoverable)", fontSize = 16.sp)
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back", fontSize = 18.sp) }
    }
}

@Composable
private fun Row(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Switch(checked, onChange)
    }
}
