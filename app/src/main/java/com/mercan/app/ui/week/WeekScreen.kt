package com.mercan.app.ui.week

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mercan.app.R
import com.mercan.app.domain.MenuState
import com.mercan.app.domain.week
import com.mercan.app.ui.components.FirstError
import com.mercan.app.ui.components.FreshLine
import com.mercan.app.ui.components.RefreshButton
import com.mercan.app.ui.components.SeyirIconButton
import com.mercan.app.ui.components.SeyirScreen
import com.mercan.app.ui.components.Skeleton
import com.mercan.app.ui.components.WeekRows
import com.mercan.app.ui.components.rememberFormats
import com.mercan.app.ui.MenuViewModel

@Composable
fun WeekScreen(
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenToday: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ui = state ?: return
    val title = stringResource(R.string.tab_week)
    val week = ui.menu.week
    if (week == null) {
        val failed = ui.refresh.lastFailed && !ui.refresh.running
        SeyirScreen(title, centered = failed) { if (failed) FirstError(onRetry) else Skeleton() }
        return
    }
    SeyirScreen(
        title = title,
        subtitle = rememberFormats().range(week),
        actions = {
            SeyirIconButton(R.drawable.ic_seyir_archive, stringResource(R.string.action_archive), onOpenArchive)
            RefreshButton(ui.refresh.running, onRefresh)
        },
    ) {
        WeekRows(
            week = week,
            today = ui.today,
            onDay = if (ui.menu is MenuState.Current) {
                { day ->
                    viewModel.select(day)
                    onOpenToday()
                }
            } else {
                null
            },
        )
        FreshLine(ui.refresh.lastSuccess, ui.today)
    }
}
