package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OfferTask
import com.example.ui.RewardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferwallScreen(viewModel: RewardViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val tasks by viewModel.offerTasks.collectAsState()
    val activeTaskId by viewModel.activeTaskId.collectAsState()

    if (currentScreen == "task_detail" && activeTaskId != null) {
        val selectedTask = tasks.find { it.id == activeTaskId }
        if (selectedTask != null) {
            TaskDetailsSubScreen(selectedTask, viewModel)
        } else {
            viewModel.navigateTo("offerwall")
        }
    } else {
        OfferTaskListSubScreen(tasks, viewModel)
    }
}

@Composable
fun OfferTaskListSubScreen(tasks: List<OfferTask>, viewModel: RewardViewModel) {
    var selectedCategory by remember { mutableStateOf("ALL") }

    val filteredTasks = if (selectedCategory == "ALL") {
        tasks
    } else {
        tasks.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Category Selector Custom Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf("ALL", "APPS", "SURVEYS", "GAMES")
            for (cat in categories) {
                val isSelected = selectedCategory == cat
                Button(
                    onClick = { selectedCategory = cat },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text(text = cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // List tasks
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Task,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "No available offers under this section.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).testTag("offer_tasks_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTasks) { task ->
                    OfferTaskRowItem(task) {
                        viewModel.viewTaskDetails(task.id)
                    }
                }
            }
        }
    }
}

@Composable
fun OfferTaskRowItem(task: OfferTask, onClick: () -> Unit) {
    val (icon, color) = when (task.category) {
        "APPS" -> Pair(Icons.Default.Download, MaterialTheme.colorScheme.tertiary)
        "SURVEYS" -> Pair(Icons.Default.Poll, MaterialTheme.colorScheme.primary)
        "GAMES" -> Pair(Icons.Default.SportsEsports, Color(0xFFE040FB))
        else -> Pair(Icons.Default.Task, MaterialTheme.colorScheme.secondary)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Task Status Indicator chips
                Spacer(modifier = Modifier.height(4.dp))
                val borderCol = when (task.status) {
                    "AVAILABLE" -> Color(0xFF29B6F6)
                    "PENDING_VERIFICATION" -> Color(0xFFFF9100)
                    "COMPLETED" -> Color(0xFF00E676)
                    else -> MaterialTheme.colorScheme.outline
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = borderCol.copy(alpha = 0.08f),
                    contentColor = borderCol
                ) {
                    Text(
                        text = when (task.status) {
                            "AVAILABLE" -> "READY TO EARN"
                            "PENDING_VERIFICATION" -> "PENDING VERIFICATION"
                            "COMPLETED" -> "APPROVED"
                            else -> task.status
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${task.rewardCoins}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Coins",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsSubScreen(task: OfferTask, viewModel: RewardViewModel) {
    var verifyInput by remember { mutableStateOf("") }

    val (icon, tint) = when (task.category) {
        "APPS" -> Pair(Icons.Default.Download, MaterialTheme.colorScheme.tertiary)
        "SURVEYS" -> Pair(Icons.Default.Poll, MaterialTheme.colorScheme.primary)
        "GAMES" -> Pair(Icons.Default.SportsEsports, Color(0xFFE040FB))
        else -> Pair(Icons.Default.Task, MaterialTheme.colorScheme.secondary)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("offerwall") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Offer Verification Board",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Details Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(tint.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Category: ${task.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = tint
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "+${task.rewardCoins} Reward Coins",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (task.status == "AVAILABLE") {
            // Task workflow fields
            Text(
                text = "SUBMIT COMPLETION PROOF",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = verifyInput,
                onValueChange = { verifyInput = it },
                label = { Text("Registration Username / Character ID / Answers") },
                placeholder = { Text("E.g. character_id: cyber_pete_99 or answers: Yes, payments look cool") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(98.dp)
                    .testTag("verification_input"),
                maxLines = 4
            )

            Button(
                onClick = {
                    viewModel.submitTaskCompletion(task.id, verifyInput)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_task_proof_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Submit Verified Answers", fontWeight = FontWeight.Bold)
            }
        } else if (task.status == "PENDING_VERIFICATION") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9100).copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🕒 Proof Under Active Review",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFF9100)
                    )
                    Text(
                        text = "Your submission is queued for approval in the admin portal. Payout releases instantly when approved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your Submitted Proof:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                      )
                    Text(
                        text = "\"${task.verificationAnswer}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        } else if (task.status == "COMPLETED") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00E676).copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = "Reward Disbursed Successfully",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF00B050)
                        )
                        Text(
                            text = "+${task.rewardCoins} coins are added to your wallet balance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
