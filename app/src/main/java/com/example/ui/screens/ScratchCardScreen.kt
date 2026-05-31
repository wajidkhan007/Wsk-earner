package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.RewardViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScratchCardScreen(viewModel: RewardViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val profile = userProfile ?: UserProfile()
    val scratchLimit by viewModel.scratchCardLimit.collectAsState()

    // Random coins awarded inside this specific active card session (ranges 20 - 50 coins)
    var rewardCoinsInCard by remember { mutableStateOf(Random.nextInt(20, 51)) }
    
    // Pointer drag coordinate paths list representing scratched out paths
    val scratchPoints = remember { mutableStateListOf<Offset>() }
    var isScratchedSuccessfully by remember { mutableStateOf(false) }

    // Re-initialize card state helper
    fun renewScratchCard() {
        scratchPoints.clear()
        isScratchedSuccessfully = false
        rewardCoinsInCard = Random.nextInt(20, 51)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper boundaries stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Scratches Used today",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${profile.dailyScrachesUsed} / $scratchLimit Cards",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${scratchLimit - profile.dailyScrachesUsed} Cards Left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Text(
            text = "Scratch Silver Board",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        // Physical Scratch interactive area
        Box(
            modifier = Modifier
                .size(width = 280.dp, height = 280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .testTag("scratch_interactive_box"),
            contentAlignment = Alignment.Center
        ) {
            // BACK LAYER: Displays the Coin rewards under the mask
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )

                Text(
                    text = "LUCKY REWARD!",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Text(
                    text = "+$rewardCoinsInCard",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Claimed successfully into wallet balance",
                    fontSize = 11.sp,
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // FRONT LAYER: Silver coating canvas that gets scratched away
            if (!isScratchedSuccessfully) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            if (profile.dailyScrachesUsed >= scratchLimit) {
                                viewModel.showToast("Daily scratch limit of $scratchLimit exceeded. Check back later!")
                                return@pointerInput
                            }

                            detectDragGestures(
                                onDragStart = {},
                                onDragEnd = {
                                    // Threshold test: If user created enough paths (e.g. drag lines), reveal content!
                                    if (scratchPoints.size > 28) {
                                        isScratchedSuccessfully = true
                                        viewModel.performScratchCardResult(rewardCoinsInCard)
                                    }
                                },
                                onDragCancel = {},
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    scratchPoints.add(change.position)
                                }
                            )
                        }
                ) {
                    // Create an off-screen layer to draw grey base, then erase scratched paths
                    drawContext.canvas.withSaveLayer(
                        bounds = androidx.compose.ui.geometry.Rect(Offset.Zero, size),
                        paint = Paint()
                    ) {
                        // Silver Grey coating cover
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFCFD8DC), Color(0xFF90A4AE))
                            ),
                            size = size
                        )

                        // Erase paths utilizing BlendMode.Clear
                        scratchPoints.forEach { point ->
                            drawCircle(
                                color = Color.Transparent,
                                radius = 28.dp.toPx(),
                                center = point,
                                blendMode = BlendMode.Clear
                            )
                        }

                        // Grid texture visual decoration on the silver card overlay
                        drawRect(
                            color = Color.White.copy(alpha = 0.15f),
                            size = size,
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }
                }
            }
        }

        // Informative guidance based on status
        if (!isScratchedSuccessfully) {
            Text(
                text = "💡 Swipe back and forth across the silver card above continuously to scratch and unlock your coin code!",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00E676).copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Lucky Winner! Claimed $rewardCoinsInCard Coins successfully! 🎉",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF008030),
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = { renewScratchCard() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("next_scratch_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text("Get Next Reward Card", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom fraud warning tag
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
                Text(
                    text = "Multiple IP scraping, screen spoofing, and cloning will result in immediate bans.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Sponsored Ad Display
        com.example.ui.components.AdBannerComponent(viewModel = viewModel)
    }
}
