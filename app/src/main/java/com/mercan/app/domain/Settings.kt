package com.mercan.app.domain

data class Settings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val reminderOn: Boolean = false,
    /** Gün içindeki dakika; 11.30 = 690. */
    val reminderMinute: Int = 690,
    /** Widget tanıtımı kapatıldı ya da widget eklendi; kart bir daha çıkmaz. */
    val widgetHintDone: Boolean = false,
)
