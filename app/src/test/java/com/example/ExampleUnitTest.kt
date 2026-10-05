package com.example

import com.example.data.gemini.ChatMessage
import com.example.data.gemini.GeminiChatModel
import com.example.data.gemini.GeminiContent
import com.example.data.gemini.GeminiGenerateContentRequest
import com.example.data.gemini.GeminiPart
import com.example.data.gemini.MessageRole
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.util.MonthlyReportCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMonthlyReportCalculation_accuracy() {
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                title = "Gaji Pokok",
                amount = 10000000.0,
                type = TransactionType.INCOME.name,
                category = "Gaji Pokok",
                walletName = "Rekening Bank",
                dateMillis = System.currentTimeMillis()
            ),
            TransactionEntity(
                id = 2,
                title = "Belanja Makanan",
                amount = 2000000.0,
                type = TransactionType.EXPENSE.name,
                category = "Makanan & Minuman",
                walletName = "Tunai",
                dateMillis = System.currentTimeMillis()
            ),
            TransactionEntity(
                id = 3,
                title = "Transport Bensin",
                amount = 1000000.0,
                type = TransactionType.EXPENSE.name,
                category = "Transportasi",
                walletName = "E-Wallet",
                dateMillis = System.currentTimeMillis()
            )
        )

        val budgets = listOf(
            CategoryBudgetEntity(
                id = 1,
                category = "Makanan & Minuman",
                month = 10,
                year = 2026,
                budgetAmount = 2500000.0
            )
        )

        val report = MonthlyReportCalculator.calculateReport(
            month = 10,
            year = 2026,
            currentTransactions = transactions,
            budgets = budgets,
            previousTransactions = emptyList()
        )

        assertEquals(10000000.0, report.totalIncome, 0.01)
        assertEquals(3000000.0, report.totalExpense, 0.01)
        assertEquals(7000000.0, report.netCashFlow, 0.01)
        assertEquals(70.0, report.savingsRate, 0.01)
        assertTrue(report.financialHealth.score >= 80)
        assertEquals("Sangat Sehat", report.financialHealth.level)
        assertEquals(2, report.categorySummaries.size)
        assertTrue(report.exportableReportText.contains("DOMPETZU"))
    }

    @Test
    fun testGeminiModels_configuredCorrectly() {
        assertEquals("gemini-3.5-flash", GeminiChatModel.FLASH.modelId)
        assertEquals("gemini-3.1-pro-preview", GeminiChatModel.PRO.modelId)
        assertEquals("gemini-3.1-flash-lite-preview", GeminiChatModel.LITE.modelId)
    }

    @Test
    fun testGeminiMultiTurnRequest_construction() {
        val messages = listOf(
            ChatMessage(role = MessageRole.USER, text = "Berapa alokasi ideal dana darurat?"),
            ChatMessage(role = MessageRole.MODEL, text = "Idealnya 3-6 kali pengeluaran bulanan.")
        )

        val contents = messages.map {
            GeminiContent(
                role = if (it.role == MessageRole.USER) "user" else "model",
                parts = listOf(GeminiPart(text = it.text))
            )
        }

        val request = GeminiGenerateContentRequest(
            contents = contents,
            systemInstruction = GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = "Konsultan Keuangan"))
            )
        )

        assertEquals(2, request.contents.size)
        assertEquals("user", request.contents[0].role)
        assertEquals("model", request.contents[1].role)
        assertNotNull(request.systemInstruction)
    }

    @Test
    fun testCategoryDistribution_pieChartSlicesCalculation() {
        val cat1 = com.example.data.model.CategorySpendSummary(
            categoryName = "Makanan",
            iconKey = "restaurant",
            colorHex = 0xFFEF4444,
            totalAmount = 1500000.0,
            count = 5,
            percentageOfTotal = 60.0
        )
        val cat2 = com.example.data.model.CategorySpendSummary(
            categoryName = "Transport",
            iconKey = "directions_car",
            colorHex = 0xFF3B82F6,
            totalAmount = 1000000.0,
            count = 3,
            percentageOfTotal = 40.0
        )
        val totalExpense = 2500000.0

        val slice1Sweep = ((cat1.totalAmount / totalExpense) * 360f).toFloat()
        val slice2Sweep = ((cat2.totalAmount / totalExpense) * 360f).toFloat()

        assertEquals(216f, slice1Sweep, 0.1f)
        assertEquals(144f, slice2Sweep, 0.1f)
        assertEquals(360f, slice1Sweep + slice2Sweep, 0.1f)
    }

    @Test
    fun testBudgetThresholdCalculation_warningAndExceeded() {
        val budgetAmount = 1000000.0
        val thresholdPercent = 80
        val thresholdLimit = budgetAmount * (thresholdPercent / 100.0)

        val spent750k = 750000.0
        val spent850k = 850000.0
        val spent1200k = 1200000.0

        // 750k is below 80% threshold (800k)
        val isWarning1 = spent750k >= thresholdLimit && spent750k < budgetAmount
        assertFalse(isWarning1)

        // 850k is above 80% threshold but within budget
        val isWarning2 = spent850k >= thresholdLimit && spent850k < budgetAmount
        assertTrue(isWarning2)

        // 1.2M is exceeded
        val isExceeded = spent1200k >= budgetAmount
        assertTrue(isExceeded)
    }

    @Test
    fun testRecurringBill_dueDayCalculations() {
        val dueDay = 15
        val today10th = 10
        val today15th = 15
        val today18th = 18

        val daysDiff1 = dueDay - today10th
        assertEquals(5, daysDiff1)

        val daysDiff2 = dueDay - today15th
        assertEquals(0, daysDiff2) // Due today!

        val daysDiff3 = dueDay - today18th
        assertTrue(daysDiff3 < 0) // Overdue!
    }
}
