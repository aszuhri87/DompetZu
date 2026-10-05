package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategorySpendSummary
import com.example.data.util.MonthlyReportCalculator
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.sqrt

// Modern fallback palette for donut slices
val ChartPalette = listOf(
    Color(0xFFEF4444), // Red
    Color(0xFF3B82F6), // Blue
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEC4899), // Pink
    Color(0xFF14B8A6), // Teal
    Color(0xFFF97316), // Orange
    Color(0xFF6366F1), // Indigo
    Color(0xFF06B6D4)  // Cyan
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDistributionPieChart(
    categories: List<CategorySpendSummary>,
    totalExpense: Double,
    onNavigateToReport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableStateOf<Int?>(null) }
    var animationTarget by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(categories) {
        animationTarget = 0f
        animationTarget = 1f
        if (selectedCategoryIndex != null && selectedCategoryIndex!! >= categories.size) {
            selectedCategoryIndex = null
        }
    }

    val progressAnim by animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "donut_chart_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_donut_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DonutLarge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Distribusi Pengeluaran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Proporsi alokasi pengeluaran per kategori",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val safeTotalExpense = if (totalExpense > 0.0 && !totalExpense.isNaN() && !totalExpense.isInfinite()) totalExpense else 0.0

            if (categories.isEmpty() || safeTotalExpense <= 0.0) {
                // Empty state donut placeholder
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(170.dp)) {
                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.25f),
                            style = Stroke(width = 22.dp.toPx())
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DonutLarge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Belum Ada\nPengeluaran",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Interactive Donut Chart Container
                val selectedItem = selectedCategoryIndex?.let { categories.getOrNull(it) }

                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .testTag("donut_chart_canvas_box"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(220.dp)
                            .testTag("donut_chart_canvas")
                            .pointerInput(categories, safeTotalExpense) {
                                detectTapGestures { tapOffset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = tapOffset.x - center.x
                                    val dy = tapOffset.y - center.y
                                    val distance = sqrt(dx * dx + dy * dy)
                                    val outerRadius = size.width / 2f
                                    val innerRadius = (outerRadius - 28.dp.toPx()).coerceAtLeast(10f)

                                    if (distance in (innerRadius * 0.7f)..(outerRadius * 1.15f) && safeTotalExpense > 0.0) {
                                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                        angle = (angle + 90f + 360f) % 360f

                                        var currentStartAngle = 0f
                                        var tappedIdx: Int? = null
                                        for (i in categories.indices) {
                                            val catAmt = categories[i].totalAmount.coerceAtLeast(0.0)
                                            val sweep = ((catAmt / safeTotalExpense) * 360.0).toFloat()
                                            if (angle >= currentStartAngle && angle < currentStartAngle + sweep) {
                                                tappedIdx = i
                                                break
                                            }
                                            currentStartAngle += sweep
                                        }

                                        selectedCategoryIndex = if (selectedCategoryIndex == tappedIdx) null else tappedIdx
                                    } else {
                                        selectedCategoryIndex = null
                                    }
                                }
                            }
                    ) {
                        val strokeWidthNormal = 18.dp.toPx()
                        val strokeWidthSelected = 24.dp.toPx()
                        val diameter = (size.minDimension - strokeWidthSelected).coerceAtLeast(10f)
                        val radius = diameter / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val arcSize = Size(diameter, diameter)
                        val topLeft = Offset(center.x - radius, center.y - radius)

                        var startAngle = -90f // Start at 12 o'clock

                        val animFactor = progressAnim.coerceIn(0f, 1f)

                        categories.forEachIndexed { index, cat ->
                            val catAmt = if (cat.totalAmount > 0 && !cat.totalAmount.isNaN()) cat.totalAmount else 0.0
                            val ratio = if (safeTotalExpense > 0.0) (catAmt / safeTotalExpense) else 0.0
                            val sweepAngle = (ratio * 360.0 * animFactor).toFloat().coerceIn(0f, 360f)

                            val isSelected = selectedCategoryIndex == index
                            val hasSelection = selectedCategoryIndex != null

                            val sliceColor = if (cat.colorHex != 0L) {
                                Color(cat.colorHex)
                            } else {
                                ChartPalette[index % ChartPalette.size]
                            }

                            val actualColor = when {
                                isSelected -> sliceColor
                                hasSelection -> sliceColor.copy(alpha = 0.35f)
                                else -> sliceColor
                            }

                            val currentStroke = if (isSelected) strokeWidthSelected else strokeWidthNormal
                            val gap = if (categories.size > 1 && sweepAngle > 4f) 2f else 0f
                            val actualSweep = (sweepAngle - gap).coerceAtLeast(0.5f)

                            if (actualSweep > 0.1f && !actualSweep.isNaN() && !startAngle.isNaN()) {
                                drawArc(
                                    color = actualColor,
                                    startAngle = startAngle + (gap / 2f),
                                    sweepAngle = actualSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(
                                        width = currentStroke,
                                        cap = StrokeCap.Round
                                    )
                                )
                            }

                            startAngle += sweepAngle
                        }
                    }

                    // Center Content in Donut Hole (Robust, No basicMarquee crash, dynamic font size)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .widthIn(max = 160.dp)
                            .padding(horizontal = 4.dp)
                    ) {
                        if (selectedItem != null) {
                            Text(
                                text = selectedItem.categoryName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val formattedItemAmount = MonthlyReportCalculator.formatRupiah(selectedItem.totalAmount)
                            val itemFontSize = if (formattedItemAmount.length > 13) 13.sp else 15.sp
                            Text(
                                text = formattedItemAmount,
                                fontSize = itemFontSize,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selectedItem.colorHex != 0L) Color(selectedItem.colorHex) else MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", selectedItem.percentageOfTotal)}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            val formattedTotal = MonthlyReportCalculator.formatRupiah(safeTotalExpense)
                            val totalFontSize = if (formattedTotal.length > 13) 14.sp else 16.sp
                            Text(
                                text = formattedTotal,
                                fontSize = totalFontSize,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${categories.size} Kategori",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Legend Chips
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("donut_chart_legend_flow_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEachIndexed { index, cat ->
                        val isSelected = selectedCategoryIndex == index
                        val sliceColor = if (cat.colorHex != 0L) {
                            Color(cat.colorHex)
                        } else {
                            ChartPalette[index % ChartPalette.size]
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) sliceColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, sliceColor) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedCategoryIndex = if (selectedCategoryIndex == index) null else index
                                }
                                .testTag("legend_item_$index")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(sliceColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.categoryName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", cat.percentageOfTotal)}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = sliceColor
                                )
                            }
                        }
                    }
                }

                if (onNavigateToReport != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = onNavigateToReport,
                        modifier = Modifier.testTag("btn_view_full_report_from_pie")
                    ) {
                        Text(
                            text = "Lihat Analisis Laporan Lengkap",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
