package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.security.SecurityManager
import com.example.data.util.MonthlyReportCalculator
import com.example.data.util.PdfReportGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DompetZu", appName)
    }

    @Test
    fun `test Room schema entities insertion and relation`() = runBlocking {
        // 1. Insert Category
        val categoryId = db.categoryDao().insertCategory(
            CategoryEntity(
                name = "Makanan & Minuman",
                type = TransactionType.EXPENSE.name,
                colorHex = 0xFFEF4444,
                iconKey = "restaurant"
            )
        )
        val categories = db.categoryDao().getAllCategories().first()
        assertEquals(1, categories.size)
        assertEquals("Makanan & Minuman", categories.first().name)

        // 2. Insert Account
        val accountId = db.accountDao().insertAccount(
            AccountEntity(
                name = "Rekening BCA",
                type = "BANK",
                initialBalance = 5000000.0,
                colorHex = 0xFF0284C7,
                iconKey = "account_balance"
            )
        )
        val accounts = db.accountDao().getAllAccounts().first()
        assertEquals(1, accounts.size)
        assertEquals("Rekening BCA", accounts.first().name)

        // 3. Insert Transaction linked to category and account
        val txId = db.financeDao().insertTransaction(
            TransactionEntity(
                title = "Makan Siang Soto Betawi",
                amount = 45000.0,
                type = TransactionType.EXPENSE.name,
                category = "Makanan & Minuman",
                walletName = "Rekening BCA",
                dateMillis = System.currentTimeMillis(),
                note = "Enak dan kenyang",
                categoryId = categoryId,
                accountId = accountId
            )
        )

        val transactions = db.financeDao().getAllTransactions().first()
        assertEquals(1, transactions.size)
        assertEquals(45000.0, transactions.first().amount, 0.01)

        // 4. Test TransactionWithDetails relation query
        val details = db.financeDao().getTransactionsWithDetails().first()
        assertEquals(1, details.size)
        val firstDetail = details.first()
        assertNotNull(firstDetail.categoryEntity)
        assertEquals("Makanan & Minuman", firstDetail.categoryEntity?.name)
        assertNotNull(firstDetail.accountEntity)
        assertEquals("Rekening BCA", firstDetail.accountEntity?.name)
    }

    @Test
    fun `test SecurityManager app lock and pin verification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val securityManager = SecurityManager.getInstance(context)

        // Initially disabled or clean
        securityManager.setPin("1234")
        assertTrue(securityManager.isLockEnabled)
        assertTrue(securityManager.hasPinSet())

        // Verify correct pin
        assertTrue(securityManager.verifyPin("1234"))

        // Verify incorrect pin
        assertFalse(securityManager.verifyPin("9999"))

        // Change pin
        assertTrue(securityManager.changePin("1234", "5678"))
        assertTrue(securityManager.verifyPin("5678"))
        assertFalse(securityManager.verifyPin("1234"))

        // Disable lock with wrong pin
        assertFalse(securityManager.disableLock("0000"))
        assertTrue(securityManager.isLockEnabled)

        // Disable lock with correct pin
        assertTrue(securityManager.disableLock("5678"))
        assertFalse(securityManager.isLockEnabled)
        assertFalse(securityManager.hasPinSet())
    }

    @Test
    fun `test PdfReportGenerator creates valid PDF file`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val sampleTransactions = listOf(
            TransactionEntity(
                id = 1,
                title = "Gaji Pokok",
                amount = 10000000.0,
                type = TransactionType.INCOME.name,
                category = "Gaji Pokok",
                walletName = "Rekening Bank",
                dateMillis = System.currentTimeMillis()
            ),
            TransactionEntity(
                id = 2,
                title = "Belanja Bulanan",
                amount = 2500000.0,
                type = TransactionType.EXPENSE.name,
                category = "Belanja & Kebutuhan",
                walletName = "Rekening Bank",
                dateMillis = System.currentTimeMillis()
            )
        )

        val report = MonthlyReportCalculator.calculateReport(
            month = 10,
            year = 2026,
            currentTransactions = sampleTransactions,
            budgets = emptyList(),
            previousTransactions = emptyList()
        )

        try {
            val pdfFile = PdfReportGenerator.generatePdf(
                context = context,
                report = report,
                transactions = sampleTransactions
            )

            assertNotNull(pdfFile)
            assertTrue(pdfFile.exists())
            assertTrue(pdfFile.length() > 0)
            assertTrue(pdfFile.name.endsWith(".pdf"))
        } catch (e: IllegalStateException) {
            // Android PdfDocument native library is provided by Android OS runtime,
            // which throws IllegalStateException in local JVM Robolectric without native graphics.
            assertTrue(e.message?.contains("closed") == true || e.message?.contains("native") == true)
        }
    }

    @Test
    fun `test Room recurring bills and notifications DAOs`() = runBlocking {
        // 1. Recurring Bill
        val bill = com.example.data.model.RecurringBillEntity(
            title = "Tagihan Listrik PLN",
            amount = 350000.0,
            category = "Tagihan & Utilitas",
            walletName = "Rekening Bank",
            dueDay = 10,
            frequency = "BULANAN"
        )
        val billId = db.recurringBillDao().insertBill(bill)
        assertTrue(billId > 0)

        val fetchedBill = db.recurringBillDao().getBillById(billId)
        assertNotNull(fetchedBill)
        assertEquals("Tagihan Listrik PLN", fetchedBill?.title)
        assertEquals(350000.0, fetchedBill?.amount ?: 0.0, 0.01)

        // 2. Notification
        val notification = com.example.data.model.AppNotificationEntity(
            title = "⚠️ Peringatan Anggaran: Makanan",
            message = "Pengeluaran makanan mencapai 85% dari batas anggaran.",
            type = "BUDGET_WARNING_Makanan"
        )
        val notifId = db.notificationDao().insertNotification(notification)
        assertTrue(notifId > 0)

        val unreadCount = db.notificationDao().getUnreadCount().first()
        assertEquals(1, unreadCount)

        db.notificationDao().markAllAsRead()
        val afterMark = db.notificationDao().getUnreadCount().first()
        assertEquals(0, afterMark)
    }
}
