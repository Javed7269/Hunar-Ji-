package com.example.ui.screens.seller

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppWorkspace
import com.example.data.model.KycApplication
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.HunarJiTopAppHeader
import com.example.ui.components.KycVerificationStatusCard
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.SalesOverviewLineChart
import com.example.ui.components.StatusPill
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberDark
import com.example.ui.theme.AccentAmberLight
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
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

enum class SellerTab { HOME, PRODUCTS, ORDERS, WALLET, ANALYTICS }

@Composable
fun SellerWorkspace(
    viewModel: HunarJiViewModel,
    onOpenWorkspaceSwitcher: () -> Unit,
    onOpenKycWizard: (String) -> Unit = {},
    onSignOut: () -> Unit
) {
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = (profileState as? UiState.Success)?.data
    val myKyc by viewModel.myKycApplication.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val sellerOrders by viewModel.sellerOrders.collectAsStateWithLifecycle()
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

    var currentTab by remember { mutableStateOf(SellerTab.HOME) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showAddProduct by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    if (currentTab != SellerTab.HOME) {
        BackHandler { currentTab = SellerTab.HOME }
    }

    val myProducts = remember(products, viewModel.currentUserId) {
        val owned = products.filter { it.sellerId == viewModel.currentUserId }
        owned.ifEmpty { products }
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
                    onMenuClick = { currentTab = SellerTab.ANALYTICS },
                    notificationCount = notifications.count { !it.read },
                    onNotificationsClick = {},
                    activeWorkspace = AppWorkspace.SELLER,
                    onSwitchWorkspaceClick = onOpenWorkspaceSwitcher
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.navigationBarsPadding()) {
                val items = listOf(
                    Triple(SellerTab.HOME, "Home", Icons.Default.Home),
                    Triple(SellerTab.PRODUCTS, "Products", Icons.Default.Inventory2),
                    Triple(SellerTab.ORDERS, "Orders", Icons.Default.ReceiptLong),
                    Triple(SellerTab.WALLET, "Wallet", Icons.Default.Wallet),
                    Triple(SellerTab.ANALYTICS, "More", Icons.Default.MoreHoriz)
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
        if (!config.sellerAppEnabled) {
            Box(Modifier.fillMaxSize().padding(innerPadding).padding(24.dp), contentAlignment = Alignment.Center) {
                EmptyStateCard(
                    icon = Icons.Default.Storefront,
                    title = "Seller App Paused by Admin",
                    subtitle = "Admin has temporarily disabled the Seller workspace.",
                    actionLabel = "Switch App",
                    onAction = onOpenWorkspaceSwitcher
                )
            }
        } else {
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when (currentTab) {
                    SellerTab.HOME -> SellerDashboardContent(
                        sellerName = profile?.displayName?.ifBlank { "Seller Partner" } ?: "Seller Partner",
                        shopName = myKyc?.shopName?.ifBlank { profile?.shopName ?: "My HUNHAR JI Store" }
                            ?: profile?.shopName?.ifBlank { "My HUNHAR JI Store" }
                            ?: "My HUNHAR JI Store",
                        hunarUserId = profile?.hunarUserId ?: "",
                        fallbackKycStatus = profile?.kycStatus ?: "NOT_SUBMITTED",
                        kyc = myKyc,
                        orders = sellerOrders,
                        lowStockCount = myProducts.count { it.stock <= 15L },
                        onOpenKycForm = { onOpenKycWizard("SELLER") },
                        onGoProducts = { currentTab = SellerTab.PRODUCTS },
                        onGoOrders = { currentTab = SellerTab.ORDERS },
                        onGoWallet = { currentTab = SellerTab.WALLET },
                        onGoAnalytics = { currentTab = SellerTab.ANALYTICS }
                    )

                    SellerTab.PRODUCTS -> SellerProductsContent(
                        products = myProducts,
                        isKycApproved = isKycApproved,
                        onOpenKyc = { onOpenKycWizard("SELLER") },
                        onAddProduct = { requireKycApproved { showAddProduct = true } },
                        onEditProduct = { prod -> requireKycApproved { editingProduct = prod } },
                        onDeleteProduct = { id -> requireKycApproved { viewModel.deleteProduct(id) } }
                    )

                    SellerTab.ORDERS -> SellerOrdersContent(
                        orders = sellerOrders,
                        onUpdateStatus = { id, st -> requireKycApproved { viewModel.updateOrderStatus(id, st) } }
                    )

                    SellerTab.WALLET -> SellerWalletContent(
                        balance = profile?.walletBalance ?: 12350.0,
                        totalEarnings = sellerOrders.sumOf { it.totalAmount }.coerceAtLeast(24850.0),
                        transactions = walletTx,
                        onWithdraw = { requireKycApproved { showWithdrawDialog = true } }
                    )

                    SellerTab.ANALYTICS -> SellerAnalyticsContent(
                        products = myProducts,
                        orders = sellerOrders,
                        kyc = myKyc,
                        hunarUserId = profile?.hunarUserId ?: "",
                        fallbackKycStatus = profile?.kycStatus ?: "NOT_SUBMITTED",
                        onOpenKycForm = { onOpenKycWizard("SELLER") },
                        onSwitchApp = onOpenWorkspaceSwitcher,
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }

    if (showAddProduct || editingProduct != null) {
        SellerProductEditorDialog(
            existing = editingProduct,
            onSave = { prod ->
                viewModel.saveProduct(prod)
                showAddProduct = false
                editingProduct = null
            },
            onDismiss = {
                showAddProduct = false
                editingProduct = null
            }
        )
    }

    if (showWithdrawDialog) {
        var amount by remember { mutableStateOf("1000") }
        var upi by remember { mutableStateOf("rajustore@upi") }
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Seller Payout Request") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = amount, onValueChange = { amount = it.filter(Char::isDigit) }, label = { Text("Amount (₹)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = upi, onValueChange = { upi = it }, label = { Text("UPI / Bank Details") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) viewModel.requestWithdrawal("SELLER", amt, upi)
                    showWithdrawDialog = false
                }) { Text("Withdraw") }
            },
            dismissButton = { TextButton(onClick = { showWithdrawDialog = false }) { Text("Cancel") } }
        )
    }
    if (showKycGateDialog) {
        AlertDialog(
            onDismissRequest = { showKycGateDialog = false },
            title = { Text("Seller KYC Approval Required") },
            text = {
                Text("Complete your Seller registration & Shop KYC and await Admin approval before publishing products, managing orders, or withdrawing payouts.")
            },
            confirmButton = {
                Button(onClick = {
                    showKycGateDialog = false
                    onOpenKycWizard("SELLER")
                }) { Text("Complete / View KYC") }
            },
            dismissButton = {
                TextButton(onClick = { showKycGateDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SellerDashboardContent(
    sellerName: String,
    shopName: String,
    hunarUserId: String,
    fallbackKycStatus: String,
    kyc: KycApplication?,
    orders: List<Order>,
    lowStockCount: Int,
    onOpenKycForm: () -> Unit,
    onGoProducts: () -> Unit,
    onGoOrders: () -> Unit,
    onGoWallet: () -> Unit,
    onGoAnalytics: () -> Unit
) {
    val totalRevenue = orders.sumOf { it.totalAmount }
    val pendingCount = orders.count { it.status == "PENDING" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            KycVerificationStatusCard(
                kyc = kyc,
                roleLabel = "Seller",
                hunarUserId = hunarUserId,
                fallbackStatus = fallbackKycStatus,
                onOpenKycForm = onOpenKycForm
            )
        }
        // Welcome + Today's Sales Blue Card (matching Image 3)
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Welcome Back!", style = MaterialTheme.typography.bodyMedium, color = BrandBlue)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(sellerName, style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                            }
                            Text("Shop: $shopName", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            if (hunarUserId.isNotBlank()) {
                                Text("Seller ID: $hunarUserId", style = MaterialTheme.typography.labelSmall, color = BrandBlueDark)
                            }
                        }
                        Surface(color = AccentAmberLight, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(56.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(30.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(BrandBlueDark, BrandBlue)), RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Today's Sales", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("₹${totalRevenue.toInt()}", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.ExtraBold))
                                    Spacer(Modifier.width(8.dp))
                                    Text("↑ 12%", style = MaterialTheme.typography.labelMedium, color = Color(0xFF86EFAC))
                                }
                            }
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                    }
                }
            }
        }

        // 2x2 Metric Grid (matching Image 3)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SellerKpiCard("Total Orders", "${orders.size}", BrandBlue, Modifier.weight(1f))
                    SellerKpiCard("Total Revenue", "₹${totalRevenue.toInt()}", AccentGreen, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SellerKpiCard("Low Stock Alerts", "$lowStockCount items", AccentOrange, Modifier.weight(1f))
                    SellerKpiCard("Pending Orders", "$pendingCount", AccentPurple, Modifier.weight(1f))
                }
            }
        }

        // Quick Access
        item {
            Text("Quick Access", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SellerQuickTile("Products", Icons.Default.Inventory2, BrandBlue, onGoProducts, Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                SellerQuickTile("Orders", Icons.Default.ReceiptLong, BrandBlueDark, onGoOrders, Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                SellerQuickTile("Wallet", Icons.Default.Wallet, AccentGreen, onGoWallet, Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                SellerQuickTile("Analytics", Icons.Default.TrendingUp, AccentOrange, onGoAnalytics, Modifier.weight(1f))
            }
        }

        // Amber Growth Banner (matching Image 3)
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(AccentAmber, AccentAmberDark)))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Grow Your Business\nwith HUNHAR JI", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.ExtraBold))
                        Text("Zero-hassle inventory, UPI payouts & nationwide delivery", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                    }
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                }
            }
        }
    }
}

@Composable
private fun SellerKpiCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(color = color, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun SellerQuickTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = tint)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SellerProductsContent(
    products: List<Product>,
    isKycApproved: Boolean,
    onOpenKyc: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit
) {
    var filter by remember { mutableStateOf("All") }
    val filtered = remember(products, filter) {
        when (filter) {
            "Active" -> products.filter { it.active }
            "Inactive" -> products.filter { !it.active }
            else -> products
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!isKycApproved) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Storefront,
                    title = "Seller Verification Pending",
                    subtitle = "Product publishing unlocks after Admin approves your Shop KYC.",
                    actionLabel = "Complete / View KYC",
                    onAction = onOpenKyc
                )
            }
        }
        item {
            Text("My Products", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Active", "Inactive").forEach { tab ->
                    val selected = filter == tab
                    Surface(
                        color = if (selected) BrandBlue else Color.White,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, if (selected) BrandBlue else Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable { filter = tab }
                    ) {
                        Text(
                            tab,
                            style = MaterialTheme.typography.labelMedium.copy(color = if (selected) Color.White else TextPrimary),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { prod ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProductThumbnail(iconKey = prod.iconKey, modifier = Modifier.size(58.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prod.title, style = MaterialTheme.typography.titleSmall)
                        Text("₹${prod.price.toInt()} • Stock: ${prod.stock}", style = MaterialTheme.typography.bodySmall, color = BrandBlueDark)
                        StatusPill(if (prod.active) "ACTIVE" else "OFFLINE")
                    }
                    IconButton(onClick = { onEditProduct(prod) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BrandBlue)
                    }
                    IconButton(onClick = { onDeleteProduct(prod.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed)
                    }
                }
            }
        }

        item {
            Button(
                onClick = onAddProduct,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("seller_add_product_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Add Product")
            }
        }
    }
}

@Composable
private fun SellerOrdersContent(
    orders: List<Order>,
    onUpdateStatus: (String, OrderStatus) -> Unit
) {
    var filter by remember { mutableStateOf("All") }
    val filtered = remember(orders, filter) {
        when (filter) {
            "Pending" -> orders.filter { it.status == "PENDING" }
            "Processing" -> orders.filter { it.status in listOf("CONFIRMED", "PROCESSING") }
            "Shipped" -> orders.filter { it.status in listOf("SHIPPED", "OUT_FOR_DELIVERY", "DELIVERED") }
            else -> orders
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Seller Orders", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Pending", "Processing", "Shipped").forEach { tab ->
                    val sel = filter == tab
                    Surface(
                        color = if (sel) BrandBlue else Color.White,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable { filter = tab }
                    ) {
                        Text(tab, color = if (sel) Color.White else TextPrimary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.ReceiptLong,
                    title = "No Seller Orders",
                    subtitle = "Orders placed for your products appear here for confirmation & shipping."
                )
            }
        } else {
            items(filtered, key = { it.id }) { ord ->
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("#${ord.id}", style = MaterialTheme.typography.titleSmall)
                            StatusPill(ord.status)
                        }
                        Text(ord.itemsSummary, style = MaterialTheme.typography.bodySmall)
                        Text("Customer: ${ord.customerName} • ₹${ord.totalAmount.toInt()}", style = MaterialTheme.typography.labelMedium, color = BrandBlueDark)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (ord.status == "PENDING") {
                                Button(onClick = { onUpdateStatus(ord.id, OrderStatus.CONFIRMED) }, modifier = Modifier.weight(1f)) { Text("Confirm Order") }
                                OutlinedButton(onClick = { onUpdateStatus(ord.id, OrderStatus.CANCELLED) }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                            } else if (ord.status == "CONFIRMED") {
                                Button(onClick = { onUpdateStatus(ord.id, OrderStatus.PROCESSING) }, modifier = Modifier.weight(1f)) { Text("Pack Order") }
                            } else if (ord.status == "PROCESSING") {
                                Button(onClick = { onUpdateStatus(ord.id, OrderStatus.SHIPPED) }, modifier = Modifier.weight(1f)) { Text("Mark Shipped") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerWalletContent(
    balance: Double,
    totalEarnings: Double,
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
                SellerKpiCard("Total Earnings", "₹${totalEarnings.toInt()}", AccentGreen, Modifier.weight(1f))
                SellerKpiCard("Payouts", "UPI / Razorpay / COD", BrandBlue, Modifier.weight(1f))
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
private fun SellerAnalyticsContent(
    products: List<Product>,
    orders: List<Order>,
    kyc: KycApplication?,
    hunarUserId: String,
    fallbackKycStatus: String,
    onOpenKycForm: () -> Unit,
    onSwitchApp: () -> Unit,
    onSignOut: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            KycVerificationStatusCard(
                kyc = kyc,
                roleLabel = "Seller",
                hunarUserId = hunarUserId,
                fallbackStatus = fallbackKycStatus,
                onOpenKycForm = onOpenKycForm
            )
        }
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sales Overview", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))
                    SalesOverviewLineChart(
                        thisMonthPoints = listOf(4f, 8f, 7f, 11f, 14f, 15f, 19f),
                        lastMonthPoints = listOf(3f, 5f, 6f, 8f, 10f, 11f, 13f),
                        labels = listOf("1", "5", "10", "15", "20", "25", "30")
                    )
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Top Products (${products.size}) • Orders (${orders.size})", style = MaterialTheme.typography.titleMedium)
                    products.take(4).forEachIndexed { idx, p ->
                        val ratio = (1f - idx * 0.18f).coerceIn(0.25f, 1f)
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(p.title, style = MaterialTheme.typography.bodySmall)
                                Text("₹${p.price.toInt()}", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                            }
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth().height(6.dp))
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onSwitchApp, modifier = Modifier.weight(1f)) { Text("Switch Role") }
                OutlinedButton(onClick = onSignOut, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, AccentRed)) {
                    Text("Logout", color = AccentRed)
                }
            }
        }
    }
}

@Composable
private fun SellerProductEditorDialog(
    existing: Product?,
    onSave: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "Electronics") }
    var price by remember { mutableStateOf(if (existing != null) existing.price.toInt().toString() else "1499") }
    var originalPrice by remember { mutableStateOf(if (existing != null) existing.originalPrice.toInt().toString() else "2499") }
    var stock by remember { mutableStateOf(if (existing != null) existing.stock.toString() else "20") }
    var description by remember { mutableStateOf(existing?.description ?: "High-quality marketplace product") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add New Product" else "Edit Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it.filter(Char::isDigit) }, label = { Text("Sale Price (₹)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = originalPrice, onValueChange = { originalPrice = it.filter(Char::isDigit) }, label = { Text("MRP (₹)") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = stock, onValueChange = { stock = it.filter(Char::isDigit) }, label = { Text("Stock Quantity") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    val p = price.toDoubleOrNull() ?: 499.0
                    val mrp = (originalPrice.toDoubleOrNull() ?: p).coerceAtLeast(p)
                    onSave(
                        (existing ?: Product()).copy(
                            title = title,
                            category = category,
                            price = p,
                            originalPrice = mrp,
                            stock = stock.toLongOrNull() ?: 10L,
                            description = description
                        )
                    )
                }
            }) { Text("Save Product") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
