package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.security.CredentialSecurityHelper
import com.example.core.storage.RegistrationDraftStore
import com.example.data.firebase.onGoogleSignInClicked
import com.example.data.model.AppWorkspace
import com.example.data.model.KycApplication
import com.example.data.model.KycDocumentRecord
import com.example.ui.screens.auth.AdminLoginScreen
import com.example.ui.screens.auth.RegistrationAndKycWizard
import com.example.ui.viewmodel.AdminClaimState
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberLight
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedLight
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueDeep
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun HunarJiBrandWordmark(
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_hunarji_logo_fg),
            contentDescription = "Hunar Ji logo",
            modifier = Modifier.size(36.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.width(6.dp))
        Column {
            Image(
                painter = painterResource(id = R.drawable.img_hunarji_wordmark),
                contentDescription = "Hunar Ji",
                modifier = Modifier
                    .height(28.dp)
                    .width(92.dp),
                contentScale = ContentScale.Fit,
                alignment = Alignment.CenterStart
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun HunarJiCenteredLogoHeader(
    modifier: Modifier = Modifier,
    subtitle: String = "Shop • Book • Earn • Grow",
    logoSize: androidx.compose.ui.unit.Dp = 66.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_hunarji_logo_fg),
            contentDescription = "Hunar Ji logo",
            modifier = Modifier.size(logoSize),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(2.dp))
        Image(
            painter = painterResource(id = R.drawable.img_hunarji_wordmark),
            contentDescription = "Hunar Ji",
            modifier = Modifier
                .height(48.dp)
                .width(168.dp),
            contentScale = ContentScale.Fit
        )
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        }
    }
}

@Composable
fun HunarJiBottomWaveDecoration(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
    ) {
        val w = size.width
        val h = size.height
        val orangePath = Path().apply {
            moveTo(0f, h * 0.48f)
            quadraticTo(w * 0.38f, h * 0.85f, w, h * 0.12f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = orangePath, color = AccentOrange)
        val bluePath = Path().apply {
            moveTo(0f, h * 0.12f)
            quadraticTo(w * 0.35f, h * 0.86f, w, h * 0.42f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = bluePath, color = BrandBlueDark)
    }
}

@Composable
fun HunarJiTopAppHeader(
    onMenuClick: () -> Unit,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    cartCount: Int? = null,
    onCartClick: (() -> Unit)? = null,
    activeWorkspace: AppWorkspace,
    onSwitchWorkspaceClick: () -> Unit,
    showWorkspaceSwitcher: Boolean = false
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("menu_button")
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Open menu", tint = TextPrimary)
            }
            HunarJiBrandWordmark(modifier = Modifier.weight(1f))
            if (showWorkspaceSwitcher) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BrandBlueLight,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onSwitchWorkspaceClick)
                        .testTag("workspace_switcher_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch app role",
                            tint = BrandBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = when (activeWorkspace) {
                                AppWorkspace.CUSTOMER -> "Customer"
                                AppWorkspace.MECHANIC -> "Mechanic"
                                AppWorkspace.SELLER -> "Seller"
                                AppWorkspace.DELIVERY -> "Delivery"
                                AppWorkspace.ADMIN -> "Admin"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = BrandBlue,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge(containerColor = AccentRed, contentColor = Color.White) {
                                Text(notificationCount.coerceAtMost(99).toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimary
                    )
                }
            }
            if (onCartClick != null && cartCount != null) {
                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier.testTag("cart_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(containerColor = AccentRed, contentColor = Color.White) {
                                    Text(cartCount.coerceAtMost(99).toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            contentDescription = "Shopping cart",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: String) {
    val normalized = status.uppercase()
    val (bg, fg, label) = when (normalized) {
        "DELIVERED", "COMPLETED", "APPROVED", "VERIFIED", "PAID", "ONLINE", "ACTIVE", "RESOLVED" ->
            Triple(AccentGreenLight, AccentGreen, normalized.replace('_', ' '))
        "PENDING", "OPEN", "PROCESSING", "SUBMITTED", "UNDER_REVIEW" ->
            Triple(AccentAmberLight, Color(0xFFB45309), normalized.replace('_', ' '))
        "CONFIRMED", "SHIPPED", "ACCEPTED", "ON_THE_WAY", "STARTED", "OUT_FOR_DELIVERY", "IN_PROGRESS" ->
            Triple(BrandBlueLight, BrandBlue, normalized.replace('_', ' '))
        "CANCELLED", "REJECTED", "FAILED", "RETURNED", "OFFLINE", "SUSPENDED", "RESUBMISSION_REQUIRED", "RESUBMIT_REQUIRED" ->
            Triple(AccentRedLight, AccentRed, normalized.replace('_', ' '))
        else ->
            Triple(AccentPurpleLight, AccentPurple, normalized.replace('_', ' '))
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = label.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall.copy(
                color = fg,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            maxLines = 1
        )
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit = {},
    onPreparePasswordLogin: (identifier: String, password: String) -> Unit = { _, _ -> },
    onPrepareOtpLogin: (mobile: String) -> Unit = {},
    onPreparePasswordReset: (identifier: String, newPassword: String) -> Unit = { _, _ -> },
    onPrepareRegistration: (KycApplication, String, List<KycDocumentRecord>, Boolean) -> Unit = { _, _, _, _ -> },
    onPrepareAdminLogin: () -> Unit = {},
    externalError: String? = null,
    onClearExternalError: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var showRegistrationWizard by remember { mutableStateOf(false) }
    var showAdminLoginScreen by remember { mutableStateOf(false) }
    var useUserIdMode by remember { mutableStateOf(false) }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember(externalError) { mutableStateOf(externalError) }
    var showOtpLoginDialog by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        RegistrationDraftStore.observeRememberedLogin(context).collect { (rem, savedId) ->
            rememberMe = rem
            if (rem && savedId.isNotBlank() && identifier.isBlank()) {
                identifier = savedId
                useUserIdMode = savedId.startsWith("HJ-", ignoreCase = true)
            }
        }
    }

    if (showRegistrationWizard) {
        var stagedApp by remember { mutableStateOf<KycApplication?>(null) }
        var stagedPassword by remember { mutableStateOf("") }
        var stagedDocs by remember { mutableStateOf<List<KycDocumentRecord>>(emptyList()) }
        var stagedTerms by remember { mutableStateOf(false) }

        RegistrationAndKycWizard(
            initialRole = "CUSTOMER",
            onSubmitRegistration = { app, rawPwd, docs, terms, onSuccess, _ ->
                stagedApp = app
                stagedPassword = rawPwd
                stagedDocs = docs
                stagedTerms = terms
                onPrepareRegistration(app, rawPwd, docs, terms)
                onSuccess(app)
            },
            onAuthenticatedBeforeSubmit = {
                stagedApp?.let { app ->
                    onPrepareRegistration(app, stagedPassword, stagedDocs, stagedTerms)
                }
            },
            onCompletedClose = {
                showRegistrationWizard = false
                onAuthSuccess()
            },
            onCancel = { showRegistrationWizard = false }
        )
        return
    }

    fun launchGoogleSignIn(beforeGoogleAuth: () -> Unit) {
        errorMessage = null
        onClearExternalError()
        isLoading = true
        beforeGoogleAuth()
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
            },
            onAuthError = { msg ->
                isLoading = false
                errorMessage = msg
            },
            scope = scope,
            onAuthCancelled = {
                isLoading = false
            }
        )
    }

    if (showAdminLoginScreen) {
        AdminLoginScreen(
            isSignedIn = false,
            signedInEmail = "",
            claimState = if (isLoading) AdminClaimState.Checking else AdminClaimState.Idle,
            isLoading = isLoading,
            externalError = errorMessage,
            onSignInWithGoogleForAdmin = {
                launchGoogleSignIn {
                    onPrepareAdminLogin()
                }
            },
            onRetryClaimVerification = {},
            onSwitchAdminAccount = {},
            onBack = {
                showAdminLoginScreen = false
                errorMessage = null
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0EDFF), Color(0xFFF5F9FF), Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            HunarJiCenteredLogoHeader(subtitle = "Shop • Book • Earn • Grow", logoSize = 68.dp)
            Spacer(Modifier.height(10.dp))

            // Hero Artwork matching the uploaded Login & Registration visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(144.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_delivery_hero),
                    contentDescription = "Hunar Ji Multi-Role Marketplace",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(BrandBlueDeep.copy(alpha = 0.78f), Color.Transparent)
                            )
                        )
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        Text(
                            "One Platform",
                            style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            "Customer • Mechanic\nSeller • Delivery",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.9f))
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.img_worker_avatar),
                        contentDescription = "Verified Partner",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Main Login Card matching mockup
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Login to your account",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(14.dp))

                    // Identifier mode pills: Mobile Number vs User ID
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf(false to "Mobile Number", true to "User ID (HJ-...)")
                        modes.forEach { (isUid, label) ->
                            val selected = useUserIdMode == isUid
                            Surface(
                                color = if (selected) BrandBlueLight else Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, if (selected) BrandBlue else Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .clickable { useUserIdMode = isUid }
                                    .testTag(if (isUid) "login_mode_userid" else "login_mode_mobile")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (selected) BrandBlueDark else TextSecondary,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = identifier,
                        onValueChange = { identifier = it },
                        label = { Text(if (useUserIdMode) "User ID (e.g. HJ-MEC-100001)" else "Mobile Number") },
                        leadingIcon = {
                            Icon(
                                if (useUserIdMode) Icons.Default.Badge else Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = BrandBlue
                            )
                        },
                        trailingIcon = {
                            if (identifier.isNotEmpty()) {
                                IconButton(onClick = { identifier = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (useUserIdMode) KeyboardType.Text else KeyboardType.Phone
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_identifier_input")
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandBlue) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password",
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it }
                            )
                            Text("Remember Me", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                        TextButton(
                            onClick = { showForgotDialog = true },
                            modifier = Modifier.testTag("forgot_password_button")
                        ) {
                            Text("Forgot Password?", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = BrandBlue)
                        }
                    }

                    if (errorMessage != null) {
                        Surface(
                            color = AccentRedLight,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = AccentRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Primary Login Button
                    Button(
                        onClick = {
                            val cleanId = identifier.trim()
                            if (cleanId.isBlank()) {
                                errorMessage = if (useUserIdMode) "Enter your Hunar Ji User ID" else "Enter your registered mobile number"
                                return@Button
                            }
                            if (password.length < 6) {
                                errorMessage = "Enter your password (minimum 6 characters)"
                                return@Button
                            }
                            scope.launch {
                                RegistrationDraftStore.saveRememberedLogin(context, rememberMe, cleanId)
                            }
                            launchGoogleSignIn {
                                onPreparePasswordLogin(cleanId, password)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Login",
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                        Text("  or continue with  ", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                    }
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                launchGoogleSignIn {}
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("google_sign_in_button")
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Google", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        }
                        OutlinedButton(
                            onClick = { showOtpLoginDialog = true },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("otp_login_button")
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("OTP Login", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Don't have an account? ", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            text = "Register Now",
                            style = MaterialTheme.typography.bodySmall.copy(color = BrandBlue, fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .clickable { showRegistrationWizard = true }
                                .padding(4.dp)
                                .testTag("register_now_button")
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RoleMiniTag(Icons.Default.ShoppingBag, "Customer")
                RoleMiniTag(Icons.Default.Build, "Mechanic")
                RoleMiniTag(Icons.Default.Storefront, "Seller")
                RoleMiniTag(Icons.Default.DeliveryDining, "Delivery")
            }
            Spacer(Modifier.height(12.dp))

            // Admin Login Access Card (opens dedicated Admin Login flow with 'is_admin' claim check)
            Surface(
                color = BrandBlueLight,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.28f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !isLoading) {
                        errorMessage = null
                        showAdminLoginScreen = true
                    }
                    .testTag("admin_google_signin_card")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Sign In",
                        tint = BrandBlueDark,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin Panel Login",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = BrandBlueDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Dedicated Admin Login • Verifies Firebase 'is_admin' custom claim",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HunarJiBottomWaveDecoration()
        }
    }

    if (showOtpLoginDialog) {
        OtpLoginModal(
            initialMobile = if (!useUserIdMode) identifier else "",
            onVerified = { mobile ->
                showOtpLoginDialog = false
                launchGoogleSignIn {
                    onPrepareOtpLogin(mobile)
                }
            },
            onDismiss = { showOtpLoginDialog = false }
        )
    }

    if (showForgotDialog) {
        ForgotPasswordOtpModal(
            initialIdentifier = identifier,
            onResetReady = { id, newPass ->
                showForgotDialog = false
                launchGoogleSignIn {
                    onPreparePasswordReset(id, newPass)
                }
            },
            onDismiss = { showForgotDialog = false }
        )
    }
}

@Composable
private fun OtpLoginModal(
    initialMobile: String,
    onVerified: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var mobile by remember { mutableStateOf(initialMobile) }
    var sentOtp by remember { mutableStateOf<String?>(null) }
    var otpInput by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mobile OTP Login") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it.filter(Char::isDigit).take(10) },
                    label = { Text("10-digit Mobile Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (!CredentialSecurityHelper.isValidMobile(mobile)) {
                            err = "Enter a valid 10-digit mobile number"
                        } else {
                            err = null
                            sentOtp = CredentialSecurityHelper.generateSecureOtp()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (sentOtp == null) "Send Verification OTP" else "Resend OTP")
                }
                if (sentOtp != null) {
                    Surface(color = BrandBlueLight, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "OTP Code for +91-$mobile: $sentOtp",
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandBlueDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it.filter(Char::isDigit).take(6) },
                        label = { Text("Enter 6-digit OTP") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (err != null) {
                    Text(err ?: "", style = MaterialTheme.typography.labelSmall, color = AccentRed)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (sentOtp != null && otpInput == sentOtp) {
                        onVerified(mobile)
                    } else {
                        err = "Enter the valid 6-digit OTP sent to your mobile"
                    }
                }
            ) { Text("Verify & Sign In") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ForgotPasswordOtpModal(
    initialIdentifier: String,
    onResetReady: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var idInput by remember { mutableStateOf(initialIdentifier) }
    var sentOtp by remember { mutableStateOf<String?>(null) }
    var otpInput by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password via OTP") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = idInput,
                    onValueChange = { idInput = it },
                    label = { Text("Registered Mobile Number or User ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (idInput.trim().length < 6) {
                            err = "Enter your registered Mobile or User ID"
                        } else {
                            err = null
                            sentOtp = CredentialSecurityHelper.generateSecureOtp()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (sentOtp == null) "Send Reset OTP" else "Resend Reset OTP")
                }
                if (sentOtp != null) {
                    Surface(color = BrandBlueLight, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Reset OTP: $sentOtp",
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandBlueDark,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it.filter(Char::isDigit).take(6) },
                        label = { Text("6-digit OTP") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password (min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (err != null) {
                    Text(err ?: "", style = MaterialTheme.typography.labelSmall, color = AccentRed)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        sentOtp == null || otpInput != sentOtp -> err = "Verify OTP first"
                        newPass.length < 6 -> err = "Password must be at least 6 characters"
                        newPass != confirmPass -> err = "Passwords do not match"
                        else -> onResetReady(idInput.trim(), newPass)
                    }
                }
            ) { Text("Update Password") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RoleMiniTag(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = BrandBlueLight,
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = BrandBlue, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
fun WorkspaceSwitcherDialog(
    current: AppWorkspace,
    isAdminUser: Boolean,
    onSelect: (AppWorkspace) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Switch Hunar Ji Workspace", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val items = listOf(
                    Triple(AppWorkspace.CUSTOMER, "Customer / User App", Icons.Default.ShoppingCart),
                    Triple(AppWorkspace.MECHANIC, "Mechanic App (Mistri Ji)", Icons.Default.Build),
                    Triple(AppWorkspace.SELLER, "Seller Store App", Icons.Default.Storefront),
                    Triple(AppWorkspace.DELIVERY, "Delivery Boy App", Icons.Default.DeliveryDining),
                    Triple(
                        AppWorkspace.ADMIN,
                        if (isAdminUser) "Admin Panel (Verified 'is_admin')" else "Protected Admin Portal ('is_admin' Claim)",
                        Icons.Default.AdminPanelSettings
                    )
                )
                items.forEach { (ws, desc, icon) ->
                    val selected = ws == current
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) BrandBlueLight else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (selected) BrandBlue else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSelect(ws)
                                onDismiss()
                            }
                            .testTag("switch_to_${ws.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = desc, tint = if (selected) BrandBlue else TextSecondary)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    desc,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (selected) BrandBlue else TextPrimary
                                )
                            }
                            if (selected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = BrandBlue)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun LanguagePickerDialog(
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = BrandBlue)
                Spacer(Modifier.width(8.dp))
                Text("Select App Language")
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(340.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(AppLanguage.entries) { lang ->
                    val selected = lang == current
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) BrandBlueLight else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(lang)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.nativeName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selected) BrandBlue else TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (selected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = BrandBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

fun productIconFor(key: String): Pair<ImageVector, Color> = when (key.lowercase()) {
    "earbuds_dark", "headphones", "earbuds_blue" -> Icons.Default.Headphones to BrandBlue
    "shoes" -> Icons.Default.ShoppingBag to Color(0xFF1E293B)
    "watch" -> Icons.Default.Watch to AccentOrange
    "bag" -> Icons.Default.Backpack to Color(0xFF334155)
    "tshirt" -> Icons.Default.Checkroom to BrandBlue
    "cooker" -> Icons.Default.Kitchen to Color(0xFF0D9488)
    "tools" -> Icons.Default.Build to AccentPurple
    "car" -> Icons.Default.DirectionsCar to BrandBlue
    "bike" -> Icons.Default.DirectionsBike to AccentTeal
    "ac" -> Icons.Default.AcUnit to Color(0xFF0284C7)
    "battery" -> Icons.Default.BatteryChargingFull to AccentGreen
    "electrician" -> Icons.Default.ElectricBolt to AccentOrange
    "plumber" -> Icons.Default.Plumbing to BrandBlueDark
    "mobile" -> Icons.Default.PhoneAndroid to BrandBlue
    "beauty", "grocery" -> Icons.Default.ShoppingBag to AccentTeal
    else -> Icons.Default.Inventory2 to BrandBlue
}

@Composable
fun ProductThumbnail(
    iconKey: String,
    modifier: Modifier = Modifier,
    imageUrl: String = ""
) {
    val resolvedIconKey = if (imageUrl.startsWith("drawable://")) {
        imageUrl.removePrefix("drawable://").ifBlank { iconKey }
    } else iconKey
    val (icon, tint) = productIconFor(resolvedIconKey)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(tint.copy(alpha = 0.12f), BrandBlueLight.copy(alpha = 0.7f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = resolvedIconKey,
            tint = tint,
            modifier = Modifier.size(34.dp)
        )
        if (imageUrl.isNotBlank() && !imageUrl.startsWith("drawable://")) {
            AsyncImage(
                model = imageUrl,
                contentDescription = resolvedIconKey,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = BrandBlueLight,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(14.dp))
                OutlinedButton(onClick = onAction, shape = RoundedCornerShape(12.dp)) {
                    Text(actionLabel)
                }
            }
        }
    }
}
