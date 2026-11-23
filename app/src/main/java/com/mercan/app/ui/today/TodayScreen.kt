package com.mercan.app.ui.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mercan.app.R
import com.mercan.app.domain.DayMenu
import com.mercan.app.domain.Dish
import com.mercan.app.domain.MenuState
import com.mercan.app.domain.plate
import com.mercan.app.domain.shownDates
import com.mercan.app.ui.MenuUi
import com.mercan.app.ui.MenuViewModel
import com.mercan.app.ui.components.DaySelector
import com.mercan.app.ui.components.EmptyState
import com.mercan.app.ui.components.FirstError
import com.mercan.app.ui.components.FreshLine
import com.mercan.app.ui.components.MotionMillis
import com.mercan.app.ui.components.RefreshButton
import com.mercan.app.ui.components.SeyirIcon
import com.mercan.app.ui.components.SeyirScreen
import com.mercan.app.ui.components.Skeleton
import com.mercan.app.ui.components.StaleCard
import com.mercan.app.ui.components.TonalButton
import com.mercan.app.ui.components.WeekRows
import com.mercan.app.ui.components.rememberFormats
import com.mercan.app.ui.theme.LocalSeyirColors
import java.time.LocalDate

@Composable
fun TodayScreen(
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onOpenWeek: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel(),
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    ui?.let { TodayContent(it, onRefresh, onRetry, onOpenWeek, viewModel::select) }
}

@Composable
private fun TodayContent(
    ui: MenuUi,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onOpenWeek: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    val formats = rememberFormats()
    val refresh = @Composable { RefreshButton(ui.refresh.running, onRefresh) }
    val today = stringResource(R.string.tab_today)
    val todayDate = formats.day(ui.today)
    when (val menu = ui.menu) {
        is MenuState.Nothing -> {
            val failed = menu.failed && !ui.refresh.running
            SeyirScreen(today, todayDate, centered = failed) {
                if (failed) FirstError(onRetry) else Skeleton()
            }
        }

        is MenuState.Weekend -> SeyirScreen(today, todayDate, actions = { refresh() }, centered = true) {
            EmptyState(
                glyph = R.drawable.ic_seyir_today,
                title = stringResource(R.string.weekend_title),
                body = stringResource(R.string.weekend_body),
            ) { TonalButton(stringResource(R.string.weekend_action), onOpenWeek) }
        }

        is MenuState.Stale -> SeyirScreen(today, todayDate, actions = { refresh() }) {
            StaleCard(
                title = stringResource(
                    if (menu.notPublished) R.string.stale_pending_title else R.string.stale_offline_title,
                ),
                body = stringResource(
                    if (menu.notPublished) R.string.stale_pending_body else R.string.stale_offline_body,
                ) + " " + stringResource(
                    if (menu.fromLastWeek) R.string.stale_below_last_week else R.string.stale_below_older,
                ),
            )
            Text(
                text = stringResource(R.string.last_downloaded, formats.range(menu.lastWeek)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, end = 4.dp),
            )
            WeekRows(menu.lastWeek)
        }

        is MenuState.Current -> {
            val isToday = ui.day == ui.today
            val title = if (isToday) today else formats.weekday(ui.day)
            val subtitle = if (isToday) todayDate else formats.dayMonth(ui.day)
            if (isToday && menu.week.menuOn(ui.day) == null) {
                SeyirScreen(title, subtitle, actions = { refresh() }, centered = true) {
                    EmptyState(
                        glyph = R.drawable.ic_seyir_today,
                        title = stringResource(R.string.no_menu_today_title),
                        body = stringResource(R.string.no_menu_today_body),
                    ) { TonalButton(stringResource(R.string.no_menu_today_action), onOpenWeek) }
                }
            } else {
                SeyirScreen(title, subtitle, actions = { refresh() }) {
                    DaySelector(menu.week.shownDates(), ui.day, ui.today, onSelect)
                    AnimatedContent(
                        targetState = ui.day,
                        transitionSpec = { fadeIn(tween(MotionMillis)) togetherWith fadeOut(tween(MotionMillis)) },
                        label = "menu",
                    ) { day ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val dayMenu = menu.week.menuOn(day)
                            if (dayMenu == null) {
                                EmptyState(
                                    glyph = R.drawable.ic_seyir_today,
                                    title = stringResource(R.string.no_menu_day_title),
                                    body = stringResource(R.string.no_menu_day_body),
                                    modifier = Modifier.padding(top = 36.dp, bottom = 24.dp),
                                )
                            } else {
                                DayMenuCards(dayMenu)
                            }
                        }
                    }
                    FreshLine(ui.refresh.lastSuccess, ui.today)
                }
            }
        }
    }
}

@Composable
private fun DayMenuCards(menu: DayMenu) {
    val plate = menu.plate()
    plate.main?.let { Hero(it, menu.calories) }
    if (plate.others.isNotEmpty()) Dishes(plate.others)
    plate.vegetarian.forEach { Vegetarian(it) }
}

@Composable
private fun Hero(dish: Dish, calories: Int?) {
    val content = MaterialTheme.colorScheme.onPrimaryContainer
    val formats = rememberFormats()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(28.dp))
            .heightIn(min = 148.dp)
            .semantics(mergeDescendants = true) {}
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.main_dish), style = MaterialTheme.typography.labelMedium, color = content)
            Text(
                text = dish.name,
                style = MaterialTheme.typography.headlineLarge.copy(lineBreak = LineBreak.Heading),
                color = content,
            )
        }
        if (calories != null) {
            Text(
                text = stringResource(R.string.menu_total, formats.number(calories)),
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                color = content,
            )
        }
    }
}

@Composable
private fun Dishes(dishes: List<Dish>) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(20.dp))
        .padding(horizontal = 16.dp, vertical = 4.dp),
) {
    dishes.forEachIndexed { index, dish ->
        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier.heightIn(min = 48.dp).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) { Text(dish.name, style = MaterialTheme.typography.bodyLarge) }
    }
}

@Composable
private fun Vegetarian(dish: Dish) {
    val colors = LocalSeyirColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.positiveContainer, RoundedCornerShape(20.dp))
            .heightIn(min = 64.dp)
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SeyirIcon(R.drawable.ic_seyir_leaf, tint = colors.onPositiveContainer)
        Column {
            Text(
                text = stringResource(R.string.vegetarian_option),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onPositiveContainer,
            )
            Text(
                text = dish.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onPositiveContainer,
            )
        }
    }
}
