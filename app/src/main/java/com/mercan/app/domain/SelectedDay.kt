package com.mercan.app.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class DayChoice(val date: LocalDate, val chosenOn: LocalDate)

/** Bugün ekranında seçili gün. Hafta ekranı da buraya yazar, o yüzden iki ekranın ortak yeri. */
@Singleton
class SelectedDay @Inject constructor() {
    private val state = MutableStateFlow<DayChoice?>(null)
    val choice: StateFlow<DayChoice?> = state.asStateFlow()

    fun choose(date: LocalDate, today: LocalDate) {
        state.value = DayChoice(date, today)
    }
}

/** Seçim yalnız yapıldığı gün ve gösterilen hafta içinde geçerlidir; yoksa ekran bugünle açılır. */
fun MenuState.Current.dayFor(choice: DayChoice?, today: LocalDate): LocalDate =
    choice?.takeIf { it.chosenOn == today && it.date in week.shownDates() }?.date ?: initialDay
