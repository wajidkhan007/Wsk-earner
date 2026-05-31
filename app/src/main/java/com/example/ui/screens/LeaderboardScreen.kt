package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RewardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(viewModel: RewardViewModel) {
    var activeRankTab by remember { mutableStateOf("WEEKLY") } // "WEEKLY", "MONTHLY"

    val mockLeaderboardWeekly = listOf(
        Pair("Sarah Parker", 18450),
        Pair("James Miller", 14200),
        Pair("Mia Wang", 12100),
        Pair("Alex Carter", 9500),
        Pair("Elena Rostova", 8200),
        Pair("David Beckham", 7300),
        Pair("Ravi Shankar", 6400),
        Pair("Chloe Jenkins", 5500)
    )

    val mockLeaderboardMonthly = listOf(
        Pair("Mia Wang", 45200),
        Pair("Alex Carter", 38400),
        Pair("James Miller", 32100),
        Pair("Sarah Parker", 29500),
        Pair("Ravi Shankar", 21300),
        Pair("Elena Rostova", 18200),
        Pair("David Beckham", 17300),
        Pair("Chloe Jenkins", 15500)
    )

    val currentRanks = if (activeRankTab == "WEEKLY") mockLeaderboardWeekly else mockLeaderboardMonthly

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("WEEKLY", "MONTHLY").forEach { tab ->
                val isSelected = activeRankTab == tab
                Button(
                    onClick = { activeRankTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text(tab, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Gold trophy header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text("Rankings updated in real time", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Top 3 active users receive extra streak bonuses each Sunday!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }

        // Podium View
        if (currentRanks.size >= 3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Left)
                PodiumColumn(
                    name = currentRanks[1].first,
                    coins = currentRanks[1].second,
                    rank = "2",
                    heightDp = 100.dp,
                    color = Color(0xFF90A4AE),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // 1st Place (Center - tallest)
                PodiumColumn(
                    name = currentRanks[0].first,
                    coins = currentRanks[0].second,
                    rank = "1",
                    heightDp = 135.dp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1.2f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // 3rd Place (Right)
                PodiumColumn(
                    name = currentRanks[2].first,
                    coins = currentRanks[2].second,
                    rank = "3",
                    heightDp = 80.dp,
                    color = Color(0xFFA1887F),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Ranked list scroll
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val listAfterPodium = currentRanks.drop(3)
            itemsIndexed(listAfterPodium) { idx, userRank ->
                val exactRank = idx + 4
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$exactRank",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                        Text(
                            text = userRank.first,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${userRank.second}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumColumn(
    name: String,
    coins: Int,
    rank: String,
    heightDp: androidx.compose.ui.unit.Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    val podiumGradient = Brush.verticalGradient(
        listOf(color, color.copy(alpha = 0.4f))
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Name Overlay
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        // Score Overlay
        Text(
            text = "${coins}🪙",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Physical graphic pillar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(podiumGradient),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Huge dynamic rank digit
                Text(
                    text = rank,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = when(rank) {
                        "1" -> "Gold"
                        "2" -> "Silver"
                        else -> "Bronze"
                    },
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
