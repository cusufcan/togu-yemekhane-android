package com.mercan.app.domain

import java.time.LocalDate

data class Dish(val name: String, val vegetarian: Boolean = false)

data class DayMenu(
    val date: LocalDate,
    val dishes: List<Dish>,
    val calories: Int? = null,
)

data class WeekMenu(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val days: List<DayMenu>,
) {
    /** O güne ait yemek yoksa null: gün listede yok ya da listesi boş. */
    fun menuOn(date: LocalDate): DayMenu? = days.firstOrNull { it.date == date && it.dishes.isNotEmpty() }
}
