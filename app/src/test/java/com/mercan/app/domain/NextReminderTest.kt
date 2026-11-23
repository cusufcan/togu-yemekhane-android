package com.mercan.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class NextReminderTest {
    private val zone = ZoneId.of("Europe/Istanbul")

    @Test
    fun `before the time it is today, at or after it is tomorrow`() {
        val at = Instant.parse("2026-11-23T08:30:00Z") // 11.30
        assertEquals(at, nextReminderAt(Instant.parse("2026-11-23T08:29:59Z"), 690, zone))
        assertEquals(Instant.parse("2026-11-24T08:30:00Z"), nextReminderAt(at, 690, zone))
    }

    @Test
    fun `it follows the phone through a clock change`() {
        val berlin = ZoneId.of("Europe/Berlin")
        assertEquals(
            Instant.parse("2026-10-25T10:30:00Z"),
            nextReminderAt(Instant.parse("2026-10-24T09:30:00Z"), 690, berlin),
        )
    }

    @Test
    fun `only the first dish keeps its capital`() {
        val dishes = listOf("Ezogelin çorba", "İçli pilav", "Irmik helvası").map { Dish(it) }
        assertEquals("Ezogelin çorba, içli pilav, ırmik helvası", dishes.asSentence())
    }
}
