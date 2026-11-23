package com.mercan.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class TurkishTimeTest {
    private fun suffix(text: String) = turkishTimeSuffix(LocalTime.parse(text))

    @Test
    fun `the last spoken digit decides`() {
        assertEquals("de", suffix("08:12"))
        assertEquals("te", suffix("08:13"))
        assertEquals("da", suffix("08:16"))
        assertEquals("da", suffix("08:59"))
    }

    @Test
    fun `round minutes use the tens`() {
        assertEquals("da", suffix("08:10"))
        assertEquals("de", suffix("08:20"))
        assertEquals("ta", suffix("08:40"))
        assertEquals("de", suffix("08:50"))
    }

    @Test
    fun `on the hour the hour is spoken`() {
        assertEquals("de", suffix("11:00"))
        assertEquals("da", suffix("10:00"))
        assertEquals("te", suffix("23:00"))
        assertEquals("da", suffix("00:00"))
    }
}
