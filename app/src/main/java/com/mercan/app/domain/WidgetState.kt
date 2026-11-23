package com.mercan.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Widget'ta ne yazacağı. Yenilemenin sonucu burada bilinmez (uygulama kapalı olabilir), o yüzden
 * [Missing] nedenini söylemez.
 */
sealed interface WidgetState {
    data class Menu(val day: DayMenu) : WidgetState
    data object NoMenuToday : WidgetState
    data object Weekend : WidgetState

    /** Bu haftanın menüsü telefonda yok. */
    data object Missing : WidgetState
}

fun widgetState(weeks: List<WeekMenu>, today: LocalDate): WidgetState {
    val menu = weeks.firstNotNullOfOrNull { it.menuOn(today) }
    return when {
        menu != null -> WidgetState.Menu(menu)
        isWeekend(today) -> WidgetState.Weekend
        weeks.any { it.weekStart == mondayOf(today) } -> WidgetState.NoMenuToday
        else -> WidgetState.Missing
    }
}

/** Widget'ın yeni güne geçeceği an. */
fun nextDayStart(now: Instant, zone: ZoneId): Instant =
    now.atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant()

data class WidgetLines(val title: Int, val rest: Int)

/**
 * Widget'a sığan satır sayıları; yarım satır görünmesin diye önceden hesaplanır. [free] başlıkla
 * yemeklere kalan yükseklik, [titleLines] başlığın istediği satır sayısıdır; ölçüler pikseldir.
 */
fun widgetLines(free: Int, titleLine: Int, titleLines: Int, restLine: Int): WidgetLines {
    val title = titleLines.coerceAtMost(free / titleLine).coerceAtLeast(1)
    val rest = ((free - title * titleLine) / restLine).coerceAtLeast(0)
    return WidgetLines(title, rest)
}
