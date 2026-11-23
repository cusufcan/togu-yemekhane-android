package com.mercan.app.di

import com.mercan.app.platform.AlarmReminderScheduler
import com.mercan.app.platform.BackgroundRefresh
import com.mercan.app.platform.JobBackgroundRefresh
import com.mercan.app.platform.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface PlatformModule {
    @Binds fun backgroundRefresh(impl: JobBackgroundRefresh): BackgroundRefresh

    @Binds fun reminderScheduler(impl: AlarmReminderScheduler): ReminderScheduler
}
