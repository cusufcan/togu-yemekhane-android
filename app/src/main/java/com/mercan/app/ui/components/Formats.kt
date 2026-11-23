package com.mercan.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.mercan.app.R
import com.mercan.app.domain.WeekMenu
import com.mercan.app.domain.shownDates
import com.mercan.app.domain.turkishTimeSuffix
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Tarih, saat ve sayı yazımı. Dil sistemden değil, gösterilen metinlerin dilinden gelir. */
@Immutable
class Formats(
    tag: String,
    day: String,
    dayMonth: String,
    month: String,
    hit: String,
    time: String,
) {
    val locale: Locale = Locale.forLanguageTag(tag)
    private val turkish = locale.language == "tr"
    private val dayFormat = DateTimeFormatter.ofPattern(day, locale)
    private val dayMonthFormat = DateTimeFormatter.ofPattern(dayMonth, locale)
    private val weekdayFormat = DateTimeFormatter.ofPattern("EEEE", locale)
    private val monthFormat = DateTimeFormatter.ofPattern(month, locale)
    private val hitFormat = DateTimeFormatter.ofPattern(hit, locale)
    private val timeFormat = DateTimeFormatter.ofPattern(time, locale)

    fun day(date: LocalDate): String = dayFormat.format(date)
    fun dayMonth(date: LocalDate): String = dayMonthFormat.format(date)
    fun weekday(date: LocalDate): String = weekdayFormat.format(date)
    fun shortWeekday(date: LocalDate): String = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
    fun month(month: YearMonth): String = monthFormat.format(month)
    fun hit(date: LocalDate): String = hitFormat.format(date)
    fun time(time: LocalTime): String = timeFormat.format(time)
    fun number(value: Int): String = NumberFormat.getIntegerInstance(locale).format(value)

    /** Cümle içindeki saat: Türkçede "08.12'de", İngilizcede "08:12". */
    fun timeInSentence(time: LocalTime): String =
        if (turkish) "${time(time)}'${turkishTimeSuffix(time)}" else time(time)

    /** "23–27 Kasım" ya da ay değişiyorsa "30 Kasım – 4 Aralık". */
    fun range(week: WeekMenu): String {
        val start = week.weekStart
        val end = week.shownDates().lastOrNull() ?: week.weekEnd
        return if (start.month == end.month) "${start.dayOfMonth}–${dayMonth(end)}"
        else "${dayMonth(start)} – ${dayMonth(end)}"
    }
}

@Composable
fun rememberFormats(): Formats {
    val tag = stringResource(R.string.language_tag)
    val day = stringResource(R.string.format_day)
    val dayMonth = stringResource(R.string.format_day_month)
    val month = stringResource(R.string.format_month)
    val hit = stringResource(R.string.format_hit)
    val time = stringResource(R.string.format_time)
    return remember(tag, day, dayMonth, month, hit, time) { Formats(tag, day, dayMonth, month, hit, time) }
}
