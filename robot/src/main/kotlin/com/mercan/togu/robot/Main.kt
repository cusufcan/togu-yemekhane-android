package com.mercan.togu.robot

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.time.Duration
import kotlin.system.exitProcess

private const val MENU_URL = "https://sosyaltesisler.gop.edu.tr/yemekhane_menu.aspx"
private const val USER_AGENT = "togu-yemekhane-robot"

fun main(args: Array<String>) {
    val output = Path.of(args.singleOrNull() ?: error("Kullanım: robot <çıktı klasörü>"))
    try {
        val week = MenuPageParser.parse(download())
        val changed = MenuStore(output).save(week)
        println("${week.weekStart} haftası: ${if (changed) "güncellendi" else "değişiklik yok"}")
    } catch (e: MenuPageException) {
        // Hata kodu iş akışını kırmızı yapar; yayındaki dosyalara dokunulmamıştır.
        System.err.println("Menü okunamadı: ${e.message}")
        exitProcess(1)
    }
}

private fun download(): String {
    val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build()
    val request = HttpRequest.newBuilder(URI(MENU_URL))
        .header("User-Agent", USER_AGENT)
        .timeout(Duration.ofSeconds(60))
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    if (response.statusCode() != 200) throw MenuPageException("sayfa ${response.statusCode()} döndü")
    return response.body()
}
