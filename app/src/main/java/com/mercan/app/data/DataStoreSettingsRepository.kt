package com.mercan.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mercan.app.data.local.SettingsPreferences
import com.mercan.app.domain.Settings
import com.mercan.app.domain.SettingsRepository
import com.mercan.app.domain.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreSettingsRepository @Inject constructor(
    @param:SettingsPreferences private val store: DataStore<Preferences>,
) : SettingsRepository {
    override val settings: Flow<Settings> = store.data.map { saved ->
        val defaults = Settings()
        Settings(
            theme = ThemeMode.entries.firstOrNull { it.name == saved[Theme] } ?: defaults.theme,
            reminderOn = saved[ReminderOn] ?: defaults.reminderOn,
            reminderMinute = saved[ReminderMinute] ?: defaults.reminderMinute,
            widgetHintDone = saved[WidgetHintDone] ?: defaults.widgetHintDone,
        )
    }

    override suspend fun setTheme(theme: ThemeMode) {
        store.edit { it[Theme] = theme.name }
    }

    override suspend fun setReminderOn(on: Boolean) {
        store.edit { it[ReminderOn] = on }
    }

    override suspend fun setReminderMinute(minute: Int) {
        store.edit { it[ReminderMinute] = minute }
    }

    override suspend fun dismissWidgetHint() {
        store.edit { it[WidgetHintDone] = true }
    }

    private companion object {
        val Theme = stringPreferencesKey("theme")
        val ReminderOn = booleanPreferencesKey("reminder_on")
        val ReminderMinute = intPreferencesKey("reminder_minute")
        val WidgetHintDone = booleanPreferencesKey("widget_hint_done")
    }
}
