package com.mercan.togu.robot

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Yayınlanan dosyalar. `weeks/` asıl kayıttır ve hiç silinmez;
 * `current.json` ve `archive.json` her seferinde ondan üretilir.
 */
class MenuStore(root: Path) {
    private val v1 = root.resolve("v1")
    private val weeksDir = v1.resolve("weeks")

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    /** @return dosyalardan en az biri değiştiyse `true`. */
    fun save(week: WeekMenu): Boolean {
        weeksDir.createDirectories()
        val weekChanged = write(weeksDir.resolve("${week.weekStart}.json"), json.encodeToString(week))
        val archiveChanged = rebuild()
        return weekChanged || archiveChanged
    }

    private fun rebuild(): Boolean {
        val weeks = weeksDir.listDirectoryEntries()
            .filter { it.extension == "json" }
            .sortedByDescending { it.name }
            .map { json.decodeFromString<WeekMenu>(it.readText()) }
        val currentChanged = write(v1.resolve("current.json"), json.encodeToString(weeks.first()))
        val archiveChanged = write(v1.resolve("archive.json"), Json.encodeToString(Archive(weeks)))
        return currentChanged || archiveChanged
    }

    private fun write(file: Path, content: String): Boolean {
        val text = content + "\n"
        if (file.exists() && file.readText() == text) return false
        file.writeText(text)
        return true
    }
}
