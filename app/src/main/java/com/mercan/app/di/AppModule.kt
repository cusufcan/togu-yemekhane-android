package com.mercan.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.mercan.app.data.local.MenuDao
import com.mercan.app.data.local.MenuDatabase
import com.mercan.app.data.local.RefreshPreferences
import com.mercan.app.data.local.SettingsPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.time.Clock
import javax.inject.Singleton

// 3.x aynı paketle kurulu ve dosyaları güncellemede yerinde kalır. Sürümlü adlar,
// onun bıraktığı bir veri tabanının ya da ayar dosyasının yanlışlıkla açılmasını önler.
private const val DATABASE = "menu_v4.db"
private const val SETTINGS = "settings_v4"
private const val REFRESH = "refresh_v4"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): MenuDatabase =
        Room.databaseBuilder(context, MenuDatabase::class.java, DATABASE).build()

    @Provides
    fun menuDao(database: MenuDatabase): MenuDao = database.menuDao()

    @Provides
    @Singleton
    @SettingsPreferences
    fun settingsPreferences(@ApplicationContext context: Context) = preferences(context, SETTINGS)

    @Provides
    @Singleton
    @RefreshPreferences
    fun refreshPreferences(@ApplicationContext context: Context) = preferences(context, REFRESH)

    /** Dosya bozulursa uygulama çökmez, varsayılanlarla açılır. */
    private fun preferences(context: Context, name: String): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            produceFile = { context.preferencesDataStoreFile(name) },
        )

    @Provides
    fun clock(): Clock = Clock.systemDefaultZone()

    @Provides
    @IoDispatcher
    fun io(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    @AppScope
    fun appScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
