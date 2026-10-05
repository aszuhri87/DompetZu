package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "CASH", "BANK", "E_WALLET"
    val initialBalance: Double = 0.0,
    val colorHex: Long = 0xFF0284C7,
    val iconKey: String = "account_balance_wallet"
)
