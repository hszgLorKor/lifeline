package org.lifeline.emergencyassistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.lifeline.emergencyassistant.fall.AccelSample
import org.lifeline.emergencyassistant.fall.FallDetector
import org.lifeline.emergencyassistant.fall.Sensitivity

class FallDetectorTest {

    private fun trace(vararg points: Pair<Long, Double>): List<AccelSample> =
        points.map { (t, g) -> AccelSample(t, 0f, 0f, (g * 9.80665).toFloat()) }

    private fun run(detector: FallDetector, samples: List<AccelSample>): Boolean =
        samples.any { detector.onSample(it) }

    @Test
    fun `free-fall then impact triggers at Medium`() {
        val d = FallDetector(Sensitivity.MEDIUM)
        val samples = trace(
            0L to 1.0, 50L to 1.0,
            100L to 0.03, 150L to 0.03, 200L to 0.03, 260L to 0.03, // free-fall >=150ms below 0.4g
            300L to 1.0, 400L to 3.0, // impact > 2.5g within 500ms of free-fall end
            500L to 1.0
        )
        assertTrue(run(d, samples))
    }

    @Test
    fun `impact then 15s stillness triggers at Medium`() {
        val d = FallDetector(Sensitivity.MEDIUM)
        val samples = mutableListOf<AccelSample>()
        var t = 0L
        samples += AccelSample(t, 0f, 0f, (1.0 * 9.80665).toFloat()); t += 40
        samples += AccelSample(t, 0f, 0f, (3.0 * 9.80665).toFloat()); t += 40 // impact, then stillness begins
        for (i in 1..400) { samples += AccelSample(t, 0f, 0f, (0.01 * 9.80665).toFloat()); t += 40 } // ~16s stillness
        assertTrue(run(d, samples))
    }

    @Test
    fun `walking trace does not trigger`() {
        val d = FallDetector(Sensitivity.MEDIUM)
        val samples = mutableListOf<AccelSample>()
        var t = 0L
        for (i in 0..600) {
            val g = 9.80665 + kotlin.math.sin(i * 0.3) * 1.5
            samples += AccelSample(t, 0f, 0f, g.toFloat()); t += 40
        }
        assertFalse(run(d, samples))
    }

    @Test
    fun `stair climbing trace does not trigger`() {
        val d = FallDetector(Sensitivity.MEDIUM)
        val samples = mutableListOf<AccelSample>()
        var t = 0L
        for (i in 0..900) {
            val g = 9.80665 + kotlin.math.sin(i * 0.15) * 2.2
            samples += AccelSample(t, 0f, 0f, g.toFloat()); t += 40
        }
        assertFalse(run(d, samples))
    }

    @Test
    fun `train ride trace does not trigger`() {
        val d = FallDetector(Sensitivity.MEDIUM)
        val samples = mutableListOf<AccelSample>()
        var t = 0L
        for (i in 0..2000) {
            val g = 9.80665 + kotlin.math.sin(i * 0.05) * 0.6 + (if (i % 211 < 2) 2.0 else 0.0)
            samples += AccelSample(t, 0f, 0f, g.toFloat()); t += 40
        }
        assertFalse(run(d, samples))
    }

    @Test
    fun `car ride over bumps does not trigger`() {
        val d = FallDetector(Sensitivity.MEDIUM)
        val samples = mutableListOf<AccelSample>()
        var t = 0L
        for (i in 0..1000) {
            val g = 9.80665 + (if (i % 97 < 3) 12.0 else kotlin.math.sin(i * 0.1) * 0.8)
            samples += AccelSample(t, 0f, 0f, g.toFloat()); t += 40
        }
        assertFalse(run(d, samples))
    }

    @Test
    fun `sensitivity adjusts thresholds by plus-minus 20 percent`() {
        // free-fall of 0.45g for 200ms then impact 30g:
        // Medium freefallG=0.40 -> 0.45 is NOT freefall -> no trigger
        // Low    freefallG=0.48 -> 0.45 IS freefall     -> trigger
        // 0.45g freefall for 200ms then 3.06g impact:
        // Medium freefallG=0.40 -> 0.45 is NOT below -> no trigger
        // Low    freefallG=0.48 -> 0.45 IS below     -> trigger
        val trace = trace(0L to 1.0, 50L to 0.45, 250L to 0.45, 300L to 3.06, 350L to 1.0)
        assertFalse(run(FallDetector(Sensitivity.MEDIUM), trace))
        assertTrue(run(FallDetector(Sensitivity.LOW), trace))
        // 0.36g freefall: Medium freefallG=0.40 counts it, High freefallG=0.32 does not
        val trace2 = trace(0L to 1.0, 50L to 0.36, 250L to 0.36, 300L to 3.06, 350L to 1.0)
        assertTrue(run(FallDetector(Sensitivity.MEDIUM), trace2))
        assertFalse(run(FallDetector(Sensitivity.HIGH), trace2))
    }
}
