package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.MonthlyReportData
import com.example.data.model.RecurringBillEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.util.GoldSavingsData
import androidx.compose.material.icons.filled.Savings
import com.example.data.util.MonthlyReportCalculator
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.CustomProgressBar
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.util.Calendar

@Composable
fun BudgetScreen(
    report: MonthlyReportData,
    budgets: List<CategoryBudgetEntity>,
    bills: List<RecurringBillEntity>,
    savingsGoals: List<SavingsGoalEntity> = emptyList(),
    liveGoldPrice: Double = GoldSavingsData.DEFAULT_GOLD_PRICE_PER_GRAM,
    liveGoldPriceChange: Double = 0.0,
    liveGoldPriceChangePercent: Double = 0.0,
    liveGoldLastUpdated: String = "",
    liveGoldSourceName: String = "Logam Mulia (Antam)",
    isRefreshingGoldPrice: Boolean = false,
    onRefreshGoldPrice: () -> Unit = {},
    onSetCustomGoldPrice: (Double) -> Unit = {},
    onAddBudget: () -> Unit,
    onEditBudget: (CategoryBudgetEntity) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onAddBill: () -> Unit,
    onEditBill: (RecurringBillEntity) -> Unit,
    onDeleteBill: (Long) -> Unit,
    onPayBill: (RecurringBillEntity) -> Unit,
    onAddSavingsGoal: () -> Unit = {},
    onEditSavingsGoal: (SavingsGoalEntity) -> Unit = {},
    onDeleteSavingsGoal: (Long) -> Unit = {},
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Anggaran Kategori, 1: Tagihan Rutin

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Pinned Top Header: Month Selector & Segmented Tabs (Fixed at the top)
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Month Selector (Permanently Fixed)
                    MonthSelectorHeader(
                        month = report.month,
                        year = report.year,
                        onPrevious = onPreviousMonth,
                        onNext = onNextMonth
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Segmented Tabs for Category Budgets vs Recurring Bills vs Savings Goals (Permanently Fixed)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedTab = 0 }
                                    .testTag("tab_category_budgets")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PieChart,
                                        contentDescription = null,
                                        tint = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Anggaran",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedTab = 1 }
                                    .testTag("tab_recurring_bills")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tagihan",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedTab == 2) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedTab = 2 }
                                    .testTag("tab_savings_goals")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Savings,
                                        contentDescription = null,
                                        tint = if (selectedTab == 2) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tabungan",
                                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = if (selectedTab == 2) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Scrollable Content Area below the pinned header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (selectedTab == 0) {
                    CategoryBudgetsList(
                        report = report,
                        budgets = budgets,
                        onAddBudget = onAddBudget,
                        onEditBudget = onEditBudget,
                        onDeleteBudget = onDeleteBudget,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (selectedTab == 1) {
                    RecurringBillsList(
                        bills = bills,
                        currentMonth = report.month,
                        currentYear = report.year,
                        onAddBill = onAddBill,
                        onEditBill = onEditBill,
                        onDeleteBill = onDeleteBill,
                        onPayBill = onPayBill,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    SavingsGoalsList(
                        goals = savingsGoals,
                        liveGoldPrice = liveGoldPrice,
                        liveGoldPriceChange = liveGoldPriceChange,
                        liveGoldPriceChangePercent = liveGoldPriceChangePercent,
                        liveGoldLastUpdated = liveGoldLastUpdated,
                        liveGoldSourceName = liveGoldSourceName,
                        isRefreshingGoldPrice = isRefreshingGoldPrice,
                        onRefreshGoldPrice = onRefreshGoldPrice,
                        onSetCustomGoldPrice = onSetCustomGoldPrice,
                        onAddGoal = onAddSavingsGoal,
                        onEditGoal = onEditSavingsGoal,
                        onDeleteGoal = onDeleteSavingsGoal,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Floating Action Button pinned to BottomEnd
        FloatingActionButton(
            onClick = {
                if (selectedTab == 0) onAddBudget() else if (selectedTab == 1) onAddBill() else onAddSavingsGoal()
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_budget_or_bill")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (selectedTab == 0) "Tambah Anggaran" else if (selectedTab == 1) "Tambah Tagihan" else "Tambah Target Tabungan"
            )
        }
    }
}

@Composable
fun CategoryBudgetsList(
    report: MonthlyReportData,
    budgets: List<CategoryBudgetEntity>,
    onAddBudget: () -> Unit,
    onEditBudget: (CategoryBudgetEntity) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalBudget = budgets.sumOf { it.budgetAmount }
    val totalSpentOnBudgetedCategories = budgets.sumOf { budget ->
        report.categorySummaries.find { it.categoryName.equals(budget.category, ignoreCase = true) }?.totalAmount ?: 0.0
    }

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            val remainingBudget = totalBudget - totalSpentOnBudgetedCategories
            val percentUsed = if (totalBudget > 0) ((totalSpentOnBudgetedCategories / totalBudget) * 100).toInt() else 0

            // Total Budget Summary Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ringkasan Anggaran",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${budgets.size} Kategori Alokasi",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                percentUsed > 100 -> ExpenseRed.copy(alpha = 0.15f)
                                percentUsed >= 80 -> AmberWarning.copy(alpha = 0.15f)
                                else -> IncomeGreen.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = when {
                                    percentUsed > 100 -> "Over ${percentUsed}%"
                                    percentUsed >= 80 -> "Waspada ${percentUsed}%"
                                    else -> "Terkendali ${percentUsed}%"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    percentUsed > 100 -> ExpenseRed
                                    percentUsed >= 80 -> AmberWarning
                                    else -> IncomeGreen
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Terpakai",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = MonthlyReportCalculator.formatRupiah(totalSpentOnBudgetedCategories),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (percentUsed > 100) ExpenseRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (remainingBudget >= 0) "Sisa Kuota" else "Kelebihan",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = MonthlyReportCalculator.formatRupiah(kotlin.math.abs(remainingBudget)),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (remainingBudget >= 0) IncomeGreen else ExpenseRed
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Plafon Total",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = MonthlyReportCalculator.formatRupiah(totalBudget),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val overallProgress = if (totalBudget > 0) {
                        (totalSpentOnBudgetedCategories / totalBudget).toFloat().coerceIn(0f, 1f)
                    } else 0f

                    CustomProgressBar(
                        progress = overallProgress,
                        barColor = when {
                            percentUsed > 100 -> ExpenseRed
                            percentUsed >= 80 -> AmberWarning
                            else -> IncomeGreen
                        },
                        height = 8.dp
                    )
                }
            }
        }

        if (budgets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Anggaran Kategori",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tetapkan plafon pengeluaran per kategori untuk mengontrol pengeluaran.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddBudget,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("empty_add_budget_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tetapkan Anggaran Sekarang")
                        }
                    }
                }
            }
        } else {
            items(budgets, key = { it.id }) { budget ->
                val spent = report.categorySummaries.find {
                    it.categoryName.equals(budget.category, ignoreCase = true)
                }?.totalAmount ?: 0.0

                CategoryBudgetItemCard(
                    budget = budget,
                    spent = spent,
                    onEdit = { onEditBudget(budget) },
                    onDelete = { onDeleteBudget(budget.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun CategoryBudgetItemCard(
    budget: CategoryBudgetEntity,
    spent: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (budget.budgetAmount > 0) {
        (spent / budget.budgetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    val percentUsed = if (budget.budgetAmount > 0) {
        ((spent / budget.budgetAmount) * 100).toInt()
    } else 0

    val isExceeded = spent >= budget.budgetAmount
    val isNearThreshold = spent >= (budget.budgetAmount * (budget.thresholdPercent / 100.0))

    val progressColor = when {
        isExceeded -> ExpenseRed
        isNearThreshold -> AmberWarning
        else -> IncomeGreen
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_item_card_${budget.category}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(category = budget.category, size = 38.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = budget.category,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (budget.alertEnabled) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Ambang: ${budget.thresholdPercent}%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "Peringatan Mati",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ubah Anggaran",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Anggaran",
                            tint = ExpenseRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Amounts Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Terpakai: ${MonthlyReportCalculator.formatRupiah(spent)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = progressColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Batas: ${MonthlyReportCalculator.formatRupiah(budget.budgetAmount)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            CustomProgressBar(
                progress = progress,
                barColor = progressColor,
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isExceeded) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Melebihi anggaran sebesar ${MonthlyReportCalculator.formatRupiah(spent - budget.budgetAmount)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                } else if (isNearThreshold) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Mendekati plafon ($percentUsed%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberWarning
                        )
                    }
                } else {
                    Text(
                        text = "Sisa: ${MonthlyReportCalculator.formatRupiah(budget.budgetAmount - spent)}",
                        fontSize = 11.sp,
                        color = IncomeGreen
                    )
                }

                Text(
                    text = "$percentUsed%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = progressColor
                )
            }
        }
    }
}

@Composable
fun RecurringBillsList(
    bills: List<RecurringBillEntity>,
    currentMonth: Int,
    currentYear: Int,
    onAddBill: () -> Unit,
    onEditBill: (RecurringBillEntity) -> Unit,
    onDeleteBill: (Long) -> Unit,
    onPayBill: (RecurringBillEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalBillAmount = bills.filter { it.isActive }.sumOf { it.amount }
    val today = Calendar.getInstance()
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Total Bills Header Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Tagihan Rutin",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${bills.count { it.isActive }} Tagihan Aktif",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = MonthlyReportCalculator.formatRupiah(totalBillAmount),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Pengingat otomatis dikirimkan sebelum tanggal jatuh tempo.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        if (bills.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Tagihan Rutin",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Catat tagihan seperti WiFi, Listrik, Kos, atau cicilan di sini.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddBill,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("empty_add_bill_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Tagihan Sekarang")
                        }
                    }
                }
            }
        } else {
            items(bills, key = { it.id }) { bill ->
                RecurringBillItemCard(
                    bill = bill,
                    todayDay = todayDay,
                    currentMonth = currentMonth,
                    currentYear = currentYear,
                    onEdit = { onEditBill(bill) },
                    onDelete = { onDeleteBill(bill.id) },
                    onPay = { onPayBill(bill) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun RecurringBillItemCard(
    bill: RecurringBillEntity,
    todayDay: Int,
    currentMonth: Int,
    currentYear: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPay: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val subtextColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
    val mutedTextColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF1E293B)

    // Check if paid this month
    val isPaidThisMonth = if (bill.lastPaidDateMillis != null) {
        val paidCal = Calendar.getInstance().apply { timeInMillis = bill.lastPaidDateMillis }
        paidCal.get(Calendar.MONTH) + 1 == currentMonth && paidCal.get(Calendar.YEAR) == currentYear
    } else false

    val daysDiff = bill.dueDay - todayDay

    val statusLabel = when {
        isPaidThisMonth -> "Sudah Dibayar Bulan Ini"
        daysDiff < 0 -> "Terlambat (tgl ${bill.dueDay})"
        daysDiff == 0 -> "Jatuh Tempo HARI INI!"
        daysDiff <= bill.reminderDaysBefore -> "H-$daysDiff (tgl ${bill.dueDay})"
        else -> "Tgl ${bill.dueDay} setiap bulan"
    }

    val statusColor = when {
        isPaidThisMonth -> IncomeGreen
        daysDiff < 0 -> ExpenseRed
        daysDiff == 0 -> ExpenseRed
        daysDiff <= bill.reminderDaysBefore -> AmberWarning
        else -> if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    }

    val statusBg = when {
        isPaidThisMonth -> IncomeGreen.copy(alpha = 0.12f)
        daysDiff < 0 -> ExpenseRed.copy(alpha = 0.12f)
        daysDiff == 0 -> ExpenseRed.copy(alpha = 0.15f)
        daysDiff <= bill.reminderDaysBefore -> AmberWarning.copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val statusBorder = when {
        isPaidThisMonth -> BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.3f))
        daysDiff < 0 -> BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f))
        daysDiff == 0 -> BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.4f))
        daysDiff <= bill.reminderDaysBefore -> BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f))
        else -> BorderStroke(1.2.dp, if (isDark) Color(0xFF64748B) else Color(0xFF334155))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bill_item_card_${bill.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(category = bill.category, size = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = bill.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Dompet: ${bill.walletName}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = subtextColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  ${bill.frequency}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = mutedTextColor
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ubah Tagihan",
                            tint = if (isDark) Color.White else Color(0xFF1E293B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Tagihan",
                            tint = ExpenseRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Amount and Due Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = MonthlyReportCalculator.formatRupiah(bill.amount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg,
                    border = statusBorder
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (bill.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = bill.note,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = subtextColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pay Action
            if (!isPaidThisMonth) {
                Button(
                    onClick = onPay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pay_bill_action_btn_${bill.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bayar & Catat Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = IncomeGreen.copy(alpha = 0.10f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = IncomeGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lunas untuk bulan ini",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IncomeGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoldMarketReferenceCard(
    livePrice: Double,
    priceChange: Double,
    priceChangePercent: Double,
    lastUpdated: String,
    sourceName: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // High contrast theme-aware colors
    val cardBg = if (isDark) Color(0xFF231E18) else Color(0xFFFFFBEB)
    val cardBorder = if (isDark) Color(0xFFD97706).copy(alpha = 0.5f) else Color(0xFFF59E0B).copy(alpha = 0.35f)
    val titleColor = if (isDark) Color(0xFFFBBF24) else Color(0xFF78350F)
    val subtitleColor = if (isDark) Color(0xFFE5E7EB) else Color(0xFF92400E)
    val priceColor = if (isDark) Color(0xFFFDE68A) else Color(0xFF78350F)
    val timestampColor = if (isDark) Color(0xFFD1D5DB) else Color(0xFF92400E)
    val refreshIconTint = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Harga Acuan Emas Pasar",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = titleColor
                        )
                        Text(
                            text = if (sourceName.isNotBlank()) "$sourceName • Indonesia" else "Pasar Logam Mulia Indonesia",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = subtitleColor
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF332A1F) else Color(0xFFFDE68A).copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !isRefreshing, onClick = onRefresh)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = refreshIconTint,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Perbarui Harga Emas",
                                tint = refreshIconTint,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRefreshing) "Memuat..." else "Perbarui",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = refreshIconTint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${GoldSavingsData.formatRupiah(livePrice)} / gram",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = priceColor
                    )
                    if (lastUpdated.isNotBlank()) {
                        Text(
                            text = "Pembaruan: $lastUpdated",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = timestampColor
                        )
                    }
                }

                // Trend Badge (Pergerakan Harga Naik / Turun)
                val isUp = priceChange > 0
                val isDown = priceChange < 0

                val trendBg = when {
                    isUp -> if (isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5)
                    isDown -> if (isDark) Color(0xFF7F1D1D) else Color(0xFFFEE2E2)
                    else -> if (isDark) Color(0xFF78350F) else Color(0xFFFEF3C7)
                }
                val trendText = when {
                    isUp -> if (isDark) Color(0xFF34D399) else Color(0xFF047857)
                    isDown -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
                    else -> if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = trendBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, trendText.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                isUp -> Icons.Default.TrendingUp
                                isDown -> Icons.Default.TrendingDown
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = trendText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                isUp -> "+${GoldSavingsData.formatRupiah(priceChange)} (+${String.format(java.util.Locale.US, "%.1f", priceChangePercent)}%)"
                                isDown -> "-${GoldSavingsData.formatRupiah(kotlin.math.abs(priceChange))} (${String.format(java.util.Locale.US, "%.1f", priceChangePercent)}%)"
                                else -> "Harga Stabil"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = trendText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsGoalsList(
    goals: List<SavingsGoalEntity>,
    liveGoldPrice: Double = GoldSavingsData.DEFAULT_GOLD_PRICE_PER_GRAM,
    liveGoldPriceChange: Double = 0.0,
    liveGoldPriceChangePercent: Double = 0.0,
    liveGoldLastUpdated: String = "",
    liveGoldSourceName: String = "Logam Mulia (Antam)",
    isRefreshingGoldPrice: Boolean = false,
    onRefreshGoldPrice: () -> Unit = {},
    onSetCustomGoldPrice: (Double) -> Unit = {},
    onAddGoal: () -> Unit,
    onEditGoal: (SavingsGoalEntity) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (goals.isEmpty()) {
        LazyColumn(
            modifier = modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                GoldMarketReferenceCard(
                    livePrice = liveGoldPrice,
                    priceChange = liveGoldPriceChange,
                    priceChangePercent = liveGoldPriceChangePercent,
                    lastUpdated = liveGoldLastUpdated,
                    sourceName = liveGoldSourceName,
                    isRefreshing = isRefreshingGoldPrice,
                    onRefresh = onRefreshGoldPrice
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Target Tabungan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mulai menabung untuk emas batangan atau impian Anda lainnya!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddGoal,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Buat Target Baru", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Item 1: Live Market Gold Price Banner / Card
            item {
                Spacer(modifier = Modifier.height(6.dp))
                GoldMarketReferenceCard(
                    livePrice = liveGoldPrice,
                    priceChange = liveGoldPriceChange,
                    priceChangePercent = liveGoldPriceChangePercent,
                    lastUpdated = liveGoldLastUpdated,
                    sourceName = liveGoldSourceName,
                    isRefreshing = isRefreshingGoldPrice,
                    onRefresh = onRefreshGoldPrice
                )
            }

            item {
                // Summary/Header card for total savings goal progress
                val goldItems = goals.map {
                    GoldSavingsData.parse(it.category, it.note, it.targetAmount, it.currentAmount, liveGoldPrice)
                }.filter { it.isGold }

                val nonGoldGoals = goals.filter {
                    !it.category.equals("Emas", ignoreCase = true) &&
                    !it.category.equals("Tabungan Emas", ignoreCase = true) &&
                    !it.note.contains("[GOLD:")
                }

                val totalCashTarget = nonGoldGoals.sumOf { it.targetAmount }
                val totalCashSaved = nonGoldGoals.sumOf { it.currentAmount }

                val totalGoldGrams = goldItems.sumOf { it.currentWeightInGram }
                val totalGoldLiveValue = goldItems.sumOf { it.liveCurrentValueRupiah }
                val totalGoldLiveTarget = goldItems.sumOf { it.liveTargetValueRupiah }
                val totalGoldAppreciation = goldItems.sumOf { it.profitLossRupiah }

                val totalOverallSaved = totalCashSaved + totalGoldLiveValue
                val totalOverallTarget = totalCashTarget + totalGoldLiveTarget
                val overallPercent = if (totalOverallTarget > 0) ((totalOverallSaved / totalOverallTarget) * 100).toInt() else 0
                
                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
                val labelColor = if (isDark) Color(0xFFD1D5DB) else Color(0xFF1E293B)
                val targetAmountColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF0F172A)
                val remainingTextColor = if (isDark) Color(0xFFD1D5DB) else Color(0xFF1E293B)

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E2430) else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF374151) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Section Title Header
                        Text(
                            text = "Kemajuan Tabungan Impian",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = titleColor
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Gold Holdings Banner (Placed cleanly above Total Terkumpul)
                        if (totalGoldGrams > 0) {
                            val goldPillBg = if (isDark) Color(0xFF262118) else Color(0xFFFFFBEB)
                            val goldPillBorder = if (isDark) Color(0xFFD97706).copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.35f)
                            val goldPillText = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = goldPillBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, goldPillBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🪙 ", fontSize = 13.sp)
                                        Text(
                                            text = "Total Emas Terkumpul:",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDark) Color(0xFFE5E7EB) else Color(0xFF78350F)
                                        )
                                    }
                                    Text(
                                        text = "${GoldSavingsData.formatWeight(totalGoldGrams, "gr")} (~${MonthlyReportCalculator.formatRupiah(totalGoldLiveValue)})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = goldPillText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Main Total Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Total Terkumpul",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = labelColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = MonthlyReportCalculator.formatRupiah(totalOverallSaved),
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isDark) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Target",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = labelColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = MonthlyReportCalculator.formatRupiah(totalOverallTarget),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = targetAmountColor
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        CustomProgressBar(
                            progress = (overallPercent / 100f).coerceIn(0f, 1f),
                            barColor = if (isDark) Color(0xFF3B82F6) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tercapai $overallPercent%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary
                            )
                            if (totalGoldAppreciation != 0.0) {
                                val isGain = totalGoldAppreciation > 0
                                val appColor = when {
                                    isGain -> if (isDark) Color(0xFF34D399) else IncomeGreen
                                    else -> if (isDark) Color(0xFFFCA5A5) else ExpenseRed
                                }
                                Text(
                                    text = if (isGain) "+${MonthlyReportCalculator.formatRupiah(totalGoldAppreciation)} Apresiasi Emas" else "-${MonthlyReportCalculator.formatRupiah(kotlin.math.abs(totalGoldAppreciation))} Penurunan",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColor
                                )
                            } else {
                                Text(
                                    text = "Sisa: " + MonthlyReportCalculator.formatRupiah(kotlin.math.max(0.0, totalOverallTarget - totalOverallSaved)),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = remainingTextColor
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            items(goals) { goal ->
                SavingsGoalCard(
                    goal = goal,
                    liveGoldPrice = liveGoldPrice,
                    onEdit = { onEditGoal(goal) },
                    onDelete = { onDeleteGoal(goal.id) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
            }
        }
    }
}

@Composable
fun SavingsGoalCard(
    goal: SavingsGoalEntity,
    liveGoldPrice: Double = GoldSavingsData.DEFAULT_GOLD_PRICE_PER_GRAM,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val goldData = remember(goal, liveGoldPrice) {
        GoldSavingsData.parse(goal.category, goal.note, goal.targetAmount, goal.currentAmount, liveGoldPrice)
    }
    val isGold = goldData.isGold
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val progressPercent = if (isGold) {
        goldData.progressPercent
    } else {
        if (goal.targetAmount > 0) ((goal.currentAmount / goal.targetAmount) * 100).toInt() else 0
    }

    val formattedTarget = MonthlyReportCalculator.formatRupiah(if (isGold) goldData.liveTargetValueRupiah else goal.targetAmount)
    val formattedCurrent = MonthlyReportCalculator.formatRupiah(if (isGold) goldData.liveCurrentValueRupiah else goal.currentAmount)
    val remaining = if (isGold) goldData.liveRemainingValueRupiah else kotlin.math.max(0.0, goal.targetAmount - goal.currentAmount)
    val formattedRemaining = MonthlyReportCalculator.formatRupiah(remaining)
    
    // Get customized tips based on the targetAmount or gold
    val tipsText = if (isGold) {
        "🪙 Emas adalah instrumen lindung nilai (hedging) yang tahan terhadap inflasi jangka panjang. Cicil pembelian emas secara konsisten (DCA) setiap bulan untuk mencapai target ${GoldSavingsData.formatWeight(goldData.targetWeight, goldData.unit)} Anda!"
    } else {
        when {
            goal.targetAmount < 5000000 -> {
                "🎯 Target jangka pendek! Sisihkan Rp 15.000 - Rp 25.000 dari uang jajan harian Anda. Kurangi belanja impulsif agar target ini bisa tercapai dalam hitungan minggu."
            }
            goal.targetAmount in 5000000.0..20000000.0 -> {
                "📈 Alokasikan sekitar 15-20% dari gaji bulanan langsung ke pos ini sesaat setelah gajian. Gunakan rekening pasif tanpa kartu ATM agar tidak mudah ditarik."
            }
            goal.targetAmount in 20000000.0..100000000.0 -> {
                "💍 Target menengah yang membutuhkan komitmen! Manfaatkan fitur autodebet bulanan, dan pertimbangkan menaruh tabungan ini di instrumen likuid & aman seperti Reksa Dana Pasar Uang (RDPU) agar bertumbuh."
            }
            else -> {
                "🏡 Target besar & berharga! Pecah target ini ke rentang tahunan atau bulanan yang realistis. Selain berhemat, coba tingkatkan pemasukan tambahan (side-job) untuk mempercepat pencapaian target ini."
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGold) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Display emoji or icon based on category
                        val emoji = if (isGold) {
                            "🪙"
                        } else {
                            when (goal.category) {
                                "Pernikahan" -> "💍"
                                "Liburan" -> "✈️"
                                "Pendidikan" -> "🎓"
                                "Kendaraan" -> "🚗"
                                "Rumah" -> "🏡"
                                "Gaya Hidup" -> "🛍️"
                                else -> "💰"
                            }
                        }
                        Text(emoji, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = goal.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = if (isGold) "Tabungan Emas (${goldData.unit})" else "Kategori: ${goal.category}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF0F172A)
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = ExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            CustomProgressBar(
                progress = (progressPercent / 100f).coerceIn(0f, 1f),
                barColor = if (progressPercent >= 100) IncomeGreen else if (isGold) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Values
            val goldAccentColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)

            if (isGold) {
                // Gold Specific: Weight Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$progressPercent% Terkumpul",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (progressPercent >= 100) IncomeGreen else goldAccentColor
                    )
                    Text(
                        text = "${GoldSavingsData.formatWeight(goldData.currentWeight, goldData.unit)} dari ${GoldSavingsData.formatWeight(goldData.targetWeight, goldData.unit)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = goldAccentColor
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Gold Specific: Dynamic Currency Valuation based on live market price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nilai Pasar Terkini:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF0F172A)
                    )
                    Text(
                        text = "$formattedCurrent dari $formattedTarget",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFFFDE68A) else Color(0xFF78350F)
                    )
                }

                // Status Apresiasi / Perubahan Nilai Emas (jika ada modal awal atau pergerakan harga)
                if (goldData.currentWeight > 0 && goldData.profitLossRupiah != 0.0) {
                    Spacer(modifier = Modifier.height(3.dp))
                    val isGain = goldData.profitLossRupiah > 0
                    val statusBg = when {
                        isGain -> if (isDark) Color(0xFF064E3B) else IncomeGreen.copy(alpha = 0.12f)
                        else -> if (isDark) Color(0xFF7F1D1D) else ExpenseRed.copy(alpha = 0.12f)
                    }
                    val statusText = when {
                        isGain -> if (isDark) Color(0xFF34D399) else IncomeGreen
                        else -> if (isDark) Color(0xFFFCA5A5) else ExpenseRed
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = if (isGain) {
                                "📈 Untung +${GoldSavingsData.formatRupiah(goldData.profitLossRupiah)} (+${String.format(java.util.Locale.US, "%.1f", goldData.profitLossPercent)}% dari harga awal ${GoldSavingsData.formatRupiah(goldData.pricePerGram)}/gr)"
                            } else {
                                "📉 Penurunan -${GoldSavingsData.formatRupiah(kotlin.math.abs(goldData.profitLossRupiah))} (${String.format(java.util.Locale.US, "%.1f", goldData.profitLossPercent)}%)"
                            },
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$progressPercent% Terkumpul",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (progressPercent >= 100) IncomeGreen else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$formattedCurrent dari $formattedTarget",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A)
                    )
                }
            }

            if (progressPercent < 100) {
                Text(
                    text = if (isGold) {
                        "Sisa tabungan: ${GoldSavingsData.formatWeight(goldData.remainingWeight, goldData.unit)} (~$formattedRemaining pada harga pasar saat ini)"
                    } else {
                        "Sisa tabungan: $formattedRemaining"
                    },
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF0F172A),
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = IncomeGreen.copy(alpha = 0.15f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = if (isGold) "🎉 Selamat! Target Tabungan Emas Tercapai!" else "🎉 Selamat! Target Tabungan Tercapai!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actionable AI Tips Section (Insight)
            val tipsBg = if (isGold) {
                if (isDark) Color(0xFF262118) else Color(0xFFFEF08A) // Kuning Emas ~70% intensitas
            } else {
                if (isDark) Color(0xFF1E293B) else Color(0xFFBAE6FD) // Biru ~70% intensitas
            }
            val tipsBorder = if (isGold) {
                if (isDark) Color(0xFFD97706).copy(alpha = 0.5f) else Color(0xFFEAB308)
            } else {
                if (isDark) Color(0xFF3B82F6).copy(alpha = 0.4f) else Color(0xFF0284C7)
            }
            val tipsTitleColor = if (isGold) {
                if (isDark) Color(0xFFFBBF24) else Color(0xFF78350F)
            } else {
                if (isDark) Color(0xFF60A5FA) else Color(0xFF0369A1)
            }
            val tipsBodyColor = if (isGold) {
                if (isDark) Color(0xFFE5E7EB) else Color(0xFF451A03)
            } else {
                if (isDark) Color(0xFFF1F5F9) else Color(0xFF0C4A6E)
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tipsBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, tipsBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = tipsTitleColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGold) "Tips Tabungan Emas:" else "Tips Sukses Tabungan:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = tipsTitleColor
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tipsText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = tipsBodyColor,
                        lineHeight = 16.5.sp
                    )
                }
            }
        }
    }
}
