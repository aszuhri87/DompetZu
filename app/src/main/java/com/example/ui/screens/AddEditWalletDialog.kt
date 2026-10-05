package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.util.MonthlyReportCalculator
import com.example.ui.theme.ExpenseRed

@Composable
fun AddEditWalletDialog(
    existingAccount: AccountEntity?,
    currentLiveBalance: Double = 0.0,
    onDismiss: () -> Unit,
    onSaveAccount: (name: String, type: String, balance: Double, colorHex: Long, iconKey: String, clearTransactions: Boolean) -> Unit,
    onDeleteAccount: ((Long) -> Unit)? = null
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existingAccount?.name ?: "") }
    
    // Default initial/live balance in text field
    var balanceText by remember {
        val initialVal = if (existingAccount != null) {
            currentLiveBalance.toLong().toString()
        } else {
            ""
        }
        mutableStateOf(if (initialVal == "0") "" else initialVal)
    }

    var selectedType by remember { mutableStateOf(existingAccount?.type ?: "BANK") }
    var selectedColor by remember { mutableLongStateOf(existingAccount?.colorHex ?: 0xFF2563EB) }
    var clearTransactionsChecked by remember { mutableStateOf(false) }

    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    val walletColors = listOf(
        0xFF10B981, // Emerald Green
        0xFF2563EB, // Royal Blue
        0xFF8B5CF6, // Purple
        0xFFF59E0B, // Amber
        0xFFEF4444, // Red
        0xFF06B6D4, // Cyan
        0xFFEC4899  // Pink
    )

    val types = listOf(
        Triple("BANK", "Rekening Bank", "account_balance"),
        Triple("E_WALLET", "E-Wallet", "account_balance_wallet"),
        Triple("CASH", "Uang Tunai", "payments"),
        Triple("SAVINGS", "Tabungan", "savings"),
        Triple("INVESTMENT", "Investasi", "trending_up"),
        Triple("OTHER", "Lainnya", "more_horiz")
    )

    val quickAddAmounts = listOf(
        Pair("+100 rb", 100_000L),
        Pair("+500 rb", 500_000L),
        Pair("+1 jt", 1_000_000L),
        Pair("+5 jt", 5_000_000L),
        Pair("+10 jt", 10_000_000L),
        Pair("+20 jt", 20_000_000L)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingAccount == null) "Tambah Dompet / Rekening" else "Kelola Dompet / Rekening",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (existingAccount != null) {
                    // Live Balance Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Saldo Saat Ini (${existingAccount.name})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = MonthlyReportCalculator.formatNominal(currentLiveBalance, false),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Dompet / Bank") },
                    placeholder = { Text("Contoh: BCA, Dompet Tunai, Gopay") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) balanceText = input
                    },
                    label = { Text("Saldo / Nominal Saldo (Rp)") },
                    placeholder = { Text("0") },
                    trailingIcon = {
                        if (balanceText.isNotBlank()) {
                            IconButton(onClick = { balanceText = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Hapus Input",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_balance_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Balance Preset Chips
                Text(
                    text = "Pilihan Cepat Saldo:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickAddAmounts) { (label, amount) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val cur = balanceText.toLongOrNull() ?: 0L
                                    balanceText = (cur + amount).toString()
                                }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tipe Dompet",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.chunked(3).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            chunk.forEach { (typeKey, typeLabel, _) ->
                                val isSelected = selectedType == typeKey
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedType = typeKey }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = typeLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pilih Warna Kartu",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    walletColors.forEach { colorVal ->
                        val isSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .clickable { selectedColor = colorVal },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }

                if (existingAccount != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { clearTransactionsChecked = !clearTransactionsChecked }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = clearTransactionsChecked,
                            onCheckedChange = { clearTransactionsChecked = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Bersihkan seluruh riwayat transaksi dompet ini",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedBalance = balanceText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        val iconKey = types.find { it.first == selectedType }?.third ?: "account_balance_wallet"
                        onSaveAccount(
                            name.trim(),
                            selectedType,
                            parsedBalance,
                            selectedColor,
                            iconKey,
                            clearTransactionsChecked
                        )
                        Toast.makeText(context, "Dompet berhasil disimpan", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Nama dompet tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.testTag("save_wallet_button")
            ) {
                Text(if (existingAccount == null) "Tambah Dompet" else "Simpan Perubahan")
            }
        },
        dismissButton = if (existingAccount != null && onDeleteAccount != null) {
            {
                TextButton(
                    onClick = { showConfirmDeleteDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                ) {
                    Text("Hapus")
                }
            }
        } else null
    )

    // Confirmation dialog for deleting wallet
    if (showConfirmDeleteDialog && existingAccount != null && onDeleteAccount != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ExpenseRed) },
            title = { Text("Hapus Dompet?") },
            text = {
                Text("Dompet '${existingAccount.name}' akan dihapus dari aplikasi. Riwayat transaksi terkait akan tetap tersimpan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAccount(existingAccount.id)
                        showConfirmDeleteDialog = false
                        Toast.makeText(context, "Dompet '${existingAccount.name}' telah dihapus", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Hapus Dompet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
