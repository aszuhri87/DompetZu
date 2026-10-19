package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SavingsGoalEntity
import com.example.data.util.GoldSavingsData
import com.example.data.util.MonthlyReportCalculator
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.util.Calendar

import androidx.compose.foundation.isSystemInDarkTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditSavingsGoalDialog(
    goal: SavingsGoalEntity?,
    liveMarketPrice: Double = GoldSavingsData.DEFAULT_GOLD_PRICE_PER_GRAM,
    liveMarketLastUpdated: String = "",
    isRefreshingMarketPrice: Boolean = false,
    onRefreshMarketPrice: () -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        targetAmount: Double,
        currentAmount: Double,
        category: String,
        targetDateMillis: Long,
        note: String
    ) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val categories = listOf("Emas", "Pernikahan", "Liburan", "Pendidikan", "Kendaraan", "Rumah", "Gaya Hidup", "Lainnya")
    val durationsInMonths = listOf(3, 6, 12, 24, 36)

    val initialGoldData = remember(goal) {
        if (goal != null) {
            GoldSavingsData.parse(goal.category, goal.note, goal.targetAmount, goal.currentAmount, liveMarketPrice)
        } else {
            GoldSavingsData(isGold = false)
        }
    }

    var selectedCategory by remember { mutableStateOf(goal?.category ?: "Emas") }
    val isGoldCategory = selectedCategory.equals("Emas", ignoreCase = true) || selectedCategory.equals("Tabungan Emas", ignoreCase = true)

    var title by remember {
        mutableStateOf(
            goal?.title ?: if (isGoldCategory) "Tabungan Emas Batangan" else ""
        )
    }

    // Gold specific inputs
    var goldUnit by remember { mutableStateOf(if (initialGoldData.isGold) initialGoldData.unit else "gram") }
    var goldTargetWeightText by remember {
        mutableStateOf(
            if (initialGoldData.isGold && initialGoldData.targetWeight > 0) {
                if (initialGoldData.targetWeight % 1.0 == 0.0) initialGoldData.targetWeight.toLong().toString()
                else initialGoldData.targetWeight.toString()
            } else ""
        )
    }
    var goldCurrentWeightText by remember {
        mutableStateOf(
            if (initialGoldData.isGold && initialGoldData.currentWeight > 0) {
                if (initialGoldData.currentWeight % 1.0 == 0.0) initialGoldData.currentWeight.toLong().toString()
                else initialGoldData.currentWeight.toString()
            } else ""
        )
    }
    var goldPricePerGramText by remember {
        mutableStateOf(
            if (initialGoldData.isGold && initialGoldData.pricePerGram > 0) {
                initialGoldData.pricePerGram.toLong().toString()
            } else {
                (if (liveMarketPrice > 0) liveMarketPrice else GoldSavingsData.DEFAULT_GOLD_PRICE_PER_GRAM).toLong().toString()
            }
        )
    }

    // Standard cash inputs
    var targetAmountText by remember {
        mutableStateOf(if (initialGoldData.isGold) "" else (goal?.targetAmount?.toLong()?.toString() ?: ""))
    }
    var currentAmountText by remember {
        mutableStateOf(if (initialGoldData.isGold) "" else (goal?.currentAmount?.toLong()?.toString() ?: "0"))
    }

    var selectedDurationMonths by remember { mutableIntStateOf(12) }
    var note by remember {
        mutableStateOf(if (initialGoldData.isGold) initialGoldData.userNote else (goal?.note ?: ""))
    }

    // Live calculations for Gold
    val targetWeightNum = goldTargetWeightText.toDoubleOrNull() ?: 0.0
    val currentWeightNum = goldCurrentWeightText.toDoubleOrNull() ?: 0.0
    val pricePerGramNum = goldPricePerGramText.toDoubleOrNull() ?: liveMarketPrice

    val liveGoldData = GoldSavingsData(
        isGold = true,
        unit = goldUnit,
        targetWeight = targetWeightNum,
        currentWeight = currentWeightNum,
        pricePerGram = pricePerGramNum,
        liveMarketPrice = if (liveMarketPrice > 0) liveMarketPrice else pricePerGramNum,
        userNote = note
    )

    // Quick chips for gold weights
    val quickGramWeights = listOf(1.0, 2.0, 5.0, 10.0, 25.0, 50.0, 100.0)
    val quickKgWeights = listOf(0.1, 0.25, 0.5, 1.0, 2.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(vertical = 16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isGoldCategory) {
                        Text("🪙 ", fontSize = 20.sp)
                    }
                    Text(
                        text = if (goal == null) {
                            if (isGoldCategory) "Tambah Tabungan Emas" else "Tambah Target Tabungan"
                        } else {
                            if (isGoldCategory) "Ubah Tabungan Emas" else "Ubah Target Tabungan"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Kategori Impian (Pilihan Kategori di paling atas)
                Column {
                    Text(
                        text = "Pilih Kategori Tabungan",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                            val emoji = when (cat) {
                                "Emas" -> "🪙"
                                "Pernikahan" -> "💍"
                                "Liburan" -> "✈️"
                                "Pendidikan" -> "🎓"
                                "Kendaraan" -> "🚗"
                                "Rumah" -> "🏡"
                                "Gaya Hidup" -> "🛍️"
                                else -> "💰"
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) {
                                    if (cat == "Emas") Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
                                } else MaterialTheme.colorScheme.surface,
                                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null,
                                shadowElevation = if (isSelected) 1.dp else 0.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedCategory = cat
                                        if (cat == "Emas" && title.isBlank()) {
                                            title = "Tabungan Emas Batangan"
                                        }
                                    }
                                    .testTag("savings_category_chip_$cat")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(emoji, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = cat,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Judul Target
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(if (isGoldCategory) "Nama Tabungan Emas (Contoh: Emas Batangan Antam)" else "Nama Impian / Target")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (isGoldCategory) {
                    // ================= GOLD INPUT MODE =================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7).copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Satuan Berat Emas: Gram vs Kg
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Satuan Berat Emas:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Gram Chip
                                    val isGram = goldUnit.equals("gram", ignoreCase = true) || goldUnit.equals("gr", ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isGram) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (!isGram) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) else null,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { goldUnit = "gram" }
                                    ) {
                                        Text(
                                            text = "Gram (gr)",
                                            fontSize = 11.sp,
                                            fontWeight = if (isGram) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isGram) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }

                                    // Kilogram Chip
                                    val isKg = goldUnit.equals("kg", ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isKg) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (!isKg) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) else null,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { goldUnit = "kg" }
                                    ) {
                                        Text(
                                            text = "Kilogram (kg)",
                                            fontSize = 11.sp,
                                            fontWeight = if (isKg) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isKg) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Harga Acuan Emas per Gram & Status Pasar Indonesia
                            val benchBg = if (isDark) Color(0xFF262118) else Color(0xFFFFFBEB)
                            val benchBorder = if (isDark) Color(0xFFD97706).copy(alpha = 0.5f) else Color(0xFFF59E0B).copy(alpha = 0.4f)
                            val benchTitleColor = if (isDark) Color(0xFFFBBF24) else Color(0xFF78350F)
                            val benchTimeColor = if (isDark) Color(0xFFE5E7EB) else Color(0xFF92400E)
                            val benchPriceColor = if (isDark) Color(0xFFFDE68A) else Color(0xFF78350F)
                            val benchSubtitleColor = if (isDark) Color(0xFFFDE68A).copy(alpha = 0.9f) else Color(0xFF92400E)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = benchBg,
                                border = BorderStroke(1.dp, benchBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "🟢 Acuan Pasar Indonesia",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = benchTitleColor
                                                )
                                                if (liveMarketLastUpdated.isNotBlank()) {
                                                    Text(
                                                        text = " • $liveMarketLastUpdated",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = benchTimeColor
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${GoldSavingsData.formatRupiah(liveMarketPrice)} / gr",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = benchPriceColor
                                            )
                                        }

                                        IconButton(
                                            onClick = onRefreshMarketPrice,
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            if (isRefreshingMarketPrice) {
                                                androidx.compose.material3.CircularProgressIndicator(
                                                    strokeWidth = 2.dp,
                                                    color = benchTitleColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Perbarui Harga Emas",
                                                    tint = benchTitleColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "⚡ Acuan harga pasar langsung terhubung otomatis.",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = benchSubtitleColor
                                    )
                                }
                            }
                        }
                    }

                    // Input Target Berat Emas
                    Column {
                        OutlinedTextField(
                            value = goldTargetWeightText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' }) goldTargetWeightText = input
                            },
                            label = { Text("Target Berat Emas ($goldUnit)") },
                            suffix = { Text(goldUnit) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gold_target_weight_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val itemsList = if (goldUnit.equals("kg", ignoreCase = true)) quickKgWeights else quickGramWeights
                            items(itemsList) { w ->
                                val label = if (goldUnit == "kg") "$w kg" else "${w.toInt()} gr"
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            goldTargetWeightText = if (w % 1.0 == 0.0) w.toLong().toString() else w.toString()
                                        }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Input Berat Emas Terkumpul Saat Ini
                    Column {
                        OutlinedTextField(
                            value = goldCurrentWeightText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' }) goldCurrentWeightText = input
                            },
                            label = { Text("Berat Emas Terkumpul Saat Ini ($goldUnit)") },
                            placeholder = { Text("0") },
                            suffix = { Text(goldUnit) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gold_current_weight_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val itemsList = if (goldUnit.equals("kg", ignoreCase = true)) listOf(0.05, 0.1, 0.25, 0.5) else listOf(0.5, 1.0, 2.0, 5.0, 10.0)
                            items(itemsList) { w ->
                                val label = if (goldUnit == "kg") "+$w kg" else "+${if (w % 1.0 == 0.0) w.toInt() else w} gr"
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val cur = goldCurrentWeightText.toDoubleOrNull() ?: 0.0
                                            val next = cur + w
                                            goldCurrentWeightText = if (next % 1.0 == 0.0) next.toLong().toString() else String.format(java.util.Locale.US, "%.2f", next).trimEnd('0').trimEnd('.')
                                        }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Otomatis Menampilkan Nilai Emas dalam Mata Uang Saat Ini
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val convTitleColor = if (isDark) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary
                            val convLabelColor = if (isDark) Color(0xFFD1D5DB) else MaterialTheme.colorScheme.onSurfaceVariant
                            val convValColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = convTitleColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Konversi Otomatis Nilai Emas ke Rupiah:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = convTitleColor
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Nilai Target Pasar Emas", fontSize = 10.sp, color = convLabelColor)
                                    Text(
                                        text = MonthlyReportCalculator.formatRupiah(liveGoldData.liveTargetValueRupiah),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = convValColor
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Nilai Pasar Terkumpul Saat Ini", fontSize = 10.sp, color = convLabelColor)
                                    Text(
                                        text = MonthlyReportCalculator.formatRupiah(liveGoldData.liveCurrentValueRupiah),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDark) Color(0xFF34D399) else IncomeGreen
                                    )
                                }
                            }

                            if (liveGoldData.currentWeight > 0 && liveGoldData.profitLossRupiah != 0.0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val isGain = liveGoldData.profitLossRupiah > 0
                                val badgeBg = when {
                                    isGain -> if (isDark) Color(0xFF064E3B) else IncomeGreen.copy(alpha = 0.12f)
                                    else -> if (isDark) Color(0xFF7F1D1D) else ExpenseRed.copy(alpha = 0.12f)
                                }
                                val badgeText = when {
                                    isGain -> if (isDark) Color(0xFF34D399) else IncomeGreen
                                    else -> if (isDark) Color(0xFFFCA5A5) else ExpenseRed
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = badgeBg
                                ) {
                                    Text(
                                        text = if (isGain) {
                                            "📈 Apresiasi Pasar: +${GoldSavingsData.formatRupiah(liveGoldData.profitLossRupiah)} (+${String.format(java.util.Locale.US, "%.1f", liveGoldData.profitLossPercent)}%)"
                                        } else {
                                            "📉 Penurunan Pasar: -${GoldSavingsData.formatRupiah(kotlin.math.abs(liveGoldData.profitLossRupiah))} (${String.format(java.util.Locale.US, "%.1f", liveGoldData.profitLossPercent)}%)"
                                        },
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (liveGoldData.targetWeight > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Sisa tabungan: ${GoldSavingsData.formatWeight(liveGoldData.remainingWeight, goldUnit)} (~${MonthlyReportCalculator.formatRupiah(liveGoldData.remainingAmountRupiah)})",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = convLabelColor
                                )
                            }
                        }
                    }
                } else {
                    // ================= STANDARD RUPIAH CASH MODE =================
                    OutlinedTextField(
                        value = targetAmountText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) targetAmountText = input
                        },
                        label = { Text("Nominal Target Tabungan (Rp)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("savings_target_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = currentAmountText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) currentAmountText = input
                        },
                        label = { Text("Nominal Terkumpul Saat Ini (Rp)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("savings_current_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Rencana Durasi Menabung
                Column {
                    Text(
                        text = "Rencana Durasi Menabung",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        durationsInMonths.forEach { months ->
                            val isSelected = selectedDurationMonths == months
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedDurationMonths = months }
                            ) {
                                Text(
                                    text = "$months Bulan",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Catatan
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan / Keterangan (Opsional)") },
                    placeholder = {
                        Text(if (isGoldCategory) "Contoh: Beli di Butik Emas Antam / Pegadaian" else "Catatan tambahan")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_note_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTarget: Double
                    val finalCurrent: Double
                    val finalNote: String

                    if (isGoldCategory) {
                        finalTarget = liveGoldData.targetAmountRupiah
                        finalCurrent = liveGoldData.currentAmountRupiah
                        finalNote = GoldSavingsData.buildNote(liveGoldData)
                    } else {
                        finalTarget = targetAmountText.toDoubleOrNull() ?: 0.0
                        finalCurrent = currentAmountText.toDoubleOrNull() ?: 0.0
                        finalNote = note.trim()
                    }

                    // Target date from selectedDurationMonths
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, selectedDurationMonths)
                    val targetDateMillis = cal.timeInMillis

                    if (title.isNotBlank() && finalTarget > 0.0) {
                        onSave(title.trim(), finalTarget, finalCurrent, selectedCategory, targetDateMillis, finalNote)
                    }
                },
                enabled = title.isNotBlank() && if (isGoldCategory) targetWeightNum > 0.0 else (targetAmountText.toDoubleOrNull() ?: 0.0) > 0.0,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGoldCategory) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("save_savings_goal_button")
            ) {
                Text(if (goal == null) "Buat Target" else "Simpan Perubahan", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Batal")
            }
        }
    )
}
