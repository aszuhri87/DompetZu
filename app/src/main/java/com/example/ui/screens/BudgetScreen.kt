package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.MonthlyReportData
import com.example.data.model.RecurringBillEntity
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
    onAddBudget: () -> Unit,
    onEditBudget: (CategoryBudgetEntity) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onAddBill: () -> Unit,
    onEditBill: (RecurringBillEntity) -> Unit,
    onDeleteBill: (Long) -> Unit,
    onPayBill: (RecurringBillEntity) -> Unit,
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

                    // Segmented Tabs for Category Budgets vs Recurring Bills (Permanently Fixed)
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Batas Anggaran (${budgets.size})",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tagihan Rutin (${bills.size})",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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
                } else {
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
                }
            }
        }

        // Floating Action Button pinned to BottomEnd
        FloatingActionButton(
            onClick = {
                if (selectedTab == 0) onAddBudget() else onAddBill()
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
                contentDescription = if (selectedTab == 0) "Tambah Anggaran" else "Tambah Tagihan"
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
    // Check if paid this month
    val isPaidThisMonth = if (bill.lastPaidDateMillis != null) {
        val paidCal = Calendar.getInstance().apply { timeInMillis = bill.lastPaidDateMillis }
        paidCal.get(Calendar.MONTH) + 1 == currentMonth && paidCal.get(Calendar.YEAR) == currentYear
    } else false

    val daysDiff = bill.dueDay - todayDay

    val (statusLabel, statusColor, statusBg) = when {
        isPaidThisMonth -> Triple("Sudah Dibayar Bulan Ini", IncomeGreen, IncomeGreen.copy(alpha = 0.12f))
        daysDiff < 0 -> Triple("Terlambat (tgl ${bill.dueDay})", ExpenseRed, ExpenseRed.copy(alpha = 0.12f))
        daysDiff == 0 -> Triple("Jatuh Tempo HARI INI!", ExpenseRed, ExpenseRed.copy(alpha = 0.15f))
        daysDiff <= bill.reminderDaysBefore -> Triple("H-$daysDiff (tgl ${bill.dueDay})", AmberWarning, AmberWarning.copy(alpha = 0.15f))
        else -> Triple("Tgl ${bill.dueDay} setiap bulan", MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceVariant)
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Dompet: ${bill.walletName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  ${bill.frequency}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ubah Tagihan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
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
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
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
