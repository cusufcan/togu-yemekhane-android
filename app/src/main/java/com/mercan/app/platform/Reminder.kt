package com.mercan.app.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mercan.app.domain.SettingsRepository
import com.mercan.app.domain.nextReminderAt
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.ZoneId
import javax.inject.Inject

interface ReminderScheduler {
    /** Telefonun hatırlatıcısını ayarlara uydurur; açıksa kurar, kapalıysa siler. */
    suspend fun sync()
}

class AlarmReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val clock: Clock,
) : ReminderScheduler {
    override suspend fun sync() {
        val alarms = context.getSystemService(AlarmManager::class.java)
        val intent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val current = settings.settings.first()
        if (!current.reminderOn) {
            alarms.cancel(intent)
            return
        }
        val at = nextReminderAt(clock.instant(), current.reminderMinute, ZoneId.systemDefault())
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toEpochMilli(), intent)
    }
}
