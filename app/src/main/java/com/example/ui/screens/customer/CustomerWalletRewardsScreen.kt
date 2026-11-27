package com.example.ui.screens.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AdMobConfig
import com.example.data.model.RewardTransaction
import com.example.data.model.WalletTopUpRequest
import com.example.data.model.WalletTransaction
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.RewardedAdWatchCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerWalletRewardsScreen(
    balance: Double,
    rewardPoints: Long,
    dailyRewardCount: Long,
    dailyRewardDate: String,
    referralCode: String,
    transactions: List<WalletTransaction>,
    topUpRequests: List<WalletTopUpRequest>,
    rewardTransactions: List<RewardTransaction>,
    adMobConfig: AdMobConfig,
    onSubmitTopUpRequest: (Double, String, String) -> Unit,
    onRewardEarnedBySdk: (String, String) -> Unit,
    onRewardStatusMessage: (String) -> Unit
) {
    var topUpAmount by remember { mutableStateOf("500") }
    var paymentMethod by remember { mutableStateOf("UPI") }
    var utrReference by remember { mutableStateOf("") }

    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val todayClaims = if (dailyRewardDate == todayKey) dailyRewardCount else 0L

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cash Wallet Card (Strictly separated from Reward Points)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(BrandBlueDark, BrandBlue)))
                        .padding(20.dp)
                ) {
                    Column {
                        Text("HUNHAR JI Cash Wallet", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                        Spacer(Modifier.height(4.dp))
                        Text("₹${balance.toInt()}", style = MaterialTheme.typography.displaySmall.copy(color = Color.White, fontWeight = FontWeight.ExtraBold))
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color.White.copy(alpha = 0.16f), shape = RoundedCornerShape(50)) {
                                Text("Reward Points: $rewardPoints pts", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFD54F), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                            Text("Ref: $referralCode", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                }
            }
        }

        // Compliant Wallet Top-Up Request Card (Verified by Admin)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Request Verified Wallet Top-Up", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        "Submit your UPI / Razorpay transaction UTR below. Admin verifies and credits your wallet balance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("250", "500", "1000", "2000").forEach { amt ->
                            OutlinedButton(
                                onClick = { topUpAmount = amt },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("₹$amt", maxLines = 1)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("UPI", "RAZORPAY", "BANK_TRANSFER").forEach { m ->
                            val selected = paymentMethod == m
                            Surface(
                                color = if (selected) BrandBlueLight else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedButton(
                                    onClick = { paymentMethod = m },
                                    border = BorderStroke(1.dp, if (selected) BrandBlue else Color.Transparent),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text(m.replace("_", " "), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = topUpAmount,
                        onValueChange = { topUpAmount = it.filter(Char::isDigit) },
                        label = { Text("Top-Up Amount (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = utrReference,
                        onValueChange = { utrReference = it },
                        label = { Text("Transaction ID / UTR Reference") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("topup_utr_input")
                    )
                    Button(
                        onClick = {
                            val amt = topUpAmount.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && utrReference.trim().length >= 6) {
                                onSubmitTopUpRequest(amt, paymentMethod, utrReference.trim())
                                utrReference = ""
                            } else {
                                onRewardStatusMessage("Enter valid amount and UTR reference (min 6 chars).")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("wallet_add_money_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Top-Up Request for Verification")
                    }
                }
            }
        }

        // AdMob Rewarded Video Card (Reward Points Only - Isolated from Cash Wallet)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = AccentAmber)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("HUNHAR JI Reward Points: $rewardPoints pts", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text("Non-withdrawable loyalty points earned only by completing Google AdMob rewarded videos.", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                    RewardedAdWatchCard(
                        config = adMobConfig,
                        todayClaimCount = todayClaims,
                        onRewardEarnedBySdk = onRewardEarnedBySdk,
                        onStatusMessage = onRewardStatusMessage
                    )
                }
            }
        }

        // Top-Up Requests Status
        if (topUpRequests.isNotEmpty()) {
            item {
                Text("Wallet Top-Up Requests (${topUpRequests.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            items(topUpRequests.sortedByDescending { it.createdAt?.seconds ?: 0L }, key = { it.id }) { req ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("₹${req.amount.toInt()} via ${req.method}", style = MaterialTheme.typography.titleSmall)
                            Text("UTR: ${req.paymentReference}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            if (req.adminNote.isNotBlank()) {
                                Text("Admin: ${req.adminNote}", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                            }
                        }
                        StatusPill(req.status)
                    }
                }
            }
        }

        // Reward Points History
        if (rewardTransactions.isNotEmpty()) {
            item {
                Text("Reward Points History (${rewardTransactions.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            items(rewardTransactions.sortedByDescending { it.createdAt?.seconds ?: 0L }.take(10), key = { it.id }) { r ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(r.title, style = MaterialTheme.typography.titleSmall)
                            Text("Token: ${r.rewardToken}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Text("+${r.points} pts", style = MaterialTheme.typography.titleSmall.copy(color = AccentAmber, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Cash Ledger
        item {
            Text("Cash Wallet Ledger", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
        if (transactions.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Wallet,
                    title = "No Cash Transactions Yet",
                    subtitle = "Submit a verified top-up request or pay via HUNHAR JI Wallet to see cash transactions."
                )
            }
        } else {
            items(transactions.sortedByDescending { it.createdAt?.seconds ?: 0L }, key = { it.id }) { tx ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.title, style = MaterialTheme.typography.titleSmall)
                            Text("Ref: ${tx.referenceId}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        val isCredit = tx.type == "CREDIT" || tx.type == "REFUND"
                        Text(
                            text = "${if (isCredit) "+" else "-"} ₹${tx.amount.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (isCredit) AccentGreen else AccentRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
