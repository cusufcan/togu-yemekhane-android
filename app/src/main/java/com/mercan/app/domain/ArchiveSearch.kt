package com.mercan.app.domain

import java.time.LocalDate

data class DishHit(
    val date: LocalDate,
    val weekStart: LocalDate,
    val dish: String,
    /** [dish] içinde aranan yazının yeri; ekranda vurgulanır. */
    val match: IntRange,
)

/** Haftası biten menüler, en yenisi başta. Yürüyen hafta arşivde görünmez. */
fun finishedWeeks(weeks: List<WeekMenu>, today: LocalDate): List<WeekMenu> = weeks
    .filter { it.weekEnd < today && it.days.any { day -> day.dishes.isNotEmpty() } }
    .sortedByDescending { it.weekStart }

/** Aranan yemeğin çıktığı günler, en yenisi başta. Bir günde en fazla bir sonuç vardır. */
fun searchDishes(weeks: List<WeekMenu>, query: String): List<DishHit> {
    val needle = query.trim().folded()
    if (needle.isEmpty()) return emptyList()
    return weeks.flatMap { week ->
        week.days.mapNotNull { day ->
            day.dishes.firstNotNullOfOrNull { dish ->
                val at = dish.name.folded().indexOf(needle)
                if (at < 0) null else DishHit(day.date, week.weekStart, dish.name, at until at + needle.length)
            }
        }
    }.sortedByDescending { it.date }
}
