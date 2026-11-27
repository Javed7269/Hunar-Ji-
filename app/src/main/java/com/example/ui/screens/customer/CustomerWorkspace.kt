package com.example.ui.screens.customer

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.localization.LocalizationStrings
import com.example.core.localization.LocalizedStrings
import com.example.data.model.AppWorkspace
import com.example.data.model.LiveStream
import com.example.data.model.Product
import com.example.data.model.ServiceItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.HunarJiBrandWordmark
import com.example.ui.components.HunarJiTopAppHeader
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.StatusPill
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberLight
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueDeep
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HunarJiViewModel
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.launch

enum class CustomerTab { HOME, CATEGORIES, DEALS, WALLET, ACCOUNT }

@Composable
fun CustomerWorkspace(
    viewModel: HunarJiViewModel,
    onOpenWorkspaceSwitcher: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onOpenRegistrationWizard: (String) -> Unit = {},
    onSignOut: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lang by viewModel.language.collectAsStateWithLifecycle()
    val strings = remember(lang) { LocalizationStrings.forLanguage(lang) }
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = (profileState as? UiState.Success)?.data
    val products by viewModel.products.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val coupons by viewModel.coupons.collectAsStateWithLifecycle()
    val liveStreams by viewModel.liveStreams.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val wishlistItems by viewModel.wishlistItems.collectAsStateWithLifecycle()
    val orders by viewModel.customerOrders.collectAsStateWithLifecycle()
    val bookings by viewModel.customerBookings.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val walletTx by viewModel.walletTransactions.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val config by viewModel.platformConfig.collectAsStateWithLifecycle()
    val selectedDistrict by viewModel.selectedDistrict.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(CustomerTab.HOME) }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showServicesOnly by remember { mutableStateOf(false) }

    // Modal states
    var detailProduct by remember { mutableStateOf<Product?>(null) }
    var bookingService by remember { mutableStateOf<ServiceItem?>(null) }
    var watchingStream by remember { mutableStateOf<LiveStream?>(null) }
    var showCartSheet by remember { mutableStateOf(false) }
    var showOrdersSheet by remember { mutableStateOf(false) }
    var showBookingsSheet by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showWishlistSheet by remember { mutableStateOf(false) }
    var showSupportSheet by remember { mutableStateOf(false) }
    var showDistrictSheet by remember { mutableStateOf(false) }
    var showAddressesSheet by remember { mutableStateOf(false) }
    var cameraSearchUri by remember { mutableStateOf<Uri?>(null) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            cameraSearchUri = uri
            searchQuery = "Earbuds"
            currentTab = CustomerTab.CATEGORIES
        }
    }

    if (currentTab != CustomerTab.HOME) {
        BackHandler { currentTab = CustomerTab.HOME }
    }

    val unreadNotifCount = notifications.count { !it.read }
    val cartCount = cartItems.sumOf { it.quantity.toInt() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(BrandBlueDeep, BrandBlue)))
                        .padding(20.dp)
                ) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = 2.dp
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            HunarJiBrandWordmark()
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = profile?.displayName?.ifBlank { viewModel.currentUserName } ?: viewModel.currentUserName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "$selectedDistrict • Wallet ₹${(profile?.walletBalance ?: 0.0).toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    val drawerItems = listOf(
                        Triple("Home", Icons.Default.Home) { currentTab = CustomerTab.HOME; showServicesOnly = false },
                        Triple("Categories & Products", Icons.Default.GridView) { currentTab = CustomerTab.CATEGORIES; showServicesOnly = false },
                        Triple("Special Deals", Icons.Default.LocalOffer) { currentTab = CustomerTab.DEALS },
                        Triple("Mistri Ji Services", Icons.Default.Build) { currentTab = CustomerTab.CATEGORIES; showServicesOnly = true },
                        Triple("Live Services", Icons.Default.RadioButtonChecked) { watchingStream = liveStreams.firstOrNull() },
                        Triple("My Orders (${orders.size})", Icons.Default.ShoppingBag) { showOrdersSheet = true },
                        Triple("My Service Bookings (${bookings.size})", Icons.Default.ReceiptLong) { showBookingsSheet = true },
                        Triple("Wishlist (${wishlistItems.size})", Icons.Default.Favorite) { showWishlistSheet = true },
                        Triple("Cart ($cartCount)", Icons.Default.ShoppingCart) { showCartSheet = true },
                        Triple("Wallet & Transactions", Icons.Default.Wallet) { currentTab = CustomerTab.WALLET },
                        Triple("Saved Addresses", Icons.Default.LocationOn) { showAddressesSheet = true },
                        Triple("Location: $selectedDistrict", Icons.Default.LocationOn) { showDistrictSheet = true },
                        Triple("Notifications ($unreadNotifCount)", Icons.Default.Notifications) { showNotificationsSheet = true },
                        Triple("Customer Support", Icons.Default.HeadsetMic) { showSupportSheet = true },
                        Triple("Language (${lang.nativeName})", Icons.Default.Language) { onOpenLanguagePicker() },
                        Triple("Switch App (Mechanic/Seller/Admin)", Icons.Default.Verified) { onOpenWorkspaceSwitcher() }
                    )
                    items(drawerItems) { (title, icon, action) ->
                        NavigationDrawerItem(
                            label = { Text(title, style = MaterialTheme.typography.bodyMedium) },
                            icon = { Icon(icon, contentDescription = title, tint = BrandBlue) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                action()
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        NavigationDrawerItem(
                            label = { Text("Logout", color = AccentRed) },
                            icon = { Icon(Icons.Default.Logout, contentDescription = "Logout", tint = AccentRed) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                onSignOut()
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = SurfaceLight,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    HunarJiTopAppHeader(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        notificationCount = unreadNotifCount,
                        onNotificationsClick = { showNotificationsSheet = true },
                        cartCount = cartCount,
                        onCartClick = { showCartSheet = true },
                        activeWorkspace = AppWorkspace.CUSTOMER,
                        onSwitchWorkspaceClick = onOpenWorkspaceSwitcher
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 6.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    val navItems = listOf(
                        Triple(CustomerTab.HOME, strings.navHome, Pair(Icons.Filled.Home, Icons.Outlined.Home)),
                        Triple(CustomerTab.CATEGORIES, strings.navCategories, Pair(Icons.Filled.GridView, Icons.Outlined.GridView)),
                        Triple(CustomerTab.DEALS, strings.navDeals, Pair(Icons.Filled.LocalOffer, Icons.Outlined.LocalOffer)),
                        Triple(CustomerTab.WALLET, strings.navWallet, Pair(Icons.Filled.Wallet, Icons.Outlined.Wallet)),
                        Triple(CustomerTab.ACCOUNT, strings.navAccount, Pair(Icons.Filled.Person, Icons.Outlined.AccountCircle))
                    )
                    navItems.forEach { (tab, label, icons) ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (selected) icons.first else icons.second,
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandBlue,
                                selectedTextColor = BrandBlue,
                                indicatorColor = BrandBlueLight,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("customer_nav_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            if (config.maintenanceMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        icon = Icons.Default.Build,
                        title = "Scheduled Maintenance",
                        subtitle = "HUNHAR JI Customer App is currently undergoing maintenance configured by Admin.",
                        actionLabel = "Switch Workspace",
                        onAction = onOpenWorkspaceSwitcher
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        CustomerTab.HOME -> CustomerHomeContent(
                            strings = strings,
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            onSearchSubmit = {
                                showServicesOnly = false
                                currentTab = CustomerTab.CATEGORIES
                            },
                            onCameraSearch = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            banners = banners.filter { it.active },
                            products = products.filter { it.active && it.approved },
                            liveStreams = liveStreams.filter { it.live && it.approved },
                            wishlistIds = wishlistItems.map { it.productId }.toSet(),
                            onBannerClick = { cat ->
                                if (cat.equals("Services", true)) {
                                    showServicesOnly = true
                                    currentTab = CustomerTab.CATEGORIES
                                } else {
                                    selectedCategory = cat
                                    showServicesOnly = false
                                    currentTab = CustomerTab.CATEGORIES
                                }
                            },
                            onCategoryShortcut = { cat ->
                                if (cat == "Services") {
                                    showServicesOnly = true
                                    currentTab = CustomerTab.CATEGORIES
                                } else {
                                    selectedCategory = cat
                                    showServicesOnly = false
                                    currentTab = CustomerTab.CATEGORIES
                                }
                            },
                            onSeeAllOffers = { currentTab = CustomerTab.DEALS },
                            onProductClick = { detailProduct = it },
                            onAddToCart = { viewModel.addToCart(it) },
                            onToggleWishlist = { p, saved -> viewModel.toggleWishlist(p, saved) },
                            onWatchLive = { stream ->
                                viewModel.setActiveStream(stream.id)
                                watchingStream = stream
                            },
                            onViewAllLive = {
                                showServicesOnly = true
                                currentTab = CustomerTab.CATEGORIES
                            }
                        )

                        CustomerTab.CATEGORIES -> CustomerBrowseContent(
                            products = products.filter { it.active && it.approved },
                            services = services.filter { it.active && it.approved },
                            providers = providers,
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            selectedCategory = selectedCategory,
                            onSelectCategory = { selectedCategory = it },
                            showServicesOnly = showServicesOnly,
                            onToggleMode = { showServicesOnly = it },
                            cameraSearchActive = cameraSearchUri != null,
                            onClearCameraSearch = { cameraSearchUri = null; searchQuery = "" },
                            wishlistIds = wishlistItems.map { it.productId }.toSet(),
                            onProductClick = { detailProduct = it },
                            onAddToCart = { viewModel.addToCart(it) },
                            onToggleWishlist = { p, saved -> viewModel.toggleWishlist(p, saved) },
                            onBookService = { bookingService = it }
                        )

                        CustomerTab.DEALS -> CustomerDealsContent(
                            products = products.filter { it.active && it.approved && it.discountPercent >= 30 }
                                .ifEmpty { products },
                            coupons = coupons.filter { it.active },
                            wishlistIds = wishlistItems.map { it.productId }.toSet(),
                            onProductClick = { detailProduct = it },
                            onAddToCart = { viewModel.addToCart(it) },
                            onToggleWishlist = { p, saved -> viewModel.toggleWishlist(p, saved) },
                            onCopyCoupon = { code ->
                                showCartSheet = true
                            }
                        )

                        CustomerTab.WALLET -> CustomerWalletContent(
                            balance = profile?.walletBalance ?: 0.0,
                            loyaltyPoints = profile?.loyaltyPoints ?: 0L,
                            referralCode = profile?.referralCode?.ifBlank { "HJ2026" } ?: "HJ2026",
                            transactions = walletTx,
                            onAddMoney = { amt, method -> viewModel.submitWalletTopUpRequest(amt, method, "PENDING_GATEWAY_VERIFICATION") }
                        )

                        CustomerTab.ACCOUNT -> CustomerAccountContent(
                            profile = profile,
                            fallbackName = viewModel.currentUserName,
                            fallbackEmail = viewModel.currentUserEmail,
                            selectedDistrict = selectedDistrict,
                            languageName = lang.nativeName,
                            ordersCount = orders.size,
                            bookingsCount = bookings.size,
                            wishlistCount = wishlistItems.size,
                            addressCount = addresses.size,
                            onOpenOrders = { showOrdersSheet = true },
                            onOpenBookings = { showBookingsSheet = true },
                            onOpenWishlist = { showWishlistSheet = true },
                            onOpenAddresses = { showAddressesSheet = true },
                            onOpenSupport = { showSupportSheet = true },
                            onOpenDistrictPicker = { showDistrictSheet = true },
                            onOpenLanguagePicker = onOpenLanguagePicker,
                            onOpenWorkspaceSwitcher = onOpenWorkspaceSwitcher,
                            onOpenRegistrationWizard = { onOpenRegistrationWizard("CUSTOMER") },
                            onDeleteAccount = {
                                viewModel.deleteOwnAccount(
                                    context = context,
                                    onDeleted = onSignOut
                                )
                            },
                            onSaveProfile = { name, phone, city ->
                                viewModel.updateProfile(
                                    displayName = name,
                                    phone = phone,
                                    city = city,
                                    district = selectedDistrict,
                                    state = profile?.state ?: "Bihar",
                                    online = true
                                )
                            },
                            onSignOut = onSignOut
                        )
                    }
                }
            }
        }
    }

    // Sub-sheets / Dialogs
    detailProduct?.let { prod ->
        ProductDetailDialog(
            product = prod,
            saved = wishlistItems.any { it.productId == prod.id },
            onAddToCart = { qty, variant ->
                repeat(qty.coerceIn(1, 5)) { viewModel.addToCart(prod, variant) }
                detailProduct = null
            },
            onBuyNow = { variant ->
                viewModel.addToCart(prod, variant)
                detailProduct = null
                showCartSheet = true
            },
            onToggleWishlist = {
                val saved = wishlistItems.any { it.productId == prod.id }
                viewModel.toggleWishlist(prod, saved)
            },
            onDismiss = { detailProduct = null }
        )
    }

    bookingService?.let { srv ->
        ServiceBookingDialog(
            service = srv,
            defaultAddress = addresses.firstOrNull()?.fullAddress ?: "Civil Lines, $selectedDistrict",
            onConfirm = { vehicle, problem, date, time, addr ->
                viewModel.bookService(srv, vehicle, problem, date, time, addr) {
                    bookingService = null
                    showBookingsSheet = true
                }
            },
            onDismiss = { bookingService = null }
        )
    }

    watchingStream?.let { stream ->
        val msgs by viewModel.activeStreamMessages.collectAsStateWithLifecycle()
        LiveStreamViewerDialog(
            stream = stream,
            messages = msgs,
            onSendMessage = { viewModel.sendStreamMessage(stream.id, it) },
            onBookMechanic = {
                watchingStream = null
                bookingService = services.firstOrNull()
            },
            onDismiss = {
                viewModel.setActiveStream(null)
                watchingStream = null
            }
        )
    }

    if (showCartSheet) {
        CartAndCheckoutDialog(
            cartItems = cartItems,
            addresses = addresses,
            coupons = coupons.filter { it.active },
            platformConfig = config,
            walletBalance = profile?.walletBalance ?: 0.0,
            defaultDistrict = selectedDistrict,
            onUpdateQty = { id, q -> viewModel.updateCartQuantity(id, q) },
            onSaveAddress = { viewModel.saveAddress(it) },
            onPlaceOrder = { addr, payMethod, code, disc ->
                viewModel.placeOrder(addr, payMethod, code, disc) {
                    showCartSheet = false
                    showOrdersSheet = true
                }
            },
            onDismiss = { showCartSheet = false }
        )
    }

    if (showOrdersSheet) {
        CustomerOrdersDialog(
            orders = orders,
            onCancelOrder = { id ->
                viewModel.updateOrderStatus(id, com.example.data.model.OrderStatus.CANCELLED)
            },
            onDismiss = { showOrdersSheet = false }
        )
    }

    if (showBookingsSheet) {
        CustomerBookingsDialog(
            bookings = bookings,
            onCancelBooking = { id ->
                viewModel.updateBookingStatus(id, com.example.data.model.BookingStatus.CANCELLED)
            },
            onDismiss = { showBookingsSheet = false }
        )
    }

    if (showWishlistSheet) {
        WishlistDialog(
            items = wishlistItems,
            products = products,
            onMoveToCart = { prod -> viewModel.addToCart(prod) },
            onRemove = { prod -> viewModel.toggleWishlist(prod, true) },
            onDismiss = { showWishlistSheet = false }
        )
    }

    if (showNotificationsSheet) {
        NotificationsDialog(
            notifications = notifications,
            onMarkRead = { viewModel.markNotificationRead(it) },
            onDismiss = { showNotificationsSheet = false }
        )
    }

    if (showAddressesSheet) {
        AddressesDialog(
            addresses = addresses,
            defaultDistrict = selectedDistrict,
            onSaveAddress = { viewModel.saveAddress(it) },
            onDeleteAddress = { viewModel.deleteAddress(it) },
            onDismiss = { showAddressesSheet = false }
        )
    }

    if (showDistrictSheet) {
        DistrictPickerDialog(
            locations = locations,
            currentDistrict = selectedDistrict,
            onSelect = { viewModel.setDistrict(it) },
            onDismiss = { showDistrictSheet = false }
        )
    }

    if (showSupportSheet) {
        SupportTicketDialog(
            tickets = viewModel.userTickets.collectAsStateWithLifecycle().value,
            onSubmit = { cat, subj, desc ->
                viewModel.createSupportTicket("CUSTOMER", cat, subj, desc)
            },
            onDismiss = { showSupportSheet = false }
        )
    }
}

@Composable
private fun CustomerHomeContent(
    strings: LocalizedStrings,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onCameraSearch: () -> Unit,
    banners: List<com.example.data.model.Banner>,
    products: List<Product>,
    liveStreams: List<LiveStream>,
    wishlistIds: Set<String>,
    onBannerClick: (String) -> Unit,
    onCategoryShortcut: (String) -> Unit,
    onSeeAllOffers: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleWishlist: (Product, Boolean) -> Unit,
    onWatchLive: (LiveStream) -> Unit,
    onViewAllLive: () -> Unit
) {
    var activeBannerIdx by remember { mutableIntStateOf(0) }
    val displayBanner = banners.getOrNull(activeBannerIdx) ?: banners.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search Bar + Camera Search (matching Image 5)
        item {
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            strings.searchHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        IconButton(onClick = onSearchSubmit) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
                        }
                    },
                    trailingIcon = {
                        Surface(
                            color = BrandBlue,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onCameraSearch)
                                .testTag("camera_search_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Camera Search",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("customer_search_input")
                )
            }
        }

        // Hero Banner Slider (matching Image 5: Best Deals Big Savings)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(176.dp)
                    .clickable { displayBanner?.let { onBannerClick(it.targetCategory) } },
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_banner_deals),
                        contentDescription = "Best Deals Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        BrandBlueDark.copy(alpha = 0.92f),
                                        BrandBlue.copy(alpha = 0.65f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = displayBanner?.title ?: strings.bestDealsTitle,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 25.sp
                                )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = displayBanner?.subtitle ?: strings.bestDealsSub,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { displayBanner?.let { onBannerClick(it.targetCategory) } },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("hero_shop_now_button")
                            ) {
                                Text(
                                    text = displayBanner?.ctaText ?: strings.shopNow,
                                    style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                banners.take(3).forEachIndexed { idx, _ ->
                                    Surface(
                                        shape = CircleShape,
                                        color = if (idx == activeBannerIdx) Color.White else Color.White.copy(alpha = 0.45f),
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clickable { activeBannerIdx = idx }
                                    ) {}
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category Shortcuts Row (Electronics, Fashion, Home & Kitchen, Services, More)
        item {
            val shortcuts = listOf(
                Triple("Electronics", Icons.Default.Devices, Color(0xFFDBEAFE) to BrandBlue),
                Triple("Fashion", Icons.Default.Checkroom, Color(0xFFFFEDD5) to AccentOrange),
                Triple("Home & Kitchen", Icons.Default.Kitchen, Color(0xFFFEF3C7) to Color(0xFF92400E)),
                Triple("Services", Icons.Default.Build, Color(0xFFDBEAFE) to BrandBlueDark),
                Triple("More", Icons.Default.GridView, Color(0xFFE0E7FF) to BrandBlue)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                shortcuts.forEach { (label, icon, colors) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onCategoryShortcut(if (label == "More") "All" else label) }
                            .padding(vertical = 4.dp)
                            .testTag("shortcut_${label.lowercase().replace(" ", "_")}")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.first,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = colors.second,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (label == "Home & Kitchen") "Home &\nKitchen" else label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                lineHeight = 13.sp
                            ),
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // Special Offers Header + Product Strip (matching Image 5)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.specialOffers,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onSeeAllOffers)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.seeAll,
                        style = MaterialTheme.typography.labelLarge,
                        color = BrandBlue
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(products.take(6), key = { it.id }) { product ->
                    SpecialOfferProductCard(
                        product = product,
                        isSaved = wishlistIds.contains(product.id),
                        onClick = { onProductClick(product) },
                        onAddToCart = { onAddToCart(product) },
                        onToggleWishlist = { onToggleWishlist(product, wishlistIds.contains(product.id)) }
                    )
                }
            }
        }

        // Live Services Section (matching Image 5: Car Engine Repair Live)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.liveServices,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onViewAllLive)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.viewAll, style = MaterialTheme.typography.labelLarge, color = BrandBlue)
                    Spacer(Modifier.width(2.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(16.dp))
                }
            }
        }

        item {
            val topStream = liveStreams.firstOrNull()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(166.dp)
                    .clickable { topStream?.let(onWatchLive) },
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_live_mechanic),
                        contentDescription = "Car Engine Repair Live",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF061B3E).copy(alpha = 0.94f),
                                        Color(0xFF0A326E).copy(alpha = 0.72f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = AccentRed,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.RadioButtonChecked, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("LIVE", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            }
                        }
                        Column {
                            Text(
                                text = "Car Engine\nRepair Live",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 22.sp
                                )
                            )
                            Text(
                                text = "Expert Mechanic at Your Service",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.9f))
                            )
                        }
                        OutlinedButton(
                            onClick = { topStream?.let(onWatchLive) },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF0B2C66).copy(alpha = 0.7f),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("watch_live_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.watchLive, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Safe & Secure Shopping + Easy Payments Trust Banner (matching Image 5)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AccentGreenLight,
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(strings.safeShopping, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text("Your Trust Our Priority", style = MaterialTheme.typography.bodySmall, color = BrandBlue)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(42.dp)
                            .background(Color(0xFFE2E8F0))
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wallet, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(strings.easyPayments, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text("Multiple Options", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("UPI", "VISA", "RuPay").forEach { badge ->
                                Surface(
                                    color = BrandBlueLight,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = badge,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = BrandBlueDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
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
fun SpecialOfferProductCard(
    product: Product,
    isSaved: Boolean,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    onToggleWishlist: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(142.dp)
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = AccentRed,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "${product.discountPercent}% OFF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = if (isSaved) AccentRed else TextMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onToggleWishlist)
                )
            }
            Spacer(Modifier.height(8.dp))
            ProductThumbnail(
                iconKey = product.iconKey,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "₹${product.price.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = BrandBlueDark,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "₹${product.originalPrice.toInt()}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        textDecoration = TextDecoration.LineThrough
                    )
                )
            }
            Spacer(Modifier.height(6.dp))
            Surface(
                color = BrandBlueLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAddToCart)
                    .testTag("add_cart_${product.id}")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Add", style = MaterialTheme.typography.labelSmall.copy(color = BrandBlue, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun CustomerBrowseContent(
    products: List<Product>,
    services: List<ServiceItem>,
    providers: List<com.example.data.model.ProviderProfile>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    showServicesOnly: Boolean,
    onToggleMode: (Boolean) -> Unit,
    cameraSearchActive: Boolean,
    onClearCameraSearch: () -> Unit,
    wishlistIds: Set<String>,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleWishlist: (Product, Boolean) -> Unit,
    onBookService: (ServiceItem) -> Unit
) {
    var sortOption by remember { mutableStateOf("Popular") }
    val categories = listOf("All", "Electronics", "Fashion", "Home & Kitchen", "Tools", "Automotive")

    val filteredProducts = remember(products, searchQuery, selectedCategory, sortOption) {
        products.filter { p ->
            (selectedCategory == "All" || p.category.equals(selectedCategory, true)) &&
                (searchQuery.isBlank() || p.title.contains(searchQuery, true) || p.category.contains(searchQuery, true) || p.sellerName.contains(searchQuery, true))
        }.let { list ->
            when (sortOption) {
                "Low to High" -> list.sortedBy { it.price }
                "High to Low" -> list.sortedByDescending { it.price }
                "Best Discount" -> list.sortedByDescending { it.discountPercent }
                else -> list.sortedByDescending { it.rating }
            }
        }
    }

    val filteredServices = remember(services, searchQuery) {
        services.filter { s ->
            searchQuery.isBlank() || s.title.contains(searchQuery, true) || s.category.contains(searchQuery, true) || s.providerName.contains(searchQuery, true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search products, mechanics, services...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (cameraSearchActive) {
            item {
                Surface(
                    color = BrandBlueLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrandBlue)
                        Spacer(Modifier.width(8.dp))
                        Text("Visual search filter active", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = BrandBlueDark)
                        TextButton(onClick = onClearCameraSearch) { Text("Clear") }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onToggleMode(false) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!showServicesOnly) BrandBlue else Color.White,
                        contentColor = if (!showServicesOnly) Color.White else TextPrimary
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Products (${filteredProducts.size})")
                }
                Button(
                    onClick = { onToggleMode(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showServicesOnly) BrandBlue else Color.White,
                        contentColor = if (showServicesOnly) Color.White else TextPrimary
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Mistri Services (${filteredServices.size})")
                }
            }
        }

        if (!showServicesOnly) {
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val active = selectedCategory == cat
                        Surface(
                            color = if (active) BrandBlue else Color.White,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, if (active) BrandBlue else Color(0xFFE2E8F0)),
                            modifier = Modifier.clickable { onSelectCategory(cat) }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (active) Color.White else TextPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Popular", "Low to High", "High to Low", "Best Discount").forEach { sort ->
                        val active = sortOption == sort
                        Surface(
                            color = if (active) AccentAmberLight else Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { sortOption = sort }
                        ) {
                            Text(
                                text = "Sort: $sort",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (active) Color(0xFF92400E) else TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.ShoppingBag,
                        title = "No Matching Products",
                        subtitle = "Try clearing your search or choosing another category.",
                        actionLabel = "Reset Filters",
                        onAction = { onSelectCategory("All"); onSearchChange("") }
                    )
                }
            } else {
                items(filteredProducts, key = { it.id }) { prod ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductClick(prod) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductThumbnail(iconKey = prod.iconKey, modifier = Modifier.size(72.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(color = AccentRedLight, shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            "${prod.discountPercent}% OFF",
                                            style = MaterialTheme.typography.labelSmall.copy(color = AccentRed, fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Default.Star, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                                    Text(" ${prod.rating}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(prod.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(prod.sellerName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("₹${prod.price.toInt()}", style = MaterialTheme.typography.titleMedium.copy(color = BrandBlueDark, fontWeight = FontWeight.Bold))
                                    Spacer(Modifier.width(6.dp))
                                    Text("₹${prod.originalPrice.toInt()}", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, textDecoration = TextDecoration.LineThrough))
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                IconButton(onClick = { onToggleWishlist(prod, wishlistIds.contains(prod.id)) }) {
                                    Icon(
                                        if (wishlistIds.contains(prod.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Wishlist",
                                        tint = if (wishlistIds.contains(prod.id)) AccentRed else TextMuted
                                    )
                                }
                                Button(
                                    onClick = { onAddToCart(prod) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Text("Verified Mechanics Nearby", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(providers.filter { it.providerType == "MECHANIC" }, key = { it.id }) { mech ->
                        Card(
                            modifier = Modifier.width(220.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_worker_avatar),
                                    contentDescription = mech.name,
                                    modifier = Modifier.size(46.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(mech.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                    Text("${mech.rating}★ • ${mech.completedJobs} jobs", style = MaterialTheme.typography.bodySmall, color = BrandBlue)
                                    Text(mech.city, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
            items(filteredServices, key = { it.id }) { srv ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        ProductThumbnail(iconKey = srv.iconKey, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(srv.title, style = MaterialTheme.typography.titleMedium)
                            Text("${srv.providerName} • ${srv.durationMinutes} min", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("₹${srv.price.toInt()}", style = MaterialTheme.typography.titleMedium.copy(color = BrandBlueDark, fontWeight = FontWeight.Bold))
                        }
                        Button(
                            onClick = { onBookService(srv) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("book_service_${srv.id}")
                        ) {
                            Text("Book")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerDealsContent(
    products: List<Product>,
    coupons: List<com.example.data.model.Coupon>,
    wishlistIds: Set<String>,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleWishlist: (Product, Boolean) -> Unit,
    onCopyCoupon: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Active Coupons & Flash Offers", style = MaterialTheme.typography.titleLarge)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(coupons, key = { it.id }) { coup ->
                    Card(
                        modifier = Modifier.width(240.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandBlueLight),
                        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = BrandBlue)
                                Spacer(Modifier.width(6.dp))
                                Text(coup.code, style = MaterialTheme.typography.titleMedium.copy(color = BrandBlueDark, fontWeight = FontWeight.ExtraBold))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(coup.title, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = { onCopyCoupon(coup.code) }, contentPadding = PaddingValues(0.dp)) {
                                Text("Apply in Cart ->")
                            }
                        }
                    }
                }
            }
        }
        item {
            Text("Top Flash Deals", style = MaterialTheme.typography.titleLarge)
        }
        items(products, key = { it.id }) { prod ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProductClick(prod) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProductThumbnail(iconKey = prod.iconKey, modifier = Modifier.size(68.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        StatusPill("${prod.discountPercent}% OFF")
                        Spacer(Modifier.height(4.dp))
                        Text(prod.title, style = MaterialTheme.typography.titleMedium)
                        Text("₹${prod.price.toInt()}  (MRP ₹${prod.originalPrice.toInt()})", style = MaterialTheme.typography.bodyMedium, color = BrandBlueDark)
                    }
                    Column {
                        IconButton(onClick = { onToggleWishlist(prod, wishlistIds.contains(prod.id)) }) {
                            Icon(
                                if (wishlistIds.contains(prod.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (wishlistIds.contains(prod.id)) AccentRed else TextMuted
                            )
                        }
                        Button(onClick = { onAddToCart(prod) }, shape = RoundedCornerShape(10.dp)) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerWalletContent(
    balance: Double,
    loyaltyPoints: Long,
    referralCode: String,
    transactions: List<com.example.data.model.WalletTransaction>,
    onAddMoney: (Double, String) -> Unit
) {
    var topUpAmount by remember { mutableStateOf("500") }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                        Text("Available Wallet Balance", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                        Spacer(Modifier.height(6.dp))
                        Text("₹${balance.toInt()}", style = MaterialTheme.typography.displaySmall.copy(color = Color.White))
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Rewards: $loyaltyPoints pts", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFD54F))
                            Text("Referral: $referralCode", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Money to HUNHAR JI Wallet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("250", "500", "1000", "2000").forEach { amt ->
                            OutlinedButton(
                                onClick = { topUpAmount = amt },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("₹$amt", maxLines = 1)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = topUpAmount,
                        onValueChange = { topUpAmount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Amount (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val amt = topUpAmount.toDoubleOrNull() ?: 0.0
                            if (amt > 0) onAddMoney(amt, "UPI")
                        },
                        modifier = Modifier.fillMaxWidth().testTag("wallet_add_money_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Money via UPI")
                    }
                }
            }
        }
        item {
            Text("Recent Wallet Transactions", style = MaterialTheme.typography.titleMedium)
        }
        if (transactions.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Wallet,
                    title = "No Transactions Yet",
                    subtitle = "Top up your wallet or place an order using HUNHAR JI Wallet to see ledger entries."
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

@Composable
private fun CustomerAccountContent(
    profile: com.example.data.model.UserProfile?,
    fallbackName: String,
    fallbackEmail: String,
    selectedDistrict: String,
    languageName: String,
    ordersCount: Int,
    bookingsCount: Int,
    wishlistCount: Int,
    addressCount: Int,
    onOpenOrders: () -> Unit,
    onOpenBookings: () -> Unit,
    onOpenWishlist: () -> Unit,
    onOpenAddresses: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenDistrictPicker: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onOpenWorkspaceSwitcher: () -> Unit,
    onOpenRegistrationWizard: () -> Unit,
    onDeleteAccount: () -> Unit,
    onSaveProfile: (String, String, String) -> Unit,
    onSignOut: () -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var nameInput by remember(profile) { mutableStateOf(profile?.displayName?.ifBlank { fallbackName } ?: fallbackName) }
    var phoneInput by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var cityInput by remember(profile) { mutableStateOf(profile?.city ?: selectedDistrict) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = BrandBlueLight,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(30.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profile?.displayName?.ifBlank { fallbackName } ?: fallbackName, style = MaterialTheme.typography.titleLarge)
                            Text(fallbackEmail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            if (!profile?.hunarUserId.isNullOrBlank()) {
                                Text("User ID: ${profile?.hunarUserId}", style = MaterialTheme.typography.labelSmall, color = BrandBlueDark)
                            }
                            Text("Referral Code: ${profile?.referralCode?.ifBlank { "HUNAR" } ?: "HUNAR"}", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                        }
                        TextButton(onClick = { editing = !editing }) {
                            Text(if (editing) "Cancel" else "Edit")
                        }
                    }
                    if (editing) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = phoneInput, onValueChange = { phoneInput = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = cityInput, onValueChange = { cityInput = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onSaveProfile(nameInput, phoneInput, cityInput)
                                editing = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Profile")
                        }
                    }
                }
            }
        }

        val rows = listOf(
            Triple("My Orders ($ordersCount)", Icons.Default.ShoppingBag, onOpenOrders),
            Triple("My Service Bookings ($bookingsCount)", Icons.Default.Build, onOpenBookings),
            Triple("My Wishlist ($wishlistCount)", Icons.Default.Favorite, onOpenWishlist),
            Triple("Saved Delivery Addresses ($addressCount)", Icons.Default.LocationOn, onOpenAddresses),
            Triple("District & Location ($selectedDistrict)", Icons.Default.LocationOn, onOpenDistrictPicker),
            Triple("App Language ($languageName)", Icons.Default.Language, onOpenLanguagePicker),
            Triple("Role Registration / Worker KYC", Icons.Default.Shield, onOpenRegistrationWizard),
            Triple("Customer Support & Tickets", Icons.Default.HeadsetMic, onOpenSupport),
            Triple("Switch Role (Mechanic / Seller / Delivery / Admin)", Icons.Default.Verified, onOpenWorkspaceSwitcher)
        )
        items(rows) { (title, icon, action) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = action),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = title, tint = BrandBlue)
                    Spacer(Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("logout_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AccentRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Sign Out")
                }
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Delete Account", color = AccentRed)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account & Data?") },
            text = { Text("This permanently deletes your profile and any stored KYC records. Active orders cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) { Text("Delete Permanently") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
