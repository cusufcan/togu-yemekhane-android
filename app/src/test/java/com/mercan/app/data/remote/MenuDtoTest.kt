package com.mercan.app.data.remote

import com.mercan.app.domain.Dish
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class MenuDtoTest {
    @Test
    fun readsTheFullWeek() {
        val week = parseWeek(
            """{"weekStart":"2026-11-23","weekEnd":"2026-11-27","yeni":1,"days":[
                {"date":"2026-11-24","dishes":[],"calories":null},
                {"date":"2026-11-23","dishes":[{"name":"Pilav","vegetarian":false},{"name":"Graten","vegetarian":true}],"calories":1056}
            ]}""",
        )
        assertEquals(LocalDate.of(2026, 11, 23), week.days.first().date)
        assertEquals(listOf(Dish("Pilav"), Dish("Graten", vegetarian = true)), week.days.first().dishes)
        assertEquals(1056, week.days.first().calories)
    }

    @Test
    fun readsTheCompactArchive() {
        val week = parseArchive(
            """{"weeks":[{"weekStart":"2026-11-23","weekEnd":"2026-11-27","days":[{"date":"2026-11-23","dishes":[{"name":"Pilav"}]}]}]}""",
        ).single()
        assertNull(week.days.single().calories)
        assertEquals(listOf(Dish("Pilav")), week.days.single().dishes)
    }

    @Test
    fun brokenTextIsAnError() {
        assertThrows(SerializationException::class.java) { parseWeek("<html>") }
    }
}
