package com.mercan.app.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MenuRepository {
    /** Telefondaki bütün haftalar. */
    val weeks: Flow<List<WeekMenu>>
    val refresh: StateFlow<RefreshState>

    /** Güncel haftayı indirir. Başarısız olursa eldeki menü durur ve false döner. */
    suspend fun refreshCurrent(): Boolean

    /** Bütün arşivi indirir. Başarısız olursa eldeki arşiv durur ve false döner. */
    suspend fun refreshArchive(): Boolean
}

interface SettingsRepository {
    val settings: Flow<Settings>
    suspend fun setTheme(theme: ThemeMode)
    suspend fun setReminderOn(on: Boolean)
    suspend fun setReminderMinute(minute: Int)
    suspend fun dismissWidgetHint()
}
