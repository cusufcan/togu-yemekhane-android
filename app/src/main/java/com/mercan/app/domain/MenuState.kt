package com.mercan.app.domain

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

fun mondayOf(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun isWeekend(date: LocalDate): Boolean = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

/** Yenilemenin durumu. [lastFailed] yalnız bu açılıştaki son denemeyi anlatır. */
data class RefreshState(
    val running: Boolean = false,
    val lastFailed: Boolean = false,
    val lastSuccess: Instant? = null,
)

/** Bugün ekranında ne gösterileceği; yalnız kayıtlı haftalara ve tarihe bakar. */
sealed interface MenuState {
    /** Telefonda hiç menü yok: ilk indirme sürüyor ya da başarısız oldu. */
    data class Nothing(val failed: Boolean) : MenuState

    /** Gösterilecek hafta var. Hafta sonu, yayınlandıysa, gelecek haftadır. */
    data class Current(val week: WeekMenu, val initialDay: LocalDate) : MenuState

    /** Hafta sonu ve yeni hafta henüz yok. */
    data class Weekend(val lastWeek: WeekMenu) : MenuState

    /** Hafta içi ama bu haftanın menüsü yok. */
    data class Stale(
        val lastWeek: WeekMenu,
        /** Sayfaya ulaşıldı, menü daha yayınlanmamış. */
        val notPublished: Boolean,
        val fromLastWeek: Boolean,
    ) : MenuState
}

fun menuState(
    weeks: List<WeekMenu>,
    refresh: RefreshState,
    today: LocalDate,
    zone: ZoneId,
): MenuState {
    val latest = weeks.maxByOrNull { it.weekStart } ?: return MenuState.Nothing(failed = refresh.lastFailed)
    val thisMonday = mondayOf(today)
    val shownMonday = if (isWeekend(today)) thisMonday.plusWeeks(1) else thisMonday
    val shown = weeks.firstOrNull { it.weekStart == shownMonday }
    return when {
        shown != null -> MenuState.Current(shown, if (isWeekend(today)) shown.weekStart else today)
        isWeekend(today) -> MenuState.Weekend(latest)
        else -> MenuState.Stale(
            lastWeek = latest,
            notPublished = !refresh.lastFailed &&
                refresh.lastSuccess?.atZone(zone)?.toLocalDate()?.let { it >= thisMonday } == true,
            fromLastWeek = latest.weekStart == thisMonday.minusWeeks(1),
        )
    }
}

/** Hafta ekranında ve eski menü kartının altında gösterilen hafta. */
val MenuState.week: WeekMenu?
    get() = when (this) {
        is MenuState.Nothing -> null
        is MenuState.Current -> week
        is MenuState.Weekend -> lastWeek
        is MenuState.Stale -> lastWeek
    }

/** Gün seçicideki günler: hafta içi günleri ve yemeği olan hafta sonu günleri. */
fun WeekMenu.shownDates(): List<LocalDate> = generateSequence(weekStart) { it.plusDays(1) }
    .takeWhile { it <= weekEnd }
    .filter { !isWeekend(it) || menuOn(it) != null }
    .toList()
