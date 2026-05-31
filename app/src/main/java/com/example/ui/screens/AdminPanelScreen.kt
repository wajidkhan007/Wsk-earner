package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OfferTask
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalRequest
import com.example.ui.RewardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(viewModel: RewardViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val tasks by viewModel.offerTasks.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()

    val profile = userProfile ?: UserProfile()

    var activeAdminTab by remember { mutableStateOf("PAYOUTS") } // "PAYOUTS", "OFFERWALL_PROOFS", "NEW_TASK", "MEMBERS", "APP_CONFIG"

    // Inputs for creating tasks
    var newTaskTitle by remember { mutableStateOf("Complete Premium Fintech Review") }
    var newTaskDesc by remember { mutableStateOf("Install and write a genuine review. Submit your App Store account handle.") }
    var newTaskRewardCoins by remember { mutableStateOf("600") }
    var newTaskCategory by remember { mutableStateOf("APPS") } // "APPS", "SURVEYS", "GAMES"

    // Inputs for dispatching custom notification alerts
    var pushNoticeTitle by remember { mutableStateOf("Server Maintenance Complete") }
    var pushNoticeText by remember { mutableStateOf("Our server-side banking integrations are active. All withdrawals are approved within 15 minutes! 🚀") }
    var pushNoticeSeverity by remember { mutableStateOf("SYSTEM") } // "SYSTEM", "ALERT", "BENEFIT"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analytics small row
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Platform Escrow", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    val pendingWithdrawalSum = withdrawals.filter { it.status == "PENDING" }.sumOf { it.coinAmount }
                    Text("$pendingWithdrawalSum 🪙 Pending Payout", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                
                Divider(modifier = Modifier.width(1.dp).height(24.dp))

                Column {
                    Text("Anti-Fraud Status", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(if (profile.isFlaggedAsFraud) MaterialTheme.colorScheme.error else Color(0xFF00E676), CircleShape))
                        Text(if (profile.isFlaggedAsFraud) "BLOCKED" else "SECURED", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Horizontal scrolling Tab layout
        ScrollableTabRow(
            selectedTabIndex = when(activeAdminTab) {
                "PAYOUTS" -> 0
                "OFFERWALL_PROOFS" -> 1
                "NEW_TASK" -> 2
                "MEMBERS" -> 3
                else -> 4
            },
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
            Tab(selected = activeAdminTab == "PAYOUTS", onClick = { activeAdminTab = "PAYOUTS" }) {
                Text("Redemptions", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            }
            Tab(selected = activeAdminTab == "OFFERWALL_PROOFS", onClick = { activeAdminTab = "OFFERWALL_PROOFS" }) {
                Text("Proofs Task", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            }
            Tab(selected = activeAdminTab == "NEW_TASK", onClick = { activeAdminTab = "NEW_TASK" }) {
                Text("Post Tasks", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            }
            Tab(selected = activeAdminTab == "MEMBERS", onClick = { activeAdminTab = "MEMBERS" }) {
                Text("Members", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            }
            Tab(selected = activeAdminTab == "APP_CONFIG", onClick = { activeAdminTab = "APP_CONFIG" }) {
                Text("App Controls", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            }
        }

        // Tab Content
        when (activeAdminTab) {
            "PAYOUTS" -> {
                val pendingRequests = withdrawals.filter { it.status == "PENDING" }
                if (pendingRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No pending withdrawal disbursements found", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).testTag("admin_withdrawals_column"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(pendingRequests) { request ->
                            AdminWithdrawalListRow(request, viewModel)
                        }
                    }
                }
            }

            "OFFERWALL_PROOFS" -> {
                val pendingProofs = tasks.filter { it.status == "PENDING_VERIFICATION" }
                if (pendingProofs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No pending task completions pending verification", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).testTag("admin_proofs_column"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(pendingProofs) { task ->
                            AdminTaskProofListRow(task, viewModel)
                        }
                    }
                }
            }

            "NEW_TASK" -> {
                LazyColumn(
                    modifier = Modifier.weight(1f).testTag("admin_new_task_form"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("POST ACTIVE NEW OFFER CONTRACTS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            label = { Text("Task Header Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newTaskDesc,
                            onValueChange = { newTaskDesc = it },
                            label = { Text("Task Instruction Details") },
                            modifier = Modifier.fillMaxWidth().height(84.dp)
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = newTaskRewardCoins,
                                onValueChange = { newTaskRewardCoins = it },
                                label = { Text("Coins Payout") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            // Simple category buttons row representation
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Section Cat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("APPS", "SURVEYS").forEach { catVal ->
                                        val isSel = newTaskCategory == catVal
                                        Button(
                                            onClick = { newTaskCategory = catVal },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(catVal, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                val coinsReward = newTaskRewardCoins.toIntOrNull() ?: 500
                                viewModel.adminCreateTask(
                                    newTaskTitle,
                                    newTaskDesc,
                                    coinsReward,
                                    newTaskCategory
                                )
                                // Clear fields
                                newTaskTitle = ""
                                newTaskDesc = ""
                                newTaskRewardCoins = "500"
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("publish_new_task")
                        ) {
                            Text("Deploy Task Live to Client Board", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            "MEMBERS" -> {
                val users by viewModel.usersList.collectAsState()
                var editingUser by remember { mutableStateOf<UserProfile?>(null) }

                LazyColumn(
                    modifier = Modifier.weight(1f).testTag("admin_users_directory"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("PLATFORM USER DIRECTORY & PROFILES", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Click 'Edit User' to manipulate values or 'Simulate' to log into their dashboard.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(users) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (user.isFlaggedAsFraud) {
                                                Surface(color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                    Text("LOCKED/FRAUD", color = MaterialTheme.colorScheme.error, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                }
                                            }
                                        }
                                        Text(user.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("${user.coinBalance} 🪙", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 11.sp)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Phone: ${user.phone.ifBlank { "N/A" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    Text("Code: ${user.referralCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { editingUser = user },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Text("Edit User Profile", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.adminSimulateUserSession(user.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Text("Simulate Session", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Edit User Dialog popup
                if (editingUser != null) {
                    val targetUser = editingUser!!
                    var editName by remember { mutableStateOf(targetUser.name) }
                    var editEmail by remember { mutableStateOf(targetUser.email) }
                    var editPhone by remember { mutableStateOf(targetUser.phone) }
                    var editCoins by remember { mutableStateOf(targetUser.coinBalance.toString()) }
                    var editFraud by remember { mutableStateOf(targetUser.isFlaggedAsFraud) }

                    androidx.compose.ui.window.Dialog(onDismissRequest = { editingUser = null }) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Edit User Profile #${targetUser.id}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                                
                                OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Display Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text("Gmail Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = editCoins, onValueChange = { editCoins = it }, label = { Text("Wallet Coins Balance") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                                
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Block Account (Fraud Security Flag)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Switch(checked = editFraud, onCheckedChange = { editFraud = it })
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(onClick = { editingUser = null }, modifier = Modifier.weight(1f)) {
                                        Text("Cancel")
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.adminUpdateUserProfile(
                                                targetUser.id,
                                                editName,
                                                editEmail,
                                                editPhone,
                                                editCoins.toIntOrNull() ?: 0,
                                                editFraud
                                            )
                                            editingUser = null
                                        },
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Text("Save Changes")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "APP_CONFIG" -> {
                val spinLimit by viewModel.spinWheelLimit.collectAsState()
                val scratchLimit by viewModel.scratchCardLimit.collectAsState()
                val rate by viewModel.coinToRupeeRate.collectAsState()
                val adsCoins by viewModel.adsBonusCoins.collectAsState()
                val refCoins by viewModel.referralBonusCoins.collectAsState()
                val announcement by viewModel.globalAnnouncement.collectAsState()

                var configSpin by remember { mutableStateOf(spinLimit.toString()) }
                var configScratch by remember { mutableStateOf(scratchLimit.toString()) }
                var configRate by remember { mutableStateOf(rate.toString()) }
                var configAds by remember { mutableStateOf(adsCoins.toString()) }
                var configRef by remember { mutableStateOf(refCoins.toString()) }
                var configMsg by remember { mutableStateOf(announcement) }

                LazyColumn(
                    modifier = Modifier.weight(1f).testTag("admin_global_configs"),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("GLOBAL APPLICATION OVERRIDES", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Customize default app restrictions, reward amounts, rates, and broadcast updates instantly.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = configSpin,
                                onValueChange = { configSpin = it },
                                label = { Text("Daily Spins Limit") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = configScratch,
                                onValueChange = { configScratch = it },
                                label = { Text("Daily Scratches Limit") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = configRate,
                                onValueChange = { configRate = it },
                                label = { Text("Coin Rate (e.g. 0.01)") },
                                placeholder = { Text("0.01") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = configAds,
                                onValueChange = { configAds = it },
                                label = { Text("Ads Coin Reward") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = configRef,
                            onValueChange = { configRef = it },
                            label = { Text("Referral Bonus Coins") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = configMsg,
                            onValueChange = { configMsg = it },
                            label = { Text("Welcome Announcement Marquee Description") },
                            modifier = Modifier.fillMaxWidth().height(72.dp)
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.adminUpdateAppConfig(
                                    configSpin.toIntOrNull() ?: 10,
                                    configScratch.toIntOrNull() ?: 5,
                                    configRate.toFloatOrNull() ?: 0.01f,
                                    configAds.toIntOrNull() ?: 15,
                                    configRef.toIntOrNull() ?: 120,
                                    configMsg
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_global_configs")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Text("Save Configurations Override", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Text("SYSTEM RESET SHORTCUTS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.resetDailySpinsAndScratches() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Reset Daily Game Cooldowns", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalListRow(request: WithdrawalRequest, viewModel: RewardViewModel) {
    var adminReasonInput by remember { mutableStateOf("") }

    val templates = listOf(
        "Payout completed successfully. ✅",
        "Invalid UPI address / coordinates. 💔",
        "Duplicate account fraud violation. ❌",
        "Bank server down, tried auto-refund. 🏦"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "METHOD: ${request.method}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                val rate by viewModel.coinToRupeeRate.collectAsState()
                val cashAmount = request.coinAmount * rate
                Text(
                    text = "${request.coinAmount} Coins (₹${String.format("%.2f", cashAmount)})",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "${request.detailsKey}: ${request.detailsValue}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            // Custom reason text input
            OutlinedTextField(
                value = adminReasonInput,
                onValueChange = { adminReasonInput = it },
                label = { Text("Approval/Rejection Reason", fontSize = 11.sp) },
                placeholder = { Text("Reason for payout status", fontSize = 11.sp) },
                textStyle = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Quick templates selection
            Text("Quick Templates:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(templates) { template ->
                    val isSelected = adminReasonInput == template
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                        modifier = Modifier.clickable {
                            adminReasonInput = template
                        }
                    ) {
                        Text(
                            text = template,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Approve & Deny Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.adminApproveWithdrawal(request.id, adminReasonInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(36.dp).testTag("approve_withdrawal_${request.id}")
                ) {
                    Text("Approve Payout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.adminRejectWithdrawal(request.id, adminReasonInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(36.dp).testTag("reject_withdrawal_${request.id}")
                ) {
                    Text("Decline/Refund", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminTaskProofListRow(task: OfferTask, viewModel: RewardViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TASK: ${task.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Text(
                    text = "+${task.rewardCoins}🪙",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "Submitted Proof: \"${task.verificationAnswer}\"",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Approve & Reject Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.adminApproveTask(task.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(34.dp).testTag("approve_task_${task.id}")
                ) {
                    Text("Verify & Credit Wallet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.adminRejectTask(task.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(34.dp).testTag("reject_task_${task.id}")
                ) {
                    Text("Reject Answer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
