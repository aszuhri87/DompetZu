package com.example.data.model

data class FinancialHealthScore(
    val score: Int, // 0 to 100
    val level: String, // "Sangat Sehat", "Sehat", "Waspada", "Kritis"
    val colorHex: Long,
    val summary: String,
    val adviceList: List<String>
)

data class CategorySpendSummary(
    val categoryName: String,
    val totalAmount: Double,
    val percentageOfTotal: Double, // 0.0 - 100.0
    val count: Int,
    val budgetAmount: Double = 0.0,
    val budgetUsedPercent: Double = 0.0,
    val isOverBudget: Boolean = false,
    val colorHex: Long = 0xFF64748B,
    val iconKey: String = "category"
)

data class DailySpendPoint(
    val day: Int,
    val dayLabel: String,
    val income: Double,
    val expense: Double
)

data class MonthlyComparison(
    val previousMonthExpense: Double,
    val expenseDiffPercent: Double, // positive means expense increased
    val previousMonthIncome: Double,
    val incomeDiffPercent: Double,
    val expenseTrend: TrendDirection,
    val incomeTrend: TrendDirection
)

enum class TrendDirection {
    UP, DOWN, STABLE
}

data class BudgetHealthSummary(
    val totalBudgeted: Double,
    val totalSpentOnBudgeted: Double,
    val overBudgetCategoriesCount: Int,
    val safeCategoriesCount: Int
)

data class MonthlyReportData(
    val month: Int,
    val year: Int,
    val monthTitle: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val netCashFlow: Double, // Income - Expense
    val savingsRate: Double, // (Net / Income) * 100
    val financialHealth: FinancialHealthScore,
    val categorySummaries: List<CategorySpendSummary>,
    val dailyTrend: List<DailySpendPoint>,
    val comparison: MonthlyComparison,
    val budgetSummary: BudgetHealthSummary,
    val topExpenseCategory: CategorySpendSummary?,
    val totalTransactions: Int,
    val exportableReportText: String
)
