package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DefaultCategories
import com.example.data.model.RecurringBillEntity
import com.example.ui.components.CategoryIconBadge

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditRecurringBillDialog(
    bill: RecurringBillEntity?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        category: String,
        walletName: String,
        dueDay: Int,
        frequency: String,
        reminderDaysBefore: Int,
        note: String
    ) -> Unit
) {
    val expenseCategories = DefaultCategories.expenseCategories
    val wallets = DefaultCategories.defaultWallets

    var title by remember { mutableStateOf(bill?.title ?: "") }
    var amountText by remember { mutableStateOf(bill?.amount?.toLong()?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(bill?.category ?: "Tagihan & Utilitas") }
    var selectedWallet by remember { mutableStateOf(bill?.walletName ?: "Rekening Bank") }
    var dueDayText by remember { mutableStateOf(bill?.dueDay?.toString() ?: "10") }
    var frequency by remember { mutableStateOf(bill?.frequency ?: "BULANAN") }
    var reminderDaysBefore by remember { mutableIntStateOf(bill?.reminderDaysBefore ?: 3) }
    var note by remember { mutableStateOf(bill?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (bill == null) "Tambah Tagihan Rutin" else "Ubah Tagihan Rutin",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Tagihan (contoh: WiFi, PLN, Kos)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            amountText = input
                        }
                    },
                    label = { Text("Nominal Tagihan (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && (input.toIntOrNull() ?: 0) in 0..31) {
                                dueDayText = input
                            }
                        },
                        label = { Text("Tgl Jatuh Tempo (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bill_due_day_input")
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pengingat H-",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1, 3, 5).forEach { d ->
                                FilterChip(
                                    selected = reminderDaysBefore == d,
                                    onClick = { reminderDaysBefore = d },
                                    label = { Text("H-$d") },
                                    modifier = Modifier.testTag("reminder_chip_$d")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Frekuensi",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("BULANAN", "MINGGUAN", "TAHUNAN").forEach { freq ->
                        FilterChip(
                            selected = frequency == freq,
                            onClick = { frequency = freq },
                            label = { Text(freq) },
                            modifier = Modifier.testTag("freq_chip_$freq")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Dompet Pembayaran",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    wallets.forEach { wName ->
                        val isSelected = wName == selectedWallet
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedWallet = wName }
                        ) {
                            Text(
                                text = wName,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Kategori",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    expenseCategories.take(6).forEach { cat ->
                        val isSelected = cat.name.equals(selectedCategory, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedCategory = cat.name }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryIconBadge(category = cat.name, size = 16.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cat.name,
                                    fontSize = 11.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan / Nomor Pelanggan (opsional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val dueDay = dueDayText.toIntOrNull() ?: 1
                    if (title.isNotBlank() && amount > 0 && dueDay in 1..31) {
                        onSave(
                            title.trim(),
                            amount,
                            selectedCategory,
                            selectedWallet,
                            dueDay,
                            frequency,
                            reminderDaysBefore,
                            note.trim()
                        )
                    }
                },
                enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("save_bill_button")
            ) {
                Text("Simpan")
            }
        }
    )
}
