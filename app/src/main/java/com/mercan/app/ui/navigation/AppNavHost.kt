package com.mercan.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import com.mercan.app.R
import com.mercan.app.ui.AppViewModel
import com.mercan.app.ui.archive.ArchiveScreen
import com.mercan.app.ui.archive.ArchiveWeekScreen
import com.mercan.app.ui.components.MotionMillis
import com.mercan.app.ui.settings.SettingsScreen
import com.mercan.app.ui.today.TodayScreen
import com.mercan.app.ui.today.WidgetHint
import com.mercan.app.ui.week.WeekScreen
import kotlinx.serialization.Serializable

@Serializable data object Today

@Serializable data object Week

@Serializable data object Settings

@Serializable data object Archive

@Serializable data class ArchiveWeek(val weekStart: String)

@Composable
fun AppNavHost(app: AppViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val destination = entry?.destination
    val tab = when {
        destination == null || destination.hasRoute<Today>() -> AppTab.TODAY
        destination.hasRoute<Settings>() -> AppTab.SETTINGS
        else -> AppTab.WEEK
    }

    val snackbar = remember { SnackbarHostState() }
    val failed = stringResource(R.string.refresh_failed)
    LaunchedEffect(failed) {
        app.refreshFailed.collect { snackbar.showSnackbar(failed) }
    }
    LifecycleStartEffect(Unit) {
        app.refresh(byHand = false)
        onStopOrDispose {}
    }
    val refresh = { app.refresh(byHand = true) }
    val retry = { app.refresh(byHand = false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { AppTabs(tab) { nav.openTab(it, reselected = it == tab) } },
    ) { padding ->
        Box(Modifier.padding(padding).consumeWindowInsets(padding).imePadding().fillMaxSize()) {
            NavHost(
                navController = nav,
                startDestination = Today,
                enterTransition = { fadeIn(tween(MotionMillis)) },
                exitTransition = { fadeOut(tween(MotionMillis)) },
            ) {
                composable<Today> {
                    val settings by app.settings.collectAsStateWithLifecycle()
                    TodayScreen(
                        onRefresh = refresh,
                        onRetry = retry,
                        onOpenWeek = { nav.openTab(AppTab.WEEK, reselected = true) },
                        widgetHint = { WidgetHint(settings?.widgetHintDone != false, app::dismissWidgetHint) },
                    )
                }
                composable<Week> {
                    WeekScreen(
                        onRefresh = refresh,
                        onRetry = retry,
                        onOpenArchive = { nav.navigate(Archive) { launchSingleTop = true } },
                        onOpenToday = { nav.openTab(AppTab.TODAY, reselected = false) },
                    )
                }
                composable<Archive> {
                    ArchiveScreen(
                        onBack = { nav.popBackStack<Archive>(inclusive = true) },
                        onOpen = { nav.navigate(ArchiveWeek(it.toString())) { launchSingleTop = true } },
                    )
                }
                composable<ArchiveWeek> {
                    ArchiveWeekScreen(onBack = { nav.popBackStack<ArchiveWeek>(inclusive = true) })
                }
                composable<Settings> { SettingsScreen() }
            }
        }
    }
}

/** Sekmeler kendi yerini hatırlar; [reselected] ise sekmenin ilk ekranına dönülür. */
private fun NavHostController.openTab(tab: AppTab, reselected: Boolean) {
    when (tab) {
        AppTab.TODAY -> popBackStack<Today>(inclusive = false, saveState = !reselected)
        AppTab.WEEK -> {
            switchTo(Week)
            if (reselected) popBackStack<Week>(inclusive = false)
        }
        AppTab.SETTINGS -> switchTo(Settings)
    }
}

private fun NavHostController.switchTo(route: Any) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
