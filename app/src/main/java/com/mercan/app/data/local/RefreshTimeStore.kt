package com.mercan.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

/** Son başarılı indirme zamanı. Menü tablolarından ayrıdır: menü değişmediyse tablolara hiç yazılmaz. */
interface RefreshTimeStore {
    suspend fun read(): Instant?
    suspend fun write(time: Instant)
}

class DataStoreRefreshTimeStore @Inject constructor(
    @param:RefreshPreferences private val store: DataStore<Preferences>,
) : RefreshTimeStore {
    override suspend fun read(): Instant? = store.data.first()[LastSuccess]?.let(Instant::ofEpochMilli)

    override suspend fun write(time: Instant) {
        store.edit { it[LastSuccess] = time.toEpochMilli() }
    }

    private companion object {
        val LastSuccess = longPreferencesKey("last_success")
    }
}
