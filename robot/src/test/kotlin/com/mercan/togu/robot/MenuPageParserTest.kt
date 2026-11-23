package com.mercan.togu.robot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuPageParserTest {
    private val page = resource("week-browser.html")

    @Test
    fun readsWeekAndDayDates() {
        val week = MenuPageParser.parse(page)

        assertEquals("2026-11-23", week.weekStart)
        assertEquals("2026-11-27", week.weekEnd)
        assertEquals(
            listOf("2026-11-23", "2026-11-24", "2026-11-25", "2026-11-26", "2026-11-27"),
            week.days.map { it.date },
        )
    }

    @Test
    fun readsDishesVegetarianMarkAndCalories() {
        val monday = MenuPageParser.parse(page).days.first()

        assertEquals(
            listOf(
                Dish("Ezogelin çorba"),
                Dish("Hasanpaşa köfte"),
                Dish("Bulgur pilavı"),
                Dish("Çikolatalı puding"),
                Dish("Patates graten", vegetarian = true),
            ),
            monday.dishes,
        )
        assertEquals(1056, monday.calories)
    }

    @Test
    fun acceptsSpaceBeforeVegetarianMark() {
        val tuesday = MenuPageParser.parse(page).days[1]

        assertEquals(Dish("Mantar sote", vegetarian = true), tuesday.dishes.last())
    }

    @Test
    fun everyDayHasFiveDishesAndCalories() {
        val days = MenuPageParser.parse(page).days

        assertEquals(List(5) { 5 }, days.map { it.dishes.size })
        assertEquals(listOf(1056, 977, 989, 1072, 984), days.map { it.calories })
    }

    /** Sunucu tarayıcı olmayan isteklere `<font>` etiketli sayfa veriyor; sonuç aynı olmalı. */
    @Test
    fun plainClientMarkupGivesSameResult() {
        assertEquals(MenuPageParser.parse(page), MenuPageParser.parse(resource("week-plain.html")))
    }

    @Test
    fun emptyDayIsKeptWithoutDishes() {
        val day = MenuPageParser.parse(samplePage(days = listOf("Çorba<hr>Pilav", "", "Tatil", "", ""))).days

        assertTrue(day[1].dishes.isEmpty())
        assertNull(day[1].calories)
        assertEquals(listOf(Dish("Tatil")), day[2].dishes)
    }

    @Test
    fun missingCaloriesBecomeNull() {
        val day = MenuPageParser.parse(samplePage(days = listOf("Çorba<hr>Pilav<hr>Toplam Kalori: <b></b>", "", "", "", ""))).days[0]

        assertEquals(2, day.dishes.size)
        assertNull(day.calories)
    }

    @Test
    fun rejectsUnusablePages() {
        val unusable = mapOf(
            "boş sayfa" to "<html></html>",
            "boş tarih" to samplePage(start = ""),
            "pazartesi değil" to samplePage(start = "24.11.2026", end = "28.11.2026"),
            "aralık yanlış" to samplePage(end = "28.11.2026"),
            "eksik gün" to samplePage(days = List(4) { "Çorba<hr>Pilav" }),
            "yer tutucu" to samplePage(days = List(5) { "Menü hazırlanıyor" }),
        )

        unusable.forEach { (name, html) ->
            assertThrows(name, MenuPageException::class.java) { MenuPageParser.parse(html) }
        }
    }

    private fun samplePage(
        start: String = "23.11.2026",
        end: String = "27.11.2026",
        days: List<String> = List(5) { "Çorba<hr>Pilav" },
    ) = """
        <span id="X_haftaBasi">$start</span><span id="X_haftaSonu">$end</span>
        <table>
          <tr><td rowspan="2"></td><td><span id="X_LabelPzt">Pazartesi</span></td></tr>
          <tr>${days.joinToString("") { "<td>$it</td>" }}</tr>
        </table>
    """

    private fun resource(name: String) = checkNotNull(javaClass.getResource("/$name")).readText()
}
