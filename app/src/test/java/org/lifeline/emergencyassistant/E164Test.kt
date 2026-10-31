package org.lifeline.emergencyassistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.lifeline.emergencyassistant.phone.E164

class E164Test {

    @Test
    fun `US number normalizes to E164`() {
        val r = E164.normalize("(202) 555-0182", "US")
        assertTrue(r.isSuccess)
        assertEquals("+12025550182", r.getOrThrow())
    }

    @Test
    fun `already E164 stays stable`() {
        val r = E164.normalize("+442079460958", "GB")
        assertTrue(r.isSuccess)
        assertEquals("+442079460958", r.getOrThrow())
    }

    @Test
    fun `malformed number fails with warning semantics`() {
        assertFalse(E164.normalize("abc", "US").isSuccess)
        assertFalse(E164.normalize("12", "US").isSuccess)
    }

    @Test
    fun `country picker default applies when no plus prefix`() {
        val r = E164.normalize("412345678", "AU")
        assertTrue(r.isSuccess)
        assertTrue(r.getOrThrow().startsWith("+61"))
    }
}
