package com.mercan.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercan.app.domain.Settings
import com.mercan.app.domain.SettingsRepository
import com.mercan.app.domain.ThemeMode
import com.mercan.app.platform.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val reminder: ReminderScheduler,
) : ViewModel() {
    val state: StateFlow<Settings?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setTheme(theme: ThemeMode) {
        viewModelScope.launch { repository.setTheme(theme) }
    }

    fun setReminderOn(on: Boolean) {
        viewModelScope.launch {
            repository.setReminderOn(on)
            reminder.sync()
        }
    }

    fun setReminderMinute(minute: Int) {
        viewModelScope.launch {
            repository.setReminderMinute(minute)
            reminder.sync()
        }
    }
}
