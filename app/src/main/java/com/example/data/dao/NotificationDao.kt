package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM app_notifications ORDER BY timestampMillis DESC")
    fun getAllNotifications(): Flow<List<AppNotificationEntity>>

    @Query("SELECT COUNT(*) FROM app_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT * FROM app_notifications WHERE type = :type AND category = :category AND timestampMillis >= :sinceMillis")
    suspend fun getRecentByTypeAndCategory(type: String, category: String, sinceMillis: Long): List<AppNotificationEntity>

    @Query("SELECT * FROM app_notifications WHERE type = :type AND relatedId = :relatedId AND timestampMillis >= :sinceMillis")
    suspend fun getRecentByTypeAndRelatedId(type: String, relatedId: Long, sinceMillis: Long): List<AppNotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity): Long

    @Update
    suspend fun updateNotification(notification: AppNotificationEntity)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM app_notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Long)

    @Delete
    suspend fun deleteNotification(notification: AppNotificationEntity)

    @Query("DELETE FROM app_notifications")
    suspend fun clearAllNotifications()
}
