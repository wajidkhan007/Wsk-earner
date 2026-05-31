package com.example.data.database

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    // User Profile Queries
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    // Transaction Queries
    @Query("SELECT * FROM reward_transactions ORDER BY timestamp DESC")
    fun getTransactionsFlow(): Flow<List<RewardTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: RewardTransaction)

    // Offer Tasks Queries
    @Query("SELECT * FROM offer_tasks")
    fun getOfferTasksFlow(): Flow<List<OfferTask>>

    @Query("SELECT * FROM offer_tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): OfferTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: OfferTask)

    @Update
    suspend fun updateTask(task: OfferTask)

    @Query("DELETE FROM offer_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    // Withdrawal Requests Queries
    @Query("SELECT * FROM withdrawal_requests ORDER BY timestamp DESC")
    fun getWithdrawalsFlow(): Flow<List<WithdrawalRequest>>

    @Query("SELECT * FROM withdrawal_requests WHERE id = :id")
    suspend fun getWithdrawalById(id: Int): WithdrawalRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(request: WithdrawalRequest)

    @Update
    suspend fun updateWithdrawal(request: WithdrawalRequest)

    // Notifications Queries
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getNotificationsFlow(): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationLog)

    @Query("UPDATE notification_logs SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Int)

    @Query("DELETE FROM notification_logs")
    suspend fun clearAllNotifications()
}
