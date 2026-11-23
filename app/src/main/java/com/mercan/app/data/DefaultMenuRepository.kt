package com.mercan.app.data

import com.mercan.app.data.local.MenuStore
import com.mercan.app.data.local.RefreshTimeStore
import com.mercan.app.data.remote.MenuApi
import com.mercan.app.domain.MenuRepository
import com.mercan.app.domain.RefreshState
import com.mercan.app.domain.WeekMenu
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultMenuRepository @Inject constructor(
    private val api: MenuApi,
    private val store: MenuStore,
    private val times: RefreshTimeStore,
    private val clock: Clock,
) : MenuRepository {
    private val state = MutableStateFlow(RefreshState())
    private val lock = Mutex()

    override val weeks: Flow<List<WeekMenu>> = store.weeks
    override val refresh = state.asStateFlow()

    override suspend fun refreshCurrent(): Boolean = lock.withLock {
        try {
            state.update { it.copy(running = true, lastSuccess = it.lastSuccess ?: times.read()) }
            val ok = succeeds { save(listOf(api.current())) }
            if (ok) {
                val now = clock.instant()
                times.write(now)
                state.update { it.copy(lastFailed = false, lastSuccess = now) }
            } else {
                state.update { it.copy(lastFailed = true) }
            }
            ok
        } finally {
            state.update { it.copy(running = false) }
        }
    }

    override suspend fun refreshArchive(): Boolean = lock.withLock { succeeds { save(api.archive()) } }

    /** Yalnız değişen haftalar yazılır; gelmeyen hafta silinmez. */
    private suspend fun save(weeks: List<WeekMenu>) {
        val known = store.all().associateBy { it.weekStart }
        val changed = weeks.filter { known[it.weekStart] != it }
        if (changed.isNotEmpty()) store.replace(changed)
    }

    /** Ağ, biçim ya da kayıt hatası menüyü silmez; yalnız başarısız sayılır. */
    private suspend fun succeeds(block: suspend () -> Unit): Boolean = try {
        block()
        true
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        false
    }
}
