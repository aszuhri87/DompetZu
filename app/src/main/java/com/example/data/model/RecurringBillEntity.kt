package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_bills")
data class RecurringBillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val walletName: String,
    val dueDay: Int, // Day of the month: 1 - 31
    val frequency: String = "MONTHLY", // "MONTHLY", "WEEKLY", "YEARLY"
    val reminderDaysBefore: Int = 3, // Days before due date to alert
    val isActive: Boolean = true,
    val lastPaidDateMillis: Long? = null,
    val note: String = ""
)
