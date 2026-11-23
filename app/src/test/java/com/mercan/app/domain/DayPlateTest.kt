package com.mercan.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DayPlateTest {
    private fun day(vararg names: String, vegetarian: String? = null) = DayMenu(
        LocalDate.of(2026, 11, 23),
        names.map { Dish(it) } + listOfNotNull(vegetarian?.let { Dish(it, vegetarian = true) }),
    )

    private fun List<Dish>.names() = map { it.name }

    @Test
    fun firstDishAfterTheSoupIsMain() {
        val plate = day("Ezogelin çorba", "Hasanpaşa köfte", "Bulgur pilavı", vegetarian = "Patates graten").plate()
        assertEquals("Hasanpaşa köfte", plate.main?.name)
        assertEquals(listOf("Ezogelin çorba", "Bulgur pilavı"), plate.others.names())
        assertEquals(listOf("Patates graten"), plate.vegetarian.names())
    }

    @Test
    fun soupMovesToTheTop() {
        val plate = day("Izgara köfte", "Pilav", "Mercimek Çorbası").plate()
        assertEquals("Izgara köfte", plate.main?.name)
        assertEquals(listOf("Mercimek Çorbası", "Pilav"), plate.others.names())
    }

    @Test
    fun onlySoupHasNoMain() {
        val plate = day("Yayla çorba").plate()
        assertNull(plate.main)
        assertEquals("Yayla çorba", plate.title)
        assertEquals(emptyList<Dish>(), plate.rest)
    }

    @Test
    fun holidayNoteIsTheTitle() {
        val plate = day("Resmî tatil").plate()
        assertEquals("Resmî tatil", plate.title)
        assertEquals(emptyList<Dish>(), plate.rest)
    }
}
