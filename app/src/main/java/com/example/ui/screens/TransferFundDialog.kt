package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.util.MonthlyReportCalculator
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.WalletBalance

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransferFundDialog(
    wallets: List<WalletBalance>,
    preselectedSource: String? = null,
    onDismiss: () -> Unit,
    onTransfer: (sourceWallet: String, targetWallet: String, amount: Double, note: String) -> Unit
) {
    if (wallets.size < 2) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Pindah Dana Antar Dompet") },
            text = {
                Text("Anda memerlukan minimal 2 dompet untuk melakukan pemindahan dana. Silakan buat dompet baru terlebih dahulu melalui tombol '+ Atur Saldo'.")
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Mengerti") }
            }
        )
        return
    }

    var sourceWallet by remember {
        val initial = preselectedSource?.takeIf { s -> wallets.any { it.walletName.equals(s, ignoreCase = true) } }
            ?: wallets.first().walletName
        mutableStateOf(initial)
    }

    var targetWallet by remember {
        val available = wallets.filter { !it.walletName.equals(sourceWallet, ignoreCase = true) }
        mutableStateOf(available.firstOrNull()?.walletName ?: "")
    }

    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val sourceBalance = wallets.find { it.walletName.equals(sourceWallet, ignoreCase = true) }?.balance ?: 0.0
    val targetBalance = wallets.find { it.walletName.equals(targetWallet, ignoreCase = true) }?.balance ?: 0.0
    val transferAmount = amountText.toDoubleOrNull() ?: 0.0

    val quickAddAmounts = listOf(
        Pair("+50 rb", 50_000L),
        Pair("+100 rb", 100_000L),
        Pair("+200 rb", 200_000L),
        Pair("+500 rb", 500_000L),
        Pair("+1 jt", 1_000_000L),
        Pair("Semua Saldo", sourceBalance.toLong().coerceAtLeast(0L))
    )

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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pindah Dana Antar Dompet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
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
                // Dompet Asal (Source Wallet)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. Dompet Asal (Sumber Dana)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Saldo: ${MonthlyReportCalculator.formatRupiah(sourceBalance)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        wallets.forEach { w ->
                            val isSelected = w.walletName.equals(sourceWallet, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null,
                                shadowElevation = if (isSelected) 1.dp else 0.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        sourceWallet = w.walletName
                                        if (targetWallet.equals(w.walletName, ignoreCase = true)) {
                                            targetWallet = wallets.firstOrNull { !it.walletName.equals(w.walletName, ignoreCase = true) }?.walletName ?: ""
                                        }
                                    }
                            ) {
                                Text(
                                    text = "${w.walletName} (${MonthlyReportCalculator.formatRupiah(w.balance)})",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Dompet Tujuan (Destination Wallet)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Dompet Tujuan (Penerima Dana)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Saldo: ${MonthlyReportCalculator.formatRupiah(targetBalance)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IncomeGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        wallets.filter { !it.walletName.equals(sourceWallet, ignoreCase = true) }.forEach { w ->
                            val isSelected = w.walletName.equals(targetWallet, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) IncomeGreen else MaterialTheme.colorScheme.surface,
                                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null,
                                shadowElevation = if (isSelected) 1.dp else 0.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { targetWallet = w.walletName }
                            ) {
                                Text(
                                    text = "${w.walletName} (${MonthlyReportCalculator.formatRupiah(w.balance)})",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Nominal Transfer
                Column {
                    Text(
                        text = "3. Nominal Pemindahan Dana",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) amountText = input
                        },
                        label = { Text("Nominal Transfer (Rp)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickAddAmounts) { (label, amount) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (label == "Semua Saldo") {
                                            amountText = amount.toString()
                                        } else {
                                            val cur = amountText.toLongOrNull() ?: 0L
                                            amountText = (cur + amount).toString()
                                        }
                                    }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Catatan Pemindahan
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan / Keterangan (Opsional)") },
                    placeholder = { Text("Contoh: Tarik tunai, Top-up e-wallet") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Transfer Preview Card
                if (transferAmount > 0 && sourceWallet.isNotBlank() && targetWallet.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Dari:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = sourceWallet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Sisa: ${MonthlyReportCalculator.formatRupiah(sourceBalance - transferAmount)}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(text = "Ke:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = targetWallet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Menjadi: ${MonthlyReportCalculator.formatRupiah(targetBalance + transferAmount)}",
                                        fontSize = 10.sp,
                                        color = IncomeGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                if (transferAmount > sourceBalance && sourceBalance > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Catatan: Nominal melebihi saldo saat ini di $sourceWallet.",
                            fontSize = 11.sp,
                            color = AmberWarning
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (transferAmount > 0 && sourceWallet.isNotBlank() && targetWallet.isNotBlank() && !sourceWallet.equals(targetWallet, ignoreCase = true)) {
                        onTransfer(sourceWallet, targetWallet, transferAmount, note)
                    }
                },
                enabled = transferAmount > 0 && sourceWallet.isNotBlank() && targetWallet.isNotBlank() && !sourceWallet.equals(targetWallet, ignoreCase = true),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("btn_confirm_transfer")
            ) {
                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pindahkan Dana", color = Color.White, fontWeight = FontWeight.Bold)
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
