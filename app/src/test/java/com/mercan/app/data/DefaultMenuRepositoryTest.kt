package com.mercan.app.data

import com.mercan.app.data.local.MenuStore
import com.mercan.app.data.local.RefreshTimeStore
import com.mercan.app.data.remote.MenuApi
import com.mercan.app.domain.DayMenu
import com.mercan.app.domain.Dish
import com.mercan.app.domain.WeekMenu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class DefaultMenuRepositoryTest {
    private val now = Instant.parse("2026-11-23T08:00:00Z")
    private val api = FakeApi()
    private val store = FakeStore()
    private val times = FakeTimes()
    private val repository = DefaultMenuRepository(api, store, times, Clock.fixed(now, ZoneOffset.UTC))

    @Test
    fun savesTheDownloadedWeek() = runTest {
        assertTrue(repository.refreshCurrent())
        assertEquals(listOf(api.week), store.weeks.value)
        assertEquals(now, times.saved)
        assertEquals(now, repository.refresh.value.lastSuccess)
    }

    @Test
    fun unchangedWeekIsNotWritten() = runTest {
        repository.refreshCurrent()
        repository.refreshCurrent()
        assertEquals(1, store.writes)
    }

    @Test
    fun correctedWeekReplacesTheOld() = runTest {
        repository.refreshCurrent()
        api.week = week(LocalDate.of(2026, 11, 23), "Mantı")
        repository.refreshCurrent()
        assertEquals(listOf(api.week), store.weeks.value)
    }

    @Test
    fun failureKeepsTheMenuAndTheOldTime() = runTest {
        repository.refreshCurrent()
        api.fails = true
        assertFalse(repository.refreshCurrent())
        assertEquals(1, store.weeks.value.size)
        assertTrue(repository.refresh.value.lastFailed)
        assertFalse(repository.refresh.value.running)
        assertEquals(now, repository.refresh.value.lastSuccess)
    }

    @Test
    fun successClearsTheFailure() = runTest {
        api.fails = true
        repository.refreshCurrent()
        api.fails = false
        repository.refreshCurrent()
        assertFalse(repository.refresh.value.lastFailed)
    }

    @Test
    fun archiveWritesOnlyTheChangedWeeksAndDeletesNothing() = runTest {
        val local = week(LocalDate.of(2024, 4, 29), "Tarhana çorba")
        store.weeks.value = listOf(local)
        repository.refreshCurrent()
        val old = week(LocalDate.of(2026, 11, 16), "Pilav")
        api.archive = listOf(api.week, old)
        assertTrue(repository.refreshArchive())
        assertEquals(setOf(local, old, api.week), store.weeks.value.toSet())
        assertEquals(listOf(old), store.lastWrite)
    }

    private fun week(start: LocalDate, dish: String) =
        WeekMenu(start, start.plusDays(4), listOf(DayMenu(start, listOf(Dish(dish)))))

    private inner class FakeApi : MenuApi {
        var week = week(LocalDate.of(2026, 11, 23), "Köfte")
        var archive = emptyList<WeekMenu>()
        var fails = false
        override suspend fun current() = if (fails) throw IOException() else week
        override suspend fun archive() = if (fails) throw IOException() else archive
    }

    private class FakeStore : MenuStore {
        override val weeks = MutableStateFlow(emptyList<WeekMenu>())
        var writes = 0
        var lastWrite = emptyList<WeekMenu>()
        override suspend fun all() = weeks.value
        override suspend fun replace(weeks: List<WeekMenu>) {
            writes++
            lastWrite = weeks
            val starts = weeks.map { it.weekStart }.toSet()
            this.weeks.value = this.weeks.value.filter { it.weekStart !in starts } + weeks
        }
    }

    private class FakeTimes : RefreshTimeStore {
        var saved: Instant? = null
        override suspend fun read() = saved
        override suspend fun write(time: Instant) {
            saved = time
        }
    }
}
