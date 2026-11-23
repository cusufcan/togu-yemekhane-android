package com.mercan.app.ui.archive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mercan.app.data.TodaySource
import com.mercan.app.domain.DishHit
import com.mercan.app.domain.MenuRepository
import com.mercan.app.domain.WeekMenu
import com.mercan.app.domain.finishedWeeks
import com.mercan.app.domain.searchDishes
import com.mercan.app.ui.navigation.ArchiveWeek
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ArchiveUi(
    /** Biten haftalar, en yeni en üstte. */
    val weeks: List<WeekMenu>,
    val query: String,
    val hits: List<DishHit>,
    val download: Download,
)

enum class Download { RUNNING, DONE, FAILED }

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val menus: MenuRepository,
    today: TodaySource,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val download = MutableStateFlow(Download.RUNNING)

    private val finished = combine(menus.weeks, today.today) { weeks, date -> finishedWeeks(weeks, date) }

    val state: StateFlow<ArchiveUi?> = combine(finished, query, download) { weeks, text, download ->
        ArchiveUi(weeks, text, if (text.isBlank()) emptyList() else searchDishes(weeks, text), download)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        refresh()
    }

    fun refresh() {
        download.value = Download.RUNNING
        viewModelScope.launch {
            download.value = if (menus.refreshArchive()) Download.DONE else Download.FAILED
        }
    }

    fun search(text: String) {
        query.value = text
    }
}

@HiltViewModel
class ArchiveWeekViewModel @Inject constructor(
    menus: MenuRepository,
    saved: SavedStateHandle,
) : ViewModel() {
    private val start = LocalDate.parse(saved.toRoute<ArchiveWeek>().weekStart)

    val week: StateFlow<WeekMenu?> = menus.weeks
        .map { weeks -> weeks.firstOrNull { it.weekStart == start } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
