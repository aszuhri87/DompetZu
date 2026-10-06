package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategorySpendSummary
import com.example.data.model.MonthlyComparison
import com.example.data.model.MonthlyReportData
import com.example.data.model.TransactionEntity
import com.example.data.model.TrendDirection
import com.example.data.util.MonthlyReportCalculator
import com.example.data.util.PdfReportGenerator
import com.example.ui.components.CategoryDistributionPieChart
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.CustomProgressBar
import com.example.ui.components.DailyTrendChart
import com.example.ui.components.HealthScoreGauge
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.util.Locale

@Composable
fun MonthlyReportScreen(
    report: MonthlyReportData,
    transactions: List<TransactionEntity> = emptyList(),
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("monthly_report_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Month Header
            MonthSelectorHeader(
                month = report.month,
                year = report.year,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth
            )
        }

        // Perhitungan Sisa Uang / Menabung Banner
        item {
            MonthlySavingsBanner(report = report)
        }

        // Executive Financial Health Card
        item {
            HealthScoreCard(report = report)
        }

        // Cashflow KPIs Card
        item {
            CashFlowKpiCard(report = report)
        }

        // Month-over-Month Comparison Card
        item {
            ComparisonCard(
                comparison = report.comparison,
                currentMonthTitle = report.monthTitle
            )
        }

        // Category Distribution Pie Chart (Diagram Lingkaran)
        if (report.categorySummaries.isNotEmpty()) {
            item {
                CategoryDistributionPieChart(
                    categories = report.categorySummaries,
                    totalExpense = report.totalExpense
                )
            }
        }

        // Category Spend Breakdown
        item {
            CategorySpendCard(categories = report.categorySummaries)
        }

        // Daily Trend Chart
        item {
            DailyTrendChart(dailyPoints = report.dailyTrend)
        }

        // Budget Realization Overview
        if (report.budgetSummary.totalBudgeted > 0) {
            item {
                BudgetRealizationCard(report = report)
            }
        }

        // Automated AI/Financial Advice Card
        if (report.financialHealth.adviceList.isNotEmpty()) {
            item {
                FinancialAdviceCard(adviceList = report.financialHealth.adviceList)
            }
        }

        // Export & Share PDF Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pdf_export_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ekspor Dokumen Laporan PDF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Simpan atau bagikan dokumen resmi riwayat transaksi bulanan",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Button: Bagikan Dokumen PDF
                    Button(
                        onClick = {
                            try {
                                val pdfFile = PdfReportGenerator.generatePdf(context, report, transactions)
                                PdfReportGenerator.sharePdfReport(
                                    context = context,
                                    pdfFile = pdfFile,
                                    title = "Laporan Keuangan ${report.monthTitle} - DompetZu"
                                )
                                Toast.makeText(context, "Dokumen PDF berhasil dibuat", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal membuat PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("share_pdf_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Bagikan Dokumen PDF", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Row: Buka PDF, Ekspor CSV & Salin Teks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val pdfFile = PdfReportGenerator.generatePdf(context, report, transactions)
                                    PdfReportGenerator.openPdfReport(context, pdfFile)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal membuka PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_pdf_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "PDF", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                com.example.data.util.CsvExportUtil.exportAndShareCsv(
                                    context = context,
                                    transactions = transactions,
                                    titlePrefix = "laporan_${report.month}_${report.year}"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_csv_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "CSV", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, report.exportableReportText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Bagikan Teks Laporan")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_text_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Teks", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HealthScoreCard(
    report: MonthlyReportData,
    modifier: Modifier = Modifier
) {
    val health = report.financialHealth
    val healthColor = Color(health.colorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("health_score_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HealthScoreGauge(
                score = health.score,
                level = health.level,
                colorHex = health.colorHex
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = healthColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = health.level,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = healthColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Kesehatan Finansial",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = health.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CashFlowKpiCard(
    report: MonthlyReportData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cashflow_kpi_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Arus Kas Bulanan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiMetricItem(
                    label = "Pemasukan",
                    value = MonthlyReportCalculator.formatRupiah(report.totalIncome),
                    valueColor = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricItem(
                    label = "Pengeluaran",
                    value = MonthlyReportCalculator.formatRupiah(report.totalExpense),
                    valueColor = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val netColor = if (report.netCashFlow >= 0) IncomeGreen else ExpenseRed
                val netLabel = if (report.netCashFlow >= 0) "Surplus Bersih" else "Defisit Kas"
                KpiMetricItem(
                    label = netLabel,
                    value = MonthlyReportCalculator.formatRupiah(report.netCashFlow),
                    valueColor = netColor,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricItem(
                    label = "Rasio Tabungan",
                    value = "${String.format(Locale.US, "%.1f", report.savingsRate)}%",
                    valueColor = if (report.savingsRate >= 20.0) IncomeGreen else AmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun KpiMetricItem(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun ComparisonCard(
    comparison: MonthlyComparison,
    currentMonthTitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_comparison_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perbandingan vs Bulan Lalu",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expense Trend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pengeluaran",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Bulan lalu: ${MonthlyReportCalculator.formatRupiah(comparison.previousMonthExpense)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val diffText = if (comparison.expenseDiffPercent >= 0) {
                    "+${String.format(Locale.US, "%.1f", comparison.expenseDiffPercent)}%"
                } else {
                    "${String.format(Locale.US, "%.1f", comparison.expenseDiffPercent)}%"
                }

                val badgeColor = when (comparison.expenseTrend) {
                    TrendDirection.DOWN -> IncomeGreen // Spending decreased = Good
                    TrendDirection.UP -> ExpenseRed   // Spending increased = Bad
                    TrendDirection.STABLE -> Color.Gray
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (comparison.expenseDiffPercent >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = diffText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySpendCard(
    categories: List<CategorySpendSummary>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_spend_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Rincian Pengeluaran per Kategori",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (categories.isEmpty()) {
                Text(
                    text = "Belum ada data pengeluaran bulan ini.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                categories.forEach { cat ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIconBadge(
                                iconKey = cat.iconKey,
                                colorHex = cat.colorHex,
                                size = 32.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = cat.categoryName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = MonthlyReportCalculator.formatRupiah(cat.totalAmount),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${cat.count} transaksi",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", cat.percentageOfTotal)}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        CustomProgressBar(
                            progress = (cat.percentageOfTotal / 100f).toFloat(),
                            barColor = Color(cat.colorHex),
                            height = 6.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetRealizationCard(
    report: MonthlyReportData,
    modifier: Modifier = Modifier
) {
    val budgetSummary = report.budgetSummary
    val usedRatio = if (budgetSummary.totalBudgeted > 0) {
        (budgetSummary.totalSpentOnBudgeted / budgetSummary.totalBudgeted).toFloat().coerceIn(0f, 1.5f)
    } else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_realization_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Realisasi Anggaran Bulanan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${String.format(Locale.US, "%.0f", usedRatio * 100)}% Terpakai",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (usedRatio > 1f) ExpenseRed else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            CustomProgressBar(
                progress = usedRatio.coerceAtMost(1f),
                barColor = if (usedRatio > 1f) ExpenseRed else MaterialTheme.colorScheme.primary,
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Terpakai: ${MonthlyReportCalculator.formatRupiah(budgetSummary.totalSpentOnBudgeted)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total Batas: ${MonthlyReportCalculator.formatRupiah(budgetSummary.totalBudgeted)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FinancialAdviceCard(
    adviceList: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("financial_advice_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AmberWarning.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = AmberWarning,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Saran Finansial Otomatis",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            adviceList.forEach { advice ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = advice,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlySavingsBanner(
    report: MonthlyReportData,
    modifier: Modifier = Modifier
) {
    val net = report.netCashFlow
    val formattedAbs = MonthlyReportCalculator.formatRupiah(kotlin.math.abs(net))
    val formattedIncome = MonthlyReportCalculator.formatRupiah(report.totalIncome)
    val formattedExpense = MonthlyReportCalculator.formatRupiah(report.totalExpense)

    val isSurplus = net > 0
    val isDeficit = net < 0

    val titleText = when {
        isSurplus -> "Yay! Anda berhasil menabung $formattedAbs bulan ini! 🎉"
        isDeficit -> "Perhatian: Pengeluaran Anda melebihi pemasukan sebesar $formattedAbs bulan ini. ⚠️"
        else -> "Saldo Pas: Pemasukan dan pengeluaran Anda seimbang pas bulan ini (Rp 0). ⚖️"
    }

    val subtitleText = when {
        isSurplus -> "Sisa saldo dari total pemasukan ($formattedIncome) dikurangi pengeluaran ($formattedExpense)."
        isDeficit -> "Total pengeluaran ($formattedExpense) lebih besar dari total pemasukan ($formattedIncome) bulan ini."
        else -> "Total pemasukan ($formattedIncome) sama dengan total pengeluaran ($formattedExpense) bulan ini."
    }

    val containerBg = when {
        isSurplus -> IncomeGreen.copy(alpha = 0.12f)
        isDeficit -> ExpenseRed.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    }

    val contentColor = when {
        isSurplus -> IncomeGreen
        isDeficit -> ExpenseRed
        else -> MaterialTheme.colorScheme.primary
    }

    val icon = when {
        isSurplus -> Icons.Default.CheckCircle
        isDeficit -> Icons.Default.Warning
        else -> Icons.Default.Info
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_savings_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, contentColor.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(containerBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitleText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
