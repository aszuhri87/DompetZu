package com.example.data.util

import java.text.NumberFormat
import java.util.Locale

data class GoldSavingsData(
    val isGold: Boolean = false,
    val unit: String = "gram", // "gram" or "kg"
    val targetWeight: Double = 0.0,
    val currentWeight: Double = 0.0,
    val pricePerGram: Double = DEFAULT_GOLD_PRICE_PER_GRAM, // Harga beli/acuan saat dicatat
    val liveMarketPrice: Double = pricePerGram, // Harga pasar dinamis terkini di Indonesia
    val userNote: String = ""
) {
    val targetWeightInGram: Double
        get() = if (unit.equals("kg", ignoreCase = true)) targetWeight * 1000.0 else targetWeight

    val currentWeightInGram: Double
        get() = if (unit.equals("kg", ignoreCase = true)) currentWeight * 1000.0 else currentWeight

    // Nilai target berdasarkan harga acuan awal
    val targetAmountRupiah: Double
        get() = targetWeightInGram * pricePerGram

    // Nilai saat ini berdasarkan harga acuan awal
    val currentAmountRupiah: Double
        get() = currentWeightInGram * pricePerGram

    // Nilai dinamis mengikuti harga pasar emas terkini di Indonesia
    val liveTargetValueRupiah: Double
        get() = targetWeightInGram * liveMarketPrice

    val liveCurrentValueRupiah: Double
        get() = currentWeightInGram * liveMarketPrice

    val liveRemainingValueRupiah: Double
        get() = remainingWeightInGram * liveMarketPrice

    val remainingAmountRupiah: Double
        get() = liveRemainingValueRupiah

    val remainingWeight: Double
        get() = kotlin.math.max(0.0, targetWeight - currentWeight)

    val remainingWeightInGram: Double
        get() = kotlin.math.max(0.0, targetWeightInGram - currentWeightInGram)

    val progressPercent: Int
        get() = if (targetWeightInGram > 0) ((currentWeightInGram / targetWeightInGram) * 100).toInt() else 0

    // Selisih keuntungan / apresiasi nilai emas berdasarkan pergerakan harga pasar (naik atau turun)
    val profitLossRupiah: Double
        get() = (currentWeightInGram * liveMarketPrice) - (currentWeightInGram * pricePerGram)

    val profitLossPercent: Double
        get() = if (pricePerGram > 0) ((liveMarketPrice - pricePerGram) / pricePerGram) * 100.0 else 0.0

    val isProfit: Boolean
        get() = profitLossRupiah >= 0

    fun withLiveMarketPrice(marketPrice: Double): GoldSavingsData {
        return this.copy(liveMarketPrice = if (marketPrice > 0) marketPrice else this.pricePerGram)
    }

    companion object {
        const val DEFAULT_GOLD_PRICE_PER_GRAM = 2_580_000.0 // Acuan harga Logam Mulia Antam Indonesia 2026

        fun parse(
            category: String,
            note: String,
            targetAmount: Double,
            currentAmount: Double,
            liveMarketPrice: Double? = null
        ): GoldSavingsData {
            val isGoldCategory = category.equals("Emas", ignoreCase = true) ||
                    category.equals("Tabungan Emas", ignoreCase = true) ||
                    note.contains("[GOLD:")

            if (!isGoldCategory) {
                return GoldSavingsData(isGold = false, userNote = note)
            }

            val marketRate = if (liveMarketPrice != null && liveMarketPrice > 0) liveMarketPrice else DEFAULT_GOLD_PRICE_PER_GRAM

            val regex = """\[GOLD:([a-zA-Z]+):([0-9.]+):([0-9.]+):([0-9.]+)\]""".toRegex()
            val match = regex.find(note)
            if (match != null) {
                val unit = match.groupValues[1]
                val targetWeight = match.groupValues[2].toDoubleOrNull() ?: 0.0
                val currentWeight = match.groupValues[3].toDoubleOrNull() ?: 0.0
                val price = match.groupValues[4].toDoubleOrNull() ?: marketRate
                val cleanNote = note.replace(match.value, "").trim()
                return GoldSavingsData(
                    isGold = true,
                    unit = if (unit.equals("kg", ignoreCase = true)) "kg" else "gram",
                    targetWeight = targetWeight,
                    currentWeight = currentWeight,
                    pricePerGram = price,
                    liveMarketPrice = marketRate,
                    userNote = cleanNote
                )
            }

            // Fallback if marked as Emas but no metadata tag yet
            val price = if (targetAmount > 0 && currentAmount > 0) marketRate else DEFAULT_GOLD_PRICE_PER_GRAM
            val targetGrams = if (price > 0) targetAmount / price else 0.0
            val currentGrams = if (price > 0) currentAmount / price else 0.0
            return GoldSavingsData(
                isGold = true,
                unit = "gram",
                targetWeight = targetGrams,
                currentWeight = currentGrams,
                pricePerGram = price,
                liveMarketPrice = marketRate,
                userNote = note
            )
        }

        fun buildNote(data: GoldSavingsData): String {
            if (!data.isGold) return data.userNote.trim()
            val tag = "[GOLD:${data.unit}:${data.targetWeight}:${data.currentWeight}:${data.pricePerGram}]"
            return if (data.userNote.isBlank()) tag else "$tag ${data.userNote.trim()}"
        }

        fun formatWeight(weight: Double, unit: String): String {
            val formatted = if (weight % 1.0 == 0.0) {
                weight.toLong().toString()
            } else {
                String.format(Locale.US, "%.3f", weight).trimEnd('0').trimEnd('.')
            }
            return "$formatted $unit"
        }

        fun formatRupiah(amount: Double): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            return formatter.format(amount)
        }
    }
}
