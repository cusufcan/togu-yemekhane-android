package com.mercan.app.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/** Bugünün tarihi; gece yarısı ve ekran her yeniden açıldığında tazelenir. */
class TodaySource @Inject constructor(private val clock: Clock) {
    val today: Flow<LocalDate> = flow {
        while (true) {
            val now = clock.instant().atZone(ZoneId.systemDefault())
            emit(now.toLocalDate())
            val midnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
            delay(Duration.between(now, midnight).toMillis() + 1)
        }
    }.distinctUntilChanged()
}
