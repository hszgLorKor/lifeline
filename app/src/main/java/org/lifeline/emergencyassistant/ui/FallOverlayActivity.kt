package org.lifeline.emergencyassistant.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lifeline.emergencyassistant.LifelineApp
import org.lifeline.emergencyassistant.data.EventLogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FallOverlayActivity : ComponentActivity() {
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        window.attributes.screenBrightness = 1.0f
        val app = application as LifelineApp
        val countdownSec = app.let { a ->
            org.lifeline.emergencyassistant.data.ProfileRepository(this, a.store).settings().countdownSeconds
        }
        var remaining by mutableStateOf(countdownSec)
        timer = object : CountDownTimer(countdownSec * 1000L, 1000) {
            override fun onTick(ms: Long) { remaining = (ms / 1000).toInt() }
            override fun onFinish() {
                app.events.add(EventLogEntry(
                    timestampIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date()),
                    triggerType = "countdown expired"
                ))
                finish()
            }
        }.start()
        setContent {
            FallOverlay(remaining, onCancel = {
                timer?.cancel()
                app.events.add(EventLogEntry(
                    timestampIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date()),
                    triggerType = "cancelled"
                ))
                finish()
            })
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}

@Composable
fun FallOverlay(secondsLeft: Int, onCancel: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("FALL DETECTED", fontSize = 40.sp, color = Color.Red)
        Text("$secondsLeft", fontSize = 120.sp, color = Color.Red)
        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("I'm OK — cancel", fontSize = 36.sp, color = Color.White)
        }
    }
}
