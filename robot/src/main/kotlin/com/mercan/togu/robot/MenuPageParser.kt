package com.mercan.togu.robot

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

/** Sayfa beklenen yapıda değil ya da içeriği yayınlanacak kadar sağlam değil. */
class MenuPageException(message: String) : Exception(message)

/**
 * Haftalık menü sayfasını okur. Sunucu isteği yapana göre farklı etiketler
 * (`style` ya da `<font>`) ürettiği için biçim etiketlerine değil metne bakılır.
 */
object MenuPageParser {
    private const val DAY_COUNT = 5
    private const val MIN_DISHES_IN_REAL_DAY = 2
    private val pageDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val vegetarianMark = Regex("""\(\s*\*\s*\)""")
    private val calorieText = Regex("""Toplam Kalori\s*:?\s*(\d+)?""")
    private val whitespace = Regex("""[\s ]+""")

    fun parse(html: String): WeekMenu {
        val document = Jsoup.parse(html)
        val start = document.date("haftaBasi")
        val end = document.date("haftaSonu")
        if (start.dayOfWeek != DayOfWeek.MONDAY) fail("Hafta pazartesi başlamıyor: $start")
        if (end != start.plusDays(DAY_COUNT - 1L)) fail("Hafta aralığı beklenmedik: $start - $end")

        val week = WeekMenu(
            weekStart = start.toString(),
            weekEnd = end.toString(),
            days = document.dayCells().mapIndexed { index, cell -> cell.toDay(start.plusDays(index.toLong())) },
        )
        // Site tatillerde ya da güncellenirken tek satırlık yer tutucu gösterebiliyor.
        if (week.days.none { it.dishes.size >= MIN_DISHES_IN_REAL_DAY }) fail("$start haftasında menü yok")
        return week
    }

    private fun Document.date(idSuffix: String): LocalDate {
        val text = selectFirst("[id$=_$idSuffix]")?.text()?.trim() ?: fail("$idSuffix bulunamadı")
        return try {
            LocalDate.parse(text, pageDate)
        } catch (_: DateTimeParseException) {
            fail("$idSuffix tarih değil: '$text'")
        }
    }

    private fun Document.dayCells(): List<Element> {
        val header = selectFirst("[id$=_LabelPzt]")?.closest("tr") ?: fail("Gün başlıkları bulunamadı")
        val cells = header.nextElementSibling()?.children().orEmpty().filter { it.tagName() == "td" }
        if (cells.size != DAY_COUNT) fail("$DAY_COUNT gün beklenirken ${cells.size} hücre var")
        return cells
    }

    private fun Element.toDay(date: LocalDate): DayMenu {
        val lines = lines()
        return DayMenu(
            date = date.toString(),
            dishes = lines.filterNot(calorieText::containsMatchIn).mapNotNull(::toDish),
            calories = lines.firstNotNullOfOrNull { calorieText.find(it) }
                ?.groupValues?.get(1)?.toIntOrNull()?.takeIf { it > 0 },
        )
    }

    private fun toDish(line: String): Dish? {
        val name = line.replace(vegetarianMark, " ").replace(whitespace, " ").trim()
        if (name.isEmpty()) return null
        return Dish(name = name, vegetarian = vegetarianMark.containsMatchIn(line))
    }

    /** Hücre metnini `<br>` ve `<hr>` yerlerinden satırlara böler. */
    private fun Element.lines(): List<String> {
        val text = StringBuilder()
        traverse(object : NodeVisitor {
            override fun head(node: Node, depth: Int) {
                when {
                    node is TextNode -> text.append(node.wholeText.replace(whitespace, " "))
                    node is Element && node.tagName() in lineBreaks -> text.append('\n')
                }
            }
        })
        return text.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }

    private val lineBreaks = setOf("br", "hr")

    private fun fail(message: String): Nothing = throw MenuPageException(message)
}
