package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.RewardViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize modern viewmodel integration
                val viewModel: RewardViewModel = viewModel()
                val context = LocalContext.current
                
                // Active alerts observe and display native snack toast
                val toastMessage by viewModel.uiMessage.collectAsState()
                LaunchedEffect(toastMessage) {
                    toastMessage?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                val isAuthenticated by viewModel.isAuthenticated.collectAsState()
                val currentScreen by viewModel.currentScreen.collectAsState()
                val notifications by viewModel.notifications.collectAsState()
                val userProfile by viewModel.userProfile.collectAsState()
                val isAdmin = userProfile?.email?.trim()?.lowercase(java.util.Locale.ROOT) == "wajidwajidkhan203@gmail.com"

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (!isAuthenticated) {
                            LoginScreen(viewModel)
                        } else {
                            // High-fidelty main frame Scaffold for authenticated user
                            Scaffold(
                                contentWindowInsets = WindowInsets.safeDrawing,
                                bottomBar = {
                                    NavigationBar(
                                        modifier = Modifier.testTag("app_bottom_bar")
                                        // Respect standard system navigation bars automatically via edgeToEdge Scaffold properties
                                    ) {
                                        NavigationBarItem(
                                            selected = currentScreen == "home" || currentScreen == "checkin" || currentScreen == "spin" || currentScreen == "scratch" || currentScreen == "offerwall" || currentScreen == "task_detail" || currentScreen == "ads" || currentScreen == "referral",
                                            onClick = { viewModel.navigateTo("home") },
                                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                            label = { Text("Earning") },
                                            modifier = Modifier.testTag("nav_home")
                                        )

                                        NavigationBarItem(
                                            selected = currentScreen == "leaderboard",
                                            onClick = { viewModel.navigateTo("leaderboard") },
                                            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard") },
                                            label = { Text("Ranks") },
                                            modifier = Modifier.testTag("nav_ranks")
                                        )

                                        NavigationBarItem(
                                            selected = currentScreen == "wallet",
                                            onClick = { viewModel.navigateTo("wallet") },
                                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                                            label = { Text("Wallet") },
                                            modifier = Modifier.testTag("nav_wallet")
                                        )

                                        NavigationBarItem(
                                            selected = currentScreen == "notifications",
                                            onClick = { viewModel.navigateTo("notifications") },
                                            icon = {
                                                BadgedBox(
                                                    badge = {
                                                        if (notifications.isNotEmpty()) {
                                                            Badge { Text("${notifications.size}") }
                                                        }
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                                                }
                                            },
                                            label = { Text("Inbox") },
                                            modifier = Modifier.testTag("nav_inbox")
                                        )

                                        if (isAdmin) {
                                            NavigationBarItem(
                                                selected = currentScreen == "admin",
                                                onClick = { viewModel.navigateTo("admin") },
                                                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                                                label = { Text("Admin") },
                                                modifier = Modifier.testTag("nav_admin")
                                            )
                                        }
                                    }
                                }
                            ) { nestedScaffoldPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(nestedScaffoldPadding)
                                ) {
                                    // Handle state-based child navigation views
                                    when (currentScreen) {
                                        "home" -> DashboardScreen(viewModel)
                                        "checkin" -> CheckInScreen(viewModel)
                                        "spin" -> SpinWheelScreen(viewModel)
                                        "scratch" -> ScratchCardScreen(viewModel)
                                        "offerwall", "task_detail" -> OfferwallScreen(viewModel)
                                        "ads" -> WatchAdsScreen(viewModel)
                                        "referral" -> ReferralScreen(viewModel)
                                        "leaderboard" -> LeaderboardScreen(viewModel)
                                        "wallet" -> WalletScreen(viewModel)
                                        "notifications" -> NotificationsScreen(viewModel)
                                        "admin" -> AdminPanelScreen(viewModel)
                                        "web_tasks" -> WebTasksScreen(viewModel)
                                        else -> DashboardScreen(viewModel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
