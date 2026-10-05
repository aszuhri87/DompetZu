package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountById(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE name = :name LIMIT 1")
    suspend fun getAccountByName(name: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Query("UPDATE accounts SET initialBalance = :balance WHERE id = :id")
    suspend fun updateInitialBalance(id: Long, balance: Double)

    @Query("UPDATE accounts SET initialBalance = 0.0")
    suspend fun resetAllBalancesToZero()

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int
}
