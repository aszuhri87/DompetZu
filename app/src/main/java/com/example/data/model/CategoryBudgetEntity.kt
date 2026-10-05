package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category_budgets",
    indices = [Index(value = ["category", "month", "year"], unique = true)]
)
data class CategoryBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val month: Int, // 1 - 12
    val year: Int,
    val budgetAmount: Double,
    val thresholdPercent: Int = 80, // Alert threshold percentage (e.g., 80% of budget)
    val alertEnabled: Boolean = true // Whether to trigger notification when threshold is crossed
)
