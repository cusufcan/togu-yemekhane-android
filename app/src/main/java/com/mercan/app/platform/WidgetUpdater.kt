package com.mercan.app.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.View.MeasureSpec
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mercan.app.R
import com.mercan.app.domain.MenuRepository
import com.mercan.app.domain.WidgetLines
import com.mercan.app.domain.WidgetState
import com.mercan.app.domain.asSentence
import com.mercan.app.domain.nextDayStart
import com.mercan.app.domain.plate
import com.mercan.app.domain.widgetLines
import com.mercan.app.domain.widgetState
import com.mercan.app.ui.components.formatsOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor

@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val menus: MenuRepository,
    private val clock: Clock,
) {
    private val lock = Mutex()

    /** Ana ekranda widget varsa çizer ve gün dönümünü kurar; yoksa kurulu olanı siler. */
    suspend fun update(localized: Context = ContextCompat.getContextForLanguage(context)) = lock.withLock {
        val manager = context.getSystemService(AppWidgetManager::class.java) ?: return@withLock
        val ids = manager.getAppWidgetIds(ComponentName(context, MenuWidget::class.java))
        val alarms = context.getSystemService(AlarmManager::class.java)
        if (ids.isEmpty()) {
            alarms.cancel(newDayIntent())
            return@withLock
        }
        val now = clock.instant()
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        val content = content(widgetState(menus.weeks.first(), today), today, localized)
        // Ölçmek için gerçek görünüm kurulur; görünümler ana iş parçacığında kurulur.
        withContext(Dispatchers.Main) {
            ids.forEach { manager.updateAppWidget(it, views(content, manager.getAppWidgetOptions(it), localized)) }
        }
        // Telefonu uyandırmaz; ekran açıldığında geç kalmışsa hemen gelir.
        alarms.set(AlarmManager.RTC, nextDayStart(now, zone).toEpochMilli(), newDayIntent())
    }

    private fun newDayIntent() = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, MenuWidget::class.java).setAction(MenuWidget.ACTION_NEW_DAY),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private class Content(
        val date: String,
        val calories: String?,
        val title: String,
        /** Yer varsa alt alta yazılır. */
        val lines: List<String>,
        /** Yer yoksa yan yana yazılır. */
        val sentence: String,
    )

    private fun content(state: WidgetState, today: LocalDate, localized: Context): Content {
        val formats = formatsOf(localized)
        fun message(title: Int, body: Int) = localized.getString(body).let {
            Content(formats.day(today), null, localized.getString(title), listOf(it), it)
        }
        return when (state) {
            is WidgetState.Menu -> {
                val plate = state.day.plate()
                Content(
                    date = formats.day(today),
                    calories = state.day.calories?.let {
                        localized.getString(R.string.widget_calories, formats.number(it))
                    },
                    title = plate.title.orEmpty(),
                    lines = plate.rest.map { it.name },
                    sentence = plate.rest.asSentence(),
                )
            }
            WidgetState.NoMenuToday -> message(R.string.no_menu_today_title, R.string.no_menu_today_body)
            WidgetState.Weekend -> message(R.string.weekend_title, R.string.widget_weekend_body)
            WidgetState.Missing -> message(R.string.widget_missing_title, R.string.widget_missing_body)
        }
    }

    private data class Measured(val titleSize: Float, val lines: WidgetLines, val stackedFits: Boolean)

    /** Seçenekler iki yönün ölçüsünü birlikte verir: dikeyde dar ve uzun, yatayda geniş ve kısa. */
    private fun views(content: Content, options: Bundle, localized: Context): RemoteViews {
        fun dp(key: String, fallback: Int) = options.getInt(key).takeIf { it > 0 } ?: fallback
        return RemoteViews(
            views(
                content,
                dp(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, DEFAULT_WIDTH),
                dp(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, DEFAULT_HEIGHT),
                localized,
            ),
            views(
                content,
                dp(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, DEFAULT_WIDTH),
                dp(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, DEFAULT_HEIGHT),
                localized,
            ),
        )
    }

    private fun views(content: Content, widthDp: Int, heightDp: Int, localized: Context): RemoteViews {
        val fontScale = localized.resources.configuration.fontScale
        val calories = content.calories.takeIf { widthDp >= CALORIES_MIN_WIDTH * fontScale }
        val stacked = content.lines.joinToString("\n")
        val (titleSize, lines, stackedFits) = measure(content, calories, stacked, widthDp, heightDp, localized)
        return RemoteViews(context.packageName, R.layout.widget_menu).apply {
            setTextViewText(R.id.widget_date, content.date)
            setTextViewText(R.id.widget_calories, calories)
            setViewVisibility(R.id.widget_calories, if (calories == null) View.GONE else View.VISIBLE)
            setTextViewText(R.id.widget_title, content.title)
            setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_PX, titleSize)
            setInt(R.id.widget_title, "setMaxLines", lines.title)
            setTextViewText(R.id.widget_rest, if (stackedFits) stacked else content.sentence)
            setInt(R.id.widget_rest, "setMaxLines", lines.rest.coerceAtLeast(1))
            setViewVisibility(
                R.id.widget_rest,
                if (lines.rest == 0 || content.lines.isEmpty()) View.GONE else View.VISIBLE,
            )
            setOnClickPendingIntent(android.R.id.background, openAppIntent(context))
        }
    }

    /**
     * Sabit satır yüksekliği kullanılmaz: Android 14'ten beri yazı ölçeği doğrusal değil, satır yüksekliği
     * de sürüme göre değişiyor. Aynı layout burada kurulup ölçülür.
     */
    private fun measure(
        content: Content,
        calories: String?,
        stacked: String,
        widthDp: Int,
        heightDp: Int,
        localized: Context,
    ): Measured {
        // Activity context'i TextView yerine AppCompatTextView kurar; launcher'dakiyle aynı olsun.
        val plain = context.createConfigurationContext(localized.resources.configuration)
        val density = plain.resources.displayMetrics.density
        val root = View.inflate(plain, R.layout.widget_menu, null)
        val title = root.findViewById<TextView>(R.id.widget_title).apply { text = content.title }
        // Sondaki satır, tek yemekte de satır aralığı ölçülsün diye.
        val sample = (content.lines + "A").joinToString("\n")
        val rest = root.findViewById<TextView>(R.id.widget_rest).apply { text = sample }
        root.findViewById<TextView>(R.id.widget_date).text = content.date
        root.findViewById<TextView>(R.id.widget_calories).run {
            text = calories
            visibility = if (calories == null) View.GONE else View.VISIBLE
        }
        fun measureRoot() = root.measure(
            MeasureSpec.makeMeasureSpec((widthDp * density).toInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
        )
        measureRoot()
        // Dar widget'ta uzun kelime ortasından bölünmesin diye başlık küçülür.
        val longestWord = content.title.split(' ').maxOf(title.paint::measureText)
        if (longestWord > title.measuredWidth) {
            val fitting = floor(title.textSize * title.measuredWidth / longestWord)
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, fitting)
            measureRoot()
        }
        val others = root.measuredHeight - title.measuredHeight - rest.measuredHeight
        val lines = widgetLines(
            free = (heightDp * density).toInt() - others,
            titleLine = title.measuredHeight / title.lineCount,
            titleLines = title.lineCount.coerceAtMost(TITLE_MAX_LINES),
            restLine = rest.layout.getLineTop(1),
        )
        return Measured(title.textSize, lines, stackedFits = rest.lineCount - 1 <= lines.rest)
    }

    private companion object {
        const val DEFAULT_WIDTH = 250
        const val DEFAULT_HEIGHT = 150
        const val CALORIES_MIN_WIDTH = 240
        const val TITLE_MAX_LINES = 3
    }
}
