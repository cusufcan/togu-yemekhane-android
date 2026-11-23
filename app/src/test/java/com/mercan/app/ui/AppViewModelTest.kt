package com.mercan.app.ui

import com.mercan.app.domain.MenuRepository
import com.mercan.app.domain.RefreshState
import com.mercan.app.domain.Settings
import com.mercan.app.domain.SettingsRepository
import com.mercan.app.domain.ThemeMode
import com.mercan.app.domain.WeekMenu
import com.mercan.app.platform.BackgroundRefresh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val menus = FakeMenus()
    private val background = FakeBackground()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun silentFailureSaysNothingButWaitsForTheNetwork() = runTest(dispatcher) {
        menus.ok = false
        assertEquals(0, failuresAfter { it.refresh(byHand = false) })
        assertEquals(listOf("retry"), background.calls)
    }

    @Test
    fun failureAskedByHandIsTold() = runTest(dispatcher) {
        menus.ok = false
        assertEquals(1, failuresAfter { it.refresh(byHand = true) })
    }

    @Test
    fun successCancelsTheWaitingRetry() = runTest(dispatcher) {
        assertEquals(0, failuresAfter { it.refresh(byHand = true) })
        assertEquals(listOf("cancel"), background.calls)
    }

    private fun TestScope.failuresAfter(action: (AppViewModel) -> Unit): Int {
        val viewModel = AppViewModel(menus, FakeSettings(), background)
        val failures = mutableListOf<Unit>()
        backgroundScope.launch(dispatcher) { viewModel.refreshFailed.toList(failures) }
        action(viewModel)
        return failures.size
    }

    private class FakeMenus : MenuRepository {
        var ok = true
        override val weeks = MutableStateFlow(emptyList<WeekMenu>())
        override val refresh = MutableStateFlow(RefreshState())
        override suspend fun refreshCurrent() = ok
        override suspend fun refreshArchive() = ok
    }

    private class FakeBackground : BackgroundRefresh {
        val calls = mutableListOf<String>()
        override fun ensureScheduled() { calls += "schedule" }
        override fun retryWhenOnline() { calls += "retry" }
        override fun cancelRetry() { calls += "cancel" }
    }

    private class FakeSettings : SettingsRepository {
        override val settings = MutableStateFlow(Settings())
        override suspend fun setTheme(theme: ThemeMode) = Unit
        override suspend fun setReminderOn(on: Boolean) = Unit
        override suspend fun setReminderMinute(minute: Int) = Unit
    }
}
