package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AddEditBudgetDialog
import com.example.ui.screens.AddEditRecurringBillDialog
import com.example.ui.screens.AddEditTransactionSheet
import com.example.ui.screens.AddEditWalletDialog
import com.example.ui.screens.AppLockScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonthlyReportScreen
import com.example.ui.screens.NotificationCenterDialog
import com.example.ui.screens.ReceiptScannerDialog
import com.example.ui.screens.SecuritySettingsDialog
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.DompetKuTheme
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.NavTab

class MainActivity : ComponentActivity() {
    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DompetKuTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

data class NavItem(
    val tab: NavTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: FinanceViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val isLockEnabled by viewModel.isLockEnabled.collectAsStateWithLifecycle()
    val isAmountHidden by viewModel.isAmountHidden.collectAsStateWithLifecycle()
    val showSecurityDialog by viewModel.showSecurityDialog.collectAsStateWithLifecycle()

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val report by viewModel.monthlyReport.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val displayTransactions by viewModel.displayTransactions.collectAsStateWithLifecycle()
    val currentMonthTransactions by viewModel.currentMonthTransactions.collectAsStateWithLifecycle()
    val budgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val allBills by viewModel.allBills.collectAsStateWithLifecycle()
    val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val showNotificationDialog by viewModel.showNotificationDialog.collectAsStateWithLifecycle()
    val showBillDialog by viewModel.showBillDialog.collectAsStateWithLifecycle()
    val editingBill by viewModel.editingBill.collectAsStateWithLifecycle()

    val showReceiptScanner by viewModel.showReceiptScanner.collectAsStateWithLifecycle()
    val isExtractingReceipt by viewModel.isExtractingReceipt.collectAsStateWithLifecycle()
    val extractedReceipt by viewModel.extractedReceipt.collectAsStateWithLifecycle()
    val receiptScanError by viewModel.receiptScanError.collectAsStateWithLifecycle()

    val walletBalances by viewModel.walletBalances.collectAsStateWithLifecycle()
    val totalNetWorth by viewModel.totalNetWorth.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()

    val chatMessages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isChatLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val selectedModel by chatViewModel.selectedModel.collectAsStateWithLifecycle()
    val chatInputText by chatViewModel.inputText.collectAsStateWithLifecycle()

    val showAddEditSheet by viewModel.showAddEditSheet.collectAsStateWithLifecycle()
    val editingTransaction by viewModel.editingTransaction.collectAsStateWithLifecycle()
    val showBudgetDialog by viewModel.showBudgetDialog.collectAsStateWithLifecycle()
    val editingBudget by viewModel.editingBudget.collectAsStateWithLifecycle()

    val showWalletDialog by viewModel.showWalletDialog.collectAsStateWithLifecycle()
    val editingAccount by viewModel.editingAccount.collectAsStateWithLifecycle()
    val editingAccountLiveBalance by viewModel.editingAccountLiveBalance.collectAsStateWithLifecycle()
    val showResetNetWorthDialog by viewModel.showResetNetWorthDialog.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Notification Permission Launcher for Android 13+ (API 33+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    viewModel.checkAlertsAndBills()
                }
            }
        )
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // If app is currently locked with PIN, display Lock Screen exclusively
    if (isAppLocked) {
        AppLockScreen(
            title = "DompetZu Terkunci",
            subtitle = "Masukkan 4 digit PIN sandi untuk mengakses",
            onPinEntered = { pin -> viewModel.unlockWithPin(pin) },
            onSuccess = { /* Unlocked successfully */ }
        )
        return
    }

    // BackHandler for secondary screens
    if (currentTab != NavTab.HOME) {
        BackHandler {
            viewModel.setTab(NavTab.HOME)
        }
    }

    val navItems = listOf(
        NavItem(NavTab.HOME, Icons.Default.Home, Icons.Outlined.Home, "Beranda"),
        NavItem(NavTab.TRANSACTIONS, Icons.Default.ReceiptLong, Icons.Outlined.ReceiptLong, "Transaksi"),
        NavItem(NavTab.REPORT, Icons.Default.Assessment, Icons.Outlined.Assessment, "Laporan"),
        NavItem(NavTab.BUDGET, Icons.Default.PieChart, Icons.Outlined.PieChart, "Anggaran"),
        NavItem(NavTab.CHAT, Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome, "Tanya AI")
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DompetZu",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    // Global Amount Visibility Toggle (Eye icon)
                    IconButton(
                        onClick = { viewModel.toggleAmountVisibility() },
                        modifier = Modifier.testTag("toggle_amount_visibility_topbar")
                    ) {
                        Icon(
                            imageVector = if (isAmountHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isAmountHidden) "Tampilkan Nominal" else "Sembunyikan Nominal",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Notification Bell with Unread Count Badge
                    IconButton(
                        onClick = { viewModel.openNotificationDialog() },
                        modifier = Modifier.testTag("notification_center_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationCount > 0) {
                                    Badge(
                                        containerColor = ExpenseRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unreadNotificationCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (unreadNotificationCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Pemberitahuan & Peringatan",
                                tint = if (unreadNotificationCount > 0) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Security Settings Icon
                    IconButton(
                        onClick = { viewModel.openSecurityDialog() },
                        modifier = Modifier.testTag("security_settings_button")
                    ) {
                        Icon(
                            imageVector = if (isLockEnabled) Icons.Default.Security else Icons.Default.LockOpen,
                            contentDescription = "Pengaturan Sandi & Keamanan",
                            tint = if (isLockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = currentTab == item.tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(item.tab) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(top = 10.dp)

        when (currentTab) {
            NavTab.HOME -> {
                HomeScreen(
                    report = report,
                    recentTransactions = allTransactions,
                    walletBalances = walletBalances,
                    totalNetWorth = totalNetWorth,
                    isAmountHidden = isAmountHidden,
                    onToggleAmountVisibility = { viewModel.toggleAmountVisibility() },
                    onScanReceipt = { viewModel.openReceiptScanner() },
                    onResetNetWorth = { viewModel.openResetNetWorthDialog() },
                    onNavigateTab = { tab -> viewModel.setTab(tab) },
                    onAddTransaction = { viewModel.openAddTransaction(it) },
                    onTransactionClick = { viewModel.openEditTransaction(it) },
                    onAddWallet = { viewModel.openAddWallet() },
                    onEditWallet = { walletName -> viewModel.openEditWalletByName(walletName) },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    modifier = contentModifier
                )
            }
            NavTab.TRANSACTIONS -> {
                TransactionsScreen(
                    transactions = displayTransactions,
                    searchQuery = searchQuery,
                    filterType = filterType,
                    isAmountHidden = isAmountHidden,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onFilterChange = { viewModel.setFilterType(it) },
                    onAddTransaction = { viewModel.openAddTransaction(it) },
                    onTransactionClick = { viewModel.openEditTransaction(it) },
                    onScanReceipt = { viewModel.openReceiptScanner() },
                    modifier = contentModifier
                )
            }
            NavTab.REPORT -> {
                MonthlyReportScreen(
                    report = report,
                    transactions = currentMonthTransactions,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    modifier = contentModifier
                )
            }
            NavTab.BUDGET -> {
                val currentMonthBudgets = budgets.filter {
                    it.month == report.month && it.year == report.year
                }
                BudgetScreen(
                    report = report,
                    budgets = currentMonthBudgets,
                    bills = allBills,
                    onAddBudget = { viewModel.openAddBudget() },
                    onEditBudget = { viewModel.openEditBudget(it) },
                    onDeleteBudget = { viewModel.deleteBudget(it) },
                    onAddBill = { viewModel.openAddBill() },
                    onEditBill = { viewModel.openEditBill(it) },
                    onDeleteBill = { viewModel.deleteBill(it) },
                    onPayBill = { viewModel.payRecurringBill(it) },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    modifier = contentModifier
                )
            }
            NavTab.CHAT -> {
                ChatScreen(
                    messages = chatMessages,
                    isLoading = isChatLoading,
                    selectedModel = selectedModel,
                    inputText = chatInputText,
                    monthlyReport = report,
                    onInputChanged = { chatViewModel.updateInputText(it) },
                    onSendMessage = { text, context -> chatViewModel.sendMessage(text, context) },
                    onSelectModel = { chatViewModel.selectModel(it) },
                    onClearChat = { chatViewModel.clearHistory() },
                    modifier = contentModifier
                )
            }
        }

        // Add/Edit Transaction BottomSheet
        if (showAddEditSheet) {
            AddEditTransactionSheet(
                sheetState = sheetState,
                existingTransaction = editingTransaction,
                availableWallets = allAccounts.map { it.name },
                onDismiss = { viewModel.closeAddEditSheet() },
                onSave = { title, amount, type, category, wallet, dateMillis, note ->
                    viewModel.saveTransaction(title, amount, type, category, wallet, dateMillis, note)
                },
                onDelete = { tx ->
                    viewModel.deleteTransaction(tx)
                    viewModel.closeAddEditSheet()
                }
            )
        }

        // Add/Edit Budget Dialog
        if (showBudgetDialog) {
            AddEditBudgetDialog(
                budget = editingBudget,
                onDismiss = { viewModel.closeBudgetDialog() },
                onSave = { category, amount, thresholdPercent, alertEnabled ->
                    viewModel.saveBudget(category, amount, thresholdPercent, alertEnabled)
                }
            )
        }

        // Add/Edit Recurring Bill Dialog
        if (showBillDialog) {
            AddEditRecurringBillDialog(
                bill = editingBill,
                onDismiss = { viewModel.closeBillDialog() },
                onSave = { title, amount, category, wallet, dueDay, freq, reminder, note ->
                    viewModel.saveBill(title, amount, category, wallet, dueDay, freq, reminder, note)
                }
            )
        }

        // Notification Center Dialog
        if (showNotificationDialog) {
            NotificationCenterDialog(
                notifications = allNotifications,
                bills = allBills,
                onDismiss = { viewModel.closeNotificationDialog() },
                onMarkAllRead = { viewModel.markAllNotificationsRead() },
                onMarkRead = { viewModel.markNotificationRead(it) },
                onDeleteNotification = { viewModel.deleteNotification(it) },
                onClearAll = { viewModel.clearAllNotifications() },
                onPayBill = { bill -> viewModel.payRecurringBill(bill) }
            )
        }

        // Camera & Photo Receipt Scanner Dialog
        if (showReceiptScanner) {
            ReceiptScannerDialog(
                isExtracting = isExtractingReceipt,
                extractedReceipt = extractedReceipt,
                error = receiptScanError,
                onDismiss = { viewModel.closeReceiptScanner() },
                onScanBitmap = { bitmap -> viewModel.scanReceiptImage(bitmap) },
                onApplyReceipt = { receipt -> viewModel.applyReceiptToNewTransaction(receipt) }
            )
        }

        // Security Settings Dialog
        if (showSecurityDialog) {
            SecuritySettingsDialog(
                isLockEnabled = isLockEnabled,
                onDismiss = { viewModel.closeSecurityDialog() },
                onSetPin = { newPin -> viewModel.setupNewPin(newPin) },
                onVerifyPin = { pin -> viewModel.verifyPin(pin) },
                onDisableLock = { pin -> viewModel.disableAppLock(pin) },
                onChangePin = { oldPin, newPin -> viewModel.changeAppPin(oldPin, newPin) },
                onLockNow = {
                    viewModel.closeSecurityDialog()
                    viewModel.lockApp()
                }
            )
        }

        // Add/Edit Wallet & Saldo Dialog
        if (showWalletDialog) {
            AddEditWalletDialog(
                existingAccount = editingAccount,
                currentLiveBalance = editingAccountLiveBalance,
                onDismiss = { viewModel.closeWalletDialog() },
                onSaveAccount = { name, type, balance, colorHex, iconKey, clearTx ->
                    viewModel.saveWallet(name, type, balance, colorHex, iconKey, clearTx)
                },
                onDeleteAccount = { accountId ->
                    viewModel.deleteWallet(accountId)
                }
            )
        }

        // Reset & Empty Net Worth Dialog
        if (showResetNetWorthDialog) {
            com.example.ui.screens.ResetNetWorthDialog(
                currentTotalNetWorth = totalNetWorth,
                accounts = allAccounts,
                onDismiss = { viewModel.closeResetNetWorthDialog() },
                onResetBalance = { targetBalance, targetWallet, clearTx ->
                    viewModel.resetTotalNetWorth(targetBalance, targetWallet, clearTx)
                }
            )
        }
    }
}
