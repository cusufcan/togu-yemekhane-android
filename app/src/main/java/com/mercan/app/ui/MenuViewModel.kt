package com.mercan.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercan.app.data.TodaySource
import com.mercan.app.domain.MenuRepository
import com.mercan.app.domain.MenuState
import com.mercan.app.domain.RefreshState
import com.mercan.app.domain.SelectedDay
import com.mercan.app.domain.dayFor
import com.mercan.app.domain.menuState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class MenuUi(
    val menu: MenuState,
    val today: LocalDate,
    /** Yalnız [MenuState.Current] iken anlamlı: gösterilen gün. */
    val day: LocalDate,
    val refresh: RefreshState,
)

/** Bugün ve Hafta aynı duruma bakar, o yüzden ikisi de bu sınıfı kullanır. */
@HiltViewModel
class MenuViewModel @Inject constructor(
    menus: MenuRepository,
    todaySource: TodaySource,
    private val selected: SelectedDay,
) : ViewModel() {
    val state: StateFlow<MenuUi?> = combine(
        menus.weeks,
        menus.refresh,
        todaySource.today,
        selected.choice,
    ) { weeks, refresh, today, choice ->
        val menu = menuState(weeks, refresh, today, ZoneId.systemDefault())
        MenuUi(menu, today, (menu as? MenuState.Current)?.dayFor(choice, today) ?: today, refresh)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun select(day: LocalDate) {
        state.value?.let { selected.choose(day, it.today) }
    }
}
