package com.mercan.app.data.remote

import com.mercan.app.domain.DayMenu
import com.mercan.app.domain.Dish
import com.mercan.app.domain.WeekMenu
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

/** Bilinmeyen alanlar yok sayılır; robot yeni alan eklerse eski sürümler bozulmaz. */
private val MenuJson = Json { ignoreUnknownKeys = true }

@Serializable
private class ArchiveDto(val weeks: List<WeekDto>)

@Serializable
private class WeekDto(val weekStart: String, val weekEnd: String, val days: List<DayDto>)

@Serializable
private class DayDto(val date: String, val dishes: List<DishDto>, val calories: Int? = null)

@Serializable
private class DishDto(val name: String, val vegetarian: Boolean = false)

internal fun parseWeek(text: String): WeekMenu = MenuJson.decodeFromString<WeekDto>(text).toDomain()

internal fun parseArchive(text: String): List<WeekMenu> =
    MenuJson.decodeFromString<ArchiveDto>(text).weeks.map { it.toDomain() }

private fun WeekDto.toDomain() = WeekMenu(
    weekStart = LocalDate.parse(weekStart),
    weekEnd = LocalDate.parse(weekEnd),
    days = days
        .map { day -> DayMenu(LocalDate.parse(day.date), day.dishes.map { Dish(it.name, it.vegetarian) }, day.calories) }
        .sortedBy { it.date },
)
