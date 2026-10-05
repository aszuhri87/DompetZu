package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.util.CsvExportUtil
import com.example.data.util.MonthlyReportCalculator
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    searchQuery: String,
    filterType: String,
    isAmountHidden: Boolean = false,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onAddTransaction: (TransactionType) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onScanReceipt: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("transactions_screen")
        ) {
            // Pinned Top Header: Search Bar & Filters & CSV Export Button (Fixed at top)
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

                    // Search input with placeholder "Cari Transaksi"
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_transactions_input"),
                        placeholder = { Text("Cari Transaksi", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Hapus",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips & CSV Export Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            item {
                                FilterChip(
                                    selected = filterType == "ALL",
                                    onClick = { onFilterChange("ALL") },
                                    label = { Text("Semua", fontSize = 12.sp) },
                                    modifier = Modifier.testTag("filter_all"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filterType == "EXPENSE",
                                    onClick = { onFilterChange("EXPENSE") },
                                    label = { Text("Pengeluaran", fontSize = 12.sp) },
                                    modifier = Modifier.testTag("filter_expense"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ExpenseRed.copy(alpha = 0.2f),
                                        selectedLabelColor = ExpenseRed
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filterType == "INCOME",
                                    onClick = { onFilterChange("INCOME") },
                                    label = { Text("Pemasukan", fontSize = 12.sp) },
                                    modifier = Modifier.testTag("filter_income"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                                        selectedLabelColor = IncomeGreen
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // CSV Export Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    CsvExportUtil.exportAndShareCsv(
                                        context = context,
                                        transactions = transactions,
                                        titlePrefix = "transaksi_dompetku"
                                    )
                                }
                                .testTag("btn_export_csv")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Ekspor CSV",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "CSV",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Summary Line
                    val totalIn = transactions.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
                    val totalOut = transactions.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${transactions.size} transaksi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+${MonthlyReportCalculator.formatNominal(totalIn, isAmountHidden)}  |  -${MonthlyReportCalculator.formatNominal(totalOut, isAmountHidden)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Scrollable Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Tidak ada transaksi yang sesuai",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 0.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(transactions, key = { it.id }) { tx ->
                            TransactionItemRow(
                                transaction = tx,
                                onClick = { onTransactionClick(tx) },
                                isAmountHidden = isAmountHidden
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Buttons pinned to BottomEnd
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = onScanReceipt,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_scan_receipt_transactions")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Pindai Struk Kamera"
                )
            }

            FloatingActionButton(
                onClick = { onAddTransaction(TransactionType.EXPENSE) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_transaction")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Transaksi"
                )
            }
        }
    }
}
