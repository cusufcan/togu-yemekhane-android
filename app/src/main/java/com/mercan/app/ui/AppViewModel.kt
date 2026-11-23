package com.mercan.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercan.app.domain.MenuRepository
import com.mercan.app.domain.Settings
import com.mercan.app.domain.SettingsRepository
import com.mercan.app.platform.BackgroundRefresh
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Bütün uygulamayı ilgilendiren işler: tema, açılış ve menüyü yenileme. */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val menus: MenuRepository,
    private val settingsRepository: SettingsRepository,
    private val background: BackgroundRefresh,
) : ViewModel() {
    /** Ayarlar ve menü telefondan okununca dolar; açılış ekranı o zamana dek durur. */
    val settings: StateFlow<Settings?> = combine(settingsRepository.settings, menus.weeks) { settings, _ -> settings }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val failed = Channel<Unit>(Channel.CONFLATED)

    /** Kullanıcının istediği yenileme başarısız oldu. */
    val refreshFailed: Flow<Unit> = failed.receiveAsFlow()

    private var job: Job? = null
    private var announce = false

    /** Uygulama öne gelince sessizce çalışır; elle yenilemede hata söylenir. */
    fun refresh(byHand: Boolean) {
        if (byHand) announce = true
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            val ok = menus.refreshCurrent()
            if (ok) background.cancelRetry() else background.retryWhenOnline()
            if (!ok && announce) failed.send(Unit)
            announce = false
        }
    }

    fun dismissWidgetHint() {
        viewModelScope.launch { settingsRepository.dismissWidgetHint() }
    }
}
