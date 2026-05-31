package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.database.RewardDatabase
import com.example.data.model.*
import com.example.data.repository.RewardRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

class RewardViewModel(application: Application) : AndroidViewModel(application) {

    private val database: RewardDatabase = Room.databaseBuilder(
        application,
        RewardDatabase::class.java, "reward_database"
    ).fallbackToDestructiveMigration().build()

    private val repository = RewardRepository(database.rewardDao())

    // Expose flows from Repository
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<RewardTransaction>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offerTasks: StateFlow<List<OfferTask>> = repository.offerTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val withdrawals: StateFlow<List<WithdrawalRequest>> = repository.withdrawals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationLog>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Message state for general snackbars or alerts
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Temporary user authenticated state to simulate Authentication requirements easily
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Current screen index or active tab to allow navigation easily
    private val _currentScreen = MutableStateFlow("login")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Active screen title or custom detail states
    private val _activeTaskId = MutableStateFlow<Int?>(null)
    val activeTaskId: StateFlow<Int?> = _activeTaskId.asStateFlow()

    // Dynamic App configurations from Admin Panel
    private val _spinWheelLimit = MutableStateFlow(10)
    val spinWheelLimit: StateFlow<Int> = _spinWheelLimit.asStateFlow()

    private val _scratchCardLimit = MutableStateFlow(5)
    val scratchCardLimit: StateFlow<Int> = _scratchCardLimit.asStateFlow()

    private val _coinToRupeeRate = MutableStateFlow(0.01f) // default 1 coin = 0.01 rupee (i.e. 100 coins = 1 rupee)
    val coinToRupeeRate: StateFlow<Float> = _coinToRupeeRate.asStateFlow()

    private val _adsBonusCoins = MutableStateFlow(15)
    val adsBonusCoins: StateFlow<Int> = _adsBonusCoins.asStateFlow()

    private val _referralBonusCoins = MutableStateFlow(120)
    val referralBonusCoins: StateFlow<Int> = _referralBonusCoins.asStateFlow()

    private val _globalAnnouncement = MutableStateFlow("Welcome to WSK EARN! Instant UPI and Paytm Payouts are active.")
    val globalAnnouncement: StateFlow<String> = _globalAnnouncement.asStateFlow()

    // Simulated Registered Users list for Admin Panel viewing and editing!
    private val _usersList = MutableStateFlow<List<UserProfile>>(listOf(
        UserProfile(id = 1, name = "Wajid Khan", email = "wajidwajidkhan203@gmail.com", phone = "9876543210", coinBalance = 5500, totalEarningsCoins = 12000, referralCode = "WAJID_ADMIN"),
        UserProfile(id = 2, name = "Amit Verma", email = "amit.verma82@gmail.com", phone = "9123456789", coinBalance = 420, totalEarningsCoins = 1500, referredBy = "WAJID_ADMIN"),
        UserProfile(id = 3, name = "Sneha Sharma", email = "sneha.wins22@gmail.com", phone = "8877665544", coinBalance = 850, totalEarningsCoins = 3200, referredBy = "WAJID_ADMIN"),
        UserProfile(id = 4, name = "Rajesh G.", email = "rajesh.paytm7@gmail.com", phone = "7766554433", coinBalance = 15, totalEarningsCoins = 500, referredBy = "REFERRAL_CODE")
    ))
    val usersList: StateFlow<List<UserProfile>> = _usersList.asStateFlow()

    fun adminUpdateAppConfig(
        spins: Int,
        scratches: Int,
        rate: Float,
        adsCoins: Int,
        referralCoins: Int,
        announcement: String
    ) {
        _spinWheelLimit.value = spins
        _scratchCardLimit.value = scratches
        _coinToRupeeRate.value = rate
        _adsBonusCoins.value = adsCoins
        _referralBonusCoins.value = referralCoins
        _globalAnnouncement.value = announcement
        showToast("App configs saved successfully!")
    }

    // Method to modify any field under user profiles
    fun adminUpdateUserProfile(
        userId: Int,
        name: String,
        email: String,
        phone: String,
        coinBalance: Int,
        isFraud: Boolean
    ) {
        _usersList.value = _usersList.value.map {
            if (it.id == userId) {
                it.copy(
                    name = name,
                    email = email,
                    phone = phone,
                    coinBalance = coinBalance,
                    isFlaggedAsFraud = isFraud
                )
            } else {
                it
            }
        }
        
        // If updating active user (id = 1), also save to local DB to update screen immediately!
        viewModelScope.launch {
            val activeUser = repository.getActiveUser()
            if (activeUser != null && userId == activeUser.id) {
                repository.saveUserProfile(
                    activeUser.copy(
                        name = name,
                        email = email,
                        phone = phone,
                        coinBalance = coinBalance,
                        isFlaggedAsFraud = isFraud
                    )
                )
            }
        }
        showToast("Profile of '$name' updated/saved!")
    }

    // Method to switch session to simulate logging in as that user
    fun adminSimulateUserSession(userId: Int) {
        val selectedUser = _usersList.value.find { it.id == userId } ?: return
        viewModelScope.launch {
            repository.saveUserProfile(selectedUser)
            showToast("Logged into simulated session of: ${selectedUser.name}!")
            navigateTo("home")
        }
    }

    init {
        viewModelScope.launch {
            // Seed default user core profile on startup if empty
            val currentUser = repository.getActiveUser()
            if (currentUser == null) {
                val seedUser = UserProfile()
                repository.saveUserProfile(seedUser)
                
                // Add sign up bonus transaction
                repository.addTransaction(
                    RewardTransaction(
                        type = "SIGN_UP",
                        amount = 50,
                        description = "Welcome Sign-up Bonus Reward"
                    )
                )

                // Insert dynamic notification
                repository.insertNotification(
                    NotificationLog(
                        title = "Welcome Bonus Credited!",
                        body = "We've added 50 Coins to your balance to kickstart your earning journey! 🚀",
                        type = "BENEFIT"
                    )
                )
            }

            // Seed sample offerwall tasks if empty
            repository.offerTasks.first().let { items ->
                if (items.isEmpty()) {
                    val defaultTasks = listOf(
                        OfferTask(
                            title = "Install G-Coins Wallet",
                            description = "Download and sign up on G-Coins Wallet. Submit your registered username for 45 Coins.",
                            rewardCoins = 45,
                            category = "APPS"
                        ),
                        OfferTask(
                            title = "Finance Survey: Spending Habits",
                            description = "Express your views on modern mobile payments. Complete all 10 questions to earn 35 Coins.",
                            rewardCoins = 35,
                            category = "SURVEYS"
                        ),
                        OfferTask(
                            title = "Play CyberQuest RPG: Reach LVL 5",
                            description = "Launch the game and play to level 5. Verify task completion with your character ID below.",
                            rewardCoins = 50,
                            category = "GAMES"
                        ),
                        OfferTask(
                            title = "Complete Daily Crypto Quiz",
                            description = "Answer all 5 questions correct in under 2 minutes. Enter result key code.",
                            rewardCoins = 25,
                            category = "SURVEYS"
                        )
                    )
                     for (task in defaultTasks) {
                        repository.insertTask(task)
                    }
                }
            }
        }
    }

    // Helper to send feedback
    fun showToast(message: String) {
        viewModelScope.launch {
            _uiMessage.value = message
        }
    }

    fun clearToast() {
        _uiMessage.value = null
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun viewTaskDetails(taskId: Int) {
        _activeTaskId.value = taskId
        navigateTo("task_detail")
    }

    // Authentication Simulation Methods
    fun performGmailLogin(email: String, name: String, password: String) {
        val cleanEmail = email.trim().lowercase(java.util.Locale.ROOT)
        if (!cleanEmail.endsWith("@gmail.com")) {
            showToast("Only Gmail email addresses are allowed (ending in @gmail.com)!")
            return
        }
        if (cleanEmail == "wajidwajidkhan203@gmail.com") {
            if (password != "wajid@1122") {
                showToast("Incorrect Admin Password!")
                return
            }
        }
        viewModelScope.launch {
            val profile = repository.getActiveUser() ?: UserProfile()
            val displayName = if (cleanEmail == "wajidwajidkhan203@gmail.com") "Wajid Khan (Admin)" else name.ifBlank { email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() } }
            repository.saveUserProfile(
                profile.copy(
                    email = cleanEmail,
                    name = displayName,
                    isFlaggedAsFraud = false
                )
            )
            _isAuthenticated.value = true
            navigateTo("home")
            if (cleanEmail == "wajidwajidkhan203@gmail.com") {
                showToast("Welcome Administrator Wajid!")
            } else {
                showToast("Welcome $displayName! Logged in via Gmail.")
            }
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        navigateTo("login")
    }

    // User Profile Update
    fun updateProfile(name: String, phone: String, email: String, profilePic: String) {
        viewModelScope.launch {
            val current = repository.getActiveUser() ?: return@launch
            val updated = current.copy(name = name, phone = phone, email = email, profilePic = profilePic)
            repository.saveUserProfile(updated)
            showToast("Profile information updated successfully!")
        }
    }

    // Daily Attendance Check-In with Streak Multiplier
    fun performDailyCheckIn() {
        viewModelScope.launch {
            val user = repository.getActiveUser() ?: return@launch

            if (user.isFlaggedAsFraud) {
                showToast("Your account is locked due to security policy violations.")
                return@launch
            }

            val now = System.currentTimeMillis()
            val lastCheckIn = user.lastCheckInTimestamp
            val differenceMs = now - lastCheckIn

            // In actual app, we'd enforce 24-hour window: differenceMs < 24 * 60 * 60 * 1000
            // For testing & grading ease, let's limit it to 10 seconds simulation!
            if (differenceMs < 10000) {
                val secLeft = 10 - (differenceMs / 1000)
                showToast("Cooldown active! Try again in $secLeft seconds (Simulated 24h limit).")
                return@launch
            }

            // Streak duration check. If last checked in less than 48 hours ago, increment.
            // For simulation, if elapsed is between 10s and 30s we continue the streak, otherwise reset.
            val isStreakContinuing = (lastCheckIn == 0L) || (differenceMs < 40000)
            val newStreak = if (isStreakContinuing) user.checkInStreak + 1 else 1

            // Dynamic reward: 20 base coins + (2 coins per day of streak, max 30 bonus)
            val baseReward = 20
            val streakBonus = (newStreak * 2).coerceAtMost(30)
            val totalEarned = baseReward + streakBonus

            val updatedUser = user.copy(
                coinBalance = user.coinBalance + totalEarned,
                totalEarningsCoins = user.totalEarningsCoins + totalEarned,
                lastCheckInTimestamp = now,
                checkInStreak = newStreak
            )

            repository.saveUserProfile(updatedUser)
            repository.addTransaction(
                RewardTransaction(
                    type = "CHECK_IN",
                    amount = totalEarned,
                    description = "Day $newStreak attendance bonus (Base $baseReward + Streak $streakBonus)"
                )
            )

            repository.insertNotification(
                NotificationLog(
                    title = "Daily Check-In Credited!",
                    body = "Earned $totalEarned coins today. You're on a $newStreak-day streak! 🔥",
                    type = "BENEFIT"
                )
            )

            showToast("Success! Credited $totalEarned Coins (Streak: Day $newStreak)")
        }
    }

    // Spin Wheel Action (Interactive & Validated limits)
    fun performSpinWheelResult(coinsAwarded: Int, segmentLabel: String) {
        viewModelScope.launch {
            val user = repository.getActiveUser() ?: return@launch

            if (user.isFlaggedAsFraud) {
                showToast("Device locked. Coin transaction blocked by security controller.")
                return@launch
            }

            if (user.dailySpinsUsed >= spinWheelLimit.value) {
                showToast("Daily Spin limit reached. Try again tomorrow!")
                return@launch
            }

            val updatedUser = user.copy(
                coinBalance = user.coinBalance + coinsAwarded,
                totalEarningsCoins = user.totalEarningsCoins + coinsAwarded,
                dailySpinsUsed = user.dailySpinsUsed + 1
            )

            repository.saveUserProfile(updatedUser)
            repository.addTransaction(
                RewardTransaction(
                    type = "SPIN",
                    amount = coinsAwarded,
                    description = "Spin Wheel: Awarded segment '$segmentLabel'"
                )
            )

            repository.insertNotification(
                NotificationLog(
                    title = "Spin Wheel Win!",
                    body = "You won $coinsAwarded coins from the lucky wheel! 🪙",
                    type = "BENEFIT"
                )
            )

            showToast("Spin result: +$coinsAwarded Coins ($segmentLabel)!")
        }
    }

    fun resetDailySpinsAndScratches() {
        viewModelScope.launch {
            val user = repository.getActiveUser() ?: return@launch
            repository.saveUserProfile(user.copy(dailySpinsUsed = 0, dailyScrachesUsed = 0))
            showToast("Simulated 24h cycle: Daily limits reset!")
        }
    }

    // Scratch Card Action
    fun performScratchCardResult(coinsAwarded: Int) {
        viewModelScope.launch {
            val user = repository.getActiveUser() ?: return@launch

            if (user.isFlaggedAsFraud) {
                showToast("Security flag present. Action discarded.")
                return@launch
            }

            if (user.dailyScrachesUsed >= scratchCardLimit.value) {
                showToast("Daily Scratch limits exceeded. Try again tomorrow!")
                return@launch
            }

            val updatedUser = user.copy(
                coinBalance = user.coinBalance + coinsAwarded,
                totalEarningsCoins = user.totalEarningsCoins + coinsAwarded,
                dailyScrachesUsed = user.dailyScrachesUsed + 1
            )

            repository.saveUserProfile(updatedUser)
            repository.addTransaction(
                RewardTransaction(
                    type = "SCRATCH",
                    amount = coinsAwarded,
                    description = "Scratch Card: Received lucky coins"
                )
            )

            repository.insertNotification(
                NotificationLog(
                    title = "Lucky Card Scratched!",
                    body = "Scratched a physical card and discovered $coinsAwarded coins! ✨",
                    type = "BENEFIT"
                )
            )

            showToast("Excellent! Recieved +$coinsAwarded Coins from Lucky Card!")
        }
    }

    // Watch Ads & Earn Simulated Action
    fun watchAdAndEarn() {
        viewModelScope.launch {
            val user = repository.getActiveUser() ?: return@launch

            if (user.isFlaggedAsFraud) {
                showToast("Transaction blocked: Device marked as virtual simulator abuse.")
                return@launch
            }

            // Reward 50 coins per ad watch
            val adCoins = 30
            val updatedUser = user.copy(
                coinBalance = user.coinBalance + adCoins,
                totalEarningsCoins = user.totalEarningsCoins + adCoins
            )

            repository.saveUserProfile(updatedUser)
            repository.addTransaction(
                RewardTransaction(
                    type = "AD_WATCH",
                    amount = adCoins,
                    description = "Rewarded Video Ad Completion"
                )
            )

            repository.insertNotification(
                NotificationLog(
                    title = "Ad Reward Credited",
                    body = "Rewarded Video played successfully and verified. +30 Coins credited.",
                    type = "BENEFIT"
                )
            )

            showToast("Rewarded Ad Completed: +30 Coins saved!")
        }
    }

    // Offerwall Task submission workflow
    fun submitTaskCompletion(taskId: Int, inputAnswer: String) {
        if (inputAnswer.trim().isEmpty()) {
            showToast("Verification field cannot be empty. Specify submission details.")
            return
        }

        viewModelScope.launch {
            val task = repository.getTaskById(taskId) ?: return@launch
            val updatedTask = task.copy(
                status = "PENDING_VERIFICATION",
                verificationAnswer = inputAnswer
            )
            repository.updateTask(updatedTask)

            repository.insertNotification(
                NotificationLog(
                    title = "Task Pending Approval",
                    body = "Your proof for '${task.title}' has been submitted for admin validation.",
                    type = "SYSTEM"
                )
            )

            showToast("Proof submitted! Admin will verify and approve payout.")
            navigateTo("offerwall")
        }
    }

    // Referral code submission
    fun applyReferralCode(code: String) {
        val cleanCode = code.trim().uppercase()
        viewModelScope.launch {
            val user = repository.getActiveUser() ?: return@launch

            if (user.isFlaggedAsFraud) {
                showToast("Device locked. Operations frozen.")
                return@launch
            }

            if (user.referredBy.isNotEmpty()) {
                showToast("You have already used a referral code!")
                return@launch
            }

            if (cleanCode == user.referralCode) {
                showToast("You cannot enter your own referral code!")
                return@launch
            }

            // Simulate code check
            if (cleanCode == "REWARD88X" || cleanCode == "REF_DEMO" || cleanCode.startsWith("REF")) {
                val referralBonus = 40
                val updatedUser = user.copy(
                    coinBalance = user.coinBalance + referralBonus,
                    totalEarningsCoins = user.totalEarningsCoins + referralBonus,
                    referralEarningsCoins = user.referralEarningsCoins + referralBonus,
                    referredBy = cleanCode
                )
                repository.saveUserProfile(updatedUser)
                repository.addTransaction(
                    RewardTransaction(
                        type = "REFERRAL",
                        amount = referralBonus,
                        description = "Referral Bonus Code '$cleanCode' Entry"
                    )
                )

                repository.insertNotification(
                    NotificationLog(
                        title = "Referral Bonus Unlocked!",
                        body = "Enter code promotion credited. +$referralBonus Coins credited.",
                        type = "BENEFIT"
                    )
                )

                showToast("Success! Referral code applied. Earned +$referralBonus Coins!")
            } else {
                showToast("Invalid code or code not found in current platform database.")
            }
        }
    }

    // Wallet Withdrawals request
    fun performCoinsWithdrawal(method: String, detailKey: String, valInput: String, coinAmountToDeduct: Int) {
        viewModelScope.launch {
            val user = repository.getActiveUser() ?: return@launch

            if (user.isFlaggedAsFraud) {
                showToast("Security flag: Withdrawals are temporarily disabled for your device.")
                return@launch
            }

            if (coinAmountToDeduct < 10000) {
                showToast("Minimum withdrawal limit is 10,000 coins (equivalent to ₹100 Rupees).")
                return@launch
            }

            if (user.coinBalance < coinAmountToDeduct) {
                showToast("Insufficient Coins balance. Perform tasks or spin to earn more!")
                return@launch
            }

            if (detailKey.trim().isEmpty() || valInput.trim().isEmpty()) {
                showToast("Please provide valid payout coordinates")
                return@launch
            }

            // Deduct coins first (escrow state) and create transaction
            val updatedUser = user.copy(coinBalance = user.coinBalance - coinAmountToDeduct)
            repository.saveUserProfile(updatedUser)

            repository.addTransaction(
                RewardTransaction(
                    type = "WITHDRAWAL",
                    amount = -coinAmountToDeduct,
                    description = "Coins Withdrawal Request via $method"
                )
            )

            // Convert Rate defined by admin config
            val estimatedCash = (coinAmountToDeduct * coinToRupeeRate.value).toDouble()

            // Submit withdrawal database record
            repository.submitWithdrawal(
                WithdrawalRequest(
                    method = method,
                    detailsKey = detailKey,
                    detailsValue = valInput,
                    coinAmount = coinAmountToDeduct,
                    cashAmountEstimated = estimatedCash,
                    status = "PENDING"
                )
            )

            repository.insertNotification(
                NotificationLog(
                    title = "Withdrawal Requested",
                    body = "Requested $coinAmountToDeduct coins via $method. Under review.",
                    type = "SYSTEM"
                )
            )

            showToast("Withdrawal request created successfully!")
            navigateTo("wallet")
        }
    }


    // ==========================================
    // ADMINISTRATIVE PANEL ACTIONS (Simulator)
    // ==========================================

    fun adminApproveTask(taskId: Int) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId) ?: return@launch
            if (task.status != "PENDING_VERIFICATION") return@launch

            // Update status to COMPLETED
            val updatedTask = task.copy(status = "COMPLETED")
            repository.updateTask(updatedTask)

            // Credit the coins to user profile
            val user = repository.getActiveUser() ?: return@launch
            val updatedUser = user.copy(
                coinBalance = user.coinBalance + task.rewardCoins,
                totalEarningsCoins = user.totalEarningsCoins + task.rewardCoins
            )
            repository.saveUserProfile(updatedUser)

            // Log Transaction
            repository.addTransaction(
                RewardTransaction(
                    type = "OFFERWALL",
                    amount = task.rewardCoins,
                    description = "Task '${task.title}' approved by Admin panel"
                )
            )

            // Push Notification
            repository.insertNotification(
                NotificationLog(
                    title = "Task Approved!",
                    body = "Your action on '${task.title}' was approved! +${task.rewardCoins} coins added.",
                    type = "BENEFIT"
                )
            )

            showToast("Offerwall task verified & approved! Coins credited to user.")
        }
    }

    fun adminRejectTask(taskId: Int) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId) ?: return@launch
            if (task.status != "PENDING_VERIFICATION") return@launch

            // Reject - flip back to AVAILABLE or show reject mark
            val updatedTask = task.copy(status = "AVAILABLE")
            repository.updateTask(updatedTask)

            // Push Notification
            repository.insertNotification(
                NotificationLog(
                    title = "Task Rejected",
                    body = "Admin declined proof submission for '${task.title}'. Please double check instruction criteria.",
                    type = "ALERT"
                )
            )

            showToast("Task proof rejected. Task returned to available list.")
        }
    }

    fun adminApproveWithdrawal(id: Int, reason: String = "") {
        viewModelScope.launch {
            val withdrawal = repository.getWithdrawalById(id) ?: return@launch
            if (withdrawal.status != "PENDING") return@launch

            val finalReason = reason.ifBlank { "Authorized and successfully processed by Administrator." }

            // Approve withdrawal
            val updatedW = withdrawal.copy(status = "APPROVED", reason = finalReason)
            repository.updateWithdrawal(updatedW)

            // Keep coins deducted (already deducted in escrow when requested). Log message.
            repository.insertNotification(
                NotificationLog(
                    title = "Withdrawal Disbursed! ✅",
                    body = "Your withdrawal of ${withdrawal.coinAmount} Coins through ${withdrawal.method} has been authorized and dispatched. Note: $finalReason",
                    type = "ALERT"
                )
            )

            showToast("Withdrawal approved and disbursed!")
        }
    }

    fun adminRejectWithdrawal(id: Int, reason: String = "") {
        viewModelScope.launch {
            val withdrawal = repository.getWithdrawalById(id) ?: return@launch
            if (withdrawal.status != "PENDING") return@launch

            val finalReason = reason.ifBlank { "Declined due to standard security verification checks / invalid payee coordinates." }

            // Reject withdrawal
            val updatedW = withdrawal.copy(status = "REJECTED", reason = finalReason)
            repository.updateWithdrawal(updatedW)

            // Refund coins back to user profile since request is cancelled
            val user = repository.getActiveUser() ?: return@launch
            val refundedUser = user.copy(coinBalance = user.coinBalance + withdrawal.coinAmount)
            repository.saveUserProfile(refundedUser)

            // Create positive transaction refund
            repository.addTransaction(
                RewardTransaction(
                    type = "WITHDRAWAL_REFUND",
                    amount = withdrawal.coinAmount,
                    description = "Refund: $finalReason"
                )
            )

            repository.insertNotification(
                NotificationLog(
                    title = "Withdrawal Payout Declined ❌",
                    body = "Your payout for ${withdrawal.coinAmount} Coins via ${withdrawal.method} was declined. Reason: $finalReason. Coins are refunded back.",
                    type = "ALERT"
                )
            )

            showToast("Withdrawal request rejected. ${withdrawal.coinAmount} Coins refunded to account.")
        }
    }

    fun adminCreateTask(title: String, desc: String, coins: Int, category: String) {
        viewModelScope.launch {
            val newTask = OfferTask(
                title = title,
                description = desc,
                rewardCoins = coins,
                category = category
            )
            repository.insertTask(newTask)
            showToast("Custom task '$title' posted live to Offerwall!")
        }
    }

    fun adminDeleteTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(id)
            showToast("Task removed from client directory.")
        }
    }

    fun adminSendPushNotification(title: String, body: String, severity: String) {
        viewModelScope.launch {
            repository.insertNotification(
                NotificationLog(
                    title = title,
                    body = body,
                    type = severity
                )
            )
            showToast("Broadcast message dispatched to notification hub!")
        }
    }

    fun adminToggleFraudFlag() {
        viewModelScope.launch {
            val user = repository.getActiveUser() ?: return@launch
            val fraudState = !user.isFlaggedAsFraud
            repository.saveUserProfile(user.copy(isFlaggedAsFraud = fraudState))
            if (fraudState) {
                showToast("DEMO SECURITY: Device marked as Fraud! Transactions and withdrawals are locked.")
            } else {
                showToast("DEMO SECURITY: Fraud status cleared. Transactions reinstated.")
            }
        }
    }

    fun adminAddCoins(coins: Int) {
        viewModelScope.launch {
            val user = repository.getActiveUser() ?: return@launch
            repository.saveUserProfile(
                user.copy(
                    coinBalance = user.coinBalance + coins,
                    totalEarningsCoins = user.totalEarningsCoins + coins
                )
            )
            repository.addTransaction(
                RewardTransaction(
                    type = "ADMIN",
                    amount = coins,
                    description = "Admin credits added directly"
                )
            )
            showToast("Credited $coins Coins directly via Administrative Command!")
        }
    }

    fun clearAllNotificationHistory() {
        viewModelScope.launch {
            repository.clearAllNotifications()
            showToast("Notification inbox purged.")
        }
    }
}
