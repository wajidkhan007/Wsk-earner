package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RewardViewModel
import kotlinx.coroutines.delay

data class SponsoredAd(
    val title: String,
    val description: String,
    val actionText: String,
    val colorScheme: List<Color>,
    val url: String = "https://play.google.com"
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AdBannerComponent(
    viewModel: RewardViewModel,
    modifier: Modifier = Modifier
) {
    var isClosed by remember { mutableStateOf(false) }

    if (isClosed) return

    val adCampaigns = listOf(
        SponsoredAd(
            title = "OctaFX Forex Trading",
            description = "Trade USD/INR with 0% commissions. Sign up today and claim 100% deposit bonus!",
            actionText = "CLAIM BONUS",
            colorScheme = listOf(Color(0xFF0F172A), Color(0xFF1E3A8A))
        ),
        SponsoredAd(
            title = "Zomato: Flat 50% OFF",
            description = "Craving hot Biryani or Crispy Dosa? Order right now and get instant 50% Discount on first 3 orders!",
            actionText = "ORDER NOW",
            colorScheme = listOf(Color(0xFFE11D48), Color(0xFFBE123C))
        ),
        SponsoredAd(
            title = "Dream11: Win ₹1 Crore Today",
            description = "Make your fantasy cricket squad for India vs Pakistan match. Entry fee starts at just ₹49 Rupees!",
            actionText = "PLAY NOW",
            colorScheme = listOf(Color(0xFFDC2626), Color(0xFF7F1D1D))
        ),
        SponsoredAd(
            title = "Groww: Mutual Funds & Stocks",
            description = "Start your SIP from just ₹100 Rupees. 0 account maintenance fees. Highly secure & intuitive dashboard.",
            actionText = "INVEST FREE",
            colorScheme = listOf(Color(0xFF0D9488), Color(0xFF115E59))
        ),
        SponsoredAd(
            title = "Ludo Empire: Real Cash Game",
            description = "Play Ludo with online competitors and withdraw winnings instantly via UPI, Paytm or Bank Transfer!",
            actionText = "GET ₹10 FREE",
            colorScheme = listOf(Color(0xFF4F46E5), Color(0xFF312E81))
        )
    )

    var currentAdIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(12000) // Rotate ads every 12 seconds
            currentAdIndex = (currentAdIndex + 1) % adCampaigns.size
        }
    }

    val activeAd = adCampaigns[currentAdIndex]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                viewModel.showToast("Sponsor Clicked: Redirecting securely to download page... You received +5 Ad Clicking Bonus Coins! 🎁")
                // Gift the user a small reward
                viewModel.adminAddCoins(5)
            },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = activeAd.colorScheme
                    )
                )
                .padding(10.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFC107),
                            contentColor = Color.Black
                        ) {
                            Text(
                                text = "AD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = activeAd.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Close ad button
                    IconButton(
                        onClick = { isClosed = true },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Ad",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Description
                Text(
                    text = activeAd.description,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Call to action bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Interact to earn bonus",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.showToast("Sponsor Link Opened: You received +5 Coins! 🪙")
                            viewModel.adminAddCoins(5)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = activeAd.colorScheme.first()
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = activeAd.actionText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
