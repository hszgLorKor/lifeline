package org.lifeline.emergencyassistant.fall

enum class Sensitivity(val factor: Double) {
    LOW(1.2), MEDIUM(1.0), HIGH(0.8);

    fun adjust(threshold: Double): Double = threshold * factor
}

data class AccelSample(val tMs: Long, val x: Float, val y: Float, val z: Float) {
    val resultantG: Double get() = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()) / 9.80665
}

class FallDetector(private val sensitivity: Sensitivity = Sensitivity.MEDIUM) {

    companion object {
        const val FREEFALL_G = 0.4
        const val FREEFALL_MIN_MS = 150L
        const val IMPACT_G = 2.5
        const val IMPACT_WINDOW_MS = 500L
        const val STILLNESS_MS = 15_000L
        const val STILLNESS_G = 0.15
    }

    private val freefallG = sensitivity.adjust(FREEFALL_G)
    private val impactG = sensitivity.adjust(IMPACT_G)

    private var freefallStartMs: Long? = null
    private var freefallWindowUntilMs: Long? = null
    private var impactAtMs: Long? = null

    fun reset() {
        freefallStartMs = null
        freefallWindowUntilMs = null
        impactAtMs = null
    }

    fun onSample(s: AccelSample): Boolean {
        val r = s.resultantG
        val now = s.tMs

        if (r < freefallG) {
            if (freefallStartMs == null) freefallStartMs = now
        } else {
            val start = freefallStartMs
            if (start != null && now - start >= FREEFALL_MIN_MS) {
                freefallWindowUntilMs = now + IMPACT_WINDOW_MS
            }
            freefallStartMs = null
        }

        if (r > impactG) {
            impactAtMs = now
            val ffWin = freefallWindowUntilMs
            if (ffWin != null && now <= ffWin) {
                freefallWindowUntilMs = null
                impactAtMs = null
                return true
            }
        } else if (r < STILLNESS_G) {
            val imp = impactAtMs
            if (imp != null && now - imp >= STILLNESS_MS) {
                impactAtMs = null
                return true
            }
        }
        return false
    }
}
