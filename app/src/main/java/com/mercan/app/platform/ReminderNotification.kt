package com.mercan.app.platform

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mercan.app.MainActivity
import com.mercan.app.R
import com.mercan.app.domain.DayMenu
import com.mercan.app.domain.asSentence
import com.mercan.app.domain.plate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ReminderNotification @Inject constructor(@ApplicationContext private val context: Context) {
    fun show(menu: DayMenu) {
        val plate = menu.plate()
        val main = plate.title ?: return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        // Uygulama içinden seçilen dil bildirimde de geçerli olsun.
        val localized = ContextCompat.getContextForLanguage(context)
        val manager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL,
                    localized.getString(R.string.reminder_channel),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val rest = plate.rest.asSentence()
        manager.notify(
            ID,
            NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_seyir_today)
                .setContentTitle(localized.getString(R.string.reminder_title, main))
                .setContentText(rest)
                .setStyle(NotificationCompat.BigTextStyle().bigText(rest))
                .setContentIntent(open)
                .setAutoCancel(true)
                .build(),
        )
    }

    private companion object {
        const val CHANNEL = "reminder"
        const val ID = 1
    }
}
