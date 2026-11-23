package com.mercan.app.domain

import java.util.Locale

private val Turkish: Locale = Locale.forLanguageTag("tr")
private const val ACCENTED = "çğıöşüâîû"
private const val PLAIN = "cgiosuaiu"

/** Arama için sade biçim. Uzunluğu değişmez; böylece bulunan yer asıl metinde de aynı yerdir. */
fun String.folded(): String = lowercase(Turkish)
    .map { char -> ACCENTED.indexOf(char).let { if (it >= 0) PLAIN[it] else char } }
    .joinToString("")
