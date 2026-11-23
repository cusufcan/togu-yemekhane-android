package com.mercan.app

import android.app.Application
import com.mercan.app.di.AppScope
import com.mercan.app.domain.MenuRepository
import com.mercan.app.platform.BackgroundRefresh
import com.mercan.app.platform.ReminderScheduler
import com.mercan.app.platform.WidgetUpdater
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class YemekhaneApp : Application() {
    @Inject lateinit var backgroundRefresh: BackgroundRefresh

    @Inject lateinit var reminder: ReminderScheduler

    @Inject lateinit var menus: MenuRepository

    @Inject lateinit var widgets: WidgetUpdater

    @Inject @AppScope lateinit var scope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        backgroundRefresh.ensureScheduled()
        // "Zorla durdur" kurulu hatırlatıcıyı siler; uygulama açılınca geri gelsin.
        scope.launch { reminder.sync() }
        // Menü hangi yoldan gelirse gelsin (ekran ya da arka plan) widget kayıtlı olanı gösterir.
        scope.launch { menus.weeks.distinctUntilChanged().collect { widgets.update() } }
    }
}
