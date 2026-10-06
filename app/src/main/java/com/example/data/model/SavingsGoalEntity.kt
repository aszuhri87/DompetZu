package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val category: String, // "Pernikahan", "Liburan", "Pendidikan", "Kendaraan", "Rumah", "Gaya Hidup", "Lainnya"
    val targetDateMillis: Long,
    val note: String = ""
)
