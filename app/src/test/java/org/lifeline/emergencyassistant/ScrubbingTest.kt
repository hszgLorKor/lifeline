package org.lifeline.emergencyassistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.lifeline.emergencyassistant.data.MedicalProfile
import org.lifeline.emergencyassistant.logging.ScrubbedLogger

class ScrubbingTest {

    @Test
    fun `medical values never appear in log messages`() {
        val profile = MedicalProfile(
            fullName = "Aisha Rahman",
            dateOfBirth = "1985-03-12",
            bloodType = "O-",
            notes = "allergic to peanuts severely"
        )
        val msg = "profile saved for Aisha Rahman dob 1985-03-12 blood O- notes allergic to peanuts severely"
        assertFalse(ScrubbedLogger.assertNoProfileValues(profile, msg))
        val safe = "profile saved ok"
        assertTrue(ScrubbedLogger.assertNoProfileValues(profile, safe))
    }

    @Test
    fun `sensitive markers are detected`() {
        assertTrue(ScrubbedLogger.isSensitive("contact phone=12345"))
        assertTrue(ScrubbedLogger.isSensitive("allergies listed"))
        assertFalse(ScrubbedLogger.isSensitive("fall detected at step 3"))
    }
}
