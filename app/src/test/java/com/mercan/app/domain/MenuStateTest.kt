package com.mercan.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class MenuStateTest {
    private val monday = LocalDate.of(2026, 11, 23)
    private val thisWeek = week(monday)
    private val lastWeek = week(monday.minusWeeks(1))
    private val nextWeek = week(monday.plusWeeks(1))

    private fun week(start: LocalDate) =
        WeekMenu(start, start.plusDays(4), listOf(DayMenu(start, listOf(Dish("Pilav")))))

    private fun state(today: LocalDate, vararg weeks: WeekMenu, refresh: RefreshState = RefreshState()) =
        menuState(weeks.toList(), refresh, today, ZoneOffset.UTC)

    @Test
    fun nothingDownloaded() {
        assertEquals(MenuState.Nothing(failed = false), state(monday))
        assertEquals(MenuState.Nothing(failed = true), state(monday, refresh = RefreshState(lastFailed = true)))
    }

    @Test
    fun weekdayOpensToday() {
        val friday = monday.plusDays(4)
        assertEquals(MenuState.Current(thisWeek, friday), state(friday, lastWeek, thisWeek, nextWeek))
    }

    @Test
    fun weekendOpensNextWeekWhenPublished() {
        val sunday = monday.plusDays(6)
        assertEquals(MenuState.Current(nextWeek, nextWeek.weekStart), state(sunday, thisWeek, nextWeek))
        assertEquals(MenuState.Weekend(thisWeek), state(sunday, lastWeek, thisWeek))
    }

    @Test
    fun staleWhenThisWeekIsMissing() {
        val stale = state(monday, lastWeek) as MenuState.Stale
        assertTrue(stale.fromLastWeek)
        assertFalse(stale.notPublished)
        val older = state(monday, week(monday.minusWeeks(3))) as MenuState.Stale
        assertFalse(older.fromLastWeek)
    }

    @Test
    fun notPublishedOnlyAfterASuccessfulRefreshThisWeek() {
        val today = Instant.parse("2026-11-23T08:00:00Z")
        fun at(refresh: RefreshState) = (state(monday, lastWeek, refresh = refresh) as MenuState.Stale).notPublished
        assertTrue(at(RefreshState(lastSuccess = today)))
        assertFalse(at(RefreshState(lastSuccess = today, lastFailed = true)))
        assertFalse(at(RefreshState(lastSuccess = Instant.parse("2026-11-20T08:00:00Z"))))
    }

    @Test
    fun selectorShowsWeekdaysAndFilledWeekendDays() {
        val saturday = monday.plusDays(5)
        val wide = WeekMenu(monday, monday.plusDays(6), listOf(DayMenu(saturday, listOf(Dish("Pilav")))))
        assertEquals(6, wide.shownDates().size)
        assertEquals(5, thisWeek.shownDates().size)
    }
}
