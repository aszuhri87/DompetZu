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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SavingsGoalEntity
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditSavingsGoalDialog(
    goal: SavingsGoalEntity?,
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
    val categories = listOf("Pernikahan", "Liburan", "Pendidikan", "Kendaraan", "Rumah", "Gaya Hidup", "Lainnya")
    val durationsInMonths = listOf(3, 6, 12, 24, 36)

    var title by remember { mutableStateOf(goal?.title ?: "") }
    var targetAmountText by remember { mutableStateOf(goal?.targetAmount?.toLong()?.toString() ?: "") }
    var currentAmountText by remember { mutableStateOf(goal?.currentAmount?.toLong()?.toString() ?: "0") }
    var selectedCategory by remember { mutableStateOf(goal?.category ?: "Pernikahan") }
    var selectedDurationMonths by remember { mutableIntStateOf(12) }
    var note by remember { mutableStateOf(goal?.note ?: "") }

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
                Text(
                    text = if (goal == null) "Tambah Target Tabungan" else "Ubah Target Tabungan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Judul Target
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Impian / Target (Contoh: Pernikahan)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target Amount
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            targetAmountText = input
                        }
                    },
                    label = { Text("Nominal Target Penyelamatan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_target_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Current Amount
                OutlinedTextField(
                    value = currentAmountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            currentAmountText = input
                        }
                    },
                    label = { Text("Nominal Terkumpul Saat Ini (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_current_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Kategori
                Text(
                    text = "Kategori Impian",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedCategory = cat }
                                .testTag("savings_category_chip_$cat")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val emoji = when (cat) {
                                    "Pernikahan" -> "💍"
                                    "Liburan" -> "✈️"
                                    "Pendidikan" -> "🎓"
                                    "Kendaraan" -> "🚗"
                                    "Rumah" -> "🏡"
                                    "Gaya Hidup" -> "🛍️"
                                    else -> "💰"
                                }
                                Text(emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Durasi Menabung (Target Waktu)
                Text(
                    text = "Rencana Durasi Menabung",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    durationsInMonths.forEach { months ->
                        val isSelected = selectedDurationMonths == months
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedDurationMonths = months }
                        ) {
                            Text(
                                text = "$months Bulan",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Catatan
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_note_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Batal")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val finalTarget = targetAmountText.toDoubleOrNull() ?: 0.0
                        val finalCurrent = currentAmountText.toDoubleOrNull() ?: 0.0
                        
                        // Calculate targetDateMillis from selectedDurationMonths
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.MONTH, selectedDurationMonths)
                        val targetDateMillis = cal.timeInMillis

                        if (title.isNotBlank() && finalTarget > 0.0) {
                            onSave(title, finalTarget, finalCurrent, selectedCategory, targetDateMillis, note)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Simpan", color = Color.White)
                }
            }
        }
    )
}
