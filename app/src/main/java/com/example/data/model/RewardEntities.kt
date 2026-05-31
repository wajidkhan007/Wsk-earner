package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single active user session
    val name: String = "Guest User",
    val phone: String = "",
    val email: String = "",
    val profilePic: String = "", // Base64 or local uri
    val coinBalance: Int = 0, // Initial sign-up bonus coins
    val totalEarningsCoins: Int = 0,
    val referralEarningsCoins: Int = 0,
    val referralCode: String = "REWARD88X",
    val referredBy: String = "",
    val lastCheckInTimestamp: Long = 0,
    val checkInStreak: Int = 0,
    val dailySpinsUsed: Int = 0,
    val dailyScrachesUsed: Int = 0,
    val deviceFingerprint: String = "DEVC-992-881-A",
    val isFlaggedAsFraud: Boolean = false,
    val lastIpAddress: String = "192.168.1.45"
)

@Entity(tableName = "reward_transactions")
data class RewardTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "CHECK_IN", "SPIN", "SCRATCH", "OFFERWALL", "AD_WATCH", "REFERRAL", "WITHDRAWAL"
    val amount: Int,  // positive or negative
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "offer_tasks")
data class OfferTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val imageUrl: String = "",
    val category: String, // "APPS", "SURVEYS", "GAMES", "WATCH"
    val status: String = "AVAILABLE", // "AVAILABLE", "PENDING_VERIFICATION", "COMPLETED"
    val verificationAnswer: String = "" // Simulates task completion submission answers
)

@Entity(tableName = "withdrawal_requests")
data class WithdrawalRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val method: String, // "UPI", "Paytm", "Bank Transfer"
    val detailsKey: String, // e.g. UPI ID or account number
    val detailsValue: String, // details details
    val coinAmount: Int,
    val cashAmountEstimated: Double, // calculated value $ or Rs
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String = "" // Custom admin action reason
)

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val type: String = "BENEFIT", // "ALERT", "BENEFIT", "SYSTEM"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
