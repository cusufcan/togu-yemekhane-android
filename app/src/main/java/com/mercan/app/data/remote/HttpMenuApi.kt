package com.mercan.app.data.remote

import com.mercan.app.di.IoDispatcher
import com.mercan.app.domain.WeekMenu
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

private const val BASE_URL = "https://cusufcan.github.io/togu-yemekhane-android/v1/"
private const val TIMEOUT_MS = 15_000

class HttpMenuApi @Inject constructor(
    @param:IoDispatcher private val io: CoroutineDispatcher,
) : MenuApi {

    override suspend fun current(): WeekMenu = withContext(io) { parseWeek(get("current.json")) }

    override suspend fun archive(): List<WeekMenu> = withContext(io) { parseArchive(get("archive.json")) }

    private fun get(path: String): String {
        val connection = URL(BASE_URL + path).openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        // Tarayıcı önbelleği değil, sunucudaki son sürüm istenir.
        connection.useCaches = false
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}: $path")
            }
            return connection.inputStream.use { it.readBytes().decodeToString() }
        } finally {
            connection.disconnect()
        }
    }
}
