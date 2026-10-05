package com.example.data.util

import com.example.data.model.BudgetHealthSummary
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.CategorySpendSummary
import com.example.data.model.DailySpendPoint
import com.example.data.model.DefaultCategories
import com.example.data.model.FinancialHealthScore
import com.example.data.model.MonthlyComparison
import com.example.data.model.MonthlyReportData
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.TrendDirection
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

object MonthlyReportCalculator {

    private val indonesianMonthNames = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    fun getMonthName(month: Int): String {
        return if (month in 1..12) indonesianMonthNames[month - 1] else "Bulan $month"
    }

    fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }

    fun formatNominal(amount: Double, isHidden: Boolean): String {
        return if (isHidden) "Rp ••••••" else formatRupiah(amount)
    }

    fun calculateReport(
        month: Int,
        year: Int,
        currentTransactions: List<TransactionEntity>,
        budgets: List<CategoryBudgetEntity>,
        previousTransactions: List<TransactionEntity>,
        totalNetWorth: Double = 0.0
    ): MonthlyReportData {
        val monthTitle = "${getMonthName(month)} $year"

        val incomeTransactions = currentTransactions.filter { it.type == TransactionType.INCOME.name }
        val expenseTransactions = currentTransactions.filter { it.type == TransactionType.EXPENSE.name }

        val totalIncome = incomeTransactions.sumOf { it.amount }
        val totalExpense = expenseTransactions.sumOf { it.amount }
        val netCashFlow = totalIncome - totalExpense

        val savingsRate = if (totalIncome > 0) {
            ((netCashFlow / totalIncome) * 100.0).coerceIn(-100.0, 100.0)
        } else if (totalNetWorth > 0) {
            if (totalExpense > 0) {
                ((totalNetWorth / (totalNetWorth + totalExpense)) * 100.0).coerceIn(0.0, 100.0)
            } else {
                100.0
            }
        } else if (totalExpense > 0) {
            -100.0
        } else {
            0.0
        }

        // Category Summaries
        val budgetsMap = budgets.associateBy { it.category }
        val expensesByCategory = expenseTransactions.groupBy { it.category }

        val categorySummaries = expensesByCategory.map { (catName, txList) ->
            val catTotal = txList.sumOf { it.amount }
            val percent = if (totalExpense > 0) (catTotal / totalExpense) * 100.0 else 0.0
            val budget = budgetsMap[catName]?.budgetAmount ?: 0.0
            val budgetPercent = if (budget > 0) (catTotal / budget) * 100.0 else 0.0
            val isOver = budget > 0 && catTotal > budget
            val catMeta = DefaultCategories.findCategory(catName)

            CategorySpendSummary(
                categoryName = catName,
                totalAmount = catTotal,
                percentageOfTotal = percent,
                count = txList.size,
                budgetAmount = budget,
                budgetUsedPercent = budgetPercent,
                isOverBudget = isOver,
                colorHex = catMeta.colorHex,
                iconKey = catMeta.iconKey
            )
        }.sortedByDescending { it.totalAmount }

        val topExpenseCategory = categorySummaries.firstOrNull()

        // Daily Trend
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val dailyTrend = (1..maxDays).map { day ->
            val dayCal = Calendar.getInstance()
            val dayTx = currentTransactions.filter { tx ->
                dayCal.timeInMillis = tx.dateMillis
                dayCal.get(Calendar.DAY_OF_MONTH) == day
            }
            val dayIncome = dayTx.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
            val dayExpense = dayTx.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }

            DailySpendPoint(
                day = day,
                dayLabel = "$day ${getMonthName(month).take(3)}",
                income = dayIncome,
                expense = dayExpense
            )
        }

        // Previous Month Comparison
        val prevExpense = previousTransactions.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        val prevIncome = previousTransactions.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }

        val expenseDiffPercent = if (prevExpense > 0) {
            ((totalExpense - prevExpense) / prevExpense) * 100.0
        } else 0.0

        val incomeDiffPercent = if (prevIncome > 0) {
            ((totalIncome - prevIncome) / prevIncome) * 100.0
        } else 0.0

        val expenseTrend = when {
            expenseDiffPercent > 3.0 -> TrendDirection.UP
            expenseDiffPercent < -3.0 -> TrendDirection.DOWN
            else -> TrendDirection.STABLE
        }

        val incomeTrend = when {
            incomeDiffPercent > 3.0 -> TrendDirection.UP
            incomeDiffPercent < -3.0 -> TrendDirection.DOWN
            else -> TrendDirection.STABLE
        }

        val comparison = MonthlyComparison(
            previousMonthExpense = prevExpense,
            expenseDiffPercent = expenseDiffPercent,
            previousMonthIncome = prevIncome,
            incomeDiffPercent = incomeDiffPercent,
            expenseTrend = expenseTrend,
            incomeTrend = incomeTrend
        )

        // Budget Health
        val totalBudgeted = budgets.sumOf { it.budgetAmount }
        val spentOnBudgeted = budgets.sumOf { b ->
            expensesByCategory[b.category]?.sumOf { it.amount } ?: 0.0
        }

        val overBudgetCategoriesCount = categorySummaries.count { it.isOverBudget }
        val safeCategoriesCount = budgets.size - overBudgetCategoriesCount

        val budgetSummary = BudgetHealthSummary(
            totalBudgeted = totalBudgeted,
            totalSpentOnBudgeted = spentOnBudgeted,
            overBudgetCategoriesCount = overBudgetCategoriesCount,
            safeCategoriesCount = safeCategoriesCount.coerceAtLeast(0)
        )

        // Financial Health Scoring (factors in 1-month income AND totalNetWorth)
        val financialHealth = calculateHealthScore(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            totalNetWorth = totalNetWorth,
            savingsRate = savingsRate,
            categorySummaries = categorySummaries,
            budgetSummary = budgetSummary,
            expenseDiffPercent = expenseDiffPercent
        )

        // Generate Exportable Text Report
        val exportText = generateReportText(
            monthTitle = monthTitle,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netCashFlow = netCashFlow,
            savingsRate = savingsRate,
            health = financialHealth,
            categorySummaries = categorySummaries,
            budgetSummary = budgetSummary,
            comparison = comparison
        )

        return MonthlyReportData(
            month = month,
            year = year,
            monthTitle = monthTitle,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netCashFlow = netCashFlow,
            savingsRate = savingsRate,
            financialHealth = financialHealth,
            categorySummaries = categorySummaries,
            dailyTrend = dailyTrend,
            comparison = comparison,
            budgetSummary = budgetSummary,
            topExpenseCategory = topExpenseCategory,
            totalTransactions = currentTransactions.size,
            exportableReportText = exportText
        )
    }

    private fun calculateHealthScore(
        totalIncome: Double,
        totalExpense: Double,
        totalNetWorth: Double,
        savingsRate: Double,
        categorySummaries: List<CategorySpendSummary>,
        budgetSummary: BudgetHealthSummary,
        expenseDiffPercent: Double
    ): FinancialHealthScore {
        var score = 0
        val adviceList = mutableListOf<String>()

        // 1. Savings Rate & Income Assessment for 1-Month Period (0 - 40 points)
        if (totalIncome == 0.0 && totalNetWorth > 0.0) {
            score += 30
            adviceList.add("Pengeluaran bulan ini ditopang dari total saldo dompet yang tersedia (${formatRupiah(totalNetWorth)}). Cadangan dana aman.")
        } else {
            when {
                savingsRate >= 30.0 -> {
                    score += 40
                    adviceList.add("Tingkat tabungan bulanan sangat prima (${String.format(Locale.US, "%.1f", savingsRate)}% dari total pemasukan 1 bulan).")
                }
                savingsRate >= 20.0 -> {
                    score += 32
                    adviceList.add("Rasio tabungan bulanan sehat (${String.format(Locale.US, "%.1f", savingsRate)}%). Pertahankan disiplin pengeluaran.")
                }
                savingsRate >= 10.0 -> {
                    score += 20
                    adviceList.add("Ada surplus tabungan bulanan (${String.format(Locale.US, "%.1f", savingsRate)}%), namun ada ruang hemat di pos non-esensial.")
                }
                savingsRate >= 0.0 -> {
                    score += 10
                    adviceList.add("Pemasukan dan pengeluaran 1 bulan hampir seimbang. Waspadai pengeluaran tidak terduga.")
                }
                else -> {
                    score += 0
                    adviceList.add("Arus kas bulanan defisit! Pengeluaran 1 bulan melampaui total pemasukan bulanan.")
                }
            }
        }

        // 2. Budget adherence (0 - 25 points)
        if (budgetSummary.totalBudgeted > 0) {
            when (budgetSummary.overBudgetCategoriesCount) {
                0 -> {
                    score += 25
                    adviceList.add("Semua pos anggaran kategori terkontrol dengan rapi dalam batas rencana.")
                }
                1 -> {
                    score += 15
                    val overCat = categorySummaries.firstOrNull { it.isOverBudget }
                    adviceList.add("Perhatian: Kategori '${overCat?.categoryName ?: ""}' melewati batas anggaran bulanan.")
                }
                else -> {
                    score += 5
                    adviceList.add("Ada ${budgetSummary.overBudgetCategoriesCount} kategori yang melebihi batas anggaran bulanan.")
                }
            }
        } else {
            score += 25
            adviceList.add("Tips: Tetapkan batas anggaran kategori bulanan untuk kontrol finansial yang lebih presisi.")
        }

        // 3. Expense control & trend (0 - 25 points)
        when {
            totalIncome > 0 && totalExpense <= totalIncome * 0.5 -> score += 25
            totalIncome > 0 && totalExpense <= totalIncome * 0.7 -> score += 18
            totalIncome > 0 && totalExpense <= totalIncome * 0.9 -> score += 10
            else -> score += 5
        }

        // 4. Net Worth & Capital Cushion Assessment (Total Saldo Semua Dompet) (0 - 10 bonus points)
        if (totalNetWorth > 0) {
            val liquidityMonths = if (totalExpense > 0) totalNetWorth / totalExpense else 12.0
            when {
                liquidityMonths >= 6.0 -> {
                    score += 10
                    adviceList.add("Ketahanan saldo sangat kuat: Total saldo dompet saat ini (${formatRupiah(totalNetWorth)}) menopang ${String.format(Locale.US, "%.1f", liquidityMonths)} bulan pengeluaran.")
                }
                liquidityMonths >= 3.0 -> {
                    score += 6
                    adviceList.add("Total saldo dompet (${formatRupiah(totalNetWorth)}) mencukupi dana darurat ${String.format(Locale.US, "%.1f", liquidityMonths)} bulan pengeluaran.")
                }
                else -> {
                    score += 3
                    adviceList.add("Total saldo dompet saat ini (${formatRupiah(totalNetWorth)}) mencukupi ${String.format(Locale.US, "%.1f", liquidityMonths)} bulan pengeluaran.")
                }
            }
        }

        if (expenseDiffPercent < -5.0) {
            adviceList.add("Bagus! Pengeluaran bulan ini turun ${String.format(Locale.US, "%.1f", -expenseDiffPercent)}% dibanding bulan sebelumnya.")
        } else if (expenseDiffPercent > 15.0) {
            adviceList.add("Waspada: Pengeluaran bulan ini meningkat ${String.format(Locale.US, "%.1f", expenseDiffPercent)}% dibanding bulan lalu.")
        }

        val finalScore = score.coerceIn(10, 100)
        val (level, colorHex, summary) = when {
            finalScore >= 80 -> Triple("Sangat Sehat", 0xFF10B981, "Kondisi finansial 1 bulan ini sangat optimal dan surplus kuat.")
            finalScore >= 65 -> Triple("Sehat", 0xFF0284C7, "Arus kas 1 bulan stabil dan total saldo dompet terjaga dengan baik.")
            finalScore >= 45 -> Triple("Perlu Perhatian", 0xFFF59E0B, "Pengeluaran 1 bulan tinggi atau cadangan saldo perlu ditingkatkan.")
            else -> Triple("Kritis", 0xFFEF4444, "Pengeluaran bulanan melampaui pemasukan dan cadangan saldo minim.")
        }

        return FinancialHealthScore(
            score = finalScore,
            level = level,
            colorHex = colorHex,
            summary = summary,
            adviceList = adviceList
        )
    }

    private fun generateReportText(
        monthTitle: String,
        totalIncome: Double,
        totalExpense: Double,
        netCashFlow: Double,
        savingsRate: Double,
        health: FinancialHealthScore,
        categorySummaries: List<CategorySpendSummary>,
        budgetSummary: BudgetHealthSummary,
        comparison: MonthlyComparison
    ): String {
        val sb = StringBuilder()
        sb.appendLine("📊 LAPORAN KEUANGAN BULANAN - DOMPETZU")
        sb.appendLine("Periode: $monthTitle")
        sb.appendLine("=========================================")
        sb.appendLine("💰 RINGKASAN ARUS KAS")
        sb.appendLine("• Total Pemasukan  : ${formatRupiah(totalIncome)}")
        sb.appendLine("• Total Pengeluaran: ${formatRupiah(totalExpense)}")
        val status = if (netCashFlow >= 0) "Surplus" else "Defisit"
        sb.appendLine("• Arus Kas Bersih  : ${formatRupiah(netCashFlow)} ($status)")
        sb.appendLine("• Tingkat Tabungan : ${String.format(Locale.US, "%.1f", savingsRate)}%")
        sb.appendLine()
        sb.appendLine("🏆 SKOR KESEHATAN KEUANGAN")
        sb.appendLine("• Skor : ${health.score}/100 (${health.level})")
        sb.appendLine("• Ulasan: ${health.summary}")
        sb.appendLine()
        if (categorySummaries.isNotEmpty()) {
            sb.appendLine("📂 PENGELUARAN PER KATEGORI")
            categorySummaries.take(5).forEachIndexed { idx, cat ->
                val overBadge = if (cat.isOverBudget) " [OVER BUDGET!]" else ""
                sb.appendLine("${idx + 1}. ${cat.categoryName}: ${formatRupiah(cat.totalAmount)} (${String.format(Locale.US, "%.1f", cat.percentageOfTotal)}%)$overBadge")
            }
            sb.appendLine()
        }
        if (budgetSummary.totalBudgeted > 0) {
            sb.appendLine("🎯 ANGGARAN & REALISASI")
            sb.appendLine("• Total Plafon: ${formatRupiah(budgetSummary.totalBudgeted)}")
            sb.appendLine("• Realisasi   : ${formatRupiah(budgetSummary.totalSpentOnBudgeted)}")
            sb.appendLine("• Kategori Aman: ${budgetSummary.safeCategoriesCount} | Lewat Batas: ${budgetSummary.overBudgetCategoriesCount}")
            sb.appendLine()
        }
        if (health.adviceList.isNotEmpty()) {
            sb.appendLine("💡 SARAN FINANSIAL OTOMATIS")
            health.adviceList.forEach { advice ->
                sb.appendLine("• $advice")
            }
            sb.appendLine()
        }
        sb.appendLine("=========================================")
        sb.appendLine("Dihasilkan otomatis oleh DompetZu App")
        return sb.toString()
    }
}
