package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RecurringBillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringBillDao {
    @Query("SELECT * FROM recurring_bills ORDER BY dueDay ASC")
    fun getAllBills(): Flow<List<RecurringBillEntity>>

    @Query("SELECT * FROM recurring_bills WHERE isActive = 1 ORDER BY dueDay ASC")
    fun getActiveBills(): Flow<List<RecurringBillEntity>>

    @Query("SELECT * FROM recurring_bills WHERE id = :id LIMIT 1")
    suspend fun getBillById(id: Long): RecurringBillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: RecurringBillEntity): Long

    @Update
    suspend fun updateBill(bill: RecurringBillEntity)

    @Delete
    suspend fun deleteBill(bill: RecurringBillEntity)

    @Query("DELETE FROM recurring_bills WHERE id = :id")
    suspend fun deleteBillById(id: Long)
}
