package com.example.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.HunarJiBottomWaveDecoration
import com.example.ui.components.HunarJiCenteredLogoHeader
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueDeep
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AdminClaimState

@Composable
fun AdminLoginScreen(
    isSignedIn: Boolean,
    signedInEmail: String,
    claimState: AdminClaimState,
    isLoading: Boolean,
    externalError: String? = null,
    onSignInWithGoogleForAdmin: () -> Unit,
    onRetryClaimVerification: () -> Unit,
    onSwitchAdminAccount: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("admin_login_back_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Protected Admin Portal",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(48.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface)
                    )
                )
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HunarJiCenteredLogoHeader(
                    subtitle = "Admin Panel • Email OTP Security",
                    logoSize = 62.dp
                )

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isDenied = claimState is AdminClaimState.Denied
                        val isChecking = isLoading || claimState is AdminClaimState.Checking

                        Surface(
                            color = when {
                                claimState is AdminClaimState.Verified -> AccentGreenLight
                                isDenied -> AccentRedLight
                                else -> BrandBlueLight
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when {
                                        claimState is AdminClaimState.Verified -> Icons.Default.VerifiedUser
                                        isDenied -> Icons.Default.GppBad
                                        else -> Icons.Default.AdminPanelSettings
                                    },
                                    contentDescription = "Admin verification status",
                                    tint = when {
                                        claimState is AdminClaimState.Verified -> AccentGreen
                                        isDenied -> AccentRed
                                        else -> BrandBlueDark
                                    },
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Text(
                            text = when {
                                isChecking -> "Verifying Admin Claim..."
                                claimState is AdminClaimState.Verified -> "Admin Verified"
                                isDenied -> "Admin Access Denied"
                                else -> "Admin Sign-In"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "This portal verifies the Firebase Authentication ID token and grants Admin access only to an account with the server-issued 'is_admin' claim or the verified bootstrap Admin account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        // Verification Checklist Preview
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ClaimCheckStepRow(
                                    label = if (isSignedIn) "1. Admin email account authenticated" else "1. Sign in on the secure Admin website",
                                    done = isSignedIn
                                )
                                ClaimCheckStepRow(
                                    label = "2. Verify email OTP and active Admin session",
                                    done = claimState is AdminClaimState.Verified,
                                    failed = isDenied
                                )
                                ClaimCheckStepRow(
                                    label = "3. Open protected Admin dashboard",
                                    done = claimState is AdminClaimState.Verified
                                )
                            }
                        }

                        val displayError = externalError ?: (claimState as? AdminClaimState.Denied)?.reason
                        if (!displayError.isNullOrBlank()) {
                            Surface(
                                color = AccentRedLight,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_claim_denied_banner")
                            ) {
                                Text(
                                    text = displayError,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentRed,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        if (!isSignedIn) {
                            Button(
                                onClick = onSignInWithGoogleForAdmin,
                                enabled = !isChecking,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBlueDeep),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("admin_login_verify_button")
                            ) {
                                if (isChecking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Open Admin Website (Email + Password + OTP)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                                }
                            }
                        } else {
                            Button(
                                onClick = onRetryClaimVerification,
                                enabled = !isChecking,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("admin_retry_claim_button")
                            ) {
                                if (isChecking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Refresh Admin Session",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onSignInWithGoogleForAdmin,
                                enabled = !isChecking,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("admin_google_account_button")
                            ) {
                                Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = BrandBlueDark)
                                Spacer(Modifier.width(8.dp))
                                Text("Sign In With Admin Google Account", color = BrandBlueDark)
                            }

                            OutlinedButton(
                                onClick = onSwitchAdminAccount,
                                enabled = !isChecking,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("admin_switch_account_button")
                            ) {
                                Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = BrandBlueDark)
                                Spacer(Modifier.width(8.dp))
                                Text("Sign Out & Use Admin Account", color = BrandBlueDark)
                            }
                        }

                        TextButton(onClick = onBack) {
                            Text(
                                text = if (isSignedIn) "Return to Standard App" else "Back to User Login",
                                style = MaterialTheme.typography.labelLarge,
                                color = BrandBlue
                            )
                        }
                    }
                }
            }
            HunarJiBottomWaveDecoration()
        }
    }
}

@Composable
private fun ClaimCheckStepRow(
    label: String,
    done: Boolean,
    failed: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = when {
                failed -> Icons.Default.GppBad
                done -> Icons.Default.CheckCircle
                else -> Icons.Default.Security
            },
            contentDescription = null,
            tint = when {
                failed -> AccentRed
                done -> AccentGreen
                else -> BrandBlue
            },
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (failed) AccentRed else TextPrimary,
                fontWeight = if (done || failed) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}
