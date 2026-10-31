package org.lifeline.emergencyassistant.sms

object SmsSegments {
    const val GSM7_SINGLE = 160
    const val GSM7_MULTI = 153
    const val UCS2_SINGLE = 70
    const val UCS2_MULTI = 67

    private val GSM7_BASIC = (
        "@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?" +
        "¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà"
        ).toSet()

    private val GSM7_EXT = "^{}\\[~]|€".toSet()

    fun isGsm7(text: String): Boolean = text.all { it in GSM7_BASIC || it in GSM7_EXT }

    fun segmentCount(text: String, forceUcs2: Boolean = false): Int {
        if (text.isEmpty()) return 0
        val ucs2 = forceUcs2 || !isGsm7(text)
        val single = if (ucs2) UCS2_SINGLE else GSM7_SINGLE
        val multi = if (ucs2) UCS2_MULTI else GSM7_MULTI
        if (text.length <= single) return 1
        val per = multi
        return (text.length + per - 1) / per
    }

    fun scriptNeedsUcs2(script: String): Boolean = script != "Latin"

    fun maxSegmentChars(script: String, segments: Int): Int =
        if (scriptNeedsUcs2(script)) {
            if (segments <= 1) UCS2_SINGLE else segments * UCS2_MULTI
        } else {
            if (segments <= 1) GSM7_SINGLE else segments * GSM7_MULTI
        }
}
