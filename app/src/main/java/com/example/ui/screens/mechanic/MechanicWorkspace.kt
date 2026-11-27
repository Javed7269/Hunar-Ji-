package com.example.ui.screens.mechanic

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.AppWorkspace
import com.example.data.model.BookingStatus
import com.example.data.model.ServiceBooking
import com.example.data.model.ServiceItem
import com.example.data.model.KycApplication
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.HunarJiTopAppHeader
import com.example.ui.components.KycVerificationStatusCard
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.StatusPill
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HunarJiViewModel
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.delay

enum class MechanicTab { HOME, ORDERS, WALLET, SETTINGS, MORE }

@Composable
fun MechanicWorkspace(
    viewModel: HunarJiViewModel,
    onOpenWorkspaceSwitcher: () -> Unit,
    onOpenKycWizard: (String) -> Unit = {},
    onSignOut: () -> Unit
) {
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = (profileState as? UiState.Success)?.data
    val myKyc by viewModel.myKycApplication.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val pendingBookings by viewModel.pendingBookings.collectAsStateWithLifecycle()
    val myBookings by viewModel.mechanicBookings.collectAsStateWithLifecycle()
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

    var currentTab by remember { mutableStateOf(MechanicTab.HOME) }
    var showLiveVideoScreen by remember { mutableStateOf(false) }
    var activeLiveId by remember { mutableStateOf<String?>(null) }
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    if (showLiveVideoScreen) {
        BackHandler { showLiveVideoScreen = false }
    } else if (currentTab != MechanicTab.HOME) {
        BackHandler { currentTab = MechanicTab.HOME }
    }

    val allBookings = remember(pendingBookings, myBookings) {
        (myBookings + pendingBookings).distinctBy { it.id }
    }

    if (showLiveVideoScreen) {
        val streamId = activeLiveId ?: "live_engine_care"
        val messages by viewModel.activeStreamMessages.collectAsStateWithLifecycle()
        MechanicLiveVideoScreen(
            mechanicName = profile?.displayName?.ifBlank { viewModel.currentUserName } ?: viewModel.currentUserName,
            isBroadcasting = activeLiveId != null,
            messages = messages,
            onStartLive = {
                viewModel.startLiveStream("Car Engine Repair Live – Expert Support", "Car Engine Repair") { id ->
                    activeLiveId = id
                }
            },
            onEndLive = {
                activeLiveId?.let { viewModel.stopLiveStream(it) }
                activeLiveId = null
                showLiveVideoScreen = false
            },
            onSendMessage = { txt -> viewModel.sendStreamMessage(streamId, txt) },
            onBack = { showLiveVideoScreen = false }
        )
        return
    }

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
                    onMenuClick = { currentTab = MechanicTab.MORE },
                    notificationCount = notifications.count { !it.read },
                    onNotificationsClick = { currentTab = MechanicTab.SETTINGS },
                    activeWorkspace = AppWorkspace.MECHANIC,
                    onSwitchWorkspaceClick = onOpenWorkspaceSwitcher
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                modifier = Modifier.navigationBarsPadding()
            ) {
                val items = listOf(
                    Triple(MechanicTab.HOME, "Home", Icons.Default.Home),
                    Triple(MechanicTab.ORDERS, "Orders", Icons.Default.ReceiptLong),
                    Triple(MechanicTab.WALLET, "Wallet", Icons.Default.Wallet),
                    Triple(MechanicTab.SETTINGS, "Settings", Icons.Default.Settings),
                    Triple(MechanicTab.MORE, "More", Icons.Default.MoreHoriz)
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
        if (!config.mechanicAppEnabled) {
            Box(Modifier.fillMaxSize().padding(innerPadding).padding(24.dp), contentAlignment = Alignment.Center) {
                EmptyStateCard(
                    icon = Icons.Default.Build,
                    title = "Mechanic App Paused by Admin",
                    subtitle = "Admin has temporarily disabled the Mechanic workspace.",
                    actionLabel = "Switch App",
                    onAction = onOpenWorkspaceSwitcher
                )
            }
        } else {
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when (currentTab) {
                    MechanicTab.HOME -> MechanicHomeContent(
                        name = profile?.displayName?.ifBlank { "Mechanic Partner" } ?: "Mechanic Partner",
                        hunarUserId = profile?.hunarUserId ?: "",
                        fallbackKycStatus = profile?.kycStatus ?: "NOT_SUBMITTED",
                        kyc = myKyc,
                        online = profile?.online ?: true,
                        walletBalance = profile?.walletBalance ?: 3250.0,
                        services = services,
                        onOpenKycForm = { onOpenKycWizard("MECHANIC") },
                        onToggleOnline = { online -> requireKycApproved { viewModel.toggleOnlineStatus(online) } },
                        onWithdraw = { requireKycApproved { showWithdrawDialog = true } },
                        onGoLive = {
                            requireKycApproved {
                                viewModel.setActiveStream(activeLiveId ?: "live_engine_care")
                                showLiveVideoScreen = true
                            }
                        },
                        onMyServices = { requireKycApproved { showAddServiceDialog = true } },
                        onBookings = { currentTab = MechanicTab.ORDERS },
                        onAddService = { requireKycApproved { showAddServiceDialog = true } }
                    )

                    MechanicTab.ORDERS -> MechanicOrdersContent(
                        bookings = allBookings,
                        isKycApproved = isKycApproved,
                        onOpenKyc = { onOpenKycWizard("MECHANIC") },
                        onUpdateStatus = { id, st ->
                            requireKycApproved { viewModel.updateBookingStatus(id, st) }
                        }
                    )

                    MechanicTab.WALLET -> MechanicWalletContent(
                        balance = profile?.walletBalance ?: 3250.0,
                        completedCount = myBookings.count { it.status == "COMPLETED" },
                        transactions = walletTx,
                        onWithdraw = { requireKycApproved { showWithdrawDialog = true } }
                    )

                    MechanicTab.SETTINGS, MechanicTab.MORE -> MechanicProfileSettingsContent(
                        name = profile?.displayName?.ifBlank { "Mechanic Partner" } ?: "Mechanic Partner",
                        hunarUserId = profile?.hunarUserId ?: "",
                        fallbackKycStatus = profile?.kycStatus ?: "NOT_SUBMITTED",
                        kyc = myKyc,
                        online = profile?.online ?: true,
                        skills = myKyc?.mainDepartment?.takeIf { it.isNotBlank() }
                            ?: profile?.skills?.ifBlank { "Car Engine, Bike Tuning, AC Service" }
                            ?: "Car Engine, Bike Tuning, AC Service",
                        onOpenKyc = { onOpenKycWizard("MECHANIC") },
                        onToggleOnline = { online -> requireKycApproved { viewModel.toggleOnlineStatus(online) } },
                        onOpenServices = { requireKycApproved { showAddServiceDialog = true } },
                        onOpenWallet = { currentTab = MechanicTab.WALLET },
                        onOpenOrders = { currentTab = MechanicTab.ORDERS },
                        onSwitchApp = onOpenWorkspaceSwitcher,
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }

    if (showAddServiceDialog) {
        AddMechanicServiceDialog(
            onSave = { title, cat, price, mins ->
                viewModel.saveServiceItem(
                    ServiceItem(
                        title = title,
                        category = cat,
                        description = "Expert $cat by verified Mistri Ji mechanic",
                        price = price,
                        durationMinutes = mins,
                        iconKey = when (cat) {
                            "Bike Repair" -> "bike"
                            "AC Repair" -> "ac"
                            "Battery Service" -> "battery"
                            else -> "car"
                        }
                    )
                )
                showAddServiceDialog = false
            },
            onDismiss = { showAddServiceDialog = false }
        )
    }

    if (showWithdrawDialog) {
        var amount by remember { mutableStateOf("500") }
        var upi by remember { mutableStateOf("raju@upi") }
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Withdraw Mechanic Earnings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = amount, onValueChange = { amount = it.filter(Char::isDigit) }, label = { Text("Amount (₹)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = upi, onValueChange = { upi = it }, label = { Text("UPI ID / Bank Account") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) viewModel.requestWithdrawal("MECHANIC", amt, upi)
                    showWithdrawDialog = false
                }) { Text("Request Payout") }
            },
            dismissButton = { TextButton(onClick = { showWithdrawDialog = false }) { Text("Cancel") } }
        )
    }
    if (showKycGateDialog) {
        AlertDialog(
            onDismissRequest = { showKycGateDialog = false },
            title = { Text("KYC Approval Required") },
            text = {
                Text("Complete your Mechanic registration & KYC documents and await Admin verification before accepting jobs, adding services, going live, or withdrawing earnings.")
            },
            confirmButton = {
                Button(onClick = {
                    showKycGateDialog = false
                    onOpenKycWizard("MECHANIC")
                }) { Text("Complete / View KYC") }
            },
            dismissButton = {
                TextButton(onClick = { showKycGateDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun MechanicHomeContent(
    name: String,
    hunarUserId: String,
    fallbackKycStatus: String,
    kyc: KycApplication?,
    online: Boolean,
    walletBalance: Double,
    services: List<ServiceItem>,
    onOpenKycForm: () -> Unit,
    onToggleOnline: (Boolean) -> Unit,
    onWithdraw: () -> Unit,
    onGoLive: () -> Unit,
    onMyServices: () -> Unit,
    onBookings: () -> Unit,
    onAddService: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            KycVerificationStatusCard(
                kyc = kyc,
                roleLabel = "Mechanic",
                hunarUserId = hunarUserId,
                fallbackStatus = fallbackKycStatus,
                onOpenKycForm = onOpenKycForm
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_worker_avatar),
                    contentDescription = name,
                    modifier = Modifier.size(60.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleLarge)
                    if (hunarUserId.isNotBlank()) {
                        Text("ID: $hunarUserId", style = MaterialTheme.typography.labelSmall, color = BrandBlueDark)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (online) AccentGreen else TextMuted,
                            shape = CircleShape,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (online) "Online" else "Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (online) AccentGreen else TextMuted
                        )
                    }
                }
                Switch(checked = online, onCheckedChange = onToggleOnline)
            }
        }

        // Blue Wallet Balance Card (matching Image 4)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(BrandBlueDark, BrandBlue)))
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Wallet Balance", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                        Text("₹${walletBalance.toInt()}", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.ExtraBold))
                    }
                    Button(
                        onClick = onWithdraw,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BrandBlueDark)
                    ) {
                        Text("Withdraw", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3 Circular Quick Actions: Go Live, My Services, Bookings (matching Image 4)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MechanicCircleAction(Icons.Default.Videocam, "Go Live", AccentRed, Color(0xFFFEE2E2), onGoLive)
                MechanicCircleAction(Icons.Default.Build, "My Services", BrandBlue, BrandBlueLight, onMyServices)
                MechanicCircleAction(Icons.Default.DateRange, "Bookings", BrandBlue, BrandBlueLight, onBookings)
            }
        }

        // Expert Mechanic Banner (matching Image 4)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(136.dp)
            ) {
                Box(Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_live_mechanic),
                        contentDescription = "Expert Mechanic",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(BrandBlueDark.copy(alpha = 0.94f), Color.Transparent)))
                    )
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Expert Mechanic\nOn Your Service", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            Text("Repair • Service • Maintenance", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                        }
                        Button(
                            onClick = onBookings,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text("Manage Jobs ->", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // My Services Horizontal Row
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("My Services", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onAddService) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Add Service")
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(services, key = { it.id }) { srv ->
                    Card(
                        modifier = Modifier.width(126.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            ProductThumbnail(iconKey = srv.iconKey, modifier = Modifier.fillMaxWidth().height(64.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(srv.category, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                            Text("₹${srv.price.toInt()}", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MechanicCircleAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconTint: Color,
    bgTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(color = bgTint, shape = CircleShape, modifier = Modifier.size(62.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
    }
}

@Composable
private fun MechanicLiveVideoScreen(
    mechanicName: String,
    isBroadcasting: Boolean,
    messages: List<com.example.data.model.StreamMessage>,
    onStartLive: () -> Unit,
    onEndLive: () -> Unit,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    var muted by remember { mutableStateOf(false) }
    var elapsedSec by remember { mutableIntStateOf(765) }
    var chatInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsedSec++
        }
    }
    val mm = (elapsedSec / 60).toString().padStart(2, '0')
    val ss = (elapsedSec % 60).toString().padStart(2, '0')

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandBlueDark)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) { Text("<- Back", color = Color.White) }
                Text("Live Video Support", style = MaterialTheme.typography.titleLarge.copy(color = Color.White), modifier = Modifier.weight(1f))
                if (!isBroadcasting) {
                    Button(onClick = onStartLive, colors = ButtonDefaults.buttonColors(containerColor = AccentRed)) {
                        Text("Start Live")
                    }
                }
            }
        }
    ) { pad ->
        Box(modifier = Modifier.fillMaxSize().padding(pad)) {
            Image(
                painter = painterResource(id = R.drawable.img_live_mechanic),
                contentDescription = "Live Video Feed",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Top badges
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(color = AccentRed, shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RadioButtonChecked, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("LIVE", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(color = Color.Black.copy(alpha = 0.65f), shape = RoundedCornerShape(8.dp)) {
                        Text("1.2K viewers", style = MaterialTheme.typography.labelSmall.copy(color = Color.White), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.img_worker_avatar),
                        contentDescription = mechanicName,
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Bottom overlay controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(14.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        messages.takeLast(3).forEach { m ->
                            Text("${m.senderName}: ${m.text}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Reply to viewers...", color = Color.White.copy(alpha = 0.7f)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (chatInput.isNotBlank()) {
                            onSendMessage(chatInput)
                            chatInput = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { muted = !muted },
                        modifier = Modifier.background(Color.DarkGray, CircleShape)
                    ) {
                        Icon(if (muted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = "Toggle mic", tint = Color.White)
                    }
                    Button(
                        onClick = onEndLive,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("End Call")
                    }
                    IconButton(
                        onClick = {},
                        modifier = Modifier.background(Color.DarkGray, CircleShape)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Switch camera", tint = Color.White)
                    }
                }
                Surface(color = Color.White, shape = RoundedCornerShape(50), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(
                        text = "● Live | 00:$mm:$ss",
                        style = MaterialTheme.typography.labelMedium.copy(color = AccentGreen, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MechanicOrdersContent(
    bookings: List<ServiceBooking>,
    isKycApproved: Boolean,
    onOpenKyc: () -> Unit,
    onUpdateStatus: (String, BookingStatus) -> Unit
) {
    var filter by remember { mutableStateOf("All") }
    val filtered = remember(bookings, filter) {
        when (filter) {
            "Pending" -> bookings.filter { it.status == "PENDING" }
            "Accepted" -> bookings.filter { it.status in listOf("ACCEPTED", "ON_THE_WAY", "STARTED") }
            "Completed" -> bookings.filter { it.status == "COMPLETED" }
            else -> bookings
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isKycApproved) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Build,
                    title = "Verification Pending",
                    subtitle = "Job acceptance unlocks after Admin approves your Mechanic KYC.",
                    actionLabel = "Complete / View KYC",
                    onAction = onOpenKyc
                )
            }
        }
        item {
            Text("My Service Orders", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Pending", "Accepted", "Completed").forEach { tab ->
                    val selected = filter == tab
                    Surface(
                        color = if (selected) BrandBlue else Color.White,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (selected) BrandBlue else Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable { filter = tab }
                    ) {
                        Text(
                            tab,
                            style = MaterialTheme.typography.labelMedium.copy(color = if (selected) Color.White else TextPrimary),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Build,
                    title = "No Service Bookings",
                    subtitle = "New customer service bookings will appear here in real time."
                )
            }
        } else {
            items(filtered, key = { it.id }) { bk ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("#${bk.id}", style = MaterialTheme.typography.titleSmall)
                            StatusPill(bk.status)
                        }
                        Text(bk.serviceTitle, style = MaterialTheme.typography.titleMedium, color = BrandBlueDark)
                        Text("Customer: ${bk.customerName} • ${bk.serviceAddress}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("₹${bk.amount.toInt()} • ${bk.scheduledDate} ${bk.scheduledTime}", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            when (bk.status) {
                                "PENDING" -> {
                                    OutlinedButton(onClick = { onUpdateStatus(bk.id, BookingStatus.CANCELLED) }, modifier = Modifier.weight(1f)) { Text("Reject") }
                                    Button(onClick = { onUpdateStatus(bk.id, BookingStatus.ACCEPTED) }, modifier = Modifier.weight(1f)) { Text("Accept Job") }
                                }
                                "ACCEPTED", "ON_THE_WAY" -> {
                                    Button(onClick = { onUpdateStatus(bk.id, BookingStatus.STARTED) }, modifier = Modifier.fillMaxWidth()) { Text("Start Service") }
                                }
                                "STARTED" -> {
                                    Button(
                                        onClick = { onUpdateStatus(bk.id, BookingStatus.COMPLETED) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Mark Completed") }
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
private fun MechanicWalletContent(
    balance: Double,
    completedCount: Int,
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
                        Text("Mechanic Wallet", color = Color.White.copy(alpha = 0.85f))
                        Text("₹${balance.toInt()}", style = MaterialTheme.typography.displaySmall.copy(color = Color.White))
                        Text("Completed Jobs: $completedCount", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    }
                    Button(onClick = onWithdraw, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BrandBlueDark)) {
                        Text("Withdraw")
                    }
                }
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
private fun MechanicProfileSettingsContent(
    name: String,
    hunarUserId: String,
    fallbackKycStatus: String,
    kyc: KycApplication?,
    online: Boolean,
    skills: String,
    onOpenKyc: () -> Unit,
    onToggleOnline: (Boolean) -> Unit,
    onOpenServices: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenOrders: () -> Unit,
    onSwitchApp: () -> Unit,
    onSignOut: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.img_worker_avatar),
                        contentDescription = name,
                        modifier = Modifier.size(74.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(name, style = MaterialTheme.typography.titleLarge)
                    if (hunarUserId.isNotBlank()) {
                        Text("User ID: $hunarUserId", style = MaterialTheme.typography.labelSmall, color = BrandBlueDark)
                    }
                    Text(skills, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
        item {
            KycVerificationStatusCard(
                kyc = kyc,
                roleLabel = "Mechanic",
                hunarUserId = hunarUserId,
                fallbackStatus = fallbackKycStatus,
                onOpenKycForm = onOpenKyc
            )
        }
        val menu = listOf(
            Triple("My Services", Icons.Default.Build, onOpenServices),
            Triple("Wallet & Payouts", Icons.Default.Wallet, onOpenWallet),
            Triple("Order History", Icons.Default.ReceiptLong, onOpenOrders),
            Triple("Switch Role Workspace", Icons.Default.Person, onSwitchApp)
        )
        items(menu) { (label, icon, act) ->
            Card(modifier = Modifier.fillMaxWidth().clickable(onClick = act), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = label, tint = BrandBlue)
                    Spacer(Modifier.width(12.dp))
                    Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandBlue)
                    Spacer(Modifier.width(12.dp))
                    Text("Live Location & Availability", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Switch(checked = online, onCheckedChange = onToggleOnline)
                }
            }
        }
        item {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, AccentRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout • App Version v1.0.0")
            }
        }
    }
}

@Composable
private fun AddMechanicServiceDialog(
    onSave: (String, String, Double, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Car Repair") }
    var price by remember { mutableStateOf("499") }
    var mins by remember { mutableStateOf("45") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Mechanic Service") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Service Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (Car Repair / Bike / AC)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it.filter(Char::isDigit) }, label = { Text("Price (₹)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = mins, onValueChange = { mins = it.filter(Char::isDigit) }, label = { Text("Duration (mins)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    onSave(title, category, price.toDoubleOrNull() ?: 499.0, mins.toLongOrNull() ?: 45L)
                }
            }) { Text("Save Service") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
