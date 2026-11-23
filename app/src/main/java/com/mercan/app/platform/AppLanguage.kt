package com.mercan.app.platform

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(val tag: String) {
    SYSTEM(""),
    ENGLISH("en"),
    TURKISH("tr"),
    ;

    companion object {
        fun current(): AppLanguage {
            val tag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            return entries.firstOrNull { it != SYSTEM && tag.startsWith(it.tag) } ?: SYSTEM
        }

        fun apply(language: AppLanguage) =
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }
}
