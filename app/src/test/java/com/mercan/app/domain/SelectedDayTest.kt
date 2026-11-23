package com.mercan.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SelectedDayTest {
    private val monday = LocalDate.of(2026, 11, 23)
    private val week = WeekMenu(
        weekStart = monday,
        weekEnd = monday.plusDays(4),
        days = (0L..4L).map { DayMenu(monday.plusDays(it), listOf(Dish("Pilav"))) },
    )
    private val current = MenuState.Current(week, initialDay = monday.plusDays(1))
    private val today = monday.plusDays(1)

    @Test
    fun `without a choice the screen opens on its own day`() {
        assertEquals(today, current.dayFor(null, today))
    }

    @Test
    fun `a choice made today is kept`() {
        val thursday = monday.plusDays(3)
        assertEquals(thursday, current.dayFor(DayChoice(thursday, today), today))
    }

    @Test
    fun `a choice from yesterday or from another week is dropped`() {
        assertEquals(today, current.dayFor(DayChoice(monday.plusDays(3), monday), today))
        assertEquals(today, current.dayFor(DayChoice(monday.plusDays(9), today), today))
    }
}
