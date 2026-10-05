package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class TransactionType(val label: String) {
    EXPENSE("Pengeluaran"),
    INCOME("Pemasukan")
}

@Entity(
    tableName = "transactions",
    indices = [
        Index("dateMillis"),
        Index("categoryId"),
        Index("accountId")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String,
    val walletName: String, // "Tunai", "Rekening Bank", "E-Wallet"
    val dateMillis: Long,
    val note: String = "",
    val categoryId: Long = 0,
    val accountId: Long = 0
)

data class TransactionWithDetails(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val categoryEntity: CategoryEntity? = null,
    @Relation(
        parentColumn = "accountId",
        entityColumn = "id"
    )
    val accountEntity: AccountEntity? = null
)

data class FinanceCategory(
    val id: String,
    val name: String,
    val type: TransactionType,
    val colorHex: Long,
    val iconKey: String
)

object DefaultCategories {
    val expenseCategories = listOf(
        FinanceCategory("food", "Makanan & Minuman", TransactionType.EXPENSE, 0xFFEF4444, "restaurant"),
        FinanceCategory("transport", "Transportasi", TransactionType.EXPENSE, 0xFFF97316, "directions_car"),
        FinanceCategory("shopping", "Belanja & Kebutuhan", TransactionType.EXPENSE, 0xFFEC4899, "shopping_bag"),
        FinanceCategory("bills", "Tagihan & Utilitas", TransactionType.EXPENSE, 0xFFEAB308, "receipt_long"),
        FinanceCategory("entertainment", "Hiburan & Liburan", TransactionType.EXPENSE, 0xFF8B5CF6, "movie"),
        FinanceCategory("health", "Kesehatan & Medis", TransactionType.EXPENSE, 0xFF06B6D4, "health_and_safety"),
        FinanceCategory("education", "Pendidikan", TransactionType.EXPENSE, 0xFF3B82F6, "school"),
        FinanceCategory("household", "Rumah Tangga", TransactionType.EXPENSE, 0xFF14B8A6, "home"),
        FinanceCategory("other_exp", "Pengeluaran Lain", TransactionType.EXPENSE, 0xFF64748B, "more_horiz")
    )

    val incomeCategories = listOf(
        FinanceCategory("salary", "Gaji Pokok", TransactionType.INCOME, 0xFF10B981, "payments"),
        FinanceCategory("bonus", "Bonus & THR", TransactionType.INCOME, 0xFF059669, "card_giftcard"),
        FinanceCategory("business", "Usaha & Bisnis", TransactionType.INCOME, 0xFF0D9488, "storefront"),
        FinanceCategory("investment", "Investasi & Dividen", TransactionType.INCOME, 0xFF2563EB, "trending_up"),
        FinanceCategory("freelance", "Freelance / Proyek", TransactionType.INCOME, 0xFF7C3AED, "laptop_mac"),
        FinanceCategory("gift", "Hadiah & Uang Saku", TransactionType.INCOME, 0xFFDB2777, "redeem"),
        FinanceCategory("other_inc", "Pemasukan Lain", TransactionType.INCOME, 0xFF64748B, "account_balance_wallet")
    )

    val allCategories = expenseCategories + incomeCategories

    val defaultWallets = listOf("Tunai", "Rekening Bank", "E-Wallet")

    fun findCategory(name: String): FinanceCategory {
        return allCategories.find { it.name.equals(name, ignoreCase = true) }
            ?: FinanceCategory("unknown", name, TransactionType.EXPENSE, 0xFF64748B, "category")
    }
}
