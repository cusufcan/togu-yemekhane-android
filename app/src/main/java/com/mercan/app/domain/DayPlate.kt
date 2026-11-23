package com.mercan.app.domain

/**
 * Bir günün ekrandaki dizilişi. [main] büyük kartta, [others] satırlarda (önce çorba),
 * [vegetarian] kendi kartında durur.
 */
data class DayPlate(
    val main: Dish?,
    val others: List<Dish>,
    val vegetarian: List<Dish>,
) {
    /** Haftalık satırın başlığı: ana yemek, o yoksa listedeki ilk yemek. */
    val title: String? get() = (main ?: others.firstOrNull() ?: vegetarian.firstOrNull())?.name

    /** Başlığın altındaki satır: başlıkta olmayan, vejetaryen olmayan yemekler. */
    val rest: List<Dish> get() = if (main != null) others else others.drop(1)
}

/** Ana yemek: çorba ve vejetaryen seçenek olmayan ilk yemek. Kaynakta bu bilgi yoktur. */
fun DayMenu.plate(): DayPlate {
    val (vegetarian, regular) = dishes.partition { it.vegetarian }
    val main = regular.firstOrNull { !it.isSoup() }
    val (soups, others) = regular.filter { it !== main }.partition { it.isSoup() }
    return DayPlate(main, soups + others, vegetarian)
}

private fun Dish.isSoup(): Boolean = name.folded().trim().let { it.endsWith("corba") || it.endsWith("corbasi") }
