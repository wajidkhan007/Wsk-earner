package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalRequest
import com.example.ui.RewardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(viewModel: RewardViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()

    val profile = userProfile ?: UserProfile()

    var activeTransferTab by remember { mutableStateOf("UPI") } // "UPI", "Paytm", "BANK"

    var upiIdInput by remember { mutableStateOf("") }
    var upiNameInput by remember { mutableStateOf("") }

    var paytmPhoneInput by remember { mutableStateOf("") }

    var bankAccountNumber by remember { mutableStateOf("") }
    var bankIfscCode by remember { mutableStateOf("") }
    var bankAccountHolderName by remember { mutableStateOf("") }

     var coinsToWithdraw by remember { mutableStateOf(10000) } // Standard starting coins deduction

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("wallet_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balances Review Deck
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "DISBURSABLE WALLET STASH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = String.format("%,d", profile.coinBalance),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val rate by viewModel.coinToRupeeRate.collectAsState()
                    val inrEquiv = profile.coinBalance * rate
                    Text(
                        text = String.format("Exchange Equiv: INR ₹%.2f (Rate: 100 Coins = ₹%.2f)", inrEquiv, rate * 100),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Payout Methods Selection
        item {
            Text(
                text = "REQUEST COIN PAYOUT",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("UPI", "Paytm", "BANK").forEach { method ->
                    val isSelected = activeTransferTab == method
                    Button(
                        onClick = { activeTransferTab = method },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text(method, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Dynamic Form sheets depending on selected tab
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
                    if (activeTransferTab == "UPI") {
                        Text("Instant UPI Wire Settlement", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = upiIdInput,
                            onValueChange = { upiIdInput = it },
                            label = { Text("UPI Address ID") },
                            placeholder = { Text("username@ybl") },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("upi_id_input")
                        )
                        OutlinedTextField(
                            value = upiNameInput,
                            onValueChange = { upiNameInput = it },
                            label = { Text("Registered Holder Name") },
                            placeholder = { Text("John Doe") },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (activeTransferTab == "Paytm") {
                        Text("Paytm Mobile Core Wallet Payout", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = paytmPhoneInput,
                            onValueChange = { paytmPhoneInput = it },
                            label = { Text("Registered Phone Number") },
                            placeholder = { Text("9876543210") },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("paytm_phone_input")
                        )
                    } else {
                        Text("Direct IFSC Bank Account Remittance", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = bankAccountHolderName,
                            onValueChange = { bankAccountHolderName = it },
                            label = { Text("Bank Account Name") },
                            placeholder = { Text("John Doe") },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = bankAccountNumber,
                            onValueChange = { bankAccountNumber = it },
                            label = { Text("Account Checking Number") },
                            placeholder = { Text("123456789012") },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("bank_account_input")
                        )
                        OutlinedTextField(
                            value = bankIfscCode,
                            onValueChange = { bankIfscCode = it },
                            label = { Text("Bank Routing IFSC Code") },
                            placeholder = { Text("SBIN0001234") },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    // Coins selectors
                    Text("Select Coins Amount to Withdraw:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(10000, 20000, 50000).forEach { coinsVal ->
                            val isSelectedCoins = coinsToWithdraw == coinsVal
                            OutlinedButton(
                                onClick = { coinsToWithdraw = coinsVal },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelectedCoins) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = if (isSelectedCoins) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("${coinsVal}c", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Trigger request button
                    Button(
                        onClick = {
                            val detailsKey = when (activeTransferTab) {
                                "UPI" -> "UPI address"
                                "Paytm" -> "Paytm number"
                                else -> "Bank A/C number (IFSC)"
                            }
                            val valInput = when (activeTransferTab) {
                                "UPI" -> "$upiIdInput ($upiNameInput)"
                                "Paytm" -> paytmPhoneInput
                                else -> "$bankAccountNumber ($bankIfscCode - $bankAccountHolderName)"
                            }
                            viewModel.performCoinsWithdrawal(
                                activeTransferTab,
                                detailsKey,
                                valInput,
                                coinsToWithdraw
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_withdrawal_button")
                    ) {
                        Text(
                            text = "Submit Cashout Request (${coinsToWithdraw} Coins)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Sponsored Ad Display Center
        item {
            com.example.ui.components.AdBannerComponent(viewModel = viewModel)
        }

        // Historic Withdrawal Logs
        item {
            Text(
                text = "PAYOUT LEDGER TRACKER",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (withdrawals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No withdrawal requests found", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        } else {
            items(withdrawals) { withdrawal ->
                WithdrawRowItem(withdrawal)
            }
        }
    }
}

@Composable
fun WithdrawRowItem(withdrawal: WithdrawalRequest) {
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(withdrawal.timestamp))

    val (statusLabel, statusColor) = when (withdrawal.status) {
        "PENDING" -> Pair("UNDER REVIEW", Color(0xFFFF9100))
        "APPROVED" -> Pair("DISBURSED", Color(0xFF00E676))
        "REJECTED" -> Pair("DECLINED/REFUNDED", Color(0xFFF44336))
        else -> Pair(withdrawal.status, MaterialTheme.colorScheme.onSurface)
    }

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
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    contentColor = statusColor
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "${withdrawal.coinAmount} Coins",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Method: ${withdrawal.method} • Details: ${withdrawal.detailsValue}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (withdrawal.reason.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (withdrawal.status == "REJECTED") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary).copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (withdrawal.status == "REJECTED") Icons.Default.Info else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (withdrawal.status == "REJECTED") MaterialTheme.colorScheme.error else Color(0xFF00C853),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Admin Note: ${withdrawal.reason}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )

                Text(
                    text = String.format("Est: ₹%.2f", withdrawal.cashAmountEstimated),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = 12.sp,
                    color = Color(0xFF00C853)
                )
            }
        }
    }
}
