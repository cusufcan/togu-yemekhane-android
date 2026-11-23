package com.mercan.app.di

import com.mercan.app.data.DataStoreSettingsRepository
import com.mercan.app.data.DefaultMenuRepository
import com.mercan.app.data.local.DataStoreRefreshTimeStore
import com.mercan.app.data.local.MenuStore
import com.mercan.app.data.local.RefreshTimeStore
import com.mercan.app.data.local.RoomMenuStore
import com.mercan.app.data.remote.HttpMenuApi
import com.mercan.app.data.remote.MenuApi
import com.mercan.app.domain.MenuRepository
import com.mercan.app.domain.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface BindingsModule {
    @Binds
    fun menuApi(impl: HttpMenuApi): MenuApi

    @Binds
    fun menuStore(impl: RoomMenuStore): MenuStore

    @Binds
    fun refreshTimes(impl: DataStoreRefreshTimeStore): RefreshTimeStore

    @Binds
    fun menuRepository(impl: DefaultMenuRepository): MenuRepository

    @Binds
    fun settingsRepository(impl: DataStoreSettingsRepository): SettingsRepository
}
