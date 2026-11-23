package com.mercan.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class WidgetStateTest {
    private val monday = LocalDate.of(2026, 11, 23)
    private val mondayMenu = DayMenu(monday, listOf(Dish("Pilav")))
    private val thisWeek = WeekMenu(monday, monday.plusDays(4), listOf(mondayMenu))

    @Test
    fun todaysMenu() {
        assertEquals(WidgetState.Menu(mondayMenu), widgetState(listOf(thisWeek), monday))
    }

    @Test
    fun holidayInAKnownWeek() {
        assertEquals(WidgetState.NoMenuToday, widgetState(listOf(thisWeek), monday.plusDays(1)))
    }

    @Test
    fun weekendEvenWithoutAnyMenu() {
        assertEquals(WidgetState.Weekend, widgetState(listOf(thisWeek), monday.plusDays(6)))
        assertEquals(WidgetState.Weekend, widgetState(emptyList(), monday.plusDays(5)))
    }

    @Test
    fun weekendMenuIsShownWhenThereIsOne() {
        val saturday = DayMenu(monday.plusDays(5), listOf(Dish("Köfte")))
        val week = WeekMenu(monday, monday.plusDays(6), listOf(saturday))
        assertEquals(WidgetState.Menu(saturday), widgetState(listOf(week), saturday.date))
    }

    @Test
    fun lastWeeksMenuIsNeverShownAsToday() {
        assertEquals(WidgetState.Missing, widgetState(listOf(thisWeek), monday.plusWeeks(1)))
        assertEquals(WidgetState.Missing, widgetState(emptyList(), monday))
    }

    @Test
    fun newDayStartsAtLocalMidnight() {
        val istanbul = ZoneId.of("Europe/Istanbul")
        val midnight = Instant.parse("2026-11-23T21:00:00Z")
        assertEquals(midnight, nextDayStart(Instant.parse("2026-11-23T20:59:59Z"), istanbul))
        assertEquals(Instant.parse("2026-11-24T21:00:00Z"), nextDayStart(midnight, istanbul))
    }

    @Test
    fun newDayWithoutAMidnightStartsAtTheFirstExistingHour() {
        // Beyrut'ta yaz saati gece yarısı başlar; o gün 00.00 yoktur.
        val beirut = ZoneId.of("Asia/Beirut")
        val now = LocalDate.of(2026, 3, 28).atTime(12, 0).atZone(beirut).toInstant()
        assertEquals(1, nextDayStart(now, beirut).atZone(beirut).hour)
    }

    @Test
    fun linesThatFit() {
        assertEquals(WidgetLines(title = 1, rest = 3), lines(free = 90, titleLines = 1))
        assertEquals(WidgetLines(title = 2, rest = 1), lines(free = 90, titleLines = 2))
        // Sığmayan satır hiç yazılmaz; başlık her zaman kalır.
        assertEquals(WidgetLines(title = 1, rest = 0), lines(free = 49, titleLines = 2))
        assertEquals(WidgetLines(title = 1, rest = 0), lines(free = -10, titleLines = 1))
    }

    private fun lines(free: Int, titleLines: Int) =
        widgetLines(free, titleLine = 30, titleLines = titleLines, restLine = 20)
}
