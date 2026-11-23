package com.mercan.app.platform

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build

/** Widget'ı uygulamanın içinden ana ekrana ekleme. Pencereyi Android açar, son sözü kullanıcı söyler. */
object WidgetPin {
    /** Android 8 öncesinde ve bazı ana ekran uygulamalarında yoktur. */
    fun isSupported(context: Context): Boolean =
        Build.VERSION.SDK_INT >= 26 && context.manager()?.isRequestPinAppWidgetSupported == true

    fun isPlaced(context: Context): Boolean =
        context.manager()?.getAppWidgetIds(context.widget())?.isNotEmpty() == true

    fun request(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) context.manager()?.requestPinAppWidget(context.widget(), null, null)
    }

    /** Ana ekran widget'ı olmayan cihazda null. */
    private fun Context.manager(): AppWidgetManager? = getSystemService(AppWidgetManager::class.java)

    private fun Context.widget() = ComponentName(this, MenuWidget::class.java)
}
