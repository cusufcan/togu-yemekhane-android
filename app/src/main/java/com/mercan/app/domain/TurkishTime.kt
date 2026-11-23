package com.mercan.app.domain

import java.time.LocalTime

private val UNITS = listOf("", "de", "de", "te", "te", "te", "da", "de", "de", "da")
private val TENS = listOf("", "da", "de", "da", "ta", "de")

/**
 * Saatin bulunma eki: 08.12'de, 09.40'ta. Eki en son okunan sayı belirler;
 * dakika sıfırsa saat okunur (11.00'de), gece yarısı "sıfır" diye okunur.
 */
fun turkishTimeSuffix(time: LocalTime): String {
    val spoken = if (time.minute == 0) time.hour else time.minute
    return when {
        spoken == 0 -> "da"
        spoken % 10 == 0 -> TENS[spoken / 10]
        else -> UNITS[spoken % 10]
    }
}
