package com.mercan.togu.robot

import java.nio.file.Path
import kotlin.io.path.readText
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MenuStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var root: Path
    private lateinit var store: MenuStore

    @Before
    fun setUp() {
        root = folder.root.toPath()
        store = MenuStore(root)
    }

    @Test
    fun savingSameWeekTwiceChangesNothing() {
        assertTrue(store.save(week("2026-11-23")))
        assertFalse(store.save(week("2026-11-23")))
    }

    @Test
    fun correctionReplacesWeekEverywhere() {
        store.save(week("2026-11-23", dish = "Pilav"))

        assertTrue(store.save(week("2026-11-23", dish = "Makarna")))
        assertEquals("Makarna", read<WeekMenu>("v1/current.json").days[0].dishes[0].name)
        assertEquals(listOf("Makarna"), read<Archive>("v1/archive.json").weeks.map { it.days[0].dishes[0].name })
    }

    @Test
    fun archiveKeepsOldWeeksNewestFirst() {
        store.save(week("2026-11-16"))
        store.save(week("2026-11-23"))

        assertEquals(
            listOf("2026-11-23", "2026-11-16"),
            read<Archive>("v1/archive.json").weeks.map { it.weekStart },
        )
    }

    @Test
    fun currentStaysNewestWhenPageGoesBack() {
        store.save(week("2026-11-23"))
        store.save(week("2026-11-16"))

        assertEquals("2026-11-23", read<WeekMenu>("v1/current.json").weekStart)
    }

    private inline fun <reified T> read(path: String): T = Json.decodeFromString(root.resolve(path).readText())

    private fun week(start: String, dish: String = "Çorba") = WeekMenu(
        weekStart = start,
        weekEnd = start,
        days = listOf(DayMenu(date = start, dishes = listOf(Dish(dish)), calories = 900)),
    )
}
