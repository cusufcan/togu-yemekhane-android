package com.mercan.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ArchiveSearchTest {
    private val start = LocalDate.of(2026, 11, 16)
    private fun week(start: LocalDate, vararg days: List<String>) = WeekMenu(
        start, start.plusDays(4),
        days.mapIndexed { i, names -> DayMenu(start.plusDays(i.toLong()), names.map { Dish(it) }) },
    )

    @Test
    fun foldingKeepsLength() {
        listOf("İzmir köfte", "Kâğıt kebabı", "IŞIK ÇĞÖŞÜ").forEach { assertEquals(it.length, it.folded().length) }
        assertEquals("izmir kofte", "İzmir köfte".folded())
        assertEquals("isik", "IŞIK".folded())
    }

    @Test
    fun findsWithoutTurkishLettersAndMarksThePlace() {
        val hit = searchDishes(listOf(week(start, listOf("Hasanpaşa köfte"))), " KOFTE ").single()
        assertEquals("köfte", hit.dish.substring(hit.match))
    }

    @Test
    fun oneHitPerDayNewestFirst() {
        val hits = searchDishes(
            listOf(week(start, listOf("Köfte", "Çiğ köfte"), listOf("Pilav"), listOf("Köfteli çorba"))),
            "köfte",
        )
        assertEquals(listOf(start.plusDays(2), start), hits.map { it.date })
        assertEquals("Köfte", hits.last().dish)
    }

    @Test
    fun blankQueryFindsNothing() = assertEquals(emptyList<DishHit>(), searchDishes(listOf(week(start, listOf("Köfte"))), "  "))

    @Test
    fun runningAndEmptyWeeksStayOut() {
        val old = week(start, listOf("Köfte"))
        val empty = week(start.minusWeeks(1), emptyList())
        val live = week(start.plusWeeks(1), listOf("Pilav"))
        assertEquals(listOf(old), finishedWeeks(listOf(empty, old, live), start.plusWeeks(1)))
    }
}
