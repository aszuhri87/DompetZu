package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DefaultCategories
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconBadge
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
    sheetState: SheetState,
    existingTransaction: TransactionEntity?,
    availableWallets: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        walletName: String,
        dateMillis: Long,
        note: String
    ) -> Unit,
    onDelete: (TransactionEntity) -> Unit
) {
    val context = LocalContext.current

    var type by remember(existingTransaction) {
        mutableStateOf(
            if (existingTransaction?.type == TransactionType.INCOME.name)
                TransactionType.INCOME
            else TransactionType.EXPENSE
        )
    }

    var title by remember(existingTransaction) { mutableStateOf(existingTransaction?.title ?: "") }
    var amountText by remember(existingTransaction) {
        mutableStateOf(
            existingTransaction?.amount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }

    val availableCategories = if (type == TransactionType.EXPENSE) {
        DefaultCategories.expenseCategories
    } else {
        DefaultCategories.incomeCategories
    }

    var selectedCategory by remember(type, existingTransaction) {
        mutableStateOf(
            existingTransaction?.category?.takeIf { cat -> availableCategories.any { it.name == cat } }
                ?: availableCategories.first().name
        )
    }

    val walletOptions = remember(availableWallets) {
        if (availableWallets.isNotEmpty()) {
            availableWallets.distinct()
        } else {
            DefaultCategories.defaultWallets
        }
    }

    var selectedWallet by remember(walletOptions, existingTransaction) {
        mutableStateOf(
            existingTransaction?.walletName?.takeIf { walletOptions.contains(it) }
                ?: walletOptions.firstOrNull() ?: "Tunai"
        )
    }

    var note by remember(existingTransaction) { mutableStateOf(existingTransaction?.note ?: "") }
    var selectedDateMillis by remember(existingTransaction) {
        mutableLongStateOf(existingTransaction?.dateMillis ?: System.currentTimeMillis())
    }

    // Date Picker Dialog setup
    val datePickerDialog = remember(selectedDateMillis) {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                selectedDateMillis = newCal.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("add_edit_transaction_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingTransaction == null) "Tambah Transaksi" else "Ubah Transaksi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Type Toggle (Pengeluaran vs Pemasukan)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TypeToggleButton(
                    label = "Pengeluaran",
                    selected = type == TransactionType.EXPENSE,
                    selectedColor = ExpenseRed,
                    onClick = {
                        type = TransactionType.EXPENSE
                        selectedCategory = DefaultCategories.expenseCategories.first().name
                    },
                    modifier = Modifier.weight(1f),
                    tag = "type_expense_btn"
                )
                TypeToggleButton(
                    label = "Pemasukan",
                    selected = type == TransactionType.INCOME,
                    selectedColor = IncomeGreen,
                    onClick = {
                        type = TransactionType.INCOME
                        selectedCategory = DefaultCategories.incomeCategories.first().name
                    },
                    modifier = Modifier.weight(1f),
                    tag = "type_income_btn"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        amountText = input
                    }
                },
                label = { Text("Jumlah Nominal (Rp)") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Amount Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(20000L, 50000L, 100000L, 500000L).forEach { quickAmount ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val current = amountText.toLongOrNull() ?: 0L
                                amountText = (current + quickAmount).toString()
                            }
                    ) {
                        Text(
                            text = "+${quickAmount / 1000}k",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nama Transaksi / Deskripsi") },
                placeholder = { Text(if (type == TransactionType.EXPENSE) "cth: Makan Siang, Belanja Mingguan" else "cth: Gaji Bulanan, Proyek Sampingan") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Transaction Date Selector
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }
                    .testTag("select_transaction_date")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tanggal Transaksi",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateFormat.format(Date(selectedDateMillis)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        text = "Pilih",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selection
            Text(
                text = "Pilih Kategori",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableCategories.forEach { cat ->
                    val isSelected = selectedCategory == cat.name
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(cat.colorHex).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(cat.colorHex)) else null,
                        modifier = Modifier
                            .clickable { selectedCategory = cat.name }
                            .testTag("category_chip_${cat.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIconBadge(
                                iconKey = cat.iconKey,
                                colorHex = cat.colorHex,
                                size = 26.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(cat.colorHex) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wallet Selection
            Text(
                text = "Sumber Dana / Dompet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                walletOptions.forEach { wallet ->
                    FilterChip(
                        selected = selectedWallet == wallet,
                        onClick = { selectedWallet = wallet },
                        label = { Text(wallet) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Catatan Tambahan (Opsional)") },
                placeholder = { Text("Tambahkan keterangan...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons (Save & Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (existingTransaction != null) {
                    OutlinedButton(
                        onClick = { onDelete(existingTransaction) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.4f)
                            .height(50.dp)
                            .testTag("delete_transaction_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus")
                    }
                }

                Button(
                    onClick = {
                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                        val finalTitle = title.ifBlank { selectedCategory }
                        if (parsedAmount > 0) {
                            onSave(
                                finalTitle,
                                parsedAmount,
                                type,
                                selectedCategory,
                                selectedWallet,
                                selectedDateMillis,
                                note
                            )
                        }
                    },
                    enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("save_transaction_button")
                ) {
                    Text(
                        text = if (existingTransaction == null) "Simpan Transaksi" else "Perbarui Transaksi",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TypeToggleButton(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) selectedColor else Color.Transparent,
        modifier = modifier
            .testTag(tag)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
