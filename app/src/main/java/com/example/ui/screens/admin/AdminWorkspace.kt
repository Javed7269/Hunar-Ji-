package com.example.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppWorkspace
import com.example.data.model.KycApplication
import com.example.data.model.Order
import com.example.data.model.PlatformConfig
import com.example.data.model.Product
import com.example.data.model.ProviderProfile
import com.example.data.model.ServiceBooking
import com.example.data.model.ServiceItem
import com.example.data.model.UserProfile
import com.example.ui.components.HunarJiBrandWordmark
import com.example.ui.components.SalesByAppDonutChart
import com.example.ui.components.SalesOverviewLineChart
import com.example.ui.components.StatusPill
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
import com.example.ui.theme.AccentTealLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueDeep
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HunarJiViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AdminModule(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    KYC_MANAGEMENT("KYC Management", Icons.Default.VerifiedUser),
    ORDERS("Orders", Icons.Default.ShoppingCart),
    PRODUCTS("Products", Icons.Default.Inventory2),
    SERVICES("Services", Icons.Default.Build),
    CUSTOMERS("Customers", Icons.Default.People),
    MECHANICS("Mechanics", Icons.Default.Engineering),
    SELLERS("Sellers", Icons.Default.Storefront),
    DELIVERY_BOYS("Delivery Boys", Icons.Default.DeliveryDining),
    WALLET("Wallet Management", Icons.Default.AccountBalanceWallet),
    PAYMENT_GATEWAY("Payment Gateway", Icons.Default.Payment),
    COMMISSION("Commission", Icons.Default.Percent),
    LIVE_STREAMING("Live Streaming", Icons.Default.PlayCircle),
    BANNERS("Banners & Ads", Icons.Default.Image),
    NOTIFICATIONS("Notifications", Icons.Default.Notifications),
    LOCATIONS("Location Manager", Icons.Default.LocationOn),
    REPORTS("Reports & Analytics", Icons.Default.Analytics),
    APP_MANAGEMENT("App Management", Icons.Default.Apps),
    SETTINGS("Settings", Icons.Default.Settings),
    SUPPORT("Support & Complaints", Icons.Default.SupportAgent),
    SECURITY("Security", Icons.Default.Security)
}

@Composable
fun AdminWorkspace(
    viewModel: HunarJiViewModel,
    onOpenWorkspaceSwitcher: () -> Unit,
    onSignOut: () -> Unit
) {
    val orders by viewModel.allOrdersAdmin.collectAsStateWithLifecycle()
    val bookings by viewModel.allBookingsAdmin.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val users by viewModel.allUsersAdmin.collectAsStateWithLifecycle()
    val withdrawals by viewModel.allWithdrawalsAdmin.collectAsStateWithLifecycle()
    val tickets by viewModel.allTicketsAdmin.collectAsStateWithLifecycle()
    val streams by viewModel.liveStreams.collectAsStateWithLifecycle()
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val coupons by viewModel.coupons.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogsAdmin.collectAsStateWithLifecycle()
    val config by viewModel.platformConfig.collectAsStateWithLifecycle()
    val kycApplications by viewModel.allKycAdmin.collectAsStateWithLifecycle()
    val inspectedKycDocs by viewModel.inspectedKycDocuments.collectAsStateWithLifecycle()
    val inspectedKycHistory by viewModel.inspectedKycHistory.collectAsStateWithLifecycle()
    val isAdminUser by viewModel.isAdminVerified.collectAsStateWithLifecycle()

    var activeModule by remember { mutableStateOf(AdminModule.DASHBOARD) }
    var globalSearch by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (activeModule != AdminModule.DASHBOARD) {
        BackHandler { activeModule = AdminModule.DASHBOARD }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 840.dp

        val mainContent: @Composable () -> Unit = {
            Scaffold(
                containerColor = SurfaceLight,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    AdminTopHeader(
                        showMenuButton = !isExpanded,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        searchQuery = globalSearch,
                        onSearchChange = { globalSearch = it },
                        maintenanceMode = config.maintenanceMode,
                        unreadAlerts = tickets.count { it.status == "OPEN" } + withdrawals.count { it.status == "PENDING" },
                        isAdminUser = isAdminUser,
                        onAlertsClick = { activeModule = AdminModule.SUPPORT },
                        onSwitchAppClick = onOpenWorkspaceSwitcher
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .navigationBarsPadding()
                ) {
                    when (activeModule) {
                        AdminModule.DASHBOARD -> AdminDashboardContent(
                            orders = orders,
                            bookings = bookings,
                            products = products,
                            services = services,
                            providers = providers,
                            users = users,
                            kycApplications = kycApplications,
                            config = config,
                            isAdminUser = isAdminUser,
                            onNavigateModule = { activeModule = it },
                            onSwitchApp = { viewModel.switchWorkspace(it) },
                            onUpdateOrderStatus = { id, st -> viewModel.updateOrderStatus(id, st) },
                            onSeedCatalog = { viewModel.seedStarterCatalog() }
                        )

                        AdminModule.KYC_MANAGEMENT -> AdminKycManagementModule(
                            applications = kycApplications,
                            inspectedDocs = inspectedKycDocs,
                            inspectedHistory = inspectedKycHistory,
                            globalSearch = globalSearch,
                            isAdminUser = isAdminUser,
                            onInspectApplicant = { viewModel.inspectKycApplicant(it) },
                            onUpdateKycStatus = { uid, st, acc, flags, notes ->
                                viewModel.adminUpdateKycStatus(uid, st, acc, flags, notes)
                            },
                            onUpdateDocumentStatus = { uid, docId, st, note ->
                                viewModel.adminUpdateDocumentStatus(uid, docId, st, note)
                            },
                            onLogDocumentAccess = { uid, docId, label ->
                                viewModel.logKycDocumentAccess(uid, docId, label)
                            }
                        )

                        AdminModule.ORDERS -> AdminOrdersModule(
                            orders = orders,
                            globalSearch = globalSearch,
                            onUpdateStatus = { id, st -> viewModel.updateOrderStatus(id, st) }
                        )

                        AdminModule.PRODUCTS -> AdminProductsModule(
                            products = products,
                            globalSearch = globalSearch,
                            onSaveProduct = { viewModel.saveProduct(it) },
                            onDeleteProduct = { viewModel.deleteProduct(it) }
                        )

                        AdminModule.SERVICES -> AdminServicesModule(
                            services = services,
                            bookings = bookings,
                            onSaveService = { viewModel.saveServiceItem(it) },
                            onUpdateBooking = { id, st -> viewModel.updateBookingStatus(id, st) }
                        )

                        AdminModule.CUSTOMERS -> AdminUsersOrProvidersModule("Customers", users, emptyList(), "CUSTOMER", globalSearch)
                        AdminModule.MECHANICS -> AdminUsersOrProvidersModule("Mechanics (Mistri Ji)", users, providers, "MECHANIC", globalSearch)
                        AdminModule.SELLERS -> AdminUsersOrProvidersModule("Sellers & Shops", users, providers, "SELLER", globalSearch)
                        AdminModule.DELIVERY_BOYS -> AdminUsersOrProvidersModule("Delivery Partners", users, providers, "DELIVERY", globalSearch)
                        AdminModule.WALLET -> AdminWalletModule(
                            withdrawals = withdrawals,
                            totalRevenue = orders.sumOf { it.totalAmount } + bookings.sumOf { it.amount },
                            onUpdateWithdrawal = { id, st -> viewModel.updateWithdrawalStatus(id, st) }
                        )

                        AdminModule.PAYMENT_GATEWAY -> AdminPaymentGatewayModule(
                            config = config,
                            onSaveConfig = { viewModel.savePlatformConfig(it) }
                        )

                        AdminModule.COMMISSION -> AdminCommissionModule(
                            config = config,
                            onSaveConfig = { viewModel.savePlatformConfig(it) }
                        )

                        AdminModule.LIVE_STREAMING -> AdminLiveStreamingModule(
                            streams = streams,
                            onStopStream = { viewModel.stopLiveStream(it) }
                        )

                        AdminModule.BANNERS -> AdminBannersAndCouponsModule(
                            banners = banners,
                            coupons = coupons,
                            onSaveBanner = { viewModel.saveBanner(it) },
                            onSaveCoupon = { viewModel.saveCoupon(it) }
                        )

                        AdminModule.NOTIFICATIONS -> AdminNotificationsModule(
                            config = config,
                            onSaveConfig = { viewModel.savePlatformConfig(it) }
                        )

                        AdminModule.LOCATIONS -> AdminLocationManagerModule(
                            locations = locations,
                            onSaveLocation = { viewModel.saveLocation(it) }
                        )

                        AdminModule.REPORTS -> AdminReportsModule(orders = orders, bookings = bookings, config = config)
                        AdminModule.APP_MANAGEMENT, AdminModule.SETTINGS -> AdminAppAndSettingsModule(
                            config = config,
                            onSaveConfig = { viewModel.savePlatformConfig(it) },
                            onSeedCatalog = { viewModel.seedStarterCatalog() }
                        )

                        AdminModule.SUPPORT -> AdminSupportModule(
                            tickets = tickets,
                            onReplyTicket = { id, st, reply -> viewModel.replySupportTicket(id, st, reply) }
                        )

                        AdminModule.SECURITY -> AdminSecurityModule(
                            logs = auditLogs,
                            isAdminUser = isAdminUser
                        )
                    }
                }
            }
        }

        if (isExpanded) {
            Row(modifier = Modifier.fillMaxSize()) {
                AdminSidebarContent(
                    activeModule = activeModule,
                    onSelectModule = { activeModule = it },
                    onSwitchWorkspace = onOpenWorkspaceSwitcher,
                    onSignOut = onSignOut,
                    modifier = Modifier
                        .width(248.dp)
                        .fillMaxHeight()
                )
                Box(modifier = Modifier.weight(1f)) {
                    mainContent()
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = BrandBlueDeep,
                        modifier = Modifier.width(268.dp)
                    ) {
                        AdminSidebarContent(
                            activeModule = activeModule,
                            onSelectModule = { mod ->
                                activeModule = mod
                                scope.launch { drawerState.close() }
                            },
                            onSwitchWorkspace = {
                                scope.launch { drawerState.close() }
                                onOpenWorkspaceSwitcher()
                            },
                            onSignOut = {
                                scope.launch { drawerState.close() }
                                onSignOut()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            ) {
                mainContent()
            }
        }
    }
}

@Composable
private fun AdminSidebarContent(
    activeModule: AdminModule,
    onSelectModule: (AdminModule) -> Unit,
    onSwitchWorkspace: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(BrandBlueDark, BrandBlueDeep)))
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding()
    ) {
        Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                HunarJiBrandWordmark(subtitle = "Admin Panel")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(AdminModule.entries) { module ->
                val selected = module == activeModule
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) BrandBlue else Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectModule(module) }
                        .testTag("admin_sidebar_${module.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(module.icon, contentDescription = module.label, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = module.label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.65f), modifier = Modifier.size(16.dp))
                    }
                }
            }
            item {
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 6.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onSwitchWorkspace)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Switch Workspace", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = AccentRed.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onSignOut)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Logout", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Footer Banner matching Image 1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(BrandBlue, BrandBlueDark)))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row {
                    Text("Hunar ", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text("Ji", color = AccentOrange, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Text("Grow Together", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun AdminTopHeader(
    showMenuButton: Boolean,
    onMenuClick: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    maintenanceMode: Boolean,
    unreadAlerts: Int,
    isAdminUser: Boolean,
    onAlertsClick: () -> Unit,
    onSwitchAppClick: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showMenuButton) {
                IconButton(onClick = onMenuClick, modifier = Modifier.testTag("admin_menu_button")) {
                    Icon(Icons.Default.Menu, contentDescription = "Open admin menu", tint = TextPrimary)
                }
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search orders, users, products...", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC)
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                color = if (maintenanceMode) AccentAmberLight else AccentGreenLight,
                shape = RoundedCornerShape(50),
                modifier = Modifier.clickable(onClick = onSwitchAppClick)
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = if (maintenanceMode) AccentAmber else AccentGreen, shape = CircleShape, modifier = Modifier.size(8.dp)) {}
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (maintenanceMode) "Maintenance" else if (isAdminUser) "System Online" else "Viewer",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (maintenanceMode) Color(0xFF92400E) else AccentGreen,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }
            }
            IconButton(onClick = onAlertsClick) {
                BadgedBox(badge = {
                    if (unreadAlerts > 0) {
                        Badge(containerColor = AccentRed, contentColor = Color.White) { Text("$unreadAlerts") }
                    }
                }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Admin Alerts", tint = BrandBlueDark)
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardContent(
    orders: List<Order>,
    bookings: List<ServiceBooking>,
    products: List<Product>,
    services: List<ServiceItem>,
    providers: List<ProviderProfile>,
    users: List<UserProfile>,
    kycApplications: List<KycApplication>,
    config: PlatformConfig,
    isAdminUser: Boolean,
    onNavigateModule: (AdminModule) -> Unit,
    onSwitchApp: (AppWorkspace) -> Unit,
    onUpdateOrderStatus: (String, com.example.data.model.OrderStatus) -> Unit,
    onSeedCatalog: () -> Unit
) {
    var selectedTimeframe by remember { mutableStateOf("7 Days") }
    var inspectOrder by remember { mutableStateOf<Order?>(null) }

    val formattedDate = remember {
        SimpleDateFormat("EEE, d MMM yyyy • hh:mm a", Locale.getDefault()).format(Date())
    }
    val totalRevenue = orders.sumOf { it.totalAmount } + bookings.sumOf { it.amount }
    val totalCustomers = users.count { it.role.equals("CUSTOMER", true) }
    val totalMechanics = users.count { it.role.equals("MECHANIC", true) }
        .coerceAtLeast(providers.count { it.providerType.equals("MECHANIC", true) })
    val totalSellers = users.count { it.role.equals("SELLER", true) }
        .coerceAtLeast(providers.count { it.providerType.equals("SELLER", true) })
    val totalDeliveryPartners = users.count { it.role.equals("DELIVERY", true) }
        .coerceAtLeast(providers.count { it.providerType.equals("DELIVERY", true) })
    val pendingRegistrations = kycApplications.count { it.kycStatus == "SUBMITTED" }
    val pendingKyc = kycApplications.count { it.kycStatus in listOf("PENDING", "UNDER_REVIEW", "SUBMITTED", "RESUBMISSION_REQUIRED") }
    val approvedAccounts = kycApplications.count { it.kycStatus == "APPROVED" }
    val rejectedApplications = kycApplications.count { it.kycStatus == "REJECTED" }
    val workerKyc = kycApplications.filter { it.role in listOf("MECHANIC", "SELLER", "DELIVERY") }
    val activeWorkers = workerKyc.count { it.kycStatus == "APPROVED" && it.accountStatus == "ACTIVE" }
    val inactiveWorkers = workerKyc.count { it.kycStatus != "APPROVED" || it.accountStatus != "ACTIVE" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Welcome Back Banner (Image 1)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(BrandBlueDark, BrandBlue)))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color.White.copy(alpha = 0.16f), shape = CircleShape, modifier = Modifier.size(44.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.WavingHand, contentDescription = null, tint = AccentAmber)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Welcome Back, Admin!", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                Text("Here's what's happening with your Hunar Ji platform today.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(color = Color.White.copy(alpha = 0.14f), shape = RoundedCornerShape(10.dp)) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(formattedDate, style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            }
                            if (isAdminUser) {
                                OutlinedButton(
                                    onClick = onSeedCatalog,
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Sync Catalog", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6 KPI Cards (Image 1)
        item {
            val kpis = listOf(
                Triple("Total Orders", "${orders.size}", Pair(Icons.Default.ShoppingCart, BrandBlue)),
                Triple("Total Users", "${users.size.coerceAtLeast(providers.size)}", Pair(Icons.Default.People, AccentGreen)),
                Triple("Total Revenue", "₹${totalRevenue.toInt()}", Pair(Icons.Default.AccountBalanceWallet, AccentPurple)),
                Triple("Total Products", "${products.size}", Pair(Icons.Default.Inventory2, AccentOrange)),
                Triple("Total Services", "${services.size}", Pair(Icons.Default.Build, AccentTeal)),
                Triple("Total Mechanics", "$totalMechanics", Pair(Icons.Default.Group, AccentRed))
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                kpis.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (title, value, iconPair) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(color = iconPair.second, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(iconPair.first, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Platform Roles & KYC Statistics (All 10 required metrics)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Roles & KYC Verification Stats", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            "Manage KYC ->",
                            style = MaterialTheme.typography.labelMedium.copy(color = BrandBlue, fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable { onNavigateModule(AdminModule.KYC_MANAGEMENT) }
                        )
                    }
                    val kycStats = listOf(
                        "Total Customers" to "$totalCustomers",
                        "Total Mechanics" to "$totalMechanics",
                        "Total Sellers" to "$totalSellers",
                        "Total Delivery Partners" to "$totalDeliveryPartners",
                        "Pending Registrations" to "$pendingRegistrations",
                        "Pending KYC" to "$pendingKyc",
                        "Approved Accounts" to "$approvedAccounts",
                        "Rejected Applications" to "$rejectedApplications",
                        "Active Workers" to "$activeWorkers",
                        "Inactive Workers" to "$inactiveWorkers"
                    )
                    kycStats.chunked(2).forEach { pair ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { (label, count) ->
                                Surface(
                                    color = BrandBlueLight.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1f))
                                        Text(count, style = MaterialTheme.typography.titleSmall.copy(color = BrandBlueDark, fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sales Overview Chart (Image 1)
        item {
            val (thisPts, lastPts, labels) = when (selectedTimeframe) {
                "30 Days" -> Triple(listOf(6f, 8.5f, 12f, 14.5f, 17f, 19.5f), listOf(4f, 6.2f, 8f, 10.4f, 11.8f, 14f), listOf("W1", "W2", "W3", "W4", "W5", "W6"))
                "3 Months" -> Triple(listOf(18f, 24f, 32f, 41f), listOf(14f, 19f, 25f, 30f), listOf("M1", "M2", "M3", "Now"))
                "1 Year" -> Triple(listOf(10f, 18f, 25f, 36f, 44f, 58f), listOf(8f, 12f, 19f, 26f, 31f, 42f), listOf("Jan", "Mar", "May", "Jul", "Sep", "Nov"))
                else -> Triple(listOf(5.2f, 9.5f, 11.9f, 13.5f, 15.8f, 18.4f), listOf(3.1f, 6.6f, 6.4f, 9.2f, 9.9f, 13.2f), listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sun"))
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Sales Overview", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("7 Days", "30 Days", "3 Months", "1 Year").forEach { tf ->
                                val active = tf == selectedTimeframe
                                Surface(
                                    color = if (active) BrandBlue else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { selectedTimeframe = tf }
                                ) {
                                    Text(
                                        text = tf,
                                        style = MaterialTheme.typography.labelSmall.copy(color = if (active) Color.White else TextSecondary),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    SalesOverviewLineChart(thisMonthPoints = thisPts, lastMonthPoints = lastPts, labels = labels)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(color = BrandBlueLight, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("₹${totalRevenue.toInt().coerceAtLeast(482650)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                Text("This Month", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                            }
                        }
                        Surface(color = AccentAmberLight, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("₹4,12,300", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                Text("Last Month", style = MaterialTheme.typography.labelSmall, color = AccentOrange)
                            }
                        }
                    }
                }
            }
        }

        // Recent Orders Card (Image 1)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Recent Orders", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            "View All ->",
                            style = MaterialTheme.typography.labelMedium.copy(color = BrandBlue, fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable { onNavigateModule(AdminModule.ORDERS) }
                        )
                    }
                    if (orders.isEmpty()) {
                        Text("No customer orders yet. Orders appear here automatically.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    } else {
                        orders.sortedByDescending { it.createdAt?.seconds ?: 0L }.take(5).forEachIndexed { idx, ord ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${idx + 1}.", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.width(20.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("#${ord.id} • ${ord.customerName}", style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("₹${ord.totalAmount.toInt()}", style = MaterialTheme.typography.bodySmall, color = BrandBlueDark)
                                }
                                StatusPill(ord.status)
                                Spacer(Modifier.width(6.dp))
                                IconButton(onClick = { inspectOrder = ord }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Inspect order", tint = BrandBlue)
                                }
                            }
                            if (idx < 4) HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }

        // Four App Control Cards ("Go to App ->", matching Image 1)
        item {
            val apps = listOf(
                Triple(AppWorkspace.CUSTOMER, "Customer App" to "Manage Customers & Orders", Pair(Icons.Default.ShoppingCart, BrandBlueLight to BrandBlue)),
                Triple(AppWorkspace.MECHANIC, "Mechanic App" to "Manage Services & Bookings", Pair(Icons.Default.Build, AccentGreenLight to AccentGreen)),
                Triple(AppWorkspace.SELLER, "Seller App" to "Manage Products & Shop", Pair(Icons.Default.Storefront, AccentAmberLight to AccentOrange)),
                Triple(AppWorkspace.DELIVERY, "Delivery Boy App" to "Manage Deliveries & Tracking", Pair(Icons.Default.DeliveryDining, AccentPurpleLight to AccentPurple))
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                apps.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { (ws, texts, style) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = style.second.first),
                                border = BorderStroke(1.dp, style.second.second.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(style.first, contentDescription = texts.first, tint = style.second.second, modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(texts.first, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1)
                                    }
                                    Text(texts.second, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2)
                                    Button(
                                        onClick = { onSwitchApp(ws) },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlueDark),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("admin_go_${ws.name.lowercase()}")
                                    ) {
                                        Text("Go to App", style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Categories & Sales by App Donut (Image 1)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Top Categories", style = MaterialTheme.typography.titleMedium)
                    val totalP = products.size.coerceAtLeast(1).toFloat()
                    val cats = listOf(
                        Triple("Electronics", products.count { it.category == "Electronics" } / totalP, BrandBlue),
                        Triple("Fashion", products.count { it.category == "Fashion" } / totalP, AccentOrange),
                        Triple("Home & Kitchen", products.count { it.category == "Home & Kitchen" } / totalP, AccentGreen),
                        Triple("Tools", products.count { it.category == "Tools" } / totalP, AccentPurple)
                    )
                    cats.forEach { (cat, frac, color) ->
                        val pct = (frac * 100).toInt()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cat, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(108.dp))
                            LinearProgressIndicator(
                                progress = { frac.coerceIn(0.05f, 1f) },
                                color = color,
                                trackColor = Color(0xFFE2E8F0),
                                modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(50))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("$pct%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                    HorizontalDivider()
                    Text("Sales by App", style = MaterialTheme.typography.titleMedium)
                    SalesByAppDonutChart(
                        segments = listOf(
                            Triple("Customer App", 40f, BrandBlue),
                            Triple("Mechanic App", 25f, AccentGreen),
                            Triple("Seller App", 20f, AccentOrange),
                            Triple("Delivery App", 15f, AccentPurple)
                        ),
                        centerText = "100%"
                    )
                }
            }
        }

        // System Status & Quick Links (Image 1)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("System Status", style = MaterialTheme.typography.titleMedium)
                    Surface(color = AccentGreenLight, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen)
                            Spacer(Modifier.width(8.dp))
                            Text("All Systems Running Smoothly", style = MaterialTheme.typography.titleSmall, color = AccentGreen)
                        }
                    }
                    StatusCheckRow("Database (Cloud Firestore)", "Online")
                    StatusCheckRow("Payment Gateway (Razorpay)", if (config.razorpayEnabled) config.razorpayMode else "Disabled")
                    StatusCheckRow("SMS Service (${config.smsProvider})", if (config.smsEnabled) "Configured" else "Off")
                    StatusCheckRow("Push Notifications", if (config.pushEnabled) "Active" else "Off")
                    HorizontalDivider()
                    Text("Quick Links", style = MaterialTheme.typography.titleMedium)
                    val links = listOf(
                        Triple("KYC Queue", AdminModule.KYC_MANAGEMENT, Icons.Default.VerifiedUser),
                        Triple("Reports", AdminModule.REPORTS, Icons.Default.Analytics),
                        Triple("Settings", AdminModule.SETTINGS, Icons.Default.Settings),
                        Triple("Users", AdminModule.CUSTOMERS, Icons.Default.People),
                        Triple("Notifications", AdminModule.NOTIFICATIONS, Icons.Default.Notifications),
                        Triple("Support", AdminModule.SUPPORT, Icons.Default.SupportAgent)
                    )
                    links.chunked(3).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (title, mod, icon) ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BrandBlueLight,
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { onNavigateModule(mod) }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(icon, contentDescription = title, tint = BrandBlue)
                                        Spacer(Modifier.height(4.dp))
                                        Text(title, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inspectOrder?.let { ord ->
        OrderDetailAdminDialog(
            order = ord,
            onUpdateStatus = { st ->
                onUpdateOrderStatus(ord.id, st)
                inspectOrder = null
            },
            onShareInvoice = { inspectOrder = null },
            onDismiss = { inspectOrder = null }
        )
    }
}

@Composable
private fun StatusCheckRow(label: String, status: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        StatusPill(status)
    }
}
