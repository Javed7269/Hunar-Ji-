package com.example.ui.screens.delivery

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.AppWorkspace
import com.example.data.model.KycApplication
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.ui.components.DeliveryRouteCanvasMap
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.HunarJiTopAppHeader
import com.example.ui.components.KycVerificationStatusCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberDark
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HunarJiViewModel
import com.example.ui.viewmodel.UiState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay

enum class DeliveryTab { HOME, ORDERS, LIVE_MAP, WALLET, PROFILE }

@Composable
fun DeliveryWorkspace(
    viewModel: HunarJiViewModel,
    onOpenWorkspaceSwitcher: () -> Unit,
    onOpenKycWizard: (String) -> Unit = {},
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = (profileState as? UiState.Success)?.data
    val myKyc by viewModel.myKycApplication.collectAsStateWithLifecycle()
    val availableDeliveries by viewModel.availableDeliveries.collectAsStateWithLifecycle()
    val myDeliveries by viewModel.myDeliveries.collectAsStateWithLifecycle()
    val walletTx by viewModel.walletTransactions.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val config by viewModel.platformConfig.collectAsStateWithLifecycle()

    val isKycApproved = viewModel.isAdminUser ||
        (myKyc?.kycStatus == "APPROVED" && myKyc?.accountStatus == "ACTIVE") ||
        (profile?.verified == true && profile.kycStatus == "APPROVED")
    var showKycGateDialog by remember { mutableStateOf(false) }

    fun requireKycApproved(action: () -> Unit) {
        if (isKycApproved) action() else showKycGateDialog = true
    }

    var currentTab by remember { mutableStateOf(DeliveryTab.HOME) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var gpsCoordsText by remember { mutableStateOf("Patna Junction -> Civil Lines (1.8 km)") }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                LocationServices.getFusedLocationProviderClient(context).lastLocation
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            gpsCoordsText = "GPS Live: ${"%.4f".format(loc.latitude)}, ${"%.4f".format(loc.longitude)}"
                        }
                    }
            } catch (_: SecurityException) {}
        }
    }

    if (currentTab != DeliveryTab.HOME) {
        BackHandler { currentTab = DeliveryTab.HOME }
    }

    val activeOrder = myDeliveries.firstOrNull { it.status == "OUT_FOR_DELIVERY" } ?: myDeliveries.firstOrNull()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                HunarJiTopAppHeader(
                    onMenuClick = { currentTab = DeliveryTab.PROFILE },
                    notificationCount = notifications.count { !it.read },
                    onNotificationsClick = {},
                    activeWorkspace = AppWorkspace.DELIVERY,
                    onSwitchWorkspaceClick = onOpenWorkspaceSwitcher
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.navigationBarsPadding()) {
                val items = listOf(
                    Triple(DeliveryTab.HOME, "Home", Icons.Default.Home),
                    Triple(DeliveryTab.ORDERS, "Orders", Icons.Default.ReceiptLong),
                    Triple(DeliveryTab.LIVE_MAP, "Tracking", Icons.Default.LocationOn),
                    Triple(DeliveryTab.WALLET, "Wallet", Icons.Default.Wallet),
                    Triple(DeliveryTab.PROFILE, "Profile", Icons.Default.Person)
                )
                items.forEach { (tab, label, icon) ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandBlue,
                            selectedTextColor = BrandBlue,
                            indicatorColor = BrandBlueLight
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        if (!config.deliveryAppEnabled) {
            Box(Modifier.fillMaxSize().padding(innerPadding).padding(24.dp), contentAlignment = Alignment.Center) {
                EmptyStateCard(
                    icon = Icons.Default.DeliveryDining,
                    title = "Delivery App Paused by Admin",
                    subtitle = "Admin has temporarily disabled the Delivery Boy workspace.",
                    actionLabel = "Switch App",
                    onAction = onOpenWorkspaceSwitcher
                )
            }
        } else {
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when (currentTab) {
                    DeliveryTab.HOME -> DeliveryDashboardContent(
                        name = profile?.displayName?.ifBlank { "Delivery Partner" } ?: "Delivery Partner",
                        hunarUserId = profile?.hunarUserId ?: "",
                        fallbackKycStatus = profile?.kycStatus ?: "NOT_SUBMITTED",
                        kyc = myKyc,
                        online = profile?.online ?: true,
                        totalDeliveries = myDeliveries.size,
                        deliveredCount = myDeliveries.count { it.status == "DELIVERED" },
                        pendingCount = availableDeliveries.size,
                        onOpenKycForm = { onOpenKycWizard("DELIVERY") },
                        onToggleOnline = { online -> requireKycApproved { viewModel.toggleOnlineStatus(online) } },
                        onOpenOrders = { currentTab = DeliveryTab.ORDERS },
                        onOpenWallet = { currentTab = DeliveryTab.WALLET },
                        onOpenProfile = { currentTab = DeliveryTab.PROFILE },
                        onOpenTracking = { currentTab = DeliveryTab.LIVE_MAP }
                    )

                    DeliveryTab.ORDERS -> DeliveryOrdersContent(
                        availableOrders = availableDeliveries,
                        myOrders = myDeliveries,
                        isKycApproved = isKycApproved,
                        onOpenKyc = { onOpenKycWizard("DELIVERY") },
                        onAccept = { ord ->
                            requireKycApproved {
                                viewModel.acceptOrUpdateDelivery(ord.id, OrderStatus.OUT_FOR_DELIVERY)
                                currentTab = DeliveryTab.LIVE_MAP
                            }
                        },
                        onReject = { ord ->
                            requireKycApproved { viewModel.updateOrderStatus(ord.id, OrderStatus.CANCELLED) }
                        }
                    )

                    DeliveryTab.LIVE_MAP -> DeliveryLiveTrackingContent(
                        order = activeOrder,
                        gpsStatus = gpsCoordsText,
                        onRequestGps = {
                            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                try {
                                    LocationServices.getFusedLocationProviderClient(context).lastLocation
                                        .addOnSuccessListener { loc ->
                                            if (loc != null) {
                                                gpsCoordsText = "GPS Live: ${"%.4f".format(loc.latitude)}, ${"%.4f".format(loc.longitude)}"
                                            }
                                        }
                                } catch (_: SecurityException) {}
                            } else {
                                locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                            }
                        },
                        onMarkDelivered = { ord ->
                            requireKycApproved { viewModel.acceptOrUpdateDelivery(ord.id, OrderStatus.DELIVERED, markPaid = true) }
                        },
                        onCancel = { ord ->
                            requireKycApproved { viewModel.acceptOrUpdateDelivery(ord.id, OrderStatus.CANCELLED) }
                        }
                    )

                    DeliveryTab.WALLET -> DeliveryWalletContent(
                        balance = profile?.walletBalance ?: 620.0,
                        deliveredCount = myDeliveries.count { it.status == "DELIVERED" },
                        transactions = walletTx,
                        onWithdraw = { requireKycApproved { showWithdrawDialog = true } }
                    )

                    DeliveryTab.PROFILE -> DeliveryProfileContent(
                        name = profile?.displayName?.ifBlank { "Delivery Partner" } ?: "Delivery Partner",
                        hunarUserId = profile?.hunarUserId ?: "",
                        fallbackKycStatus = profile?.kycStatus ?: "NOT_SUBMITTED",
                        kyc = myKyc,
                        online = profile?.online ?: true,
                        vehicle = myKyc?.let { k ->
                            listOf(k.vehicleType, k.vehicleBrand, k.vehicleModel, k.vehicleRegNumber)
                                .filter { it.isNotBlank() }
                                .joinToString(" • ")
                        }?.ifBlank { null }
                            ?: profile?.vehicleInfo?.ifBlank { "Vehicle details pending" }
                            ?: "Vehicle details pending",
                        onOpenKycForm = { onOpenKycWizard("DELIVERY") },
                        onToggleOnline = { online -> requireKycApproved { viewModel.toggleOnlineStatus(online) } },
                        onSwitchApp = onOpenWorkspaceSwitcher,
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }

    if (showWithdrawDialog) {
        var amount by remember { mutableStateOf("500") }
        var upi by remember { mutableStateOf("rajudelivery@upi") }
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Withdraw Delivery Earnings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = amount, onValueChange = { amount = it.filter(Char::isDigit) }, label = { Text("Amount (₹)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = upi, onValueChange = { upi = it }, label = { Text("UPI ID / Bank Account") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) viewModel.requestWithdrawal("DELIVERY", amt, upi)
                    showWithdrawDialog = false
                }) { Text("Withdraw") }
            },
            dismissButton = { TextButton(onClick = { showWithdrawDialog = false }) { Text("Cancel") } }
        )
    }
    if (showKycGateDialog) {
        AlertDialog(
            onDismissRequest = { showKycGateDialog = false },
            title = { Text("Delivery Partner KYC Required") },
            text = {
                Text("Complete your Delivery Partner registration, vehicle details, and KYC documents and await Admin approval before going online, accepting deliveries, or withdrawing earnings.")
            },
            confirmButton = {
                Button(onClick = {
                    showKycGateDialog = false
                    onOpenKycWizard("DELIVERY")
                }) { Text("Complete / View KYC") }
            },
            dismissButton = {
                TextButton(onClick = { showKycGateDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun DeliveryDashboardContent(
    name: String,
    hunarUserId: String,
    fallbackKycStatus: String,
    kyc: KycApplication?,
    online: Boolean,
    totalDeliveries: Int,
    deliveredCount: Int,
    pendingCount: Int,
    onOpenKycForm: () -> Unit,
    onToggleOnline: (Boolean) -> Unit,
    onOpenOrders: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenTracking: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            KycVerificationStatusCard(
                kyc = kyc,
                roleLabel = "Delivery",
                hunarUserId = hunarUserId,
                fallbackStatus = fallbackKycStatus,
                onOpenKycForm = onOpenKycForm
            )
        }
        // Blue Welcome Banner + Today's Earning (matching Image 2)
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(BrandBlueDark, BrandBlue)), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Welcome Back!", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(name, style = MaterialTheme.typography.titleLarge.copy(color = Color.White))
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = if (hunarUserId.isNotBlank()) "Partner ID: $hunarUserId" else "Complete registration for Partner ID",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Image(
                                painter = painterResource(id = R.drawable.img_worker_avatar),
                                contentDescription = name,
                                modifier = Modifier.size(56.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrandBlue, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Today's Earning", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("₹${(deliveredCount * 120).coerceAtLeast(750)}", style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.ExtraBold))
                                    Spacer(Modifier.width(8.dp))
                                    Text("↑ 12%", style = MaterialTheme.typography.labelMedium, color = Color(0xFF86EFAC))
                                }
                            }
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }

        // 4 Stat Tiles (Total Orders, Delivered, Pending, Rating 4.8★)
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DeliveryStatTile("Orders", "$totalDeliveries", BrandBlue, Modifier.weight(1f))
                DeliveryStatTile("Delivered", "$deliveredCount", AccentGreen, Modifier.weight(1f))
                DeliveryStatTile("Available", "$pendingCount", AccentOrange, Modifier.weight(1f))
                DeliveryStatTile("Rating", "4.8★", AccentAmber, Modifier.weight(1f))
            }
        }

        // Quick Actions (Go Online/Offline, My Orders, Wallet, Profile)
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (online) "Online" else "Offline", style = MaterialTheme.typography.labelMedium, color = if (online) AccentGreen else TextMuted)
                    Spacer(Modifier.width(6.dp))
                    Switch(checked = online, onCheckedChange = onToggleOnline)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DeliveryQuickTile(if (online) "Go Offline" else "Go Online", Icons.Default.PlayArrow, AccentGreen, { onToggleOnline(!online) }, Modifier.weight(1f))
                DeliveryQuickTile("My Orders", Icons.Default.ReceiptLong, BrandBlue, onOpenOrders, Modifier.weight(1f))
                DeliveryQuickTile("Wallet", Icons.Default.Wallet, BrandBlueDark, onOpenWallet, Modifier.weight(1f))
                DeliveryQuickTile("Tracking", Icons.Default.LocationOn, AccentOrange, onOpenTracking, Modifier.weight(1f))
            }
        }

        // Deliver Happiness Amber Banner (matching Image 2)
        item {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(132.dp)) {
                Box(Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_delivery_hero),
                        contentDescription = "Deliver Happiness",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(AccentAmberDark.copy(alpha = 0.92f), Color.Transparent)))
                    )
                    Column(modifier = Modifier.padding(16.dp).align(Alignment.CenterStart)) {
                        Text("Deliver Happiness\nwith HUNHAR JI", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.ExtraBold))
                        Spacer(Modifier.height(4.dp))
                        Text("Fast pickups • Instant wallet credit", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryStatTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium.copy(color = color, fontWeight = FontWeight.Bold))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun DeliveryQuickTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
        Column(modifier = Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = tint)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
private fun DeliveryOrdersContent(
    availableOrders: List<Order>,
    myOrders: List<Order>,
    isKycApproved: Boolean,
    onOpenKyc: () -> Unit,
    onAccept: (Order) -> Unit,
    onReject: (Order) -> Unit
) {
    var tab by remember { mutableStateOf("New") }
    val displayList = when (tab) {
        "History" -> myOrders.filter { it.status in listOf("DELIVERED", "CANCELLED") }
        "Active" -> myOrders.filter { it.status == "OUT_FOR_DELIVERY" }
        else -> availableOrders
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isKycApproved) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.DeliveryDining,
                    title = "Delivery KYC Pending",
                    subtitle = "Accepting orders unlocks once Admin verifies your identity & vehicle documents.",
                    actionLabel = "Complete / View KYC",
                    onAction = onOpenKyc
                )
            }
        }
        item {
            Text("Available Orders", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("New" to availableOrders.size, "Active" to myOrders.count { it.status == "OUT_FOR_DELIVERY" }, "History" to myOrders.count { it.status == "DELIVERED" }).forEach { (lbl, cnt) ->
                    val sel = tab == lbl
                    Surface(
                        color = if (sel) BrandBlue else Color.White,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, if (sel) BrandBlue else Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable { tab = lbl }
                    ) {
                        Text("$lbl ($cnt)", color = if (sel) Color.White else TextPrimary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp))
                    }
                }
            }
        }
        if (displayList.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.DeliveryDining,
                    title = "No $tab Deliveries",
                    subtitle = "Confirmed seller orders ready for pickup appear here automatically."
                )
            }
        } else {
            items(displayList, key = { it.id }) { ord ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Order #${ord.id}", style = MaterialTheme.typography.titleSmall)
                            Surface(color = BrandBlueLight, shape = RoundedCornerShape(8.dp)) {
                                Text("₹${ord.deliveryFee.coerceAtLeast(120.0).toInt()}", style = MaterialTheme.typography.labelLarge.copy(color = BrandBlueDark, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                        Text(ord.sellerName, style = MaterialTheme.typography.titleMedium)
                        Text("Pickup: ${ord.pickupAddress}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("Drop: ${ord.dropAddress}", style = MaterialTheme.typography.bodySmall, color = BrandBlueDark)
                        if (tab == "New") {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { onReject(ord) },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, AccentRed),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                                ) {
                                    Text("Reject")
                                }
                                Button(
                                    onClick = { onAccept(ord) },
                                    modifier = Modifier.weight(1f).testTag("accept_delivery_${ord.id}")
                                ) {
                                    Text("Accept")
                                }
                            }
                        } else {
                            StatusPill(ord.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryLiveTrackingContent(
    order: Order?,
    gpsStatus: String,
    onRequestGps: () -> Unit,
    onMarkDelivered: (Order) -> Unit,
    onCancel: (Order) -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(504) }
    LaunchedEffect(order?.id) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
    }
    val mins = (secondsLeft / 60).toString().padStart(2, '0')
    val secs = (secondsLeft % 60).toString().padStart(2, '0')

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Live Tracking", style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = onRequestGps, shape = RoundedCornerShape(50), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sync GPS")
                }
            }
            Text(gpsStatus, style = MaterialTheme.typography.bodySmall, color = BrandBlue)
        }

        if (order == null) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.LocationOn,
                    title = "No Active Route",
                    subtitle = "Accept an available order from the Orders tab to start live route navigation."
                )
            }
        } else {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Order #${order.id}", style = MaterialTheme.typography.titleMedium)
                            StatusPill(order.status)
                        }
                        Text("Customer: ${order.customerName}", style = MaterialTheme.typography.bodyMedium)
                        Box(modifier = Modifier.fillMaxWidth().height(175.dp).clip(RoundedCornerShape(14.dp))) {
                            DeliveryRouteCanvasMap(progress = 0.6f, modifier = Modifier.fillMaxSize())
                            Surface(
                                color = BrandBlue,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                            ) {
                                Text("1.8 km", style = MaterialTheme.typography.labelSmall.copy(color = Color.White), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        Text("Pickup: ${order.pickupAddress}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("Drop: ${order.dropAddress}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BrandBlue, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Delivery Time", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                Text("00:$mins:$secs", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            }
                        }
                        if (order.status != "DELIVERED" && order.status != "CANCELLED") {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { onCancel(order) },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, AccentRed),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = { onMarkDelivered(order) },
                                    modifier = Modifier.weight(1f).testTag("mark_delivered_${order.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Delivered")
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
private fun DeliveryWalletContent(
    balance: Double,
    deliveredCount: Int,
    transactions: List<com.example.data.model.WalletTransaction>,
    onWithdraw: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = BrandBlue)) {
                Row(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Available Balance", color = Color.White.copy(alpha = 0.85f))
                        Text("₹${balance.toInt()}", style = MaterialTheme.typography.displaySmall.copy(color = Color.White))
                    }
                    Button(onClick = onWithdraw, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BrandBlueDark)) {
                        Text("Withdraw")
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DeliveryStatTile("₹${(deliveredCount * 120).coerceAtLeast(24850)}", "Total Earnings", BrandBlueDark, Modifier.weight(1f))
                DeliveryStatTile("${deliveredCount.coerceAtLeast(42)}", "Total Deliveries", AccentGreen, Modifier.weight(1f))
            }
        }
        items(transactions, key = { it.id }) { tx ->
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(tx.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Text("₹${tx.amount.toInt()}", style = MaterialTheme.typography.titleMedium, color = BrandBlueDark)
                }
            }
        }
    }
}

@Composable
private fun DeliveryProfileContent(
    name: String,
    hunarUserId: String,
    fallbackKycStatus: String,
    kyc: KycApplication?,
    online: Boolean,
    vehicle: String,
    onOpenKycForm: () -> Unit,
    onToggleOnline: (Boolean) -> Unit,
    onSwitchApp: () -> Unit,
    onSignOut: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            KycVerificationStatusCard(
                kyc = kyc,
                roleLabel = "Delivery",
                hunarUserId = hunarUserId,
                fallbackStatus = fallbackKycStatus,
                onOpenKycForm = onOpenKycForm
            )
        }
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.img_worker_avatar),
                        contentDescription = name,
                        modifier = Modifier.size(64.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, style = MaterialTheme.typography.titleLarge)
                        if (hunarUserId.isNotBlank()) {
                            Text("User ID: $hunarUserId", style = MaterialTheme.typography.labelSmall, color = BrandBlueDark)
                        }
                        Text(if (online) "● Online • Delivery Boy" else "● Offline • Delivery Boy", style = MaterialTheme.typography.bodySmall, color = if (online) AccentGreen else TextMuted)
                        Text(vehicle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Switch(checked = online, onCheckedChange = onToggleOnline)
                }
            }
        }
        val rows = listOf(
            "Vehicle Details ($vehicle)" to Icons.Default.DirectionsBike,
            "Bank Details (${if (kyc?.bankVerified == true) "Verified" else "Pending"})" to Icons.Default.AccountBalance,
            "KYC & License Documents" to Icons.Default.Description,
            "Ratings & Reviews (4.8★)" to Icons.Default.Star,
            "Help & Support" to Icons.Default.HeadsetMic
        )
        items(rows) { (title, icon) ->
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = title, tint = BrandBlue)
                    Spacer(Modifier.width(12.dp))
                    Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onSwitchApp, modifier = Modifier.weight(1f)) { Text("Switch App") }
                OutlinedButton(onClick = onSignOut, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, AccentRed)) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = AccentRed)
                    Spacer(Modifier.width(4.dp))
                    Text("Logout", color = AccentRed)
                }
            }
        }
    }
}
