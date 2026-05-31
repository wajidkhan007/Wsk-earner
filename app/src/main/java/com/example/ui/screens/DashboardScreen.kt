package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RewardTransaction
import com.example.data.model.UserProfile
import com.example.ui.RewardViewModel
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: RewardViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val profile = userProfile ?: UserProfile()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Bar Simulation Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.wsk_logo_1780118863187),
                        contentDescription = "WSK EARN Logo",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )

                    Column {
                        Text(
                            text = "Good morning,",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }

                // Security Tag
                Surface(
                    shape = CircleShape,
                    color = if (profile.isFlaggedAsFraud) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else Color(0xFF00E676).copy(alpha = 0.1f),
                    contentColor = if (profile.isFlaggedAsFraud) MaterialTheme.colorScheme.error else Color(0xFF00C853),
                    modifier = Modifier.testTag("security_indicator")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (profile.isFlaggedAsFraud) Icons.Default.Lock else Icons.Default.VerifiedUser,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (profile.isFlaggedAsFraud) MaterialTheme.colorScheme.error else Color(0xFF00C853)
                        )
                        Text(
                            text = if (profile.isFlaggedAsFraud) "BLOCKED" else "VERIFIED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Welcome and Balance Card Gradient Deck
        item {
            val coinToRupeeRate by viewModel.coinToRupeeRate.collectAsState()
            BalanceBoardCard(
                profile = profile,
                coinToRupeeRate = coinToRupeeRate,
                onWithdrawClick = { viewModel.navigateTo("wallet") },
                onHistoryClick = { viewModel.navigateTo("wallet") }
            )
        }

        // Daily Attendance Slate Card
        item {
            DailyAttendanceCard(streak = profile.checkInStreak) {
                viewModel.navigateTo("checkin")
            }
        }

        // Quick Earning Grid Title
        item {
            Text(
                text = "Quick Earning Zones",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        // Grid contents
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SleekGridCard(
                        title = "Lucky Spin",
                        subtitle = "wheel rotations daily",
                        emoji = "🎡",
                        containerColor = Color(0xFFFFF3E0), // orange-100
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo("spin")
                    }

                    SleekGridCard(
                        title = "Scratch Card",
                        subtitle = "scratch and win",
                        emoji = "✨",
                        containerColor = Color(0xFFF3E5F5), // purple-100
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo("scratch")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SleekGridCard(
                        title = "Watch Ads",
                        subtitle = "instant video coin sets",
                        emoji = "📺",
                        containerColor = Color(0xFFE8F5E9), // green-100
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo("ads")
                    }

                    SleekGridCard(
                        title = "Offerwalls",
                        subtitle = "rewards for tasks",
                        emoji = "📋",
                        containerColor = Color(0xFFE3F2FD), // blue-100
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo("offerwall")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SleekGridCard(
                        title = "Web Portal Tasks",
                        subtitle = "visit sites & earn cash",
                        emoji = "🌐",
                        containerColor = Color(0xFFE0F7FA), // cyan-100
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.navigateTo("web_tasks")
                    }
                }
            }
        }

        // Interactive Commercial Ad Placement
        item {
            com.example.ui.components.AdBannerComponent(viewModel = viewModel)
        }

        // Sleek Referral Banner purple-indigo
        item {
            SleekReferralBanner(referralCode = profile.referralCode) {
                viewModel.navigateTo("referral")
            }
        }

        // Transactions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity History",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF0F172A)
                )

                TextButton(onClick = { viewModel.navigateTo("wallet") }) {
                    Text(
                        text = "View History",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Recent transaction list items
        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFFE2E8F0),
                                Color(0xFFF1F5F9)
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No activities logged yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Claim daily or play activities to earn coins!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(transactions.take(5)) { trx ->
                TransactionRowItem(trx)
            }
        }
    }
}

@Composable
fun BalanceBoardCard(profile: UserProfile, coinToRupeeRate: Float, onWithdrawClick: () -> Unit, onHistoryClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("balance_board_card"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0061A4),
                            Color(0xFF003865)
                        )
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Available Balance",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    val estimatedInr = profile.coinBalance * coinToRupeeRate
                    Text(
                        text = String.format("₹%.2f", estimatedInr),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                }

                // Balance Pill Container
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 11.sp
                        )
                        Text(
                            text = String.format("%,d COINS", profile.coinBalance),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            // Dual Button row matching theme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onWithdrawClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0061A4)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(
                        text = "Withdraw",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = onHistoryClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        )
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(
                        text = "History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DailyAttendanceCard(streak: Int, onClaimClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClaimClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1E2EC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📅", fontSize = 20.sp)
                }

                Column {
                    Text(
                        text = "Daily Check-in",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Streak: $streak Days",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = onClaimClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0061A4),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "CLAIM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SleekGridCard(
    title: String,
    subtitle: String,
    emoji: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(115.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    Color(0xFFE2E8F0),
                    Color(0xFFF1F5F9)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(containerColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = Color(0xFF0F172A),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SleekReferralBanner(referralCode: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6), // purple-600
                            Color(0xFF4F46E5)  // indigo-600
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Refer & Earn $10",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Share with companions & friends",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = referralCode.ifBlank { "EARN2024" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionRowItem(trx: RewardTransaction) {
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(trx.timestamp))

    val (icon, tint) = when (trx.type) {
        "CHECK_IN" -> Pair(Icons.Default.CalendarToday, MaterialTheme.colorScheme.primary)
        "SPIN" -> Pair(Icons.Default.Toys, Color(0xFFFF9100))
        "SCRATCH" -> Pair(Icons.Default.SquareFoot, Color(0xFFAA00FF))
        "OFFERWALL" -> Pair(Icons.Default.Task, Color(0xFF0061A4))
        "AD_WATCH" -> Pair(Icons.Default.PlayCircle, Color(0xFF00C853))
        "REFERRAL" -> Pair(Icons.Default.CardGiftcard, Color(0xFFFF5252))
        "WITHDRAWAL" -> Pair(Icons.Default.AccountBalanceWallet, CrimsonError)
        "WITHDRAWAL_REFUND" -> Pair(Icons.Default.Refresh, EmeraldSuccess)
        else -> Pair(Icons.Default.Stars, MaterialTheme.colorScheme.primary)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    Color(0xFFE2E8F0),
                    Color(0xFFF1F5F9)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trx.description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }

            Text(
                text = "${if (trx.amount >= 0) "+" else ""}${trx.amount} 🪙",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = if (trx.amount >= 0) EmeraldSuccess else CrimsonError
            )
        }
    }
}
