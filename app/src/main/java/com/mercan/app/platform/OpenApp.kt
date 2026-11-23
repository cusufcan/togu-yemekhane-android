package com.mercan.app.platform

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mercan.app.MainActivity

/**
 * Uygulama simgesine basmakla aynı: açık ekran varsa öne gelir. Simgenin intent'i taklit edilmezse
 * sistem açık ekranın üstüne ikinci bir MainActivity koyar.
 */
fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
    context,
    0,
    Intent(context, MainActivity::class.java)
        .setAction(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
)
