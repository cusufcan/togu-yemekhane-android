package com.mercan.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

/** Hatırlatıcının bir sonraki zamanı: bugünkü saat geçtiyse yarın. */
fun nextReminderAt(now: Instant, minuteOfDay: Int, zone: ZoneId): Instant {
    val time = LocalTime.ofSecondOfDay(minuteOfDay * 60L)
    fun on(date: LocalDate) = date.atTime(time).atZone(zone).toInstant()
    val today = now.atZone(zone).toLocalDate()
    return on(today).takeIf { it > now } ?: on(today.plusDays(1))
}

/** "Ezogelin çorba, bulgur pilavı": yemek adları Türkçedir, o yüzden küçültme de Türkçedir. */
fun List<Dish>.asSentence(): String = mapIndexed { index, dish ->
    if (index == 0) dish.name else dish.name.replaceFirstChar { it.lowercase(Locale.forLanguageTag("tr")) }
}.joinToString(", ")
