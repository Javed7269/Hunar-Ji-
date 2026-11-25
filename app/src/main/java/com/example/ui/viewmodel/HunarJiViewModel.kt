package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.localization.AppLanguage
import com.example.data.firebase.AdminClaimVerificationResult
import com.example.data.firebase.HunarJiRepository
import com.example.data.firebase.verifyAdminCustomClaim
import com.example.data.model.AdEventLog
import com.example.data.model.AdMobConfig
import com.example.data.model.Address
import com.example.data.model.ApiIntegrationConfig
import com.example.data.model.AppNotification
import com.example.data.model.AppWorkspace
import com.example.data.model.AuditLog
import com.example.data.model.Banner
import com.example.data.model.BookingStatus
import com.example.data.model.CartItem
import com.example.data.model.CategoryItem
import com.example.data.model.Coupon
import com.example.data.model.KycApplication
import com.example.data.model.KycDocumentRecord
import com.example.data.model.LiveStream
import com.example.data.model.MarketplaceDomainRules
import com.example.data.model.MarketplaceReview
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PlatformConfig
import com.example.data.model.Product
import com.example.data.model.ProviderProfile
import com.example.data.model.ReturnRequest
import com.example.data.model.RewardTransaction
import com.example.data.model.ServiceBooking
import com.example.data.model.ServiceItem
import com.example.data.model.ServiceLocation
import com.example.data.model.StarterCatalog
import com.example.data.model.StreamMessage
import com.example.data.model.SupportTicket
import com.example.data.model.TranslationOverride
import com.example.data.model.UserProfile
import com.example.data.model.VerificationHistoryEntry
import com.example.data.model.VideoContentItem
import com.example.data.model.WalletTopUpRequest
import com.example.data.model.WalletTransaction
import com.example.data.model.WishlistItem
import com.example.data.model.WithdrawalRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AdminClaimState {
    data object Idle : AdminClaimState
    data object Checking : AdminClaimState
    data class Verified(val uid: String, val email: String) : AdminClaimState
    data class Denied(val reason: String, val email: String = "") : AdminClaimState
}

class HunarJiViewModel(
    private val repository: HunarJiRepository,
    val currentUserId: String,
    val currentUserEmail: String,
    val currentUserName: String
) : ViewModel() {

    private val _adminClaimState = MutableStateFlow<AdminClaimState>(AdminClaimState.Idle)
    val adminClaimState: StateFlow<AdminClaimState> = _adminClaimState.asStateFlow()

    val isAdminVerified: StateFlow<Boolean> = _adminClaimState
        .map { it is AdminClaimState.Verified }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isAdminUser: Boolean
        get() = _adminClaimState.value is AdminClaimState.Verified

    private val _activeWorkspace = MutableStateFlow(AppWorkspace.CUSTOMER)
    val activeWorkspace: StateFlow<AppWorkspace> = _activeWorkspace.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _selectedDistrict = MutableStateFlow("Patna")
    val selectedDistrict: StateFlow<String> = _selectedDistrict.asStateFlow()

    private val _activeStreamId = MutableStateFlow<String?>(null)
    val activeStreamId: StateFlow<String?> = _activeStreamId.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val _inspectedKycUid = MutableStateFlow<String?>(null)
    private var hasAutoSyncedRole = false

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(currentUserId, currentUserName, currentUserEmail)
                .onFailure { emitEvent(it.localizedMessage ?: "Profile sync warning") }
        }
        viewModelScope.launch {
            verifyAdminClaimSilently()
        }
        viewModelScope.launch {
            repository.observeUserProfile(currentUserId).collect { prof ->
                if (!hasAutoSyncedRole && prof != null && prof.hunarUserId.isNotBlank()) {
                    hasAutoSyncedRole = true
                    if (_activeWorkspace.value != AppWorkspace.ADMIN) {
                        syncWorkspaceToRegisteredRole(prof.role)
                    }
                }
            }
        }
    }

    private suspend fun verifyAdminClaimSilently() {
        val result = verifyAdminCustomClaim(Firebase.auth.currentUser, forceRefresh = true)
        _adminClaimState.value = when (result) {
            is AdminClaimVerificationResult.Verified -> AdminClaimState.Verified(result.uid, result.email)
            is AdminClaimVerificationResult.Denied -> AdminClaimState.Denied(result.reason, result.email)
        }
    }

    fun verifyAdminClaimAndRedirect(
        forceRefresh: Boolean = true,
        onVerified: () -> Unit = {},
        onDenied: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _adminClaimState.value = AdminClaimState.Checking
            when (val result = verifyAdminCustomClaim(Firebase.auth.currentUser, forceRefresh = forceRefresh)) {
                is AdminClaimVerificationResult.Verified -> {
                    _adminClaimState.value = AdminClaimState.Verified(result.uid, result.email)
                    _activeWorkspace.value = AppWorkspace.ADMIN
                    emitEvent("Admin custom claim ('is_admin') verified")
                    onVerified()
                }
                is AdminClaimVerificationResult.Denied -> {
                    _adminClaimState.value = AdminClaimState.Denied(result.reason, result.email)
                    emitEvent(result.reason)
                    onDenied(result.reason)
                }
            }
        }
    }

    fun switchWorkspace(workspace: AppWorkspace) {
        val profileRole = (userProfile.value as? UiState.Success)?.data?.role ?: "CUSTOMER"
        if (!MarketplaceDomainRules.canSwitchWorkspace(workspace, profileRole, isAdminUser)) {
            emitEvent("Role isolation: register as that role or sign in as Admin to switch workspaces.")
            return
        }
        if (workspace == AppWorkspace.ADMIN && _adminClaimState.value !is AdminClaimState.Verified) {
            _activeWorkspace.value = AppWorkspace.ADMIN
            verifyAdminClaimAndRedirect(forceRefresh = true)
            return
        }
        _activeWorkspace.value = workspace
    }

    fun syncWorkspaceToRegisteredRole(role: String) {
        val target = when (role.uppercase()) {
            "MECHANIC" -> AppWorkspace.MECHANIC
            "SELLER" -> AppWorkspace.SELLER
            "DELIVERY" -> AppWorkspace.DELIVERY
            "ADMIN" -> if (_adminClaimState.value is AdminClaimState.Verified) AppWorkspace.ADMIN else AppWorkspace.CUSTOMER
            else -> AppWorkspace.CUSTOMER
        }
        _activeWorkspace.value = target
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        emitEvent("Language switched to ${lang.nativeName}")
    }

    fun setDistrict(district: String) {
        _selectedDistrict.value = district
    }

    fun setActiveStream(streamId: String?) {
        _activeStreamId.value = streamId
    }

    private fun emitEvent(msg: String) {
        _events.tryEmit(msg)
    }

    private fun <T> Flow<T>.toSafeState(
        tag: String,
        fallback: T
    ): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Error in $tag", root)
                emit(UiState.Success(fallback))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    private fun <T> Flow<List<T>>.toCatalogState(
        tag: String,
        starterFallback: List<T>
    ): StateFlow<List<T>> =
        this.map { items -> items.ifEmpty { starterFallback } }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Catalog fallback in $tag", firestoreError ?: error)
                emit(starterFallback)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), starterFallback)

    private fun <T> Flow<List<T>>.toListState(tag: String): StateFlow<List<T>> =
        this.catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Stream warning in $tag", firestoreError ?: error)
            emit(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userProfile: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId)
            .toSafeState("userProfile", null)

    val cartItems: StateFlow<List<CartItem>> =
        repository.observeCart(currentUserId).toListState("cart")

    val wishlistItems: StateFlow<List<WishlistItem>> =
        repository.observeWishlist(currentUserId).toListState("wishlist")

    val addresses: StateFlow<List<Address>> =
        repository.observeAddresses(currentUserId).toListState("addresses")

    val walletTransactions: StateFlow<List<WalletTransaction>> =
        repository.observeWalletTransactions(currentUserId).toListState("walletTx")

    val notifications: StateFlow<List<AppNotification>> =
        repository.observeNotifications(currentUserId).toListState("notifications")

    val products: StateFlow<List<Product>> =
        repository.observeProducts().toCatalogState("products", StarterCatalog.defaultProducts)

    val services: StateFlow<List<ServiceItem>> =
        repository.observeServices().toCatalogState("services", StarterCatalog.defaultServices)

    val providers: StateFlow<List<ProviderProfile>> =
        repository.observeProviders().toCatalogState("providers", StarterCatalog.defaultProviders)

    val liveStreams: StateFlow<List<LiveStream>> =
        repository.observeLiveStreams().toCatalogState("liveStreams", StarterCatalog.defaultLiveStreams)

    val banners: StateFlow<List<Banner>> =
        repository.observeBanners().toCatalogState("banners", StarterCatalog.defaultBanners)

    val coupons: StateFlow<List<Coupon>> =
        repository.observeCoupons().toCatalogState("coupons", StarterCatalog.defaultCoupons)

    val locations: StateFlow<List<ServiceLocation>> =
        repository.observeLocations().toCatalogState("locations", StarterCatalog.defaultLocations)

    val platformConfig: StateFlow<PlatformConfig> =
        repository.observePlatformConfig()
            .catch { error ->
                val fe = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (fe == null && error is CancellationException) throw error
                emit(PlatformConfig())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), PlatformConfig())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeStreamMessages: StateFlow<List<StreamMessage>> =
        _activeStreamId.flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyList())
            else repository.observeStreamMessages(id)
        }.toListState("streamMessages")

    val customerOrders: StateFlow<List<Order>> =
        repository.observeCustomerOrders(currentUserId).toListState("customerOrders")

    val sellerOrders: StateFlow<List<Order>> =
        repository.observeSellerOrders(currentUserId).toListState("sellerOrders")

    val availableDeliveries: StateFlow<List<Order>> =
        repository.observeAvailableDeliveries().toListState("availableDeliveries")

    val myDeliveries: StateFlow<List<Order>> =
        repository.observeMyDeliveries(currentUserId).toListState("myDeliveries")

    @OptIn(ExperimentalCoroutinesApi::class)
    val allOrdersAdmin: StateFlow<List<Order>> =
        isAdminVerified.flatMapLatest { verified ->
            if (verified) repository.observeAllOrdersAdmin()
            else repository.observeCustomerOrders(currentUserId)
        }.toListState("adminOrders")

    val customerBookings: StateFlow<List<ServiceBooking>> =
        repository.observeCustomerBookings(currentUserId).toListState("customerBookings")

    val pendingBookings: StateFlow<List<ServiceBooking>> =
        repository.observePendingBookings().toListState("pendingBookings")

    val mechanicBookings: StateFlow<List<ServiceBooking>> =
        repository.observeMechanicBookings(currentUserId).toListState("mechanicBookings")

    @OptIn(ExperimentalCoroutinesApi::class)
    val allBookingsAdmin: StateFlow<List<ServiceBooking>> =
        isAdminVerified.flatMapLatest { verified ->
            if (verified) repository.observeAllBookingsAdmin()
            else repository.observeCustomerBookings(currentUserId)
        }.toListState("adminBookings")

    val userWithdrawals: StateFlow<List<WithdrawalRequest>> =
        repository.observeUserWithdrawals(currentUserId).toListState("userWithdrawals")

    @OptIn(ExperimentalCoroutinesApi::class)
    val allWithdrawalsAdmin: StateFlow<List<WithdrawalRequest>> =
        isAdminVerified.flatMapLatest { verified ->
            if (verified) repository.observeAllWithdrawalsAdmin()
            else repository.observeUserWithdrawals(currentUserId)
        }.toListState("adminWithdrawals")

    val userTickets: StateFlow<List<SupportTicket>> =
        repository.observeUserTickets(currentUserId).toListState("userTickets")

    @OptIn(ExperimentalCoroutinesApi::class)
    val allTicketsAdmin: StateFlow<List<SupportTicket>> =
        isAdminVerified.flatMapLatest { verified ->
            if (verified) repository.observeAllTicketsAdmin()
            else repository.observeUserTickets(currentUserId)
        }.toListState("adminTickets")

    @OptIn(ExperimentalCoroutinesApi::class)
    val allUsersAdmin: StateFlow<List<UserProfile>> =
        isAdminVerified.flatMapLatest { verified ->
            if (verified) repository.observeAllUsersAdmin()
            else flowOf(emptyList())
        }.toListState("adminUsers")

    @OptIn(ExperimentalCoroutinesApi::class)
    val auditLogsAdmin: StateFlow<List<AuditLog>> =
        isAdminVerified.flatMapLatest { verified ->
            if (verified) repository.observeAuditLogsAdmin()
            else flowOf(emptyList())
        }.toListState("auditLogs")

    val myKycApplication: StateFlow<KycApplication?> =
        repository.observeMyKycApplication(currentUserId)
            .catch { error ->
                val fe = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (fe == null && error is CancellationException) throw error
                emit(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val myKycDocuments: StateFlow<List<KycDocumentRecord>> =
        repository.observeMyKycDocuments(currentUserId).toListState("myKycDocs")

    val myVerificationHistory: StateFlow<List<VerificationHistoryEntry>> =
        repository.observeMyVerificationHistory(currentUserId).toListState("myKycHistory")

    @OptIn(ExperimentalCoroutinesApi::class)
    val allKycAdmin: StateFlow<List<KycApplication>> =
        isAdminVerified.flatMapLatest { verified ->
            if (verified) repository.observeAllKycApplicationsAdmin()
            else flowOf(emptyList())
        }.toListState("adminKyc")

    @OptIn(ExperimentalCoroutinesApi::class)
    val inspectedKycDocuments: StateFlow<List<KycDocumentRecord>> =
        kotlinx.coroutines.flow.combine(isAdminVerified, _inspectedKycUid) { verified, uid ->
            verified to uid
        }.flatMapLatest { (verified, uid) ->
            if (!verified || uid.isNullOrBlank()) flowOf(emptyList())
            else repository.observeKycDocuments(uid)
        }.toListState("inspectedKycDocs")

    @OptIn(ExperimentalCoroutinesApi::class)
    val inspectedKycHistory: StateFlow<List<VerificationHistoryEntry>> =
        kotlinx.coroutines.flow.combine(isAdminVerified, _inspectedKycUid) { verified, uid ->
            verified to uid
        }.flatMapLatest { (verified, uid) ->
            if (!verified || uid.isNullOrBlank()) flowOf(emptyList())
            else repository.observeVerificationHistory(uid)
        }.toListState("inspectedKycHistory")

    val categories: StateFlow<List<CategoryItem>> =
        repository.observeCategories().toCatalogState("categories", StarterCatalog.defaultCategories)

    val videos: StateFlow<List<VideoContentItem>> =
        repository.observeVideos().toCatalogState("videos", StarterCatalog.defaultVideos)

    val adMobConfig: StateFlow<AdMobConfig> =
        repository.observeAdMobConfig()
            .catch { error ->
                val fe = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (fe == null && error is CancellationException) throw error
                emit(AdMobConfig())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AdMobConfig())

    val rewardTransactions: StateFlow<List<RewardTransaction>> =
        repository.observeRewardTransactions(currentUserId).toListState("rewardTx")

    val recentlyViewedIds: StateFlow<List<String>> =
        repository.observeRecentlyViewed(currentUserId)
            .map { list -> list.sortedByDescending { it.viewedAt?.seconds ?: 0L }.map { it.productId } }
            .toListState("recentlyViewed")

    val reviews: StateFlow<List<MarketplaceReview>> =
        repository.observeReviews().toListState("reviews")

    val walletTopUps: StateFlow<List<WalletTopUpRequest>> =
        repository.observeUserWalletTopUps(currentUserId).toListState("walletTopUps")

    @OptIn(ExperimentalCoroutinesApi::class)
    val allWalletTopUpsAdmin: StateFlow<List<WalletTopUpRequest>> =
        isAdminVerified.flatMapLatest { verified ->
            if (verified) repository.observeAllWalletTopUpsAdmin()
            else repository.observeUserWalletTopUps(currentUserId)
        }.toListState("adminWalletTopUps")

    val userReturnRequests: StateFlow<List<ReturnRequest>> =
        repository.observeUserReturns(currentUserId).toListState("userReturns")

    val sellerReturnRequests: StateFlow<List<ReturnRequest>> =
        repository.observeSellerReturns(currentUserId).toListState("sellerReturns")

    @OptIn(ExperimentalCoroutinesApi::class)
    val allReturnRequestsAdmin: StateFlow<List<ReturnRequest>> =
        isAdminVerified.flatMapLatest { verified ->
            if (verified) repository.observeAllReturnsAdmin()
            else repository.observeUserReturns(currentUserId)
        }.toListState("adminReturns")

    @OptIn(ExperimentalCoroutinesApi::class)
    val apiIntegrationsAdmin: StateFlow<List<ApiIntegrationConfig>> =
        isAdminVerified.flatMapLatest { verified ->
            if (verified) repository.observeApiIntegrationsAdmin().map { it.ifEmpty { StarterCatalog.defaultApiIntegrations } }
            else flowOf(emptyList())
        }.toListState("apiIntegrations")

    @OptIn(ExperimentalCoroutinesApi::class)
    val adEventsAdmin: StateFlow<List<AdEventLog>> =
        isAdminVerified.flatMapLatest { verified ->
            if (verified) repository.observeAdEventsAdmin()
            else flowOf(emptyList())
        }.toListState("adEvents")

    val translationOverrides: StateFlow<List<TranslationOverride>> =
        repository.observeTranslationOverrides().toListState("translationOverrides")

    // --- ACTIONS ---

    fun addToCart(product: Product, variant: String = "Standard") {
        viewModelScope.launch {
            repository.addToCart(product, variant)
                .onSuccess { emitEvent("${product.title} added to cart") }
                .onFailure { emitEvent(it.localizedMessage ?: "Failed to add to cart") }
        }
    }

    fun updateCartQuantity(cartItemId: String, quantity: Long) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, quantity)
                .onFailure { emitEvent(it.localizedMessage ?: "Could not update quantity") }
        }
    }

    fun toggleWishlist(product: Product, currentlySaved: Boolean) {
        viewModelScope.launch {
            repository.toggleWishlist(product, currentlySaved)
                .onSuccess {
                    emitEvent(if (currentlySaved) "Removed from wishlist" else "Saved to wishlist")
                }
                .onFailure { emitEvent(it.localizedMessage ?: "Wishlist update failed") }
        }
    }

    fun saveAddress(address: Address) {
        viewModelScope.launch {
            repository.saveAddress(address)
                .onSuccess { emitEvent("Address saved") }
                .onFailure { emitEvent(it.localizedMessage ?: "Failed to save address") }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            repository.deleteAddress(addressId)
                .onSuccess { emitEvent("Address removed") }
                .onFailure { emitEvent(it.localizedMessage ?: "Failed to remove address") }
        }
    }

    fun placeOrder(
        dropAddress: String,
        paymentMethod: PaymentMethod,
        couponCode: String,
        discountAmount: Double,
        onOrderPlaced: (String) -> Unit
    ) {
        val currentCart = cartItems.value
        val config = platformConfig.value
        val subtotal = currentCart.sumOf { it.price * it.quantity }
        val deliveryFee = if (subtotal >= 999.0) 0.0 else 40.0
        val taxAmount = ((subtotal - discountAmount).coerceAtLeast(0.0) * (config.gstPercent / 100.0))
        val profile = (userProfile.value as? UiState.Success)?.data
        val custName = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        val walletBal = profile?.walletBalance ?: 0.0
        viewModelScope.launch {
            repository.placeOrder(
                cartItems = currentCart,
                customerName = custName,
                dropAddress = dropAddress,
                district = _selectedDistrict.value,
                paymentMethod = paymentMethod,
                couponCode = couponCode,
                discountAmount = discountAmount,
                deliveryFee = deliveryFee,
                taxAmount = taxAmount,
                walletBalance = walletBal
            ).onSuccess { id ->
                emitEvent("Order #$id placed successfully!")
                onOrderPlaced(id)
            }.onFailure {
                emitEvent(it.localizedMessage ?: "Order failed")
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
                .onSuccess { emitEvent("Order #$orderId -> ${status.label}") }
                .onFailure { emitEvent(it.localizedMessage ?: "Could not update order") }
        }
    }

    fun acceptOrUpdateDelivery(orderId: String, status: OrderStatus, markPaid: Boolean = false) {
        val profile = (userProfile.value as? UiState.Success)?.data
        val riderName = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        viewModelScope.launch {
            repository.acceptOrUpdateDeliveryOrder(orderId, riderName, status, markPaid)
                .onSuccess { emitEvent("Delivery #$orderId -> ${status.label}") }
                .onFailure { emitEvent(it.localizedMessage ?: "Delivery update failed") }
        }
    }

    fun bookService(
        service: ServiceItem,
        vehicleInfo: String,
        problemDescription: String,
        date: String,
        time: String,
        address: String,
        onBooked: (String) -> Unit
    ) {
        val profile = (userProfile.value as? UiState.Success)?.data
        val custName = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        viewModelScope.launch {
            repository.createServiceBooking(
                service = service,
                customerName = custName,
                vehicleInfo = vehicleInfo,
                problemDescription = problemDescription,
                scheduledDate = date,
                scheduledTime = time,
                serviceAddress = address,
                district = _selectedDistrict.value
            ).onSuccess { id ->
                emitEvent("Service booked #$id")
                onBooked(id)
            }.onFailure {
                emitEvent(it.localizedMessage ?: "Booking failed")
            }
        }
    }

    fun updateBookingStatus(bookingId: String, status: BookingStatus) {
        val profile = (userProfile.value as? UiState.Success)?.data
        val mechName = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, mechName, status)
                .onSuccess { emitEvent("Booking #$bookingId -> ${status.label}") }
                .onFailure { emitEvent(it.localizedMessage ?: "Could not update booking") }
        }
    }

    fun saveProduct(product: Product) {
        val profile = (userProfile.value as? UiState.Success)?.data
        val sellerName = profile?.shopName?.ifBlank { profile.displayName } ?: currentUserName
        viewModelScope.launch {
            repository.saveProduct(product, sellerName)
                .onSuccess { emitEvent("Product saved to catalog") }
                .onFailure { emitEvent(it.localizedMessage ?: "Could not save product") }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
                .onSuccess { emitEvent("Product deleted") }
                .onFailure { emitEvent(it.localizedMessage ?: "Delete failed") }
        }
    }

    fun saveServiceItem(service: ServiceItem) {
        val profile = (userProfile.value as? UiState.Success)?.data
        val providerName = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        viewModelScope.launch {
            repository.saveServiceItem(service, providerName)
                .onSuccess { emitEvent("Service saved") }
                .onFailure { emitEvent(it.localizedMessage ?: "Could not save service") }
        }
    }

    fun submitWalletTopUpRequest(
        amount: Double,
        method: String,
        paymentReference: String,
        onSubmitted: () -> Unit = {}
    ) {
        val profile = (userProfile.value as? UiState.Success)?.data
        val userName = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        viewModelScope.launch {
            repository.submitWalletTopUpRequest(userName, amount, method, paymentReference)
                .onSuccess { id ->
                    emitEvent("Top-up request #$id submitted for Admin verification")
                    onSubmitted()
                }
                .onFailure { emitEvent(it.localizedMessage ?: "Top-up submission failed") }
        }
    }

    fun adminReviewWalletTopUp(
        topUpId: String,
        targetUid: String,
        amount: Double,
        approve: Boolean,
        adminNote: String
    ) {
        viewModelScope.launch {
            repository.adminReviewWalletTopUp(topUpId, targetUid, amount, approve, adminNote)
                .onSuccess { emitEvent(if (approve) "Top-up verified & wallet credited" else "Top-up rejected") }
                .onFailure { emitEvent("Admin verification failed: ${it.localizedMessage}") }
        }
    }

    fun recordRewardedAdEarned(
        adUnitId: String,
        rewardToken: String,
        onCredited: (Long) -> Unit = {}
    ) {
        val cfg = adMobConfig.value
        if (!cfg.adsEnabled || !cfg.rewardedEnabled) {
            emitEvent("Rewarded ads are paused by Admin")
            return
        }
        viewModelScope.launch {
            repository.recordRewardedAdEarned(
                adUnitId = adUnitId,
                points = cfg.rewardPointsPerAd,
                rewardToken = rewardToken,
                dailyCap = cfg.dailyRewardCap
            ).onSuccess { pts ->
                emitEvent("+$pts Reward Points credited!")
                onCredited(pts)
            }.onFailure {
                emitEvent(it.localizedMessage ?: "Could not credit reward points")
            }
        }
    }

    fun logAdEvent(
        eventType: String,
        adFormat: String,
        adUnitId: String,
        rewardPoints: Long = 0L,
        rewardToken: String = ""
    ) {
        viewModelScope.launch {
            repository.logAdEvent(eventType, adFormat, adUnitId, rewardPoints, rewardToken)
        }
    }

    fun requestWithdrawal(role: String, amount: Double, payoutMethod: String) {
        val profile = (userProfile.value as? UiState.Success)?.data
        val bal = profile?.walletBalance ?: 0.0
        val name = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        viewModelScope.launch {
            repository.requestWithdrawal(name, role, amount, payoutMethod, bal)
                .onSuccess { emitEvent("Withdrawal request submitted") }
                .onFailure { emitEvent(it.localizedMessage ?: "Withdrawal request failed") }
        }
    }

    fun updateProfile(
        displayName: String,
        phone: String,
        city: String,
        district: String,
        state: String,
        online: Boolean,
        shopName: String = "",
        vehicleInfo: String = "",
        skills: String = ""
    ) {
        viewModelScope.launch {
            repository.updateUserProfile(
                displayName, phone, city, district, state, online, shopName, vehicleInfo, skills
            ).onSuccess { emitEvent("Profile updated") }
                .onFailure { emitEvent(it.localizedMessage ?: "Profile update failed") }
        }
    }

    fun toggleOnlineStatus(online: Boolean) {
        val profile = (userProfile.value as? UiState.Success)?.data ?: return
        updateProfile(
            displayName = profile.displayName,
            phone = profile.phone,
            city = profile.city,
            district = profile.district,
            state = profile.state,
            online = online,
            shopName = profile.shopName,
            vehicleInfo = profile.vehicleInfo,
            skills = profile.skills
        )
    }

    fun startLiveStream(title: String, category: String, onStarted: (String) -> Unit) {
        val profile = (userProfile.value as? UiState.Success)?.data
        val hostName = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        viewModelScope.launch {
            repository.startLiveStream(hostName, title, category)
                .onSuccess { id ->
                    _activeStreamId.value = id
                    emitEvent("You are LIVE now!")
                    onStarted(id)
                }
                .onFailure { emitEvent(it.localizedMessage ?: "Failed to go live") }
        }
    }

    fun stopLiveStream(streamId: String) {
        viewModelScope.launch {
            repository.stopLiveStream(streamId)
                .onSuccess {
                    if (_activeStreamId.value == streamId) _activeStreamId.value = null
                    emitEvent("Live stream ended")
                }
                .onFailure { emitEvent(it.localizedMessage ?: "Failed to end stream") }
        }
    }

    fun sendStreamMessage(streamId: String, text: String) {
        val profile = (userProfile.value as? UiState.Success)?.data
        val name = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        viewModelScope.launch {
            repository.sendStreamMessage(streamId, name, text)
        }
    }

    fun createSupportTicket(role: String, category: String, subject: String, description: String) {
        val profile = (userProfile.value as? UiState.Success)?.data
        val name = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        viewModelScope.launch {
            repository.createSupportTicket(name, role, category, subject, description)
                .onSuccess { emitEvent("Support ticket submitted") }
                .onFailure { emitEvent(it.localizedMessage ?: "Ticket creation failed") }
        }
    }

    fun markNotificationRead(notifId: String) {
        viewModelScope.launch { repository.markNotificationRead(notifId) }
    }

    // --- ADMIN ACTIONS ---

    fun savePlatformConfig(config: PlatformConfig) {
        viewModelScope.launch {
            repository.savePlatformConfig(config)
                .onSuccess { emitEvent("Platform settings saved") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun saveBanner(banner: Banner) {
        viewModelScope.launch {
            repository.saveBanner(banner)
                .onSuccess { emitEvent("Banner saved") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun saveCoupon(coupon: Coupon) {
        viewModelScope.launch {
            repository.saveCoupon(coupon)
                .onSuccess { emitEvent("Coupon saved") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun saveLocation(location: ServiceLocation) {
        viewModelScope.launch {
            repository.saveLocation(location)
                .onSuccess { emitEvent("Location saved") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun updateWithdrawalStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateWithdrawalStatus(id, status)
                .onSuccess { emitEvent("Withdrawal $status") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun replySupportTicket(id: String, status: String, reply: String) {
        viewModelScope.launch {
            repository.replySupportTicket(id, status, reply)
                .onSuccess { emitEvent("Ticket updated") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun seedStarterCatalog() {
        viewModelScope.launch {
            repository.seedStarterCatalogAsAdmin()
                .onSuccess { emitEvent("Marketplace catalog synced to Firestore") }
                .onFailure { emitEvent("Admin access required to seed global catalog") }
        }
    }

    fun deleteBanner(bannerId: String) {
        viewModelScope.launch {
            repository.deleteBanner(bannerId)
                .onSuccess { emitEvent("Banner deleted") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun trackBannerEvent(bannerId: String, isClick: Boolean) {
        viewModelScope.launch {
            repository.trackBannerEvent(bannerId, isClick)
        }
    }

    fun saveCategory(category: CategoryItem) {
        viewModelScope.launch {
            repository.saveCategory(category)
                .onSuccess { emitEvent("Category saved") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
                .onSuccess { emitEvent("Category deleted") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun saveVideoContent(video: VideoContentItem) {
        viewModelScope.launch {
            repository.saveVideoContent(video)
                .onSuccess { emitEvent("Video saved") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun deleteVideoContent(videoId: String) {
        viewModelScope.launch {
            repository.deleteVideoContent(videoId)
                .onSuccess { emitEvent("Video removed") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun incrementVideoView(videoId: String) {
        viewModelScope.launch {
            repository.incrementVideoView(videoId)
        }
    }

    fun saveAdMobConfig(config: AdMobConfig) {
        viewModelScope.launch {
            repository.saveAdMobConfig(config)
                .onSuccess { emitEvent("AdMob settings saved") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun submitReview(targetId: String, targetType: String, rating: Long, title: String, comment: String) {
        val profile = (userProfile.value as? UiState.Success)?.data
        val userName = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        viewModelScope.launch {
            repository.submitReview(targetId, targetType, userName, rating, title, comment)
                .onSuccess { emitEvent("Review published") }
                .onFailure { emitEvent(it.localizedMessage ?: "Failed to submit review") }
        }
    }

    fun trackRecentlyViewed(productId: String) {
        viewModelScope.launch {
            repository.trackRecentlyViewed(productId)
        }
    }

    fun createReturnRequest(order: Order, requestType: String, reason: String, onCreated: () -> Unit = {}) {
        val profile = (userProfile.value as? UiState.Success)?.data
        val custName = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName
        viewModelScope.launch {
            repository.createReturnRequest(order, custName, requestType, reason)
                .onSuccess { id ->
                    emitEvent("$requestType request #$id submitted")
                    onCreated()
                }
                .onFailure { emitEvent(it.localizedMessage ?: "Failed to submit request") }
        }
    }

    fun updateReturnStatus(
        requestId: String,
        status: String,
        adminNote: String,
        targetUid: String = "",
        refundAmount: Double = 0.0
    ) {
        viewModelScope.launch {
            repository.updateReturnStatus(requestId, status, adminNote, targetUid, refundAmount)
                .onSuccess { emitEvent("Return #$requestId -> $status") }
                .onFailure { emitEvent(it.localizedMessage ?: "Could not update return status") }
        }
    }

    fun updateSellerShopProfile(
        shopName: String,
        shopDescription: String,
        shopLogoUrl: String,
        shopBannerUrl: String,
        shopHours: String,
        shopReturnPolicy: String
    ) {
        viewModelScope.launch {
            repository.updateSellerShopProfile(
                shopName, shopDescription, shopLogoUrl, shopBannerUrl, shopHours, shopReturnPolicy
            ).onSuccess { emitEvent("Shop profile updated") }
                .onFailure { emitEvent(it.localizedMessage ?: "Could not update shop profile") }
        }
    }

    fun saveApiIntegration(config: ApiIntegrationConfig) {
        viewModelScope.launch {
            repository.saveApiIntegration(config)
                .onSuccess { emitEvent("${config.providerName} saved") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    fun testApiIntegration(config: ApiIntegrationConfig, onResult: (String) -> Unit = {}) {
        viewModelScope.launch {
            repository.testApiIntegration(config)
                .onSuccess { detail ->
                    emitEvent("${config.providerName}: $detail")
                    onResult(detail)
                }
                .onFailure { err ->
                    val msg = err.localizedMessage ?: "Test failed"
                    emitEvent(msg)
                    onResult(msg)
                }
        }
    }

    fun saveTranslationOverride(languageCode: String, key: String, value: String) {
        viewModelScope.launch {
            repository.saveTranslationOverride(languageCode, key, value)
                .onSuccess { emitEvent("Translation saved ($languageCode.$key)") }
                .onFailure { emitEvent("Admin access required: ${it.localizedMessage}") }
        }
    }

    // --- MULTI-ROLE REGISTRATION, CREDENTIALS & KYC ACTIONS ---

    fun submitRegistration(
        application: KycApplication,
        rawPassword: String,
        docRecords: List<KycDocumentRecord>,
        termsAccepted: Boolean,
        onSuccess: (KycApplication) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.submitMultiRoleRegistration(application, rawPassword, docRecords, termsAccepted)
                .onSuccess { saved ->
                    syncWorkspaceToRegisteredRole(saved.role)
                    emitEvent("Registered ${saved.hunarUserId} (${saved.kycStatus})")
                    onSuccess(saved)
                }
                .onFailure { err ->
                    val msg = err.localizedMessage ?: "Registration failed"
                    emitEvent(msg)
                    onError(msg)
                }
        }
    }

    fun uploadKycDocument(doc: KycDocumentRecord) {
        viewModelScope.launch {
            repository.saveOrUpdateKycDocument(currentUserId, doc)
                .onSuccess { emitEvent("${doc.docLabel} uploaded") }
                .onFailure { emitEvent(it.localizedMessage ?: "Document upload failed") }
        }
    }

    fun removeKycDocument(docType: String, localPath: String) {
        viewModelScope.launch {
            repository.deleteKycDocument(currentUserId, docType, localPath)
                .onSuccess { emitEvent("Document removed") }
                .onFailure { emitEvent(it.localizedMessage ?: "Could not remove document") }
        }
    }

    fun verifyPasswordLogin(
        identifier: String,
        password: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.verifyLocalCredentials(identifier, password)
                .onSuccess { profile ->
                    syncWorkspaceToRegisteredRole(profile.role)
                    emitEvent("Welcome back, ${profile.displayName}!")
                    onSuccess(profile)
                }
                .onFailure { err ->
                    val msg = err.localizedMessage ?: "Login verification failed"
                    onError(msg)
                }
        }
    }

    fun resetPasswordWithOtp(
        identifier: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.resetPasswordWithOtp(identifier, newPassword)
                .onSuccess {
                    emitEvent("Password updated securely")
                    onSuccess()
                }
                .onFailure { err ->
                    onError(err.localizedMessage ?: "Failed to reset password")
                }
        }
    }

    fun inspectKycApplicant(uid: String?) {
        _inspectedKycUid.value = uid
    }

    fun adminUpdateKycStatus(
        targetUid: String,
        newKycStatus: String,
        accountStatus: String,
        verificationFlags: Map<String, Boolean>,
        adminNotes: String
    ) {
        viewModelScope.launch {
            repository.adminUpdateKycStatus(targetUid, newKycStatus, accountStatus, verificationFlags, adminNotes)
                .onSuccess { emitEvent("KYC status -> $newKycStatus") }
                .onFailure { emitEvent("Admin action failed: ${it.localizedMessage}") }
        }
    }

    fun adminUpdateDocumentStatus(
        targetUid: String,
        docId: String,
        status: String,
        adminNote: String
    ) {
        viewModelScope.launch {
            repository.adminUpdateDocumentStatus(targetUid, docId, status, adminNote)
                .onSuccess { emitEvent("Document marked $status") }
                .onFailure { emitEvent("Document update failed: ${it.localizedMessage}") }
        }
    }

    fun logKycDocumentAccess(targetUid: String, docId: String, docLabel: String) {
        viewModelScope.launch {
            repository.logKycDocumentAccess(targetUid, docId, docLabel)
        }
    }

    fun deleteOwnAccount(context: Context, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteUserAccountAndKyc(context, currentUserId)
                .onSuccess {
                    emitEvent("Account & KYC documents deleted")
                    onDeleted()
                }
                .onFailure { emitEvent(it.localizedMessage ?: "Account deletion failed") }
        }
    }

    private companion object {
        const val TAG = "HunarJiVM"
    }
}
