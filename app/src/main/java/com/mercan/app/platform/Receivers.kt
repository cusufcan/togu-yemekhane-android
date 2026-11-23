package com.mercan.app.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mercan.app.data.TodaySource
import com.mercan.app.di.AppScope
import com.mercan.app.domain.MenuRepository
import com.mercan.app.domain.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Hatırlatma saati geldi: o günün menüsü varsa göster, sonra yarını kur. */
@AndroidEntryPoint(BroadcastReceiver::class)
class ReminderReceiver : Hilt_ReminderReceiver() {
    @Inject lateinit var menus: MenuRepository

    @Inject lateinit var settings: SettingsRepository

    @Inject lateinit var today: TodaySource

    @Inject lateinit var scheduler: ReminderScheduler

    @Inject lateinit var notification: ReminderNotification

    @Inject @AppScope lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        finishLater(scope) {
            if (settings.settings.first().reminderOn) {
                val date = today.today.first()
                menus.weeks.first().firstNotNullOfOrNull { it.menuOn(date) }?.let(notification::show)
            }
            scheduler.sync()
        }
    }
}

/** Telefon açılınca, saat değişince ve güncellemeden sonra hatırlatıcıyı yeniden kurar. */
@AndroidEntryPoint(BroadcastReceiver::class)
class BootReceiver : Hilt_BootReceiver() {
    @Inject lateinit var scheduler: ReminderScheduler

    @Inject @AppScope lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in SyncActions) finishLater(scope) { scheduler.sync() }
    }
}

private val SyncActions = setOf(
    Intent.ACTION_BOOT_COMPLETED,
    Intent.ACTION_MY_PACKAGE_REPLACED,
    Intent.ACTION_TIMEZONE_CHANGED,
    Intent.ACTION_TIME_CHANGED,
)

private fun BroadcastReceiver.finishLater(scope: CoroutineScope, work: suspend () -> Unit) {
    val pending = goAsync()
    scope.launch {
        try {
            work()
        } finally {
            pending.finish()
        }
    }
}
