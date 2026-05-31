package com.example.data.repository

import com.example.data.database.RewardDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class RewardRepository(private val rewardDao: RewardDao) {
    val userProfile: Flow<UserProfile?> = rewardDao.getUserProfileFlow()
    val transactions: Flow<List<RewardTransaction>> = rewardDao.getTransactionsFlow()
    val offerTasks: Flow<List<OfferTask>> = rewardDao.getOfferTasksFlow()
    val withdrawals: Flow<List<WithdrawalRequest>> = rewardDao.getWithdrawalsFlow()
    val notifications: Flow<List<NotificationLog>> = rewardDao.getNotificationsFlow()

    suspend fun getActiveUser(): UserProfile? {
        return rewardDao.getUserProfile()
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        rewardDao.saveUserProfile(profile)
    }

    suspend fun addTransaction(transaction: RewardTransaction) {
        rewardDao.insertTransaction(transaction)
    }

    suspend fun getTaskById(id: Int): OfferTask? {
        return rewardDao.getTaskById(id)
    }

    suspend fun insertTask(task: OfferTask) {
        rewardDao.insertTask(task)
    }

    suspend fun updateTask(task: OfferTask) {
        rewardDao.updateTask(task)
    }

    suspend fun deleteTaskById(id: Int) {
        rewardDao.deleteTaskById(id)
    }

    suspend fun getWithdrawalById(id: Int): WithdrawalRequest? {
        return rewardDao.getWithdrawalById(id)
    }

    suspend fun submitWithdrawal(request: WithdrawalRequest) {
        rewardDao.insertWithdrawal(request)
    }

    suspend fun updateWithdrawal(request: WithdrawalRequest) {
        rewardDao.updateWithdrawal(request)
    }

    suspend fun insertNotification(notification: NotificationLog) {
        rewardDao.insertNotification(notification)
    }

    suspend fun markNotificationRead(id: Int) {
        rewardDao.markNotificationRead(id)
    }

    suspend fun clearAllNotifications() {
        rewardDao.clearAllNotifications()
    }
}
