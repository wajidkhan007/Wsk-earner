package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        UserProfile::class,
        RewardTransaction::class,
        OfferTask::class,
        WithdrawalRequest::class,
        NotificationLog::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RewardDatabase : RoomDatabase() {
    abstract fun rewardDao(): RewardDao
}
