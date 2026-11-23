package com.mercan.togu.robot

import kotlinx.serialization.Serializable

// Yayınlanan JSON'un biçimi. Yayındaki uygulamalar bunu okur: alan silinmez, adı değişmez.
// Uyumsuz değişiklik gerekirse v2 klasörü açılır.

/** Tarihler ISO biçimindedir (2026-11-23). */
@Serializable
data class WeekMenu(
    val weekStart: String,
    val weekEnd: String,
    val days: List<DayMenu>,
)

@Serializable
data class DayMenu(
    val date: String,
    val dishes: List<Dish>,
    val calories: Int? = null,
)

@Serializable
data class Dish(
    val name: String,
    val vegetarian: Boolean = false,
)

@Serializable
data class Archive(val weeks: List<WeekMenu>)
