package com.example.data.repository

import android.content.Context
import com.example.data.dao.AccountDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.FinanceDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.RecurringBillDao
import com.example.data.model.AccountEntity
import com.example.data.model.AppNotificationEntity
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DefaultCategories
import com.example.data.model.RecurringBillEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.notification.AppNotificationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class FinanceRepository(
    private val financeDao: FinanceDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val recurringBillDao: RecurringBillDao,
    private val notificationDao: NotificationDao
) {

    val allTransactions: Flow<List<TransactionEntity>> = financeDao.getAllTransactions()
    val transactionsWithDetails: Flow<List<TransactionWithDetails>> = financeDao.getTransactionsWithDetails()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val allBills: Flow<List<RecurringBillEntity>> = recurringBillDao.getAllBills()
    val activeBills: Flow<List<RecurringBillEntity>> = recurringBillDao.getActiveBills()
    val allNotifications: Flow<List<AppNotificationEntity>> = notificationDao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = notificationDao.getUnreadCount()
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = financeDao.getAllSavingsGoals()

    fun getTransactionsForRange(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>> {
        return financeDao.getTransactionsBetween(startMillis, endMillis)
    }

    suspend fun upsertSavingsGoal(goal: SavingsGoalEntity): Long {
        return financeDao.upsertSavingsGoal(goal)
    }

    suspend fun deleteSavingsGoalById(id: Long) {
        financeDao.deleteSavingsGoalById(id)
    }

    suspend fun insertTransaction(transaction: TransactionEntity, context: Context? = null): Long {
        val id = financeDao.insertTransaction(transaction)
        if (transaction.type == TransactionType.EXPENSE.name) {
            val cal = Calendar.getInstance().apply { timeInMillis = transaction.dateMillis }
            val month = cal.get(Calendar.MONTH) + 1
            val year = cal.get(Calendar.YEAR)
            checkCategoryBudgetThreshold(transaction.category, month, year, context)
        }
        return id
    }

    suspend fun updateTransaction(transaction: TransactionEntity, context: Context? = null) {
        financeDao.updateTransaction(transaction)
        if (transaction.type == TransactionType.EXPENSE.name) {
            val cal = Calendar.getInstance().apply { timeInMillis = transaction.dateMillis }
            val month = cal.get(Calendar.MONTH) + 1
            val year = cal.get(Calendar.YEAR)
            checkCategoryBudgetThreshold(transaction.category, month, year, context)
        }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity, context: Context? = null) {
        financeDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long, context: Context? = null) {
        financeDao.deleteTransactionById(id)
    }

    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<CategoryBudgetEntity>> {
        return financeDao.getBudgetsForMonth(month, year)
    }

    suspend fun upsertBudget(budget: CategoryBudgetEntity, context: Context? = null): Long {
        val id = financeDao.upsertBudget(budget)
        if (budget.alertEnabled) {
            checkCategoryBudgetThreshold(budget.category, budget.month, budget.year, context)
        }
        return id
    }

    suspend fun deleteBudgetById(id: Long, context: Context? = null) {
        financeDao.deleteBudgetById(id)
    }

    // Category CRUD
    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    suspend fun deleteCategoryById(id: Long) {
        categoryDao.deleteCategoryById(id)
    }

    // Account CRUD
    suspend fun insertAccount(account: AccountEntity): Long {
        return accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: AccountEntity) {
        accountDao.deleteAccount(account)
    }

    suspend fun deleteAccountById(id: Long) {
        accountDao.deleteAccountById(id)
    }

    suspend fun updateAccountInitialBalance(id: Long, balance: Double) {
        accountDao.updateInitialBalance(id, balance)
    }

    suspend fun resetAllAccountBalancesToZero() {
        accountDao.resetAllBalancesToZero()
    }

    suspend fun deleteTransactionsByWalletName(walletName: String) {
        financeDao.deleteTransactionsByWalletName(walletName)
    }

    suspend fun deleteAllTransactions() {
        financeDao.deleteAllTransactions()
    }

    // Recurring Bills CRUD
    suspend fun insertBill(bill: RecurringBillEntity): Long {
        return recurringBillDao.insertBill(bill)
    }

    suspend fun updateBill(bill: RecurringBillEntity) {
        recurringBillDao.updateBill(bill)
    }

    suspend fun deleteBill(bill: RecurringBillEntity) {
        recurringBillDao.deleteBill(bill)
    }

    suspend fun deleteBillById(id: Long) {
        recurringBillDao.deleteBillById(id)
    }

    suspend fun payRecurringBill(bill: RecurringBillEntity, paidDateMillis: Long, context: Context? = null) {
        // Record as an expense transaction
        val transaction = TransactionEntity(
            title = "Bayar: ${bill.title}",
            amount = bill.amount,
            type = TransactionType.EXPENSE.name,
            category = bill.category,
            walletName = bill.walletName,
            dateMillis = paidDateMillis,
            note = "Pembayaran tagihan rutin: ${bill.title} (${bill.frequency})"
        )
        insertTransaction(transaction, context)

        // Update bill lastPaidDateMillis
        val updatedBill = bill.copy(lastPaidDateMillis = paidDateMillis)
        recurringBillDao.updateBill(updatedBill)

        // Save a system confirmation notification
        val localeId = Locale.forLanguageTag("id-ID")
        val formattedAmount = NumberFormat.getCurrencyInstance(localeId).format(bill.amount)
        notificationDao.insertNotification(
            AppNotificationEntity(
                title = "Tagihan Berhasil Dibayar",
                message = "Pembayaran untuk '${bill.title}' sebesar $formattedAmount telah dicatat di dompet ${bill.walletName}.",
                type = "SYSTEM",
                category = bill.category,
                relatedId = bill.id,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }

    // Notification operations
    suspend fun insertNotification(notification: AppNotificationEntity): Long {
        return notificationDao.insertNotification(notification)
    }

    suspend fun markNotificationRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun deleteNotification(id: Long) {
        notificationDao.deleteNotificationById(id)
    }

    suspend fun clearAllNotifications() {
        notificationDao.clearAllNotifications()
    }

    // Budget Threshold & Recurring Bill Alert Checkers
    suspend fun checkCategoryBudgetThreshold(
        category: String,
        month: Int,
        year: Int,
        context: Context?
    ) {
        val budgets = financeDao.getBudgetsForMonth(month, year).first()
        val budget = budgets.find { it.category.equals(category, ignoreCase = true) } ?: return

        if (!budget.alertEnabled || budget.budgetAmount <= 0) return

        // Calculate total spend for this category in the month
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val transactions = financeDao.getTransactionsBetween(startCal.timeInMillis, endCal.timeInMillis).first()
        val categoryExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE.name && it.category.equals(category, ignoreCase = true) }
            .sumOf { it.amount }

        val thresholdAmount = budget.budgetAmount * (budget.thresholdPercent / 100.0)
        val percentUsed = ((categoryExpenses / budget.budgetAmount) * 100).toInt()

        val localeId = Locale.forLanguageTag("id-ID")
        val formatter = NumberFormat.getCurrencyInstance(localeId)
        val formattedExpenses = formatter.format(categoryExpenses)
        val formattedBudget = formatter.format(budget.budgetAmount)

        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)

        if (categoryExpenses >= budget.budgetAmount) {
            // Budget Exceeded Alert
            val recentAlerts = notificationDao.getRecentByTypeAndCategory("BUDGET_EXCEEDED", category, oneDayAgo)
            if (recentAlerts.isEmpty()) {
                val title = "⚠️ Anggaran '$category' Melebihi Batas!"
                val message = "Pengeluaran telah mencapai $formattedExpenses ($percentUsed%) melampaui batas anggaran bulanan $formattedBudget."
                
                notificationDao.insertNotification(
                    AppNotificationEntity(
                        title = title,
                        message = message,
                        type = "BUDGET_EXCEEDED",
                        category = category,
                        relatedId = budget.id,
                        timestampMillis = System.currentTimeMillis()
                    )
                )

                if (context != null) {
                    AppNotificationManager.sendBudgetAlertNotification(
                        context = context,
                        notificationId = (category.hashCode() and 0x7FFFFFFF) + 1000,
                        title = title,
                        message = message,
                        isExceeded = true
                    )
                }
            }
        } else if (categoryExpenses >= thresholdAmount) {
            // Budget Warning Alert
            val recentAlerts = notificationDao.getRecentByTypeAndCategory("BUDGET_WARNING", category, oneDayAgo)
            if (recentAlerts.isEmpty()) {
                val title = "⚡ Peringatan Anggaran '$category'"
                val message = "Pengeluaran sudah mencapai $formattedExpenses ($percentUsed%) dari kuota anggaran $formattedBudget (ambang batas ${budget.thresholdPercent}%)."
                
                notificationDao.insertNotification(
                    AppNotificationEntity(
                        title = title,
                        message = message,
                        type = "BUDGET_WARNING",
                        category = category,
                        relatedId = budget.id,
                        timestampMillis = System.currentTimeMillis()
                    )
                )

                if (context != null) {
                    AppNotificationManager.sendBudgetAlertNotification(
                        context = context,
                        notificationId = (category.hashCode() and 0x7FFFFFFF) + 2000,
                        title = title,
                        message = message,
                        isExceeded = false
                    )
                }
            }
        }
    }

    suspend fun checkAllAlertsAndBills(context: Context?) {
        val today = Calendar.getInstance()
        val currentMonth = today.get(Calendar.MONTH) + 1
        val currentYear = today.get(Calendar.YEAR)
        val currentDay = today.get(Calendar.DAY_OF_MONTH)

        // 1. Check all budgets for current month
        val budgets = financeDao.getBudgetsForMonth(currentMonth, currentYear).first()
        for (budget in budgets) {
            if (budget.alertEnabled) {
                checkCategoryBudgetThreshold(budget.category, currentMonth, currentYear, context)
            }
        }

        // 2. Check all active recurring bills
        val activeBillsList = recurringBillDao.getActiveBills().first()
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        val localeId = Locale.forLanguageTag("id-ID")
        val formatter = NumberFormat.getCurrencyInstance(localeId)

        for (bill in activeBillsList) {
            // Check if already paid this month
            val isPaidThisMonth = if (bill.lastPaidDateMillis != null) {
                val paidCal = Calendar.getInstance().apply { timeInMillis = bill.lastPaidDateMillis }
                paidCal.get(Calendar.MONTH) + 1 == currentMonth && paidCal.get(Calendar.YEAR) == currentYear
            } else false

            if (isPaidThisMonth) continue

            val formattedAmount = formatter.format(bill.amount)
            val daysDiff = bill.dueDay - currentDay

            if (daysDiff < 0) {
                // Overdue
                val recentAlerts = notificationDao.getRecentByTypeAndRelatedId("BILL_OVERDUE", bill.id, oneDayAgo)
                if (recentAlerts.isEmpty()) {
                    val title = "🔴 Tagihan Terlambat: ${bill.title}"
                    val message = "Tagihan '${bill.title}' sebesar $formattedAmount jatuh tempo tanggal ${bill.dueDay} dan belum terbayar."
                    
                    notificationDao.insertNotification(
                        AppNotificationEntity(
                            title = title,
                            message = message,
                            type = "BILL_OVERDUE",
                            category = bill.category,
                            relatedId = bill.id,
                            timestampMillis = System.currentTimeMillis()
                        )
                    )

                    if (context != null) {
                        AppNotificationManager.sendBillReminderNotification(
                            context = context,
                            notificationId = (bill.id.toInt() * 100) + 3,
                            title = title,
                            message = message,
                            isOverdue = true
                        )
                    }
                }
            } else if (daysDiff <= bill.reminderDaysBefore) {
                // Due soon or today
                val recentAlerts = notificationDao.getRecentByTypeAndRelatedId("BILL_DUE", bill.id, oneDayAgo)
                if (recentAlerts.isEmpty()) {
                    val dueString = if (daysDiff == 0) "hari ini" else "dalam $daysDiff hari (tgl ${bill.dueDay})"
                    val title = "⏰ Pengingat Tagihan: ${bill.title}"
                    val message = "Tagihan '${bill.title}' sebesar $formattedAmount akan jatuh tempo $dueString."

                    notificationDao.insertNotification(
                        AppNotificationEntity(
                            title = title,
                            message = message,
                            type = "BILL_DUE",
                            category = bill.category,
                            relatedId = bill.id,
                            timestampMillis = System.currentTimeMillis()
                        )
                    )

                    if (context != null) {
                        AppNotificationManager.sendBillReminderNotification(
                            context = context,
                            notificationId = (bill.id.toInt() * 100) + 1,
                            title = title,
                            message = message,
                            isOverdue = false
                        )
                    }
                }
            }
        }
    }

    suspend fun checkAndSeedInitialData() {
        seedDefaultsIfEmpty()
    }

    // Seed defaults
    suspend fun seedDefaultsIfEmpty() {
        val existingCats = categoryDao.getAllCategories().first()
        if (existingCats.isEmpty()) {
            DefaultCategories.expenseCategories.forEach {
                categoryDao.insertCategory(
                    CategoryEntity(name = it.name, type = it.type.name, colorHex = it.colorHex, iconKey = it.iconKey)
                )
            }
            DefaultCategories.incomeCategories.forEach {
                categoryDao.insertCategory(
                    CategoryEntity(name = it.name, type = it.type.name, colorHex = it.colorHex, iconKey = it.iconKey)
                )
            }
        }

        val existingAccs = accountDao.getAllAccounts().first()
        if (existingAccs.isEmpty()) {
            val defaultAccs = listOf(
                AccountEntity(name = "Tunai", type = "CASH", initialBalance = 1000000.0, colorHex = 0xFF10B981, iconKey = "payments"),
                AccountEntity(name = "Rekening Bank", type = "BANK", initialBalance = 5000000.0, colorHex = 0xFF2563EB, iconKey = "account_balance"),
                AccountEntity(name = "E-Wallet", type = "E_WALLET", initialBalance = 500000.0, colorHex = 0xFF8B5CF6, iconKey = "account_balance_wallet")
            )
            defaultAccs.forEach { accountDao.insertAccount(it) }
        }

        val existingBills = recurringBillDao.getAllBills().first()
        if (existingBills.isEmpty()) {
            val defaultBills = listOf(
                RecurringBillEntity(
                    title = "Langganan WiFi & Internet",
                    amount = 350000.0,
                    category = "Tagihan & Utilitas",
                    walletName = "Rekening Bank",
                    dueDay = 10,
                    frequency = "BULANAN",
                    reminderDaysBefore = 3,
                    note = "IndiHome 50 Mbps"
                ),
                RecurringBillEntity(
                    title = "Listrik PLN Pascabayar",
                    amount = 450000.0,
                    category = "Tagihan & Utilitas",
                    walletName = "Rekening Bank",
                    dueDay = 18,
                    frequency = "BULANAN",
                    reminderDaysBefore = 3,
                    note = "Token / Tagihan PLN Bulanan"
                ),
                RecurringBillEntity(
                    title = "Langganan Streaming & Hiburan",
                    amount = 186000.0,
                    category = "Hiburan & Liburan",
                    walletName = "E-Wallet",
                    dueDay = 25,
                    frequency = "BULANAN",
                    reminderDaysBefore = 2,
                    note = "Netflix & Spotify Family"
                )
            )
            defaultBills.forEach { recurringBillDao.insertBill(it) }
        }
    }

    suspend fun getBackupJson(): String {
        // Query everything from database
        val transactions = financeDao.getAllTransactions().first()
        val budgets = financeDao.getAllBudgets().first()
        val accounts = accountDao.getAllAccounts().first()
        val bills = recurringBillDao.getAllBills().first()
        val savings = financeDao.getAllSavingsGoals().first()

        val backupObj = org.json.JSONObject()
        backupObj.put("version", 1)
        backupObj.put("backupDate", System.currentTimeMillis())

        // Transactions
        val txArray = org.json.JSONArray()
        for (tx in transactions) {
            val obj = org.json.JSONObject()
            obj.put("title", tx.title)
            obj.put("amount", tx.amount)
            obj.put("type", tx.type)
            obj.put("category", tx.category)
            obj.put("walletName", tx.walletName)
            obj.put("dateMillis", tx.dateMillis)
            obj.put("note", tx.note)
            txArray.put(obj)
        }
        backupObj.put("transactions", txArray)

        // Budgets
        val budgetArray = org.json.JSONArray()
        for (b in budgets) {
            val obj = org.json.JSONObject()
            obj.put("category", b.category)
            obj.put("month", b.month)
            obj.put("year", b.year)
            obj.put("budgetAmount", b.budgetAmount)
            obj.put("thresholdPercent", b.thresholdPercent)
            obj.put("alertEnabled", b.alertEnabled)
            budgetArray.put(obj)
        }
        backupObj.put("budgets", budgetArray)

        // Accounts
        val accountArray = org.json.JSONArray()
        for (a in accounts) {
            val obj = org.json.JSONObject()
            obj.put("name", a.name)
            obj.put("type", a.type)
            obj.put("initialBalance", a.initialBalance)
            obj.put("colorHex", a.colorHex)
            obj.put("iconKey", a.iconKey)
            accountArray.put(obj)
        }
        backupObj.put("accounts", accountArray)

        // Bills
        val billArray = org.json.JSONArray()
        for (bill in bills) {
            val obj = org.json.JSONObject()
            obj.put("title", bill.title)
            obj.put("amount", bill.amount)
            obj.put("category", bill.category)
            obj.put("walletName", bill.walletName)
            obj.put("dueDay", bill.dueDay)
            obj.put("frequency", bill.frequency)
            obj.put("reminderDaysBefore", bill.reminderDaysBefore)
            obj.put("isActive", bill.isActive)
            obj.put("lastPaidDateMillis", bill.lastPaidDateMillis ?: -1L)
            obj.put("note", bill.note)
            billArray.put(obj)
        }
        backupObj.put("bills", billArray)

        // Savings Goals
        val savingsArray = org.json.JSONArray()
        for (s in savings) {
            val obj = org.json.JSONObject()
            obj.put("title", s.title)
            obj.put("targetAmount", s.targetAmount)
            obj.put("currentAmount", s.currentAmount)
            obj.put("category", s.category)
            obj.put("targetDateMillis", s.targetDateMillis)
            obj.put("note", s.note)
            savingsArray.put(obj)
        }
        backupObj.put("savingsGoals", savingsArray)

        return backupObj.toString(4) // pretty print
    }

    suspend fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val backupObj = org.json.JSONObject(jsonString)

            // Clear current tables
            financeDao.deleteAllTransactions()
            financeDao.getAllBudgets().first().forEach { financeDao.deleteBudgetById(it.id) }
            accountDao.getAllAccounts().first().forEach { accountDao.deleteAccountById(it.id) }
            recurringBillDao.getAllBills().first().forEach { recurringBillDao.deleteBillById(it.id) }
            financeDao.getAllSavingsGoals().first().forEach { financeDao.deleteSavingsGoalById(it.id) }

            // 1. Restore Accounts
            val accountArray = backupObj.optJSONArray("accounts")
            if (accountArray != null) {
                for (i in 0 until accountArray.length()) {
                    val obj = accountArray.getJSONObject(i)
                    accountDao.insertAccount(
                        AccountEntity(
                            name = obj.getString("name"),
                            type = obj.getString("type"),
                            initialBalance = obj.getDouble("initialBalance"),
                            colorHex = obj.getLong("colorHex"),
                            iconKey = obj.getString("iconKey")
                        )
                    )
                }
            }

            // 2. Restore Transactions
            val txArray = backupObj.optJSONArray("transactions")
            if (txArray != null) {
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    financeDao.insertTransaction(
                        TransactionEntity(
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            type = obj.getString("type"),
                            category = obj.getString("category"),
                            walletName = obj.getString("walletName"),
                            dateMillis = obj.getLong("dateMillis"),
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            // 3. Restore Budgets
            val budgetArray = backupObj.optJSONArray("budgets")
            if (budgetArray != null) {
                for (i in 0 until budgetArray.length()) {
                    val obj = budgetArray.getJSONObject(i)
                    financeDao.upsertBudget(
                        CategoryBudgetEntity(
                            category = obj.getString("category"),
                            month = obj.getInt("month"),
                            year = obj.getInt("year"),
                            budgetAmount = obj.getDouble("budgetAmount"),
                            thresholdPercent = obj.optInt("thresholdPercent", 80),
                            alertEnabled = obj.optBoolean("alertEnabled", true)
                        )
                    )
                }
            }

            // 4. Restore Bills
            val billArray = backupObj.optJSONArray("bills")
            if (billArray != null) {
                for (i in 0 until billArray.length()) {
                    val obj = billArray.getJSONObject(i)
                    val lastPaid = obj.optLong("lastPaidDateMillis", -1L)
                    recurringBillDao.insertBill(
                        RecurringBillEntity(
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            category = obj.getString("category"),
                            walletName = obj.getString("walletName"),
                            dueDay = obj.getInt("dueDay"),
                            frequency = obj.optString("frequency", "MONTHLY"),
                            reminderDaysBefore = obj.optInt("reminderDaysBefore", 3),
                            isActive = obj.optBoolean("isActive", true),
                            lastPaidDateMillis = if (lastPaid == -1L) null else lastPaid,
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            // 5. Restore Savings Goals
            val savingsArray = backupObj.optJSONArray("savingsGoals")
            if (savingsArray != null) {
                for (i in 0 until savingsArray.length()) {
                    val obj = savingsArray.getJSONObject(i)
                    financeDao.upsertSavingsGoal(
                        SavingsGoalEntity(
                            title = obj.getString("title"),
                            targetAmount = obj.getDouble("targetAmount"),
                            currentAmount = obj.getDouble("currentAmount"),
                            category = obj.getString("category"),
                            targetDateMillis = obj.getLong("targetDateMillis"),
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
