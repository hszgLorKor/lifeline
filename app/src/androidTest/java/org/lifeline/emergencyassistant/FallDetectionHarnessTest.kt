package org.lifeline.emergencyassistant

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.lifeline.emergencyassistant.fall.AccelSample
import org.lifeline.emergencyassistant.fall.FallDetector
import org.lifeline.emergencyassistant.fall.Sensitivity

@RunWith(AndroidJUnit4::class)
class FallDetectionHarnessTest {

    private fun gSample(t: Long, g: Double) = AccelSample(t, 0f, 0f, (g * 9.80665).toFloat())

    @Test
    fun stagedFreefallThenImpact_triggersAtMedium() {
        val d = FallDetector(Sensitivity.MEDIUM)
        val trace = listOf(
            gSample(0, 1.0), gSample(50, 1.0),
            gSample(100, 0.03), gSample(150, 0.03), gSample(200, 0.03), gSample(260, 0.03),
            gSample(300, 1.0), gSample(400, 3.0), gSample(500, 1.0)
        )
        assertTrue(trace.any { d.onSample(it) })
    }

    @Test
    fun stagedImpactThenStillness_triggersAtMedium() {
        val d = FallDetector(Sensitivity.MEDIUM)
        var t = 0L
        assertFalse(d.onSample(gSample(t, 1.0))); t += 40
        assertFalse(d.onSample(gSample(t, 3.0))); t += 40
        var triggered = false
        for (i in 1..400) {
            if (d.onSample(gSample(t, 0.01))) { triggered = true; break }
            t += 40
        }
        assertTrue(triggered)
    }

    @Test
    fun dailyLifeTraces_doNotTrigger() {
        for (seed in 0..20) {
            val d = FallDetector(Sensitivity.MEDIUM)
            var t = 0L
            var triggered = false
            for (i in 0..3000) {
                val g = 1.0 + kotlin.math.sin(i * 0.07 + seed) * 0.25 + (if (i % 173 < 2) 0.3 else 0.0)
                if (d.onSample(gSample(t, g))) { triggered = true; break }
                t += 40
            }
            assertFalse("seed $seed false-triggered", triggered)
        }
    }
}
