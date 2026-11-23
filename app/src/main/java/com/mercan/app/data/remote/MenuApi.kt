package com.mercan.app.data.remote

import com.mercan.app.domain.WeekMenu

/** Robotun yayınladığı dosyalar. Üniversitenin sayfasına uygulama hiç gitmez. */
interface MenuApi {
    suspend fun current(): WeekMenu
    suspend fun archive(): List<WeekMenu>
}
