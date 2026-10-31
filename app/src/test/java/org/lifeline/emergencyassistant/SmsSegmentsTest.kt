package org.lifeline.emergencyassistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.lifeline.emergencyassistant.sms.SmsSegments

class SmsSegmentsTest {

    @Test
    fun `gsm7 latin single segment up to 160`() {
        val text = "A".repeat(160)
        assertTrue(SmsSegments.isGsm7(text))
        assertEquals(1, SmsSegments.segmentCount(text))
    }

    @Test
    fun `gsm7 multi-segment uses 153 per segment`() {
        assertEquals(1, SmsSegments.segmentCount("B".repeat(160)))
        assertEquals(2, SmsSegments.segmentCount("B".repeat(161)))
        assertEquals(2, SmsSegments.segmentCount("B".repeat(306)))
        assertEquals(3, SmsSegments.segmentCount("B".repeat(307)))
    }

    @Test
    fun `blood type glyphs are GSM-7 safe ASCII`() {
        for (bt in listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown")) {
            assertTrue("$bt must be GSM-7", SmsSegments.isGsm7("blood $bt"))
            assertEquals(1, SmsSegments.segmentCount("blood $bt"))
        }
    }

    @Test
    fun `non-latin forces UCS-2 70 per segment`() {
        val arabic = "القاهرة"
        assertFalse(SmsSegments.isGsm7(arabic))
        assertEquals(1, SmsSegments.segmentCount(arabic))
        val long = "م" .repeat(71)
        assertEquals(2, SmsSegments.segmentCount(long))
        assertEquals(2, SmsSegments.segmentCount("م".repeat(134)))
        assertEquals(3, SmsSegments.segmentCount("م".repeat(135)))
    }

    @Test
    fun `default english template fits within 2 segments`() {
        val body = "EMERGENCY: Jane Doe, DOB 1990-01-01, blood A+. Allergies: Peanut. Conditions: Asthma. Medications: Metformin. Implants: Pacemaker. Doctor: Dr Smith +1234. Location: Cairo 30.04440,31.23570 (2026-10-31T10:00:00+0200)"
        assertTrue(SmsSegments.segmentCount(body) <= 2)
    }
}
