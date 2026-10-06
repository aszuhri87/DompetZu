package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AppNotificationEntity
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.DefaultCategories
import com.example.data.model.MonthlyReportData
import com.example.data.model.RecurringBillEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.data.security.ThemeMode
import com.example.data.util.MonthlyReportCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class NavTab(val title: String) {
    HOME("Beranda"),
    TRANSACTIONS("Transaksi"),
    REPORT("Laporan"),
    BUDGET("Anggaran"),
    CHAT("Tanya AI")
}

data class WalletBalance(
    val walletName: String,
    val balance: Double
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    private val securityManager = com.example.data.security.SecurityManager.getInstance(application)
    private val geminiRepository = com.example.data.gemini.GeminiRepository()

    private val _isLockEnabled = MutableStateFlow(securityManager.isLockEnabled)
    val isLockEnabled: StateFlow<Boolean> = _isLockEnabled.asStateFlow()

    private val _isAppLocked = MutableStateFlow(securityManager.hasPinSet())
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _isAmountHidden = MutableStateFlow(securityManager.isAmountHidden)
    val isAmountHidden: StateFlow<Boolean> = _isAmountHidden.asStateFlow()

    private val _themeMode = MutableStateFlow(securityManager.themeMode)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        securityManager.themeMode = mode
    }

    fun toggleNextThemeMode() {
        val nextMode = if (_themeMode.value == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(nextMode)
    }

    fun toggleAmountVisibility() {
        val newVal = !_isAmountHidden.value
        _isAmountHidden.value = newVal
        securityManager.isAmountHidden = newVal
    }

    private val _showReceiptScanner = MutableStateFlow(false)
    val showReceiptScanner: StateFlow<Boolean> = _showReceiptScanner.asStateFlow()

    private val _isExtractingReceipt = MutableStateFlow(false)
    val isExtractingReceipt: StateFlow<Boolean> = _isExtractingReceipt.asStateFlow()

    private val _extractedReceipt = MutableStateFlow<com.example.data.gemini.ParsedReceipt?>(null)
    val extractedReceipt: StateFlow<com.example.data.gemini.ParsedReceipt?> = _extractedReceipt.asStateFlow()

    private val _receiptScanError = MutableStateFlow<String?>(null)
    val receiptScanError: StateFlow<String?> = _receiptScanError.asStateFlow()

    fun openReceiptScanner() {
        _extractedReceipt.value = null
        _receiptScanError.value = null
        _showReceiptScanner.value = true
    }

    fun closeReceiptScanner() {
        _showReceiptScanner.value = false
        _extractedReceipt.value = null
        _receiptScanError.value = null
    }

    fun scanReceiptImage(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _isExtractingReceipt.value = true
            _receiptScanError.value = null
            val result = geminiRepository.extractReceipt(bitmap)
            _isExtractingReceipt.value = false
            result.onSuccess {
                _extractedReceipt.value = it
            }.onFailure {
                _receiptScanError.value = it.message ?: "Gagal mengekstrak struk."
            }
        }
    }

    fun applyReceiptToNewTransaction(receipt: com.example.data.gemini.ParsedReceipt) {
        _editingTransaction.value = TransactionEntity(
            title = receipt.merchant,
            amount = receipt.amount,
            type = receipt.type,
            category = receipt.category,
            walletName = receipt.walletName,
            dateMillis = receipt.dateMillis,
            note = receipt.note
        )
        closeReceiptScanner()
        _showAddEditSheet.value = true
    }

    private val _showSecurityDialog = MutableStateFlow(false)
    val showSecurityDialog: StateFlow<Boolean> = _showSecurityDialog.asStateFlow()

    private val calendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH) + 1)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow("ALL") // "ALL", "EXPENSE", "INCOME"
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    private val _showAddEditSheet = MutableStateFlow(false)
    val showAddEditSheet: StateFlow<Boolean> = _showAddEditSheet.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)
    val editingTransaction: StateFlow<TransactionEntity?> = _editingTransaction.asStateFlow()

    private val _showBudgetDialog = MutableStateFlow(false)
    val showBudgetDialog: StateFlow<Boolean> = _showBudgetDialog.asStateFlow()

    private val _editingBudget = MutableStateFlow<CategoryBudgetEntity?>(null)
    val editingBudget: StateFlow<CategoryBudgetEntity?> = _editingBudget.asStateFlow()

    private val _showShareSheet = MutableStateFlow(false)
    val showShareSheet: StateFlow<Boolean> = _showShareSheet.asStateFlow()

    // Recurring Bills & Notifications State
    private val _showNotificationDialog = MutableStateFlow(false)
    val showNotificationDialog: StateFlow<Boolean> = _showNotificationDialog.asStateFlow()

    private val _showBillDialog = MutableStateFlow(false)
    val showBillDialog: StateFlow<Boolean> = _showBillDialog.asStateFlow()

    private val _editingBill = MutableStateFlow<RecurringBillEntity?>(null)
    val editingBill: StateFlow<RecurringBillEntity?> = _editingBill.asStateFlow()

    // Wallet / Saldo Management
    private val _showWalletDialog = MutableStateFlow(false)
    val showWalletDialog: StateFlow<Boolean> = _showWalletDialog.asStateFlow()
    private val _editingAccount = MutableStateFlow<AccountEntity?>(null)
    val editingAccount: StateFlow<AccountEntity?> = _editingAccount.asStateFlow()
    private val _editingAccountLiveBalance = MutableStateFlow(0.0)
    val editingAccountLiveBalance: StateFlow<Double> = _editingAccountLiveBalance.asStateFlow()

    private val _showResetNetWorthDialog = MutableStateFlow(false)
    val showResetNetWorthDialog: StateFlow<Boolean> = _showResetNetWorthDialog.asStateFlow()

    // Savings Goals UI State
    private val _showSavingsGoalDialog = MutableStateFlow(false)
    val showSavingsGoalDialog: StateFlow<Boolean> = _showSavingsGoalDialog.asStateFlow()

    private val _editingSavingsGoal = MutableStateFlow<SavingsGoalEntity?>(null)
    val editingSavingsGoal: StateFlow<SavingsGoalEntity?> = _editingSavingsGoal.asStateFlow()

    fun openResetNetWorthDialog() {
        _showResetNetWorthDialog.value = true
    }

    fun closeResetNetWorthDialog() {
        _showResetNetWorthDialog.value = false
    }

    fun openAddSavingsGoal() {
        _editingSavingsGoal.value = null
        _showSavingsGoalDialog.value = true
    }

    fun openEditSavingsGoal(goal: SavingsGoalEntity) {
        _editingSavingsGoal.value = goal
        _showSavingsGoalDialog.value = true
    }

    fun closeSavingsGoalDialog() {
        _showSavingsGoalDialog.value = false
        _editingSavingsGoal.value = null
    }

    val allTransactions: StateFlow<List<TransactionEntity>>
    val allBudgets: StateFlow<List<CategoryBudgetEntity>>
    val allCategories: StateFlow<List<com.example.data.model.CategoryEntity>>
    val allAccounts: StateFlow<List<com.example.data.model.AccountEntity>>
    val allBills: StateFlow<List<RecurringBillEntity>>
    val allNotifications: StateFlow<List<AppNotificationEntity>>
    val unreadNotificationCount: StateFlow<Int>
    val allSavingsGoals: StateFlow<List<SavingsGoalEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(
            database.financeDao(),
            database.categoryDao(),
            database.accountDao(),
            database.recurringBillDao(),
            database.notificationDao()
        )

        allTransactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allBudgets = database.financeDao().getAllBudgets().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allCategories = repository.allCategories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allAccounts = repository.allAccounts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allBills = repository.allBills.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allNotifications = repository.allNotifications.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        unreadNotificationCount = repository.unreadNotificationCount.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        allSavingsGoals = repository.allSavingsGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.seedDefaultsIfEmpty()
            repository.checkAllAlertsAndBills(application)
        }
    }

    // Filtered transactions for the Transactions Screen
    val displayTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _searchQuery,
        _filterType,
        _selectedMonth,
        _selectedYear
    ) { txList, query, typeFilter, month, year ->
        txList.filter { tx ->
            val matchQuery = query.isBlank() ||
                    tx.title.contains(query, ignoreCase = true) ||
                    tx.category.contains(query, ignoreCase = true) ||
                    tx.note.contains(query, ignoreCase = true) ||
                    tx.walletName.contains(query, ignoreCase = true)

            val matchType = when (typeFilter) {
                "EXPENSE" -> tx.type == TransactionType.EXPENSE.name
                "INCOME" -> tx.type == TransactionType.INCOME.name
                else -> true
            }

            matchQuery && matchType
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current month transactions (unfiltered by search, matching selected month & year)
    val currentMonthTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _selectedMonth,
        _selectedYear
    ) { txList, month, year ->
        val cal = Calendar.getInstance()
        txList.filter { tx ->
            cal.timeInMillis = tx.dateMillis
            (cal.get(Calendar.MONTH) + 1) == month && cal.get(Calendar.YEAR) == year
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Automated Monthly Report state
    val monthlyReport: StateFlow<MonthlyReportData> = combine(
        allTransactions,
        allBudgets,
        allAccounts,
        _selectedMonth,
        _selectedYear
    ) { txList, budgetList, accounts, month, year ->
        val cal = Calendar.getInstance()

        // Filter current month transactions
        val currentMonthTx = txList.filter { tx ->
            cal.timeInMillis = tx.dateMillis
            (cal.get(Calendar.MONTH) + 1) == month && cal.get(Calendar.YEAR) == year
        }

        // Filter previous month transactions
        val prevCal = Calendar.getInstance()
        prevCal.set(Calendar.YEAR, year)
        prevCal.set(Calendar.MONTH, month - 1)
        prevCal.add(Calendar.MONTH, -1)
        val prevMonth = prevCal.get(Calendar.MONTH) + 1
        val prevYear = prevCal.get(Calendar.YEAR)

        val previousMonthTx = txList.filter { tx ->
            cal.timeInMillis = tx.dateMillis
            (cal.get(Calendar.MONTH) + 1) == prevMonth && cal.get(Calendar.YEAR) == prevYear
        }

        val currentMonthBudgets = budgetList.filter { it.month == month && it.year == year }

        val calculatedNetWorth = accounts.sumOf { account ->
            val accountTx = txList.filter { it.walletName.equals(account.name, ignoreCase = true) }
            val inc = accountTx.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
            val exp = accountTx.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
            (account.initialBalance ?: 0.0) + inc - exp
        }

        MonthlyReportCalculator.calculateReport(
            month = month,
            year = year,
            currentTransactions = currentMonthTx,
            budgets = currentMonthBudgets,
            previousTransactions = previousMonthTx,
            totalNetWorth = calculatedNetWorth
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyReportCalculator.calculateReport(
            month = calendar.get(Calendar.MONTH) + 1,
            year = calendar.get(Calendar.YEAR),
            currentTransactions = emptyList(),
            budgets = emptyList(),
            previousTransactions = emptyList(),
            totalNetWorth = 0.0
        )
    )

    // Wallet balances (dynamic calculation from all transactions)
    val walletBalances: StateFlow<List<WalletBalance>> = combine(
        allTransactions,
        allAccounts
    ) { txList, accounts ->
        accounts.map { account ->
            val accountTx = txList.filter { it.walletName.equals(account.name, ignoreCase = true) }
            val income = accountTx.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
            val expense = accountTx.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
            val calculatedBalance = (account.initialBalance ?: 0.0) + income - expense

            WalletBalance(
                walletName = account.name,
                balance = calculatedBalance
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Total net worth across all wallets
    val totalNetWorth: StateFlow<Double> = walletBalances.combine(
        MutableStateFlow(Unit)
    ) { balances, _ ->
        balances.sumOf { it.balance }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun checkAlertsAndBills() {
        viewModelScope.launch {
            repository.checkAllAlertsAndBills(getApplication())
        }
    }

    fun setTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: String) {
        _filterType.value = type
    }

    fun previousMonth() {
        if (_selectedMonth.value == 1) {
            _selectedMonth.value = 12
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
    }

    fun nextMonth() {
        if (_selectedMonth.value == 12) {
            _selectedMonth.value = 1
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
    }

    fun setMonthYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun openAddTransaction(initialType: TransactionType = TransactionType.EXPENSE) {
        _editingTransaction.value = null
        _showAddEditSheet.value = true
    }

    fun openEditTransaction(tx: TransactionEntity) {
        _editingTransaction.value = tx
        _showAddEditSheet.value = true
    }

    fun closeAddEditSheet() {
        _showAddEditSheet.value = false
        _editingTransaction.value = null
    }

    // Wallet CRUD
    fun openAddWallet() {
        _editingAccount.value = null
        _editingAccountLiveBalance.value = 0.0
        _showWalletDialog.value = true
    }

    fun openEditWallet(account: AccountEntity) {
        _editingAccount.value = account
        val initial = account.initialBalance ?: 0.0
        val txList = allTransactions.value.filter { it.walletName.equals(account.name, ignoreCase = true) }
        val income = txList.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
        val expense = txList.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        _editingAccountLiveBalance.value = initial + income - expense
        _showWalletDialog.value = true
    }

    fun openEditWalletByName(walletName: String) {
        val found = allAccounts.value.find { it.name.equals(walletName, ignoreCase = true) }
        val initial = found?.initialBalance ?: 0.0
        val txList = allTransactions.value.filter { it.walletName.equals(walletName, ignoreCase = true) }
        val income = txList.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
        val expense = txList.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        _editingAccountLiveBalance.value = initial + income - expense

        if (found != null) {
            _editingAccount.value = found
        } else {
            _editingAccount.value = AccountEntity(name = walletName, type = "BANK", initialBalance = 0.0)
        }
        _showWalletDialog.value = true
    }

    fun closeWalletDialog() {
        _showWalletDialog.value = false
        _editingAccount.value = null
        _editingAccountLiveBalance.value = 0.0
    }

    fun saveWallet(
        name: String,
        type: String,
        targetBalance: Double,
        colorHex: Long,
        iconKey: String,
        clearWalletTransactions: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _editingAccount.value
            if (current != null && current.id != 0L) {
                val oldName = current.name
                if (clearWalletTransactions) {
                    repository.deleteTransactionsByWalletName(oldName)
                    repository.updateAccount(
                        current.copy(
                            name = name,
                            type = type,
                            initialBalance = targetBalance,
                            colorHex = colorHex,
                            iconKey = iconKey
                        )
                    )
                } else {
                    val txList = allTransactions.value.filter { it.walletName.equals(oldName, ignoreCase = true) }
                    val income = txList.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
                    val expense = txList.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
                    val netFlow = income - expense
                    val adjustedInitialBalance = targetBalance - netFlow

                    repository.updateAccount(
                        current.copy(
                            name = name,
                            type = type,
                            initialBalance = adjustedInitialBalance,
                            colorHex = colorHex,
                            iconKey = iconKey
                        )
                    )
                }
            } else {
                repository.insertAccount(
                    AccountEntity(
                        name = name,
                        type = type,
                        initialBalance = targetBalance,
                        colorHex = colorHex,
                        iconKey = iconKey
                    )
                )
            }
            closeWalletDialog()
        }
    }

    fun resetWalletBalanceToZero(accountId: Long, clearTransactions: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = allAccounts.value.find { it.id == accountId } ?: return@launch
            if (clearTransactions) {
                repository.deleteTransactionsByWalletName(account.name)
                repository.updateAccountInitialBalance(accountId, 0.0)
            } else {
                val txList = allTransactions.value.filter { it.walletName.equals(account.name, ignoreCase = true) }
                val income = txList.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
                val expense = txList.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
                val netFlow = income - expense
                repository.updateAccountInitialBalance(accountId, 0.0 - netFlow)
            }
            closeWalletDialog()
        }
    }

    fun resetAllWalletsAndBalances(clearAllTransactions: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (clearAllTransactions) {
                repository.deleteAllTransactions()
            }
            repository.resetAllAccountBalancesToZero()
            closeWalletDialog()
        }
    }

    fun resetTotalNetWorth(
        targetTotalBalance: Double,
        targetWalletName: String? = null,
        clearAllTransactions: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (clearAllTransactions) {
                repository.deleteAllTransactions()
            }

            val accounts = allAccounts.value
            if (accounts.isEmpty()) {
                repository.insertAccount(
                    AccountEntity(
                        name = "Dompet Utama",
                        type = "CASH",
                        initialBalance = targetTotalBalance,
                        colorHex = 0xFF10B981,
                        iconKey = "account_balance_wallet"
                    )
                )
            } else {
                if (targetTotalBalance == 0.0) {
                    if (clearAllTransactions) {
                        repository.resetAllAccountBalancesToZero()
                    } else {
                        accounts.forEach { acc ->
                            val txList = allTransactions.value.filter { it.walletName.equals(acc.name, ignoreCase = true) }
                            val income = txList.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
                            val expense = txList.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
                            val netFlow = income - expense
                            repository.updateAccountInitialBalance(acc.id, 0.0 - netFlow)
                        }
                    }
                } else {
                    val selectedAcc = if (targetWalletName != null) {
                        accounts.find { it.name.equals(targetWalletName, ignoreCase = true) } ?: accounts.first()
                    } else {
                        accounts.first()
                    }

                    accounts.forEach { acc ->
                        if (acc.id == selectedAcc.id) {
                            if (clearAllTransactions) {
                                repository.updateAccountInitialBalance(acc.id, targetTotalBalance)
                            } else {
                                val txList = allTransactions.value.filter { it.walletName.equals(acc.name, ignoreCase = true) }
                                val income = txList.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
                                val expense = txList.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
                                val netFlow = income - expense
                                repository.updateAccountInitialBalance(acc.id, targetTotalBalance - netFlow)
                            }
                        } else {
                            if (clearAllTransactions) {
                                repository.updateAccountInitialBalance(acc.id, 0.0)
                            } else {
                                val txList = allTransactions.value.filter { it.walletName.equals(acc.name, ignoreCase = true) }
                                val income = txList.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
                                val expense = txList.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
                                val netFlow = income - expense
                                repository.updateAccountInitialBalance(acc.id, 0.0 - netFlow)
                            }
                        }
                    }
                }
            }
            closeResetNetWorthDialog()
        }
    }

    fun deleteWallet(accountId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAccountById(accountId)
            closeWalletDialog()
        }
    }

    fun saveTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        walletName: String,
        dateMillis: Long,
        note: String
    ) {
        val current = _editingTransaction.value
        _editingTransaction.value = null // Clear state immediately so subsequent calls are treated as new insertions

        viewModelScope.launch(Dispatchers.IO) {
            val cleanWallet = walletName.trim()
            val cleanCategory = category.trim()
            val cleanTitle = title.trim()
            val catId = allCategories.value.find { it.name.trim().equals(cleanCategory, ignoreCase = true) }?.id ?: 0L
            var accId = allAccounts.value.find { it.name.trim().equals(cleanWallet, ignoreCase = true) }?.id ?: 0L
            if (accId == 0L && cleanWallet.isNotBlank()) {
                accId = repository.insertAccount(
                    AccountEntity(
                        name = cleanWallet,
                        type = "CASH",
                        initialBalance = 0.0,
                        colorHex = 0xFF10B981,
                        iconKey = "account_balance_wallet"
                    )
                )
            }

            if (current != null && current.id > 0L) {
                repository.updateTransaction(
                    current.copy(
                        title = cleanTitle,
                        amount = amount,
                        type = type.name,
                        category = cleanCategory,
                        walletName = cleanWallet,
                        dateMillis = dateMillis,
                        note = note.trim(),
                        categoryId = catId,
                        accountId = accId
                    ),
                    context = getApplication()
                )
            } else {
                repository.insertTransaction(
                    TransactionEntity(
                        id = 0L, // Force id = 0L so SQLite auto-generates a new ID and NEVER overwrites existing records!
                        title = cleanTitle,
                        amount = amount,
                        type = type.name,
                        category = cleanCategory,
                        walletName = cleanWallet,
                        dateMillis = dateMillis,
                        note = note.trim(),
                        categoryId = catId,
                        accountId = accId
                    ),
                    context = getApplication()
                )
            }

            // Sync selected month and year so the saved transaction is immediately visible in the active month view
            val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
            _selectedMonth.value = cal.get(Calendar.MONTH) + 1
            _selectedYear.value = cal.get(Calendar.YEAR)

            closeAddEditSheet()
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx, context = getApplication())
        }
    }

    fun deleteTransactionById(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id, context = getApplication())
        }
    }

    fun openAddBudget() {
        _editingBudget.value = null
        _showBudgetDialog.value = true
    }

    fun openEditBudget(budget: CategoryBudgetEntity) {
        _editingBudget.value = budget
        _showBudgetDialog.value = true
    }

    fun closeBudgetDialog() {
        _showBudgetDialog.value = false
        _editingBudget.value = null
    }

    fun saveBudget(
        category: String,
        amount: Double,
        thresholdPercent: Int = 80,
        alertEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            val current = _editingBudget.value
            val month = _selectedMonth.value
            val year = _selectedYear.value

            val entity = if (current != null) {
                current.copy(
                    category = category,
                    budgetAmount = amount,
                    thresholdPercent = thresholdPercent,
                    alertEnabled = alertEnabled
                )
            } else {
                CategoryBudgetEntity(
                    category = category,
                    month = month,
                    year = year,
                    budgetAmount = amount,
                    thresholdPercent = thresholdPercent,
                    alertEnabled = alertEnabled
                )
            }
            repository.upsertBudget(entity, context = getApplication())
            closeBudgetDialog()
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudgetById(id, context = getApplication())
        }
    }

    // Recurring Bills
    fun openAddBill() {
        _editingBill.value = null
        _showBillDialog.value = true
    }

    fun openEditBill(bill: RecurringBillEntity) {
        _editingBill.value = bill
        _showBillDialog.value = true
    }

    fun closeBillDialog() {
        _showBillDialog.value = false
        _editingBill.value = null
    }

    fun saveBill(
        title: String,
        amount: Double,
        category: String,
        walletName: String,
        dueDay: Int,
        frequency: String,
        reminderDaysBefore: Int,
        note: String
    ) {
        viewModelScope.launch {
            val current = _editingBill.value
            if (current != null) {
                repository.updateBill(
                    current.copy(
                        title = title,
                        amount = amount,
                        category = category,
                        walletName = walletName,
                        dueDay = dueDay,
                        frequency = frequency,
                        reminderDaysBefore = reminderDaysBefore,
                        note = note
                    )
                )
            } else {
                repository.insertBill(
                    RecurringBillEntity(
                        title = title,
                        amount = amount,
                        category = category,
                        walletName = walletName,
                        dueDay = dueDay,
                        frequency = frequency,
                        reminderDaysBefore = reminderDaysBefore,
                        note = note
                    )
                )
            }
            closeBillDialog()
            repository.checkAllAlertsAndBills(getApplication())
        }
    }

    fun deleteBill(id: Long) {
        viewModelScope.launch {
            repository.deleteBillById(id)
        }
    }

    fun payRecurringBill(bill: RecurringBillEntity) {
        viewModelScope.launch {
            repository.payRecurringBill(
                bill = bill,
                paidDateMillis = System.currentTimeMillis(),
                context = getApplication()
            )
        }
    }

    // Notifications
    fun openNotificationDialog() {
        _showNotificationDialog.value = true
    }

    fun closeNotificationDialog() {
        _showNotificationDialog.value = false
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun openShareSheet() {
        _showShareSheet.value = true
    }

    fun closeShareSheet() {
        _showShareSheet.value = false
    }

    fun unlockWithPin(pin: String): Boolean {
        val valid = securityManager.verifyPin(pin)
        if (valid) {
            _isAppLocked.value = false
        }
        return valid
    }

    fun lockApp() {
        if (securityManager.hasPinSet()) {
            _isAppLocked.value = true
        }
    }

    fun openSecurityDialog() {
        _showSecurityDialog.value = true
    }

    fun closeSecurityDialog() {
        _showSecurityDialog.value = false
    }

    fun setupNewPin(pin: String) {
        securityManager.setPin(pin)
        _isLockEnabled.value = true
        _isAppLocked.value = false
    }

    fun verifyPin(pin: String): Boolean {
        return securityManager.verifyPin(pin)
    }

    fun disableAppLock(currentPin: String): Boolean {
        val success = securityManager.disableLock(currentPin)
        if (success) {
            _isLockEnabled.value = false
            _isAppLocked.value = false
        }
        return success
    }

    fun changeAppPin(oldPin: String, newPin: String): Boolean {
        return securityManager.changePin(oldPin, newPin)
    }

    fun saveSavingsGoal(
        title: String,
        targetAmount: Double,
        currentAmount: Double,
        category: String,
        targetDateMillis: Long,
        note: String
    ) {
        viewModelScope.launch {
            val id = _editingSavingsGoal.value?.id ?: 0L
            val goal = SavingsGoalEntity(
                id = id,
                title = title,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                category = category,
                targetDateMillis = targetDateMillis,
                note = note
            )
            repository.upsertSavingsGoal(goal)
            closeSavingsGoalDialog()
        }
    }

    fun deleteSavingsGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteSavingsGoalById(id)
        }
    }
}
