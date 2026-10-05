package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "BUDGET_WARNING", "BUDGET_EXCEEDED", "BILL_DUE", "BILL_OVERDUE", "SYSTEM"
    val category: String? = null,
    val relatedId: Long? = null,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
