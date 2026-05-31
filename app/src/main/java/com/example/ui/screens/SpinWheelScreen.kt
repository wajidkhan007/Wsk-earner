package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.RewardViewModel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinWheelScreen(viewModel: RewardViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val profile = userProfile ?: UserProfile()
    val spinLimit by viewModel.spinWheelLimit.collectAsState()

    val segments = listOf(
        Pair(30, "30 Coins"),
        Pair(25, "25 Coins"),
        Pair(45, "Lucky 45"),
        Pair(20, "20 Coins"),
        Pair(50, "50 Coins"),
        Pair(35, "35 Coins"),
        Pair(40, "Bonus 40"),
        Pair(22, "22 Coins")
    )

    val segmentAngle = 360f / segments.size
    val rotationState = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var isSpinning by remember { mutableStateOf(false) }
    var currentWinningSegmentLabel by remember { mutableStateOf("") }
    var winningRewardAmount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper stats banner
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
                        text = "Spins Used today",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${profile.dailySpinsUsed} / $spinLimit Spins",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${spinLimit - profile.dailySpinsUsed} Remaining",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Wheel spacer and layout
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Top pointer arrow
                Icon(
                    imageVector = Icons.Default.ArrowDropUp,
                    contentDescription = "Target",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(54.dp)
                        .rotate(180f)
                )

                // The Wheel drawing canvas wrapper
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .rotate(rotationState.value),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasRadius = size.minDimension / 2
                        val center = Offset(size.width / 2, size.height / 2)

                        // Segment arcs drawing colors
                        val colorsOfWheel = listOf(
                            Color(0xFF29B6F6), Color(0xFFAB47BC), Color(0xFFFF7043), Color(0xFF26A69A),
                            Color(0xFFFFB300), Color(0xFF8D6E63), Color(0xFFEC407A), Color(0xFF26ADE4)
                        )

                        for (i in segments.indices) {
                            val startAngle = i * segmentAngle
                            drawArc(
                                color = colorsOfWheel[i % colorsOfWheel.size],
                                startAngle = startAngle,
                                sweepAngle = segmentAngle,
                                useCenter = true,
                                size = Size(size.width, size.height)
                            )
                        }

                        // Outline Stroke circle bounds
                        drawCircle(
                            color = Color.White.copy(alpha = 0.4f),
                            radius = canvasRadius,
                            center = center,
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }

                    // Content text values overlay (numbers placed on segment pivots)
                    for (i in segments.indices) {
                        val rotationOfText = i * segmentAngle + (segmentAngle / 2)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(rotationOfText),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = "${segments[i].first}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 22.dp)
                            )
                        }
                    }

                    // Center peg/cap button hub
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White, CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color.White, MaterialTheme.colorScheme.primary)
                                ), CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Hub",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Result declaration text
        if (currentWinningSegmentLabel.isNotEmpty() && !isSpinning) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Congratulations! Won $winningRewardAmount Coins ($currentWinningSegmentLabel)!",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(14.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Spacer(modifier = Modifier.height(30.dp))
        }

        // Action Trigger Button
        Button(
            onClick = {
                if (profile.dailySpinsUsed >= spinLimit) {
                    viewModel.showToast("Daily spins limit reached. Unlock tasks for more options.")
                    return@Button
                }

                if (!isSpinning) {
                    isSpinning = true
                    // Random destination segment index (0 to 7)
                    val winningIndex = Random.nextInt(segments.size)
                    val targetLabel = segments[winningIndex].second
                    val rewardedAmount = segments[winningIndex].first

                    // Trigonometry calculations for exact stopping point
                    // We spin several full circles + precise angle matching. Center is top pointer at 270f offset!
                    val segmentOffset = 270f - (winningIndex * segmentAngle) - (segmentAngle / 2)
                    val totalRevolutions = 360f * 6 // 6 full rolls
                    val targetRotation = totalRevolutions + segmentOffset

                    coroutineScope.launch {
                        // Reset rotation state first if currently high
                        if (rotationState.value > 1000f) {
                            rotationState.snapTo(rotationState.value % 360f)
                        }
                        rotationState.animateTo(
                            targetValue = targetRotation,
                            animationSpec = tween(durationMillis = 3500)
                        )
                        isSpinning = false
                        currentWinningSegmentLabel = targetLabel
                        winningRewardAmount = rewardedAmount
                        viewModel.performSpinWheelResult(rewardedAmount, targetLabel)
                    }
                }
            },
            enabled = !isSpinning,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("spin_wheel_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.RotateRight, contentDescription = null)
                Text(
                    text = if (isSpinning) "Spinning Lucky..." else "SPIN WHEEL NOW",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Description tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "Server-side reward checks are invoked. Any virtual emulator or auto-clicker tampering is banned instantly by security algorithms.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Sponsored Ad Display
        com.example.ui.components.AdBannerComponent(viewModel = viewModel)
    }
}
