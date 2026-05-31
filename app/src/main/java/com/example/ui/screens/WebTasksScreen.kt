package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.RewardViewModel
import kotlinx.coroutines.delay

data class WebAppPreset(
    val title: String,
    val domain: String,
    val url: String,
    val emoji: String,
    val description: String,
    val accentColor: Color
)

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebTasksScreen(viewModel: RewardViewModel) {
    var urlInput by remember { mutableStateOf("https://www.google.com") }
    var activeUrl by remember { mutableStateOf("") }
    var isWebLoading by remember { mutableStateOf(false) }
    var webProgress by remember { mutableStateOf(0) }
    
    // Surf to earn mechanics
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerProgress by remember { mutableStateOf(30) } // 30 seconds countdown
    var totalCoinsWonInSession by remember { mutableStateOf(0) }
    var showRewardCelebration by remember { mutableStateOf(false) }

    val presets = listOf(
        WebAppPreset(
            title = "Google Search",
            domain = "google.com",
            url = "https://www.google.com",
            emoji = "🔍",
            description = "Search dynamic results, articles, and premium blogs online.",
            accentColor = Color(0xFF4285F4)
        ),
        WebAppPreset(
            title = "Wikipedia Info",
            domain = "wikipedia.org",
            url = "https://en.wikipedia.org",
            emoji = "📖",
            description = "Search encyclopedia trivia, history, and find answers to online tasks.",
            accentColor = Color(0xFF2C3E50)
        ),
        WebAppPreset(
            title = "WSK Earn Blog",
            domain = "wskearnapp.blogspot.com",
            url = "https://www.wikipedia.org", // fall back elegant info web page
            emoji = "📰",
            description = "Officially integrated web blog. Read guides and check announcements.",
            accentColor = Color(0xFFFFA000)
        ),
        WebAppPreset(
            title = "Crazy Games Portal",
            domain = "crazygames.com",
            url = "https://www.crazygames.com",
            emoji = "🎮",
            description = "Access high-earning premium HTML5 online games within WSK.",
            accentColor = Color(0xFFE040FB)
        )
    )

    // Earning timer trigger effect
    LaunchedEffect(activeUrl, isTimerRunning) {
        if (activeUrl.isNotBlank() && isTimerRunning) {
            while (timerProgress > 0) {
                delay(1000)
                if (isTimerRunning) {
                    timerProgress--
                }
            }
            if (timerProgress == 0) {
                viewModel.adminAddCoins(15) // Adds +15 reward coins
                totalCoinsWonInSession += 15
                showRewardCelebration = true
                isTimerRunning = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("web_tasks_screen_root")
    ) {
        // App top navigation bar styled like a mini web portal header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D1B2A),
                            Color(0xFF1B263B)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (activeUrl.isNotBlank()) {
                                    activeUrl = ""
                                    timerProgress = 30
                                    isTimerRunning = false
                                } else {
                                    viewModel.navigateTo("home")
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = if (activeUrl.isNotBlank()) "WSK Web Reader" else "WSK Website Portal",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700)
                        )
                    }

                    // Total coins accumulated badge in session
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD700).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🪙", fontSize = 12.sp)
                            Text(
                                text = "+$totalCoinsWonInSession Coins",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }

                // If inside active browsing, show progressive ticking bar & controls
                if (activeUrl.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Timer / Reward indicator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (timerProgress == 0) Color(0xFF00E676).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (timerProgress == 0) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = if (timerProgress == 0) Color(0xFF00E676) else Color(0xFFFFC107),
                                    modifier = Modifier.size(16.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (timerProgress == 0) "Seconds target complete!" else "Surf web app for $timerProgress seconds to win:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    // Progress row
                                    val progressRatio = (30f - timerProgress.toFloat()) / 30f
                                    LinearProgressIndicator(
                                        progress = { progressRatio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .padding(top = 2.dp)
                                            .clip(CircleShape),
                                        color = Color(0xFF00E676),
                                        trackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                }
                                Text(
                                    text = "+15 🪙",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }

                        // Play/Pause surf timer icon
                        if (timerProgress > 0) {
                            IconButton(
                                onClick = { isTimerRunning = !isTimerRunning },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (isTimerRunning) Color(0xFFDC2626) else Color(0xFF00C853)
                                ),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Control Timer",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            // Reset/Reclaim button
                            Button(
                                onClick = {
                                    timerProgress = 30
                                    isTimerRunning = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Surf More", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Active View State Controller
        if (activeUrl.isBlank()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive dynamic introductory banner info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🌐", fontSize = 34.sp)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Earn from Web Apps & Portals",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Turn online websites, dynamic HTML5 app portals, blogs, and search results into rewarding experiences. Win automatic coins by staying connected.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // URL Input card form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Enter Custom Web Portal Address",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedTextField(
                                value = urlInput,
                                onValueChange = { urlInput = it },
                                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                                placeholder = { Text("https://example.com") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    imeAction = ImeAction.Go
                                ),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        var cleanUrl = urlInput.trim()
                                        if (cleanUrl.isNotBlank()) {
                                            if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                                                cleanUrl = "https://$cleanUrl"
                                            }
                                            activeUrl = cleanUrl
                                            isTimerRunning = true
                                        }
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    var cleanUrl = urlInput.trim()
                                    if (cleanUrl.isBlank()) return@Button
                                    if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                                        cleanUrl = "https://$cleanUrl"
                                    }
                                    activeUrl = cleanUrl
                                    isTimerRunning = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Open Web Portal Wrapper", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Grid label
                item {
                    Text(
                        text = "Verified Earning Presets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Grid lists
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presets.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { preset ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                activeUrl = preset.url
                                                timerProgress = 30
                                                isTimerRunning = true
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, preset.accentColor.copy(alpha = 0.2f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = preset.emoji, fontSize = 22.sp)
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = preset.accentColor.copy(alpha = 0.1f),
                                                    contentColor = preset.accentColor
                                                ) {
                                                    Text(
                                                        text = "15 COINS",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                Text(
                                                    text = preset.title,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = preset.domain,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 9.sp,
                                                    color = preset.accentColor
                                                )
                                            }

                                            Text(
                                                text = preset.description,
                                                fontSize = 9.sp,
                                                lineHeight = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Interactive web browser view
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(Color.White)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                databaseEnabled = true
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isWebLoading = true
                                    super.onPageStarted(view, url, favicon)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isWebLoading = false
                                    super.onPageFinished(view, url)
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    webProgress = newProgress
                                    super.onProgressChanged(view, newProgress)
                                }
                            }
                            loadUrl(activeUrl)
                        }
                    },
                    update = { view ->
                        if (view.url != activeUrl) {
                            view.loadUrl(activeUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isWebLoading) {
                    LinearProgressIndicator(
                        progress = { webProgress.toFloat() / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = Color(0xFFFFD700)
                    )
                }
            }
        }
    }

    // Interactive reward celebration popup
    if (showRewardCelebration) {
        AlertDialog(
            onDismissRequest = { showRewardCelebration = false },
            confirmButton = {
                Button(
                    onClick = { showRewardCelebration = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Superb!", fontWeight = FontWeight.Black)
                }
            },
            icon = {
                Text("🎁", fontSize = 42.sp)
            },
            title = {
                Text("Portal Reward Claimed!", fontWeight = FontWeight.Black, fontSize = 20.sp)
            },
            text = {
                Text(
                    text = "Congratulations! You browsed the dynamic app portal for 30 seconds and received +15 Coins added directly into your wallet database. Keep visiting to unlock unlimited cash sets!",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
