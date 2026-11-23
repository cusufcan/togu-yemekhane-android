package com.mercan.app.platform

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.mercan.app.di.AppScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

/** Ana ekran widget'ı. Eklenince, boyu değişince, kaldırılınca ve gün dönünce yeniden çizilir. */
@AndroidEntryPoint(AppWidgetProvider::class)
class MenuWidget : Hilt_MenuWidget() {
    @Inject lateinit var widgets: WidgetUpdater

    @Inject @AppScope lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        finishLater(scope) { widgets.update() }
    }

    companion object {
        const val ACTION_NEW_DAY = "com.mercan.app.action.WIDGET_NEW_DAY"
    }
}
