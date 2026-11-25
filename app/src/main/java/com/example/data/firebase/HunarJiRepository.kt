package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.core.security.CredentialSecurityHelper
import com.example.core.storage.PrivateKycStorage
import com.example.data.model.AdEventLog
import com.example.data.model.AdMobConfig
import com.example.data.model.Address
import com.example.data.model.ApiIntegrationConfig
import com.example.data.model.AppNotification
import com.example.data.model.AuditLog
import com.example.data.model.Banner
import com.example.data.model.BookingStatus
import com.example.data.model.CartItem
import com.example.data.model.CategoryItem
import com.example.data.model.Coupon
import com.example.data.model.KycApplication
import com.example.data.model.KycCatalogConstants
import com.example.data.model.KycDocumentRecord
import com.example.data.model.LiveStream
import com.example.data.model.MarketplaceDomainRules
import com.example.data.model.MarketplaceReview
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PlatformConfig
import com.example.data.model.Product
import com.example.data.model.ProductGuideStep
import com.example.data.model.ProductMediaItem
import com.example.data.model.ProductWarrantyDoc
import com.example.data.model.ProviderProfile
import com.example.data.model.RecentlyViewedRecord
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HunarJiRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun est() = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE

    // --- REALTIME STREAMS ---

    fun observeUserProfile(uid: String): Flow<UserProfile?> =
        db.collection("users").document(uid).snapshots()
            .map { snap -> if (snap.exists()) snap.toObject(UserProfile::class.java, est()) else null }

    fun observeCart(uid: String): Flow<List<CartItem>> =
        db.collection("users").document(uid).collection("cart").snapshots()
            .map { it.toObjects(CartItem::class.java, est()) }

    fun observeWishlist(uid: String): Flow<List<WishlistItem>> =
        db.collection("users").document(uid).collection("wishlist").snapshots()
            .map { it.toObjects(WishlistItem::class.java, est()) }

    fun observeAddresses(uid: String): Flow<List<Address>> =
        db.collection("users").document(uid).collection("addresses").snapshots()
            .map { it.toObjects(Address::class.java, est()) }

    fun observeWalletTransactions(uid: String): Flow<List<WalletTransaction>> =
        db.collection("users").document(uid).collection("wallet_transactions").snapshots()
            .map { it.toObjects(WalletTransaction::class.java, est()) }

    fun observeNotifications(uid: String): Flow<List<AppNotification>> =
        db.collection("users").document(uid).collection("notifications").snapshots()
            .map { it.toObjects(AppNotification::class.java, est()) }

    fun observeProducts(): Flow<List<Product>> =
        db.collection("products").snapshots()
            .map { it.toObjects(Product::class.java, est()) }

    fun observeServices(): Flow<List<ServiceItem>> =
        db.collection("services").snapshots()
            .map { it.toObjects(ServiceItem::class.java, est()) }

    fun observeProviders(): Flow<List<ProviderProfile>> =
        db.collection("providers").snapshots()
            .map { it.toObjects(ProviderProfile::class.java, est()) }

    fun observeLiveStreams(): Flow<List<LiveStream>> =
        db.collection("live_streams").snapshots()
            .map { it.toObjects(LiveStream::class.java, est()) }

    fun observeStreamMessages(streamId: String): Flow<List<StreamMessage>> =
        db.collection("live_streams").document(streamId).collection("messages").snapshots()
            .map { it.toObjects(StreamMessage::class.java, est()) }

    fun observeBanners(): Flow<List<Banner>> =
        db.collection("banners").snapshots()
            .map { it.toObjects(Banner::class.java, est()) }

    fun observeCoupons(): Flow<List<Coupon>> =
        db.collection("coupons").snapshots()
            .map { it.toObjects(Coupon::class.java, est()) }

    fun observeLocations(): Flow<List<ServiceLocation>> =
        db.collection("locations").snapshots()
            .map { it.toObjects(ServiceLocation::class.java, est()) }

    fun observePlatformConfig(): Flow<PlatformConfig> =
        db.collection("app_config").document("platform_settings").snapshots()
            .map { snap ->
                if (snap.exists()) snap.toObject(PlatformConfig::class.java, est()) ?: PlatformConfig()
                else PlatformConfig()
            }

    fun observeCustomerOrders(uid: String): Flow<List<Order>> =
        db.collection("orders").whereEqualTo("userId", uid).snapshots()
            .map { it.toObjects(Order::class.java, est()) }

    fun observeSellerOrders(sellerUid: String): Flow<List<Order>> =
        db.collection("orders").whereEqualTo("sellerId", sellerUid).snapshots()
            .map { it.toObjects(Order::class.java, est()) }

    fun observeAvailableDeliveries(): Flow<List<Order>> =
        db.collection("orders").whereEqualTo("status", "CONFIRMED").snapshots()
            .map { it.toObjects(Order::class.java, est()) }

    fun observeMyDeliveries(deliveryUid: String): Flow<List<Order>> =
        db.collection("orders").whereEqualTo("deliveryPartnerId", deliveryUid).snapshots()
            .map { it.toObjects(Order::class.java, est()) }

    fun observeAllOrdersAdmin(): Flow<List<Order>> =
        db.collection("orders").snapshots()
            .map { it.toObjects(Order::class.java, est()) }

    fun observeCustomerBookings(uid: String): Flow<List<ServiceBooking>> =
        db.collection("service_bookings").whereEqualTo("userId", uid).snapshots()
            .map { it.toObjects(ServiceBooking::class.java, est()) }

    fun observePendingBookings(): Flow<List<ServiceBooking>> =
        db.collection("service_bookings").whereEqualTo("status", "PENDING").snapshots()
            .map { it.toObjects(ServiceBooking::class.java, est()) }

    fun observeMechanicBookings(mechUid: String): Flow<List<ServiceBooking>> =
        db.collection("service_bookings").whereEqualTo("mechanicId", mechUid).snapshots()
            .map { it.toObjects(ServiceBooking::class.java, est()) }

    fun observeAllBookingsAdmin(): Flow<List<ServiceBooking>> =
        db.collection("service_bookings").snapshots()
            .map { it.toObjects(ServiceBooking::class.java, est()) }

    fun observeUserWithdrawals(uid: String): Flow<List<WithdrawalRequest>> =
        db.collection("withdrawals").whereEqualTo("userId", uid).snapshots()
            .map { it.toObjects(WithdrawalRequest::class.java, est()) }

    fun observeAllWithdrawalsAdmin(): Flow<List<WithdrawalRequest>> =
        db.collection("withdrawals").snapshots()
            .map { it.toObjects(WithdrawalRequest::class.java, est()) }

    fun observeUserTickets(uid: String): Flow<List<SupportTicket>> =
        db.collection("support_tickets").whereEqualTo("userId", uid).snapshots()
            .map { it.toObjects(SupportTicket::class.java, est()) }

    fun observeAllTicketsAdmin(): Flow<List<SupportTicket>> =
        db.collection("support_tickets").snapshots()
            .map { it.toObjects(SupportTicket::class.java, est()) }

    fun observeAllUsersAdmin(): Flow<List<UserProfile>> =
        db.collection("users").snapshots()
            .map { it.toObjects(UserProfile::class.java, est()) }

    fun observeAuditLogsAdmin(): Flow<List<AuditLog>> =
        db.collection("audit_logs").snapshots()
            .map { it.toObjects(AuditLog::class.java, est()) }

    fun observeMyKycApplication(uid: String): Flow<KycApplication?> =
        db.collection("kyc_applications").document(uid).snapshots()
            .map { snap -> if (snap.exists()) snap.toObject(KycApplication::class.java, est()) else null }

    fun observeMyKycDocuments(uid: String): Flow<List<KycDocumentRecord>> =
        db.collection("kyc_applications").document(uid).collection("documents").snapshots()
            .map { it.toObjects(KycDocumentRecord::class.java, est()) }

    fun observeMyVerificationHistory(uid: String): Flow<List<VerificationHistoryEntry>> =
        db.collection("kyc_applications").document(uid).collection("verification_history").snapshots()
            .map { it.toObjects(VerificationHistoryEntry::class.java, est()) }

    fun observeAllKycApplicationsAdmin(): Flow<List<KycApplication>> =
        db.collection("kyc_applications").snapshots()
            .map { it.toObjects(KycApplication::class.java, est()) }

    fun observeKycDocuments(userId: String): Flow<List<KycDocumentRecord>> =
        db.collection("kyc_applications").document(userId).collection("documents").snapshots()
            .map { it.toObjects(KycDocumentRecord::class.java, est()) }

    fun observeVerificationHistory(userId: String): Flow<List<VerificationHistoryEntry>> =
        db.collection("kyc_applications").document(userId).collection("verification_history").snapshots()
            .map { it.toObjects(VerificationHistoryEntry::class.java, est()) }

    fun observeCategories(): Flow<List<CategoryItem>> =
        db.collection("categories").snapshots()
            .map { snap -> snap.toObjects(CategoryItem::class.java, est()).sortedBy { it.sortOrder } }

    fun observeVideos(): Flow<List<VideoContentItem>> =
        db.collection("videos").snapshots()
            .map { snap -> snap.toObjects(VideoContentItem::class.java, est()).sortedBy { it.sortOrder } }

    fun observeAdMobConfig(): Flow<AdMobConfig> =
        db.collection("app_config").document("admob_settings").snapshots()
            .map { snap ->
                if (snap.exists()) snap.toObject(AdMobConfig::class.java, est()) ?: AdMobConfig()
                else AdMobConfig()
            }

    fun observeRewardTransactions(uid: String): Flow<List<RewardTransaction>> =
        db.collection("users").document(uid).collection("reward_transactions").snapshots()
            .map { it.toObjects(RewardTransaction::class.java, est()) }

    fun observeRecentlyViewed(uid: String): Flow<List<RecentlyViewedRecord>> =
        db.collection("users").document(uid).collection("recently_viewed").snapshots()
            .map { it.toObjects(RecentlyViewedRecord::class.java, est()) }

    fun observeReviews(): Flow<List<MarketplaceReview>> =
        db.collection("reviews").snapshots()
            .map { it.toObjects(MarketplaceReview::class.java, est()) }

    fun observeUserWalletTopUps(uid: String): Flow<List<WalletTopUpRequest>> =
        db.collection("wallet_topups").whereEqualTo("userId", uid).snapshots()
            .map { it.toObjects(WalletTopUpRequest::class.java, est()) }

    fun observeAllWalletTopUpsAdmin(): Flow<List<WalletTopUpRequest>> =
        db.collection("wallet_topups").snapshots()
            .map { it.toObjects(WalletTopUpRequest::class.java, est()) }

    fun observeUserReturns(uid: String): Flow<List<ReturnRequest>> =
        db.collection("return_requests").whereEqualTo("userId", uid).snapshots()
            .map { it.toObjects(ReturnRequest::class.java, est()) }

    fun observeSellerReturns(sellerUid: String): Flow<List<ReturnRequest>> =
        db.collection("return_requests").whereEqualTo("sellerId", sellerUid).snapshots()
            .map { it.toObjects(ReturnRequest::class.java, est()) }

    fun observeAllReturnsAdmin(): Flow<List<ReturnRequest>> =
        db.collection("return_requests").snapshots()
            .map { it.toObjects(ReturnRequest::class.java, est()) }

    fun observeApiIntegrationsAdmin(): Flow<List<ApiIntegrationConfig>> =
        db.collection("api_integrations").snapshots()
            .map { it.toObjects(ApiIntegrationConfig::class.java, est()) }

    fun observeAdEventsAdmin(): Flow<List<AdEventLog>> =
        db.collection("ad_events").snapshots()
            .map { it.toObjects(AdEventLog::class.java, est()) }

    fun observeTranslationOverrides(): Flow<List<TranslationOverride>> =
        db.collection("translation_overrides").snapshots()
            .map { it.toObjects(TranslationOverride::class.java, est()) }

    // --- USER PROFILE & INITIALIZATION ---

    suspend fun ensureUserProfile(
        uid: String,
        displayName: String,
        email: String
    ): Result<Unit> = runSafe(OperationType.WRITE, "users/$uid") {
        val docRef = db.collection("users").document(uid)
        val snap = docRef.get().await()
        if (!snap.exists()) {
            val cleanName = displayName.ifBlank { email.substringBefore("@").ifBlank { "Hunar Ji User" } }
            val refCode = "HJ" + uid.take(6).uppercase()
            val payload = mapOf(
                "userId" to uid,
                "displayName" to cleanName.take(80),
                "email" to email.take(160),
                "phone" to "",
                "role" to "CUSTOMER",
                "walletBalance" to 0.0,
                "loyaltyPoints" to 50L,
                "verified" to false,
                "online" to true,
                "blocked" to false,
                "city" to "Patna",
                "district" to "Patna",
                "state" to "Bihar",
                "referralCode" to refCode,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        }
    }

    suspend fun updateUserProfile(
        displayName: String,
        phone: String,
        city: String,
        district: String,
        state: String,
        online: Boolean,
        shopName: String = "",
        vehicleInfo: String = "",
        skills: String = ""
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid"
        return runSafe(OperationType.UPDATE, path) {
            val updates = mapOf(
                "displayName" to displayName.trim().ifBlank { "Hunar Ji User" }.take(80),
                "phone" to phone.trim().take(24),
                "city" to city.trim().take(80),
                "district" to district.trim().take(80),
                "state" to state.trim().take(80),
                "online" to online,
                "shopName" to shopName.trim().take(120),
                "vehicleInfo" to vehicleInfo.trim().take(120),
                "skills" to skills.trim().take(200),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).update(updates).await()
        }
    }

    // --- CART & WISHLIST ---

    suspend fun addToCart(product: Product, variant: String = "Standard"): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = product.id.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { UUID.randomUUID().toString() }
        val path = "users/$uid/cart/$docId"
        return runSafe(OperationType.WRITE, path) {
            val ref = db.collection("users").document(uid).collection("cart").document(docId)
            val snap = ref.get().await()
            if (snap.exists()) {
                val currentQty = snap.getLong("quantity") ?: 1L
                ref.update(
                    mapOf(
                        "quantity" to (currentQty + 1L).coerceAtMost(99L),
                        "variant" to variant.take(60),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = mapOf(
                    "productId" to product.id.ifBlank { docId },
                    "title" to product.title.take(120),
                    "category" to product.category.take(60),
                    "sellerId" to product.sellerId.ifBlank { uid },
                    "sellerName" to product.sellerName.ifBlank { "Hunar Ji Seller" }.take(100),
                    "price" to product.price.coerceAtLeast(0.0),
                    "originalPrice" to product.originalPrice.coerceAtLeast(product.price),
                    "quantity" to 1L,
                    "variant" to variant.take(60),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                ref.set(payload).await()
            }
        }
    }

    suspend fun updateCartQuantity(cartItemId: String, newQuantity: Long): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid/cart/$cartItemId"
        return runSafe(OperationType.UPDATE, path) {
            val ref = db.collection("users").document(uid).collection("cart").document(cartItemId)
            if (newQuantity <= 0L) {
                ref.delete().await()
            } else {
                ref.update(
                    mapOf(
                        "quantity" to newQuantity.coerceIn(1L, 99L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        }
    }

    suspend fun toggleWishlist(product: Product, currentlySaved: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = product.id.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { UUID.randomUUID().toString() }
        val path = "users/$uid/wishlist/$docId"
        return runSafe(OperationType.WRITE, path) {
            val ref = db.collection("users").document(uid).collection("wishlist").document(docId)
            if (currentlySaved) {
                ref.delete().await()
            } else {
                val payload = mapOf(
                    "productId" to product.id.ifBlank { docId },
                    "title" to product.title.take(120),
                    "category" to product.category.take(60),
                    "price" to product.price.coerceAtLeast(0.0),
                    "originalPrice" to product.originalPrice.coerceAtLeast(product.price),
                    "discountPercent" to product.discountPercent.coerceIn(0L, 99L),
                    "createdAt" to FieldValue.serverTimestamp()
                )
                ref.set(payload).await()
            }
        }
    }

    // --- ADDRESSES ---

    suspend fun saveAddress(address: Address): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = address.id.ifBlank { "addr_${System.currentTimeMillis()}" }
        val path = "users/$uid/addresses/$docId"
        return runSafe(OperationType.WRITE, path) {
            val ref = db.collection("users").document(uid).collection("addresses").document(docId)
            val existingSnap = ref.get().await()
            val allowedLabels = listOf("Home", "Office", "Work", "Other")
            val payload = mapOf(
                "label" to (if (address.label in allowedLabels) address.label else "Home"),
                "recipientName" to address.recipientName.trim().take(80),
                "mobile" to address.mobile.trim().take(20),
                "houseFlatNo" to address.houseFlatNo.trim().take(80),
                "streetRoad" to address.streetRoad.trim().take(120),
                "areaLocality" to address.areaLocality.trim().take(120),
                "fullAddress" to address.fullAddress.trim().take(250),
                "landmark" to address.landmark.trim().take(120),
                "city" to address.city.trim().take(80),
                "district" to address.district.trim().take(80),
                "state" to address.state.trim().take(80),
                "pincode" to address.pincode.trim().take(12),
                "gpsLocation" to address.gpsLocation.trim().take(120),
                "defaultAddress" to address.defaultAddress,
                "createdAt" to (existingSnap.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.set(payload).await()
        }
    }

    suspend fun deleteAddress(addressId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return runSafe(OperationType.DELETE, "users/$uid/addresses/$addressId") {
            db.collection("users").document(uid).collection("addresses").document(addressId).delete().await()
        }
    }

    // --- ORDERS & CHECKOUT ---

    suspend fun placeOrder(
        cartItems: List<CartItem>,
        customerName: String,
        dropAddress: String,
        district: String,
        paymentMethod: PaymentMethod,
        couponCode: String,
        discountAmount: Double,
        deliveryFee: Double,
        taxAmount: Double,
        walletBalance: Double
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        if (cartItems.isEmpty()) return Result.failure(IllegalArgumentException("Cart is empty"))
        val subtotal = cartItems.sumOf { it.price * it.quantity }
        val total = (subtotal - discountAmount + deliveryFee + taxAmount).coerceAtLeast(0.0)
        if (paymentMethod == PaymentMethod.WALLET && walletBalance < total) {
            return Result.failure(IllegalStateException("Insufficient wallet balance"))
        }
        val orderId = "HJ${(1000..9999).random()}_${System.currentTimeMillis() % 1000}"
        val path = "orders/$orderId"
        return runSafeValue(OperationType.CREATE, path) {
            val firstSellerId = cartItems.first().sellerId.ifBlank { uid }
            val firstSellerName = cartItems.first().sellerName.ifBlank { "Hunar Ji Seller" }
            val summary = cartItems.joinToString(", ") { "${it.title} x${it.quantity}" }.take(390)
            val totalQty = cartItems.sumOf { it.quantity }.coerceAtLeast(1L)
            val payStatus = when (paymentMethod) {
                PaymentMethod.COD -> "PENDING"
                PaymentMethod.WALLET -> "PAID"
                else -> "PENDING"
            }

            val orderPayload = mapOf(
                "userId" to uid,
                "sellerId" to firstSellerId,
                "deliveryPartnerId" to "",
                "customerName" to customerName.ifBlank { "Customer" }.take(80),
                "sellerName" to firstSellerName.take(100),
                "deliveryPartnerName" to "",
                "itemsSummary" to summary,
                "itemCount" to totalQty,
                "subtotal" to subtotal,
                "discountAmount" to discountAmount,
                "deliveryFee" to deliveryFee,
                "taxAmount" to taxAmount,
                "totalAmount" to total,
                "couponCode" to couponCode.take(32),
                "paymentMethod" to paymentMethod.name,
                "paymentStatus" to payStatus,
                "status" to "PENDING",
                "pickupAddress" to "Main Market Hub, $district",
                "dropAddress" to dropAddress.ifBlank { "Customer Address, $district" }.take(250),
                "district" to district.ifBlank { "Patna" }.take(80),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("orders").document(orderId).set(orderPayload).await()

            if (paymentMethod == PaymentMethod.WALLET && total > 0.0) {
                recordWalletMutation(
                    uid = uid,
                    delta = -total,
                    type = "DEBIT",
                    title = "Order Payment #$orderId",
                    referenceId = orderId
                )
            }

            // Clear user's cart
            cartItems.forEach { item ->
                if (item.id.isNotBlank()) {
                    db.collection("users").document(uid).collection("cart").document(item.id).delete().await()
                }
            }

            // Create notification
            addNotification(
                uid = uid,
                title = "Order Placed #$orderId",
                message = "Your order ($summary) for ₹${total.toInt()} has been placed.",
                category = "ORDER"
            )
            orderId
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> {
        requireUserId()
        return runSafe(OperationType.UPDATE, "orders/$orderId") {
            db.collection("orders").document(orderId).update(
                mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun acceptOrUpdateDeliveryOrder(
        orderId: String,
        deliveryPartnerName: String,
        newStatus: OrderStatus,
        markPaid: Boolean = false
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return runSafe(OperationType.UPDATE, "orders/$orderId") {
            val updates = mutableMapOf<String, Any>(
                "deliveryPartnerId" to uid,
                "deliveryPartnerName" to deliveryPartnerName.ifBlank { "HJ Rider" }.take(80),
                "status" to newStatus.name,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (markPaid) {
                updates["paymentStatus"] = "PAID"
            }
            db.collection("orders").document(orderId).update(updates).await()
        }
    }

    // --- SERVICES & BOOKINGS ---

    suspend fun createServiceBooking(
        service: ServiceItem,
        customerName: String,
        vehicleInfo: String,
        problemDescription: String,
        scheduledDate: String,
        scheduledTime: String,
        serviceAddress: String,
        district: String
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val bookingId = "BK${(1000..9999).random()}_${System.currentTimeMillis() % 1000}"
        return runSafeValue(OperationType.CREATE, "service_bookings/$bookingId") {
            val payload = mapOf(
                "userId" to uid,
                "mechanicId" to service.providerId.ifBlank { "" },
                "serviceId" to service.id.ifBlank { "srv_custom" },
                "serviceTitle" to service.title.take(120),
                "customerName" to customerName.ifBlank { "Customer" }.take(80),
                "mechanicName" to service.providerName.ifBlank { "Assigned Mechanic" }.take(80),
                "vehicleInfo" to vehicleInfo.take(120),
                "problemDescription" to problemDescription.ifBlank { "Standard service checkup" }.take(500),
                "scheduledDate" to scheduledDate.take(40),
                "scheduledTime" to scheduledTime.take(40),
                "serviceAddress" to serviceAddress.ifBlank { "Customer Location" }.take(250),
                "district" to district.ifBlank { "Patna" }.take(80),
                "amount" to service.price.coerceAtLeast(0.0),
                "status" to "PENDING",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("service_bookings").document(bookingId).set(payload).await()
            addNotification(
                uid = uid,
                title = "Booking Confirmed #$bookingId",
                message = "${service.title} scheduled for $scheduledDate ($scheduledTime).",
                category = "BOOKING"
            )
            bookingId
        }
    }

    suspend fun updateBookingStatus(
        bookingId: String,
        mechanicName: String,
        newStatus: BookingStatus
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return runSafe(OperationType.UPDATE, "service_bookings/$bookingId") {
            val updates = if (newStatus == BookingStatus.CANCELLED) {
                mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            } else {
                mapOf(
                    "mechanicId" to uid,
                    "mechanicName" to mechanicName.ifBlank { "Mechanic" }.take(80),
                    "status" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            }
            db.collection("service_bookings").document(bookingId).update(updates).await()
        }
    }

    // --- SELLER PRODUCTS & MECHANIC SERVICES ---

    private fun serializeMediaGallery(items: List<ProductMediaItem>): List<Map<String, Any>> =
        items.mapIndexed { idx, m ->
            mapOf(
                "id" to m.id.ifBlank { "media_$idx" }.take(64),
                "type" to (if (m.type.equals("VIDEO", true)) "VIDEO" else "IMAGE"),
                "angleTag" to m.angleTag.ifBlank { "Front" }.take(40),
                "uriOrUrl" to m.uriOrUrl.trim().take(600),
                "thumbnailUrl" to m.thumbnailUrl.trim().take(600),
                "caption" to m.caption.trim().take(180),
                "sortOrder" to idx.toLong(),
                "youtubeVideo" to m.youtubeVideo
            )
        }

    private fun serializeGuideSteps(steps: List<ProductGuideStep>): List<Map<String, Any>> =
        steps.mapIndexed { idx, s ->
            mapOf(
                "stepNumber" to (if (s.stepNumber > 0L) s.stepNumber else (idx + 1L)),
                "heading" to s.heading.trim().take(120),
                "description" to s.description.trim().take(600),
                "imageUrl" to s.imageUrl.trim().take(500),
                "videoUrl" to s.videoUrl.trim().take(500),
                "instructions" to s.instructions.trim().take(500)
            )
        }

    private fun serializeWarrantyDocs(docs: List<ProductWarrantyDoc>): List<Map<String, Any>> =
        docs.mapIndexed { idx, d ->
            mapOf(
                "id" to d.id.ifBlank { "wdoc_$idx" }.take(64),
                "label" to d.label.trim().take(120),
                "docType" to d.docType.ifBlank { "WARRANTY_CARD" }.take(40),
                "fileName" to d.fileName.trim().take(180),
                "mimeType" to d.mimeType.ifBlank { "application/pdf" }.take(80),
                "uriOrUrl" to d.uriOrUrl.trim().take(600)
            )
        }

    suspend fun saveProduct(product: Product, sellerName: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val rawId = product.id.ifBlank { "prod_${System.currentTimeMillis()}" }
        val docId = rawId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(120)
        return runSafe(OperationType.WRITE, "products/$docId") {
            val disc = if (product.originalPrice > product.price && product.originalPrice > 0) {
                (((product.originalPrice - product.price) / product.originalPrice) * 100).toLong().coerceIn(0L, 95L)
            } else {
                product.discountPercent.coerceIn(0L, 95L)
            }
            val cleanPrice = product.price.coerceAtLeast(1.0)
            val cleanOrig = product.originalPrice.coerceAtLeast(cleanPrice)
            val sharedFields = mutableMapOf<String, Any>(
                "title" to product.title.trim().take(120),
                "category" to product.category.trim().ifBlank { "Electronics" }.take(60),
                "subcategory" to product.subcategory.trim().ifBlank { "General" }.take(60),
                "description" to product.description.trim().take(3000),
                "price" to cleanPrice,
                "originalPrice" to cleanOrig,
                "discountPercent" to disc,
                "stock" to product.stock.coerceAtLeast(0L),
                "iconKey" to product.iconKey.ifBlank { "box" }.take(60),
                "active" to product.active,
                "district" to product.district.ifBlank { "Patna" }.take(80),
                "mediaGallery" to serializeMediaGallery(product.mediaGallery),
                "brand" to product.brand.trim().take(80),
                "modelNumber" to product.modelNumber.trim().take(80),
                "sku" to product.sku.trim().take(60),
                "weight" to product.weight.trim().take(40),
                "dimensions" to product.dimensions.trim().take(80),
                "material" to product.material.trim().take(80),
                "manufacturer" to product.manufacturer.trim().take(120),
                "countryOfOrigin" to product.countryOfOrigin.trim().ifBlank { "India" }.take(60),
                "packageContents" to product.packageContents.trim().take(400),
                "specs" to product.specs.entries.take(30).associate { it.key.take(60) to it.value.take(160) },
                "highlights" to product.highlights.filter { it.isNotBlank() }.take(20).map { it.take(160) },
                "features" to product.features.filter { it.isNotBlank() }.take(20).map { it.take(160) },
                "benefits" to product.benefits.filter { it.isNotBlank() }.take(20).map { it.take(160) },
                "usageInstructions" to product.usageInstructions.trim().take(1000),
                "installationInstructions" to product.installationInstructions.trim().take(1000),
                "safetyInfo" to product.safetyInfo.trim().take(600),
                "faqs" to product.faqs.take(15).map { faq ->
                    mapOf("q" to (faq["q"] ?: "").take(200), "a" to (faq["a"] ?: "").take(500))
                },
                "colorOptions" to product.colorOptions.filter { it.isNotBlank() }.take(15).map { it.take(40) },
                "sizeOptions" to product.sizeOptions.filter { it.isNotBlank() }.take(15).map { it.take(40) },
                "stepByStepSections" to serializeGuideSteps(product.stepByStepSections),
                "warrantyAvailable" to product.warrantyAvailable,
                "warrantyDuration" to product.warrantyDuration.trim().take(60),
                "warrantyType" to product.warrantyType.trim().take(80),
                "warrantyConditions" to product.warrantyConditions.trim().take(400),
                "warrantyServiceCentre" to product.warrantyServiceCentre.trim().take(200),
                "warrantyRegistrationSteps" to product.warrantyRegistrationSteps.trim().take(400),
                "returnPolicy" to product.returnPolicy.trim().take(120),
                "replacementPolicy" to product.replacementPolicy.trim().take(120),
                "warrantyDocs" to serializeWarrantyDocs(product.warrantyDocs),
                "newArrival" to product.newArrival,
                "codAvailable" to product.codAvailable,
                "estimatedDeliveryDays" to product.estimatedDeliveryDays.coerceIn(1L, 30L),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val ref = db.collection("products").document(docId)
            val exists = if (product.id.isNotBlank()) ref.get().await().exists() else false
            if (!exists) {
                sharedFields["sellerId"] = uid
                sharedFields["sellerName"] = sellerName.ifBlank { "Seller" }.take(100)
                sharedFields["rating"] = 4.6
                sharedFields["reviewCount"] = 1L
                sharedFields["approved"] = true
                sharedFields["featured"] = false
                sharedFields["trending"] = true
                sharedFields["soldCount"] = 0L
                sharedFields["viewCount"] = 0L
                sharedFields["createdAt"] = FieldValue.serverTimestamp()
                ref.set(sharedFields).await()
            } else {
                // Only update keys allowed by firestore.rules for non-admin sellers
                ref.update(sharedFields).await()
            }
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        requireUserId()
        return runSafe(OperationType.DELETE, "products/$productId") {
            db.collection("products").document(productId).delete().await()
        }
    }

    suspend fun saveServiceItem(service: ServiceItem, providerName: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = service.id.ifBlank { "srv_${System.currentTimeMillis()}" }
        return runSafe(OperationType.WRITE, "services/$docId") {
            if (service.id.isBlank()) {
                val payload = mapOf(
                    "providerId" to uid,
                    "providerName" to providerName.ifBlank { "Mechanic" }.take(100),
                    "title" to service.title.trim().take(120),
                    "category" to service.category.trim().take(60),
                    "description" to service.description.trim().take(1000),
                    "price" to service.price.coerceAtLeast(1.0),
                    "durationMinutes" to service.durationMinutes.coerceIn(10L, 1440L),
                    "rating" to 4.8,
                    "iconKey" to service.iconKey.ifBlank { "car" }.take(60),
                    "approved" to true,
                    "active" to service.active,
                    "district" to service.district.ifBlank { "Patna" }.take(80),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("services").document(docId).set(payload).await()
            } else {
                val updates = mapOf(
                    "title" to service.title.trim().take(120),
                    "category" to service.category.trim().take(60),
                    "description" to service.description.trim().take(1000),
                    "price" to service.price.coerceAtLeast(1.0),
                    "durationMinutes" to service.durationMinutes.coerceIn(10L, 1440L),
                    "iconKey" to service.iconKey.ifBlank { "car" }.take(60),
                    "active" to service.active,
                    "district" to service.district.ifBlank { "Patna" }.take(80),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("services").document(docId).update(updates).await()
            }
        }
    }

    // --- WALLET & WITHDRAWALS ---

    private suspend fun recordWalletMutation(
        uid: String,
        delta: Double,
        type: String,
        title: String,
        referenceId: String
    ) {
        val txId = "tx_${System.currentTimeMillis()}"
        db.collection("users").document(uid).update(
            mapOf(
                "walletBalance" to FieldValue.increment(delta),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        db.collection("users").document(uid).collection("wallet_transactions").document(txId).set(
            mapOf(
                "type" to type,
                "amount" to kotlin.math.abs(delta),
                "title" to title.take(140),
                "referenceId" to referenceId.take(128),
                "status" to "COMPLETED",
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun submitWalletTopUpRequest(
        userName: String,
        amount: Double,
        method: String,
        paymentReference: String
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        if (amount <= 0.0 || amount > 100000.0) {
            return Result.failure(IllegalArgumentException("Amount must be between ₹1 and ₹1,00,000"))
        }
        val cleanRef = paymentReference.trim()
        if (cleanRef.length < 4) {
            return Result.failure(IllegalArgumentException("Enter a valid Transaction / UTR reference (min 4 characters)"))
        }
        val topUpId = "topup_${System.currentTimeMillis()}"
        return runSafeValue(OperationType.CREATE, "wallet_topups/$topUpId") {
            db.collection("wallet_topups").document(topUpId).set(
                mapOf(
                    "userId" to uid,
                    "userName" to userName.ifBlank { "Customer" }.take(80),
                    "amount" to amount,
                    "method" to method.ifBlank { "UPI" }.take(40),
                    "paymentReference" to cleanRef.take(120),
                    "status" to "PENDING",
                    "adminNote" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            addNotification(
                uid = uid,
                title = "Top-Up Submitted (Pending Verification)",
                message = "₹${amount.toInt()} via $method (Ref: $cleanRef) submitted for Admin payment verification.",
                category = "WALLET"
            )
            topUpId
        }
    }

    suspend fun adminReviewWalletTopUp(
        topUpId: String,
        targetUid: String,
        amount: Double,
        approve: Boolean,
        adminNote: String
    ): Result<Unit> {
        val adminUid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val adminEmail = auth.currentUser?.email ?: ""
        return runSafe(OperationType.UPDATE, "wallet_topups/$topUpId") {
            val newStatus = if (approve) "VERIFIED" else "REJECTED"
            db.collection("wallet_topups").document(topUpId).update(
                mapOf(
                    "status" to newStatus,
                    "adminNote" to adminNote.trim().take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            if (approve && amount > 0.0) {
                recordWalletMutation(
                    uid = targetUid,
                    delta = amount,
                    type = "CREDIT",
                    title = "Verified Wallet Top-Up (#$topUpId)",
                    referenceId = topUpId
                )
                addNotification(
                    uid = targetUid,
                    title = "Wallet Credited ₹${amount.toInt()}",
                    message = "Admin verified your top-up request #$topUpId.",
                    category = "WALLET"
                )
            } else {
                addNotification(
                    uid = targetUid,
                    title = "Top-Up Request Rejected",
                    message = adminNote.trim().ifBlank { "Payment reference could not be verified." },
                    category = "WALLET"
                )
            }
            logAdminAction(adminUid, adminEmail, "TOPUP_$newStatus", "wallet_topups", topUpId, "₹${amount.toInt()} for $targetUid")
        }
    }

    suspend fun recordRewardedAdEarned(
        adUnitId: String,
        points: Long,
        rewardToken: String,
        dailyCap: Long
    ): Result<Long> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val cleanToken = rewardToken.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(120)
        if (cleanToken.length < 4) {
            return Result.failure(IllegalArgumentException("Invalid reward token"))
        }
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return runSafeValue(OperationType.WRITE, "users/$uid/reward_transactions/$cleanToken") {
            val rewardRef = db.collection("users").document(uid).collection("reward_transactions").document(cleanToken)
            val userRef = db.collection("users").document(uid)
            val existingReward = rewardRef.get().await()
            if (existingReward.exists()) {
                throw IllegalStateException("This rewarded ad session was already credited.")
            }
            val userSnap = userRef.get().await()
            val storedDate = userSnap.getString("dailyRewardDate") ?: ""
            val currentCount = if (storedDate == todayStr) (userSnap.getLong("dailyRewardCount") ?: 0L) else 0L
            val currentPoints = userSnap.getLong("rewardPoints") ?: 0L
            val eval = MarketplaceDomainRules.evaluateRewardedAdClaim(
                adsEnabled = true,
                rewardedEnabled = true,
                rewardEarnedBySdkCallback = true,
                todayCount = currentCount,
                dailyCap = dailyCap,
                pointsPerAd = points,
                rewardToken = cleanToken,
                usedTokens = emptySet()
            ).getOrThrow()

            rewardRef.set(
                mapOf(
                    "points" to eval,
                    "type" to "REWARDED_AD",
                    "title" to "Rewarded Video Ad Completed",
                    "rewardToken" to cleanToken,
                    "adUnitId" to adUnitId.take(100),
                    "status" to "CREDITED",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            userRef.update(
                mapOf(
                    "rewardPoints" to (currentPoints + eval),
                    "dailyRewardCount" to (currentCount + 1L),
                    "dailyRewardDate" to todayStr,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            logAdEvent(
                eventType = "REWARDED_COMPLETE",
                adFormat = "REWARDED",
                adUnitId = adUnitId,
                rewardPoints = eval,
                rewardToken = cleanToken
            )
            eval
        }
    }

    suspend fun logAdEvent(
        eventType: String,
        adFormat: String,
        adUnitId: String,
        rewardPoints: Long = 0L,
        rewardToken: String = ""
    ) {
        val uid = auth.currentUser?.uid ?: return
        val normalizedType = if (eventType in listOf("IMPRESSION", "REWARDED_COMPLETE", "BANNER_CLICK")) eventType else "IMPRESSION"
        val eid = "adev_${System.currentTimeMillis()}_${(100..999).random()}"
        try {
            db.collection("ad_events").document(eid).set(
                mapOf(
                    "userId" to uid,
                    "eventType" to normalizedType,
                    "adFormat" to adFormat.take(30),
                    "adUnitId" to adUnitId.take(100),
                    "rewardPoints" to rewardPoints.coerceAtLeast(0L),
                    "rewardToken" to rewardToken.take(128),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {
        }
    }

    suspend fun requestWithdrawal(
        userName: String,
        role: String,
        amount: Double,
        payoutMethod: String,
        currentBalance: Double
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        if (amount <= 0.0 || amount > currentBalance) {
            return Result.failure(IllegalArgumentException("Invalid withdrawal amount"))
        }
        val wid = "wd_${System.currentTimeMillis()}"
        return runSafe(OperationType.CREATE, "withdrawals/$wid") {
            db.collection("withdrawals").document(wid).set(
                mapOf(
                    "userId" to uid,
                    "userName" to userName.ifBlank { "User" }.take(80),
                    "role" to role.take(30),
                    "amount" to amount,
                    "payoutMethod" to payoutMethod.trim().take(120),
                    "status" to "PENDING",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            recordWalletMutation(uid, -amount, "WITHDRAWAL", "Withdrawal Request ($payoutMethod)", wid)
        }
    }

    // --- LIVE STREAMING ---

    suspend fun startLiveStream(hostName: String, title: String, category: String): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val streamId = "live_${System.currentTimeMillis()}"
        return runSafeValue(OperationType.CREATE, "live_streams/$streamId") {
            db.collection("live_streams").document(streamId).set(
                mapOf(
                    "hostId" to uid,
                    "hostName" to hostName.ifBlank { "Mechanic" }.take(80),
                    "title" to title.trim().take(140),
                    "category" to category.trim().take(60),
                    "viewerCount" to 1L,
                    "live" to true,
                    "approved" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            streamId
        }
    }

    suspend fun stopLiveStream(streamId: String): Result<Unit> {
        requireUserId()
        return runSafe(OperationType.UPDATE, "live_streams/$streamId") {
            db.collection("live_streams").document(streamId).update(
                mapOf(
                    "live" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun sendStreamMessage(streamId: String, senderName: String, text: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        if (text.isBlank()) return Result.success(Unit)
        val msgId = "msg_${System.currentTimeMillis()}"
        return runSafe(OperationType.CREATE, "live_streams/$streamId/messages/$msgId") {
            db.collection("live_streams").document(streamId).collection("messages").document(msgId).set(
                mapOf(
                    "userId" to uid,
                    "senderName" to senderName.ifBlank { "Viewer" }.take(80),
                    "text" to text.trim().take(300),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    // --- SUPPORT & NOTIFICATIONS ---

    suspend fun createSupportTicket(
        userName: String,
        role: String,
        category: String,
        subject: String,
        description: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val tid = "tk_${System.currentTimeMillis()}"
        return runSafe(OperationType.CREATE, "support_tickets/$tid") {
            db.collection("support_tickets").document(tid).set(
                mapOf(
                    "userId" to uid,
                    "userName" to userName.ifBlank { "User" }.take(80),
                    "role" to role.take(30),
                    "category" to category.take(60),
                    "subject" to subject.trim().take(140),
                    "description" to description.trim().take(1000),
                    "status" to "OPEN",
                    "adminReply" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    private suspend fun addNotification(uid: String, title: String, message: String, category: String) {
        val nid = "notif_${System.currentTimeMillis()}"
        db.collection("users").document(uid).collection("notifications").document(nid).set(
            mapOf(
                "title" to title.take(120),
                "message" to message.take(400),
                "category" to category.take(40),
                "read" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun markNotificationRead(notifId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return runSafe(OperationType.UPDATE, "users/$uid/notifications/$notifId") {
            db.collection("users").document(uid).collection("notifications").document(notifId)
                .update(mapOf("read" to true)).await()
        }
    }

    // --- ADMIN OPERATIONS ---

    suspend fun savePlatformConfig(config: PlatformConfig): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        return runSafe(OperationType.WRITE, "app_config/platform_settings") {
            val payload = mapOf(
                "customerAppEnabled" to config.customerAppEnabled,
                "mechanicAppEnabled" to config.mechanicAppEnabled,
                "sellerAppEnabled" to config.sellerAppEnabled,
                "deliveryAppEnabled" to config.deliveryAppEnabled,
                "maintenanceMode" to config.maintenanceMode,
                "razorpayEnabled" to config.razorpayEnabled,
                "razorpayMode" to (if (config.razorpayMode == "LIVE") "LIVE" else "TEST"),
                "razorpayKeyId" to config.razorpayKeyId.take(120),
                "upiEnabled" to config.upiEnabled,
                "cardEnabled" to config.cardEnabled,
                "netBankingEnabled" to config.netBankingEnabled,
                "walletEnabled" to config.walletEnabled,
                "codEnabled" to config.codEnabled,
                "globalCommissionPercent" to config.globalCommissionPercent.coerceIn(0.0, 50.0),
                "productCommissionPercent" to config.productCommissionPercent.coerceIn(0.0, 50.0),
                "serviceCommissionPercent" to config.serviceCommissionPercent.coerceIn(0.0, 50.0),
                "deliveryCommissionPercent" to config.deliveryCommissionPercent.coerceIn(0.0, 50.0),
                "gstPercent" to config.gstPercent.coerceIn(0.0, 40.0),
                "smsProvider" to config.smsProvider.take(60),
                "smsEnabled" to config.smsEnabled,
                "emailEnabled" to config.emailEnabled,
                "pushEnabled" to config.pushEnabled,
                "otpExpiryMinutes" to config.otpExpiryMinutes.coerceIn(1L, 60L),
                "supportEmail" to config.supportEmail.take(120),
                "supportPhone" to config.supportPhone.take(30),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("app_config").document("platform_settings").set(payload).await()
            logAdminAction(uid, email, "UPDATE_PLATFORM_CONFIG", "app_config", "platform_settings", "Updated platform gateway/commission/app controls")
        }
    }

    suspend fun saveBanner(banner: Banner): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        val rawId = banner.id.ifBlank { "banner_${System.currentTimeMillis()}" }
        val docId = rawId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(120)
        return runSafe(OperationType.WRITE, "banners/$docId") {
            val ref = db.collection("banners").document(docId)
            val snap = ref.get().await()
            val payload = mapOf(
                "title" to banner.title.trim().take(100),
                "subtitle" to banner.subtitle.trim().take(160),
                "ctaText" to banner.ctaText.trim().take(40),
                "targetCategory" to banner.targetCategory.trim().take(60),
                "active" to banner.active,
                "sortOrder" to banner.sortOrder.coerceAtLeast(0L),
                "description" to banner.description.trim().take(500),
                "imageUrl" to banner.imageUrl.trim().take(600),
                "videoUrl" to banner.videoUrl.trim().take(600),
                "linkUrl" to banner.linkUrl.trim().take(600),
                "targetApp" to banner.targetApp.ifBlank { "CUSTOMER_HOME" }.take(40),
                "placement" to banner.placement.ifBlank { "MAIN_SLIDER" }.take(40),
                "targetProductId" to banner.targetProductId.trim().take(128),
                "startDate" to banner.startDate.trim().take(32),
                "endDate" to banner.endDate.trim().take(32),
                "impressions" to (if (snap.exists()) (snap.getLong("impressions") ?: banner.impressions) else banner.impressions).coerceAtLeast(0L),
                "clicks" to (if (snap.exists()) (snap.getLong("clicks") ?: banner.clicks) else banner.clicks).coerceAtLeast(0L),
                "createdAt" to (snap.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.set(payload).await()
            logAdminAction(uid, email, "SAVE_BANNER", "banners", docId, "Saved banner ${banner.title}")
        }
    }

    suspend fun deleteBanner(bannerId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        return runSafe(OperationType.DELETE, "banners/$bannerId") {
            db.collection("banners").document(bannerId).delete().await()
            logAdminAction(uid, email, "DELETE_BANNER", "banners", bannerId, "Deleted banner")
        }
    }

    suspend fun trackBannerEvent(bannerId: String, isClick: Boolean) {
        if (bannerId.isBlank() || auth.currentUser == null) return
        val docId = bannerId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        try {
            val ref = db.collection("banners").document(docId)
            val snap = ref.get().await()
            if (snap.exists()) {
                val field = if (isClick) "clicks" else "impressions"
                ref.update(mapOf(field to FieldValue.increment(1L))).await()
            }
        } catch (_: Exception) {
        }
    }

    suspend fun saveCategory(category: CategoryItem): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        val rawId = category.id.ifBlank { "cat_${category.name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')}_${System.currentTimeMillis() % 1000}" }
        val docId = rawId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(120)
        return runSafe(OperationType.WRITE, "categories/$docId") {
            val ref = db.collection("categories").document(docId)
            val snap = ref.get().await()
            ref.set(
                mapOf(
                    "name" to category.name.trim().take(60),
                    "iconKey" to category.iconKey.ifBlank { "box" }.take(60),
                    "subcategories" to category.subcategories.filter { it.isNotBlank() }.take(30).map { it.trim().take(60) },
                    "active" to category.active,
                    "sortOrder" to category.sortOrder.coerceAtLeast(0L),
                    "createdAt" to (snap.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(uid, email, "SAVE_CATEGORY", "categories", docId, "Saved category ${category.name}")
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        return runSafe(OperationType.DELETE, "categories/$categoryId") {
            db.collection("categories").document(categoryId).delete().await()
            logAdminAction(uid, email, "DELETE_CATEGORY", "categories", categoryId, "Deleted category")
        }
    }

    suspend fun saveVideoContent(video: VideoContentItem): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        val rawId = video.id.ifBlank { "vid_${System.currentTimeMillis()}" }
        val docId = rawId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(120)
        return runSafe(OperationType.WRITE, "videos/$docId") {
            val ref = db.collection("videos").document(docId)
            val snap = ref.get().await()
            ref.set(
                mapOf(
                    "title" to video.title.trim().take(120),
                    "description" to video.description.trim().take(500),
                    "sectionType" to video.sectionType.ifBlank { "HOME_PROMO" }.take(40),
                    "videoUrl" to video.videoUrl.trim().take(600),
                    "youtubeId" to video.youtubeId.trim().take(60),
                    "thumbnailUrl" to video.thumbnailUrl.trim().take(600),
                    "targetCategory" to video.targetCategory.trim().ifBlank { "All" }.take(60),
                    "active" to video.active,
                    "sortOrder" to video.sortOrder.coerceAtLeast(0L),
                    "views" to (if (snap.exists()) (snap.getLong("views") ?: video.views) else video.views).coerceAtLeast(0L),
                    "createdAt" to (snap.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(uid, email, "SAVE_VIDEO", "videos", docId, "Saved video ${video.title}")
        }
    }

    suspend fun deleteVideoContent(videoId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        return runSafe(OperationType.DELETE, "videos/$videoId") {
            db.collection("videos").document(videoId).delete().await()
            logAdminAction(uid, email, "DELETE_VIDEO", "videos", videoId, "Deleted video")
        }
    }

    suspend fun incrementVideoView(videoId: String) {
        if (videoId.isBlank() || auth.currentUser == null) return
        val docId = videoId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        try {
            val ref = db.collection("videos").document(docId)
            if (ref.get().await().exists()) {
                ref.update(mapOf("views" to FieldValue.increment(1L))).await()
            }
        } catch (_: Exception) {
        }
    }

    suspend fun saveAdMobConfig(config: AdMobConfig): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        return runSafe(OperationType.WRITE, "app_config/admob_settings") {
            db.collection("app_config").document("admob_settings").set(
                mapOf(
                    "adsEnabled" to config.adsEnabled,
                    "testMode" to config.testMode,
                    "appId" to config.appId.trim().take(120),
                    "bannerUnitId" to config.bannerUnitId.trim().take(120),
                    "interstitialUnitId" to config.interstitialUnitId.trim().take(120),
                    "rewardedUnitId" to config.rewardedUnitId.trim().take(120),
                    "nativeUnitId" to config.nativeUnitId.trim().take(120),
                    "bannerEnabled" to config.bannerEnabled,
                    "interstitialEnabled" to config.interstitialEnabled,
                    "rewardedEnabled" to config.rewardedEnabled,
                    "nativeEnabled" to config.nativeEnabled,
                    "interstitialFrequency" to config.interstitialFrequency.coerceIn(1L, 30L),
                    "rewardPointsPerAd" to config.rewardPointsPerAd.coerceIn(1L, 500L),
                    "dailyRewardCap" to config.dailyRewardCap.coerceIn(1L, 100L),
                    "placementHome" to config.placementHome,
                    "placementCategories" to config.placementCategories,
                    "placementDeals" to config.placementDeals,
                    "placementProductDetail" to config.placementProductDetail,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(uid, email, "SAVE_ADMOB_CONFIG", "app_config", "admob_settings", "Updated AdMob controls (ads=${config.adsEnabled}, test=${config.testMode})")
        }
    }

    suspend fun submitReview(
        targetId: String,
        targetType: String,
        userName: String,
        rating: Long,
        title: String,
        comment: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val reviewId = "rev_${System.currentTimeMillis()}"
        return runSafe(OperationType.CREATE, "reviews/$reviewId") {
            db.collection("reviews").document(reviewId).set(
                mapOf(
                    "targetId" to targetId.take(128),
                    "targetType" to (if (targetType == "SERVICE") "SERVICE" else "PRODUCT"),
                    "userId" to uid,
                    "userName" to userName.ifBlank { "Customer" }.take(80),
                    "rating" to rating.coerceIn(1L, 5L),
                    "title" to title.trim().take(120),
                    "comment" to comment.trim().take(1000),
                    "verifiedPurchase" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun trackRecentlyViewed(productId: String) {
        val uid = auth.currentUser?.uid ?: return
        if (productId.isBlank()) return
        val docId = productId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(120)
        try {
            db.collection("users").document(uid).collection("recently_viewed").document(docId).set(
                mapOf(
                    "productId" to productId.take(128),
                    "viewedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {
        }
    }

    suspend fun createReturnRequest(
        order: Order,
        customerName: String,
        requestType: String,
        reason: String
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val cleanReason = reason.trim()
        if (cleanReason.length < 3) {
            return Result.failure(IllegalArgumentException("Please enter at least 3 characters describing the reason"))
        }
        val reqId = "ret_${System.currentTimeMillis()}"
        val cleanType = if (requestType in listOf("RETURN", "REPLACEMENT", "REFUND")) requestType else "RETURN"
        return runSafeValue(OperationType.CREATE, "return_requests/$reqId") {
            db.collection("return_requests").document(reqId).set(
                mapOf(
                    "orderId" to order.id.take(128),
                    "userId" to uid,
                    "sellerId" to order.sellerId.take(128),
                    "customerName" to customerName.ifBlank { "Customer" }.take(80),
                    "reason" to cleanReason.take(600),
                    "requestType" to cleanType,
                    "status" to "PENDING",
                    "refundAmount" to order.totalAmount.coerceAtLeast(0.0),
                    "adminNote" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            addNotification(uid, "$cleanType Requested", "Request #$reqId for order #${order.id} submitted.", "ORDER")
            reqId
        }
    }

    suspend fun updateReturnStatus(
        requestId: String,
        status: String,
        adminNote: String,
        targetUid: String = "",
        refundAmount: Double = 0.0
    ): Result<Unit> {
        val actorUid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val actorEmail = auth.currentUser?.email ?: ""
        return runSafe(OperationType.UPDATE, "return_requests/$requestId") {
            db.collection("return_requests").document(requestId).update(
                mapOf(
                    "status" to status,
                    "adminNote" to adminNote.trim().take(400),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            if (status == "REFUNDED" && targetUid.isNotBlank() && refundAmount > 0.0) {
                recordWalletMutation(
                    uid = targetUid,
                    delta = refundAmount,
                    type = "REFUND",
                    title = "Refund for Return #$requestId",
                    referenceId = requestId
                )
                addNotification(targetUid, "Refund Credited ₹${refundAmount.toInt()}", "Refund for #$requestId credited to Hunar Ji wallet.", "WALLET")
            }
            if (actorEmail.isNotBlank()) {
                try {
                    logAdminAction(actorUid, actorEmail, "RETURN_$status", "return_requests", requestId, adminNote.take(300))
                } catch (_: Exception) {
                }
            }
        }
    }

    suspend fun updateSellerShopProfile(
        shopName: String,
        shopDescription: String,
        shopLogoUrl: String,
        shopBannerUrl: String,
        shopHours: String,
        shopReturnPolicy: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return runSafe(OperationType.UPDATE, "users/$uid") {
            db.collection("users").document(uid).update(
                mapOf(
                    "shopName" to shopName.trim().take(120),
                    "shopDescription" to shopDescription.trim().take(600),
                    "shopLogoUrl" to shopLogoUrl.trim().take(500),
                    "shopBannerUrl" to shopBannerUrl.trim().take(500),
                    "shopHours" to shopHours.trim().take(120),
                    "shopReturnPolicy" to shopReturnPolicy.trim().take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun saveApiIntegration(config: ApiIntegrationConfig): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        val docId = (config.id.ifBlank { config.providerKey.ifBlank { "api_${System.currentTimeMillis()}" } })
            .replace(Regex("[^a-zA-Z0-9_-]"), "_").take(120)
        return runSafe(OperationType.WRITE, "api_integrations/$docId") {
            val status = if (!config.enabled) "DISABLED" else if (config.status == "VERIFIED") "VERIFIED" else "CONFIGURED"
            db.collection("api_integrations").document(docId).set(
                mapOf(
                    "providerKey" to config.providerKey.ifBlank { docId }.take(60),
                    "providerName" to config.providerName.trim().take(120),
                    "category" to config.category.trim().take(40),
                    "publicKey" to config.publicKey.trim().take(240),
                    "secretMasked" to config.secretMasked.trim().take(160),
                    "webhookUrl" to config.webhookUrl.trim().take(400),
                    "endpointUrl" to config.endpointUrl.trim().take(400),
                    "mode" to (if (config.mode == "LIVE") "LIVE" else "TEST"),
                    "enabled" to config.enabled,
                    "status" to status,
                    "lastTestedAt" to config.lastTestedAt.take(60),
                    "lastError" to config.lastError.take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(uid, email, "SAVE_API_INTEGRATION", "api_integrations", docId, "${config.providerName} (${config.mode})")
        }
    }

    suspend fun testApiIntegration(config: ApiIntegrationConfig): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        val docId = config.id.ifBlank { config.providerKey }.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(120)
        return runSafeValue(OperationType.UPDATE, "api_integrations/$docId") {
            val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
            val endpoint = config.endpointUrl.trim()
            val (newStatus, detail) = if (endpoint.startsWith("https://", true) || endpoint.startsWith("http://", true)) {
                withContext(Dispatchers.IO) {
                    try {
                        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                            requestMethod = "HEAD"
                            connectTimeout = 4500
                            readTimeout = 4500
                            instanceFollowRedirects = true
                        }
                        val code = conn.responseCode
                        conn.disconnect()
                        if (code in 200..499) {
                            "VERIFIED" to "Reachable (HTTP $code) • ${config.mode}"
                        } else {
                            "ERROR" to "Endpoint returned HTTP $code"
                        }
                    } catch (e: Exception) {
                        "ERROR" to (e.localizedMessage ?: "Connection failed")
                    }
                }
            } else if (endpoint.isNotBlank()) {
                "CONFIGURED" to "Validated endpoint schema ($endpoint)"
            } else {
                "ERROR" to "Missing endpoint URL"
            }

            db.collection("api_integrations").document(docId).set(
                mapOf(
                    "providerKey" to config.providerKey.ifBlank { docId }.take(60),
                    "providerName" to config.providerName.trim().take(120),
                    "category" to config.category.trim().take(40),
                    "publicKey" to config.publicKey.trim().take(240),
                    "secretMasked" to config.secretMasked.trim().take(160),
                    "webhookUrl" to config.webhookUrl.trim().take(400),
                    "endpointUrl" to endpoint.take(400),
                    "mode" to (if (config.mode == "LIVE") "LIVE" else "TEST"),
                    "enabled" to config.enabled,
                    "status" to newStatus,
                    "lastTestedAt" to nowStr,
                    "lastError" to (if (newStatus == "ERROR") detail.take(300) else ""),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(uid, email, "TEST_API_INTEGRATION", "api_integrations", docId, "$newStatus: $detail")
            detail
        }
    }

    suspend fun saveTranslationOverride(languageCode: String, key: String, value: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        val cleanLang = languageCode.trim().lowercase().take(12)
        val cleanKey = key.trim().take(80)
        val docId = "${cleanLang}_${cleanKey}".replace(Regex("[^a-zA-Z0-9_-]"), "_").take(120)
        return runSafe(OperationType.WRITE, "translation_overrides/$docId") {
            db.collection("translation_overrides").document(docId).set(
                mapOf(
                    "languageCode" to cleanLang,
                    "key" to cleanKey,
                    "value" to value.trim().take(500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(uid, email, "SAVE_TRANSLATION", "translation_overrides", docId, "$cleanLang.$cleanKey")
        }
    }

    suspend fun saveCoupon(coupon: Coupon): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        val docId = coupon.id.ifBlank { "coup_${System.currentTimeMillis()}" }
        return runSafe(OperationType.WRITE, "coupons/$docId") {
            val payload = mapOf(
                "code" to coupon.code.trim().uppercase().take(32),
                "title" to coupon.title.trim().take(120),
                "discountPercent" to coupon.discountPercent.coerceIn(1L, 95L),
                "maxDiscount" to coupon.maxDiscount.coerceAtLeast(0.0),
                "minOrderAmount" to coupon.minOrderAmount.coerceAtLeast(0.0),
                "active" to coupon.active,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("coupons").document(docId).set(payload).await()
            logAdminAction(uid, email, "SAVE_COUPON", "coupons", docId, "Saved coupon ${coupon.code}")
        }
    }

    suspend fun saveLocation(location: ServiceLocation): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        val docId = location.id.ifBlank { "loc_${System.currentTimeMillis()}" }
        return runSafe(OperationType.WRITE, "locations/$docId") {
            val payload = mapOf(
                "state" to location.state.trim().take(80),
                "district" to location.district.trim().take(80),
                "city" to location.city.trim().take(80),
                "pincode" to location.pincode.trim().take(12),
                "serviceAvailable" to location.serviceAvailable,
                "deliveryAvailable" to location.deliveryAvailable,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("locations").document(docId).set(payload).await()
            logAdminAction(uid, email, "SAVE_LOCATION", "locations", docId, "${location.district} (${location.pincode})")
        }
    }

    suspend fun updateWithdrawalStatus(withdrawalId: String, status: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        return runSafe(OperationType.UPDATE, "withdrawals/$withdrawalId") {
            db.collection("withdrawals").document(withdrawalId).update(
                mapOf("status" to status, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            logAdminAction(uid, email, "WITHDRAWAL_$status", "withdrawals", withdrawalId, "Marked withdrawal $status")
        }
    }

    suspend fun replySupportTicket(ticketId: String, status: String, reply: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        return runSafe(OperationType.UPDATE, "support_tickets/$ticketId") {
            db.collection("support_tickets").document(ticketId).update(
                mapOf(
                    "status" to status,
                    "adminReply" to reply.trim().take(1000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(uid, email, "REPLY_TICKET", "support_tickets", ticketId, "Status -> $status")
        }
    }

    suspend fun seedStarterCatalogAsAdmin(): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val email = auth.currentUser?.email ?: ""
        return runSafe(OperationType.WRITE, "catalog_seed") {
            StarterCatalog.defaultCategories.forEach { cat ->
                db.collection("categories").document(cat.id).set(
                    mapOf(
                        "name" to cat.name,
                        "iconKey" to cat.iconKey,
                        "subcategories" to cat.subcategories,
                        "active" to cat.active,
                        "sortOrder" to cat.sortOrder,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            StarterCatalog.defaultVideos.forEach { vid ->
                db.collection("videos").document(vid.id).set(
                    mapOf(
                        "title" to vid.title,
                        "description" to vid.description,
                        "sectionType" to vid.sectionType,
                        "videoUrl" to vid.videoUrl,
                        "youtubeId" to vid.youtubeId,
                        "thumbnailUrl" to vid.thumbnailUrl,
                        "targetCategory" to vid.targetCategory,
                        "active" to vid.active,
                        "sortOrder" to vid.sortOrder,
                        "views" to vid.views,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            StarterCatalog.defaultBanners.forEach { b ->
                db.collection("banners").document(b.id).set(
                    mapOf(
                        "title" to b.title,
                        "subtitle" to b.subtitle,
                        "ctaText" to b.ctaText,
                        "targetCategory" to b.targetCategory,
                        "description" to b.description,
                        "imageUrl" to b.imageUrl,
                        "videoUrl" to b.videoUrl,
                        "linkUrl" to b.linkUrl,
                        "targetApp" to b.targetApp,
                        "placement" to b.placement,
                        "targetProductId" to b.targetProductId,
                        "startDate" to b.startDate,
                        "endDate" to b.endDate,
                        "impressions" to b.impressions,
                        "clicks" to b.clicks,
                        "active" to b.active,
                        "sortOrder" to b.sortOrder,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            StarterCatalog.defaultCoupons.forEach { c ->
                db.collection("coupons").document(c.id).set(
                    mapOf(
                        "code" to c.code,
                        "title" to c.title,
                        "discountPercent" to c.discountPercent,
                        "maxDiscount" to c.maxDiscount,
                        "minOrderAmount" to c.minOrderAmount,
                        "active" to c.active,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            StarterCatalog.defaultLocations.forEach { l ->
                db.collection("locations").document(l.id).set(
                    mapOf(
                        "state" to l.state,
                        "district" to l.district,
                        "city" to l.city,
                        "pincode" to l.pincode,
                        "serviceAvailable" to l.serviceAvailable,
                        "deliveryAvailable" to l.deliveryAvailable,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            StarterCatalog.defaultProducts.forEach { p ->
                db.collection("products").document(p.id).set(
                    mapOf(
                        "sellerId" to uid,
                        "sellerName" to p.sellerName,
                        "title" to p.title,
                        "category" to p.category,
                        "subcategory" to p.subcategory,
                        "description" to p.description,
                        "price" to p.price,
                        "originalPrice" to p.originalPrice,
                        "discountPercent" to p.discountPercent,
                        "stock" to p.stock,
                        "rating" to p.rating,
                        "reviewCount" to p.reviewCount,
                        "iconKey" to p.iconKey,
                        "approved" to p.approved,
                        "featured" to p.featured,
                        "trending" to p.trending,
                        "active" to p.active,
                        "district" to p.district,
                        "mediaGallery" to serializeMediaGallery(p.mediaGallery),
                        "brand" to p.brand,
                        "modelNumber" to p.modelNumber,
                        "sku" to p.sku,
                        "weight" to p.weight,
                        "dimensions" to p.dimensions,
                        "material" to p.material,
                        "manufacturer" to p.manufacturer,
                        "countryOfOrigin" to p.countryOfOrigin,
                        "packageContents" to p.packageContents,
                        "specs" to p.specs,
                        "highlights" to p.highlights,
                        "features" to p.features,
                        "benefits" to p.benefits,
                        "usageInstructions" to p.usageInstructions,
                        "installationInstructions" to p.installationInstructions,
                        "safetyInfo" to p.safetyInfo,
                        "faqs" to p.faqs,
                        "colorOptions" to p.colorOptions,
                        "sizeOptions" to p.sizeOptions,
                        "stepByStepSections" to serializeGuideSteps(p.stepByStepSections),
                        "warrantyAvailable" to p.warrantyAvailable,
                        "warrantyDuration" to p.warrantyDuration,
                        "warrantyType" to p.warrantyType,
                        "warrantyConditions" to p.warrantyConditions,
                        "warrantyServiceCentre" to p.warrantyServiceCentre,
                        "warrantyRegistrationSteps" to p.warrantyRegistrationSteps,
                        "returnPolicy" to p.returnPolicy,
                        "replacementPolicy" to p.replacementPolicy,
                        "warrantyDocs" to serializeWarrantyDocs(p.warrantyDocs),
                        "soldCount" to p.soldCount,
                        "viewCount" to p.viewCount,
                        "newArrival" to p.newArrival,
                        "codAvailable" to p.codAvailable,
                        "estimatedDeliveryDays" to p.estimatedDeliveryDays,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            StarterCatalog.defaultServices.forEach { s ->
                db.collection("services").document(s.id).set(
                    mapOf(
                        "providerId" to uid,
                        "providerName" to s.providerName,
                        "title" to s.title,
                        "category" to s.category,
                        "description" to s.description,
                        "price" to s.price,
                        "durationMinutes" to s.durationMinutes,
                        "rating" to s.rating,
                        "iconKey" to s.iconKey,
                        "approved" to s.approved,
                        "active" to s.active,
                        "district" to s.district,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            StarterCatalog.defaultApiIntegrations.forEach { api ->
                db.collection("api_integrations").document(api.id).set(
                    mapOf(
                        "providerKey" to api.providerKey,
                        "providerName" to api.providerName,
                        "category" to api.category,
                        "publicKey" to api.publicKey,
                        "secretMasked" to api.secretMasked,
                        "webhookUrl" to api.webhookUrl,
                        "endpointUrl" to api.endpointUrl,
                        "mode" to api.mode,
                        "enabled" to api.enabled,
                        "status" to api.status,
                        "lastTestedAt" to api.lastTestedAt,
                        "lastError" to api.lastError,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            val admob = AdMobConfig()
            db.collection("app_config").document("admob_settings").set(
                mapOf(
                    "adsEnabled" to admob.adsEnabled,
                    "testMode" to admob.testMode,
                    "appId" to admob.appId,
                    "bannerUnitId" to admob.bannerUnitId,
                    "interstitialUnitId" to admob.interstitialUnitId,
                    "rewardedUnitId" to admob.rewardedUnitId,
                    "nativeUnitId" to admob.nativeUnitId,
                    "bannerEnabled" to admob.bannerEnabled,
                    "interstitialEnabled" to admob.interstitialEnabled,
                    "rewardedEnabled" to admob.rewardedEnabled,
                    "nativeEnabled" to admob.nativeEnabled,
                    "interstitialFrequency" to admob.interstitialFrequency,
                    "rewardPointsPerAd" to admob.rewardPointsPerAd,
                    "dailyRewardCap" to admob.dailyRewardCap,
                    "placementHome" to admob.placementHome,
                    "placementCategories" to admob.placementCategories,
                    "placementDeals" to admob.placementDeals,
                    "placementProductDetail" to admob.placementProductDetail,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(uid, email, "SEED_MARKETPLACE", "catalog", "starter", "Initialized starter marketplace catalog, categories, videos, AdMob, and integrations")
        }
    }

    // --- MULTI-ROLE REGISTRATION, CREDENTIALS & KYC SYSTEM ---

    suspend fun generateUniqueHunarUserId(role: String): String {
        val normalizedRole = role.uppercase().let {
            if (it in listOf("CUSTOMER", "MECHANIC", "SELLER", "DELIVERY")) it else "CUSTOMER"
        }
        val counterRef = db.collection("counters").document("reg_$normalizedRole")
        val seq = db.runTransaction { tx ->
            val snap = tx.get(counterRef)
            if (!snap.exists()) {
                tx.set(counterRef, mapOf("nextSeq" to 100001L))
                100001L
            } else {
                val next = (snap.getLong("nextSeq") ?: 100000L) + 1L
                tx.update(counterRef, mapOf("nextSeq" to next))
                next
            }
        }.await()
        return "HJ-${KycCatalogConstants.rolePrefix(normalizedRole)}-$seq"
    }

    suspend fun submitMultiRoleRegistration(
        application: KycApplication,
        rawPassword: String,
        docRecords: List<KycDocumentRecord>,
        termsAccepted: Boolean
    ): Result<KycApplication> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Sign in required before submitting registration"))
        val email = (application.email.ifBlank { auth.currentUser?.email ?: "" }).trim().take(160)
        val role = application.role.uppercase().let {
            if (it in listOf("CUSTOMER", "MECHANIC", "SELLER", "DELIVERY")) it else "CUSTOMER"
        }
        return runSafeValue(OperationType.WRITE, "kyc_applications/$uid") {
            val userRef = db.collection("users").document(uid)
            val userSnap = userRef.get().await()
            val kycRef = db.collection("kyc_applications").document(uid)
            val kycSnap = kycRef.get().await()

            val existingRole = userSnap.getString("role") ?: ""
            val existingHunarId = userSnap.getString("hunarUserId") ?: ""
            val hunarUserId = if (existingHunarId.isNotBlank() && existingRole.equals(role, true)) {
                existingHunarId
            } else {
                generateUniqueHunarUserId(role)
            }

            val existingHash = userSnap.getString("passwordHash") ?: ""
            val passwordHash = if (rawPassword.isNotBlank()) {
                CredentialSecurityHelper.hashPassword(rawPassword)
            } else {
                existingHash
            }

            // Per zero-trust rules, self-submitted status is SUBMITTED (Customer) or PENDING (Worker)
            val submittedStatus = if (role == "CUSTOMER") "SUBMITTED" else "PENDING"
            val summarySkills = if (role == "MECHANIC") {
                (listOf(application.mainDepartment) + application.departments + application.specificSkills)
                    .filter { it.isNotBlank() }.distinct().joinToString(", ").take(200)
            } else ""
            val summaryVehicle = if (role == "DELIVERY") {
                listOf(application.vehicleType, application.vehicleBrand, application.vehicleModel, application.vehicleRegNumber)
                    .filter { it.isNotBlank() }.joinToString(" • ").take(120)
            } else ""

            if (!userSnap.exists()) {
                val cleanName = application.fullName.trim().ifBlank { "Hunar Ji User" }.take(80)
                val refCode = "HJ" + uid.take(6).uppercase()
                userRef.set(
                    mapOf(
                        "userId" to uid,
                        "displayName" to cleanName,
                        "email" to email,
                        "phone" to application.mobile.trim().take(24),
                        "role" to role,
                        "walletBalance" to 0.0,
                        "loyaltyPoints" to 50L,
                        "verified" to false,
                        "online" to true,
                        "blocked" to false,
                        "city" to application.city.trim().ifBlank { "Patna" }.take(80),
                        "district" to application.district.trim().ifBlank { "Patna" }.take(80),
                        "state" to application.state.trim().ifBlank { "Bihar" }.take(80),
                        "referralCode" to refCode,
                        "shopName" to application.shopName.trim().take(120),
                        "vehicleInfo" to summaryVehicle,
                        "skills" to summarySkills,
                        "hunarUserId" to hunarUserId,
                        "kycStatus" to submittedStatus,
                        "accountStatus" to "ACTIVE",
                        "dob" to application.dob.trim().take(32),
                        "gender" to application.gender.trim().take(32),
                        "profilePhotoUri" to application.profilePhotoName.trim().take(300),
                        "passwordHash" to passwordHash,
                        "termsAccepted" to termsAccepted,
                        "kycConsentAccepted" to application.kycConsentAccepted,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                userRef.update(
                    mapOf(
                        "displayName" to application.fullName.trim().ifBlank { userSnap.getString("displayName") ?: "Hunar Ji User" }.take(80),
                        "email" to email,
                        "phone" to application.mobile.trim().take(24),
                        "role" to role,
                        "hunarUserId" to hunarUserId,
                        "kycStatus" to submittedStatus,
                        "city" to application.city.trim().ifBlank { "Patna" }.take(80),
                        "district" to application.district.trim().ifBlank { "Patna" }.take(80),
                        "state" to application.state.trim().ifBlank { "Bihar" }.take(80),
                        "shopName" to application.shopName.trim().take(120),
                        "vehicleInfo" to summaryVehicle,
                        "skills" to summarySkills,
                        "dob" to application.dob.trim().take(32),
                        "gender" to application.gender.trim().take(32),
                        "profilePhotoUri" to application.profilePhotoName.trim().take(300),
                        "passwordHash" to passwordHash,
                        "termsAccepted" to termsAccepted,
                        "kycConsentAccepted" to application.kycConsentAccepted,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            val existingCreatedAt = kycSnap.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()
            val prevStatus = kycSnap.getString("kycStatus") ?: "NOT_SUBMITTED"
            val existingAadhaarVerified = kycSnap.getBoolean("aadhaarVerified") ?: false
            val existingPanVerified = kycSnap.getBoolean("panVerified") ?: false
            val existingAddressVerified = kycSnap.getBoolean("addressVerified") ?: false
            val existingBankVerified = kycSnap.getBoolean("bankVerified") ?: false
            val existingSkillVerified = kycSnap.getBoolean("skillVerified") ?: false
            val existingShopAddressVerified = kycSnap.getBoolean("shopAddressVerified") ?: false
            val existingGstVerified = kycSnap.getBoolean("gstVerified") ?: false
            val existingDocsVerified = kycSnap.getBoolean("documentsVerified") ?: false
            val existingDlVerified = kycSnap.getBoolean("drivingLicenceVerified") ?: false
            val existingVehicleVerified = kycSnap.getBoolean("vehicleDocVerified") ?: false

            val kycPayload = hashMapOf<String, Any>(
                "userId" to uid,
                "hunarUserId" to hunarUserId,
                "role" to role,
                "fullName" to application.fullName.trim().ifBlank { "User" }.take(80),
                "mobile" to application.mobile.trim().take(20),
                "email" to email,
                "dob" to application.dob.trim().take(32),
                "gender" to application.gender.trim().take(32),
                "profilePhotoName" to application.profilePhotoName.trim().take(180),
                "kycStatus" to submittedStatus,
                "accountStatus" to (kycSnap.getString("accountStatus") ?: "ACTIVE"),
                "houseNumber" to application.houseNumber.trim().take(80),
                "streetRoad" to application.streetRoad.trim().take(120),
                "villageTown" to application.villageTown.trim().take(120),
                "areaLocality" to application.areaLocality.trim().take(120),
                "landmark" to application.landmark.trim().take(120),
                "state" to application.state.trim().take(80),
                "district" to application.district.trim().take(80),
                "city" to application.city.trim().take(80),
                "pincode" to application.pincode.trim().take(12),
                "gpsLocation" to application.gpsLocation.trim().take(120),
                "fullAddress" to application.fullAddress.trim().take(250),
                "aadhaarMasked" to application.aadhaarMasked.take(24),
                "aadhaarVerified" to existingAadhaarVerified,
                "panNumberMasked" to application.panNumberMasked.take(24),
                "panVerified" to existingPanVerified,
                "addressVerified" to existingAddressVerified,
                "bankHolderName" to application.bankHolderName.trim().take(80),
                "bankName" to application.bankName.trim().take(80),
                "bankAccountMasked" to application.bankAccountMasked.take(32),
                "ifscCode" to application.ifscCode.trim().uppercase().take(16),
                "bankVerified" to existingBankVerified,
                "mainDepartment" to application.mainDepartment.trim().take(80),
                "departments" to application.departments.take(18),
                "specialization" to application.specialization.trim().take(120),
                "specificSkills" to application.specificSkills.take(20),
                "experienceYears" to application.experienceYears.coerceIn(0L, 70L),
                "previousWorkExperience" to application.previousWorkExperience.trim().take(500),
                "certificateName" to application.certificateName.trim().take(120),
                "serviceDescription" to application.serviceDescription.trim().take(500),
                "serviceCharges" to application.serviceCharges.coerceAtLeast(0.0),
                "workingHours" to application.workingHours.trim().take(80),
                "availableDays" to application.availableDays.take(7),
                "emergencyAvailable" to application.emergencyAvailable,
                "serviceAreas" to application.serviceAreas.trim().take(200),
                "serviceRadiusKm" to application.serviceRadiusKm.coerceIn(1L, 200L),
                "preferredWorkingArea" to application.preferredWorkingArea.trim().take(120),
                "skillVerified" to existingSkillVerified,
                "shopName" to application.shopName.trim().take(120),
                "businessName" to application.businessName.trim().take(120),
                "businessType" to application.businessType.trim().take(80),
                "shopCategory" to application.shopCategory.trim().take(80),
                "shopDescription" to application.shopDescription.trim().take(500),
                "shopContactNumber" to application.shopContactNumber.trim().take(20),
                "businessEmail" to application.businessEmail.trim().take(160),
                "shopOpeningTime" to application.shopOpeningTime.trim().take(32),
                "shopClosingTime" to application.shopClosingTime.trim().take(32),
                "weeklyClosingDay" to application.weeklyClosingDay.trim().take(32),
                "shopAddressLine" to application.shopAddressLine.trim().take(250),
                "shopState" to application.shopState.trim().take(80),
                "shopDistrict" to application.shopDistrict.trim().take(80),
                "shopCity" to application.shopCity.trim().take(80),
                "shopPincode" to application.shopPincode.trim().take(12),
                "shopGpsLocation" to application.shopGpsLocation.trim().take(120),
                "shopAddressVerified" to existingShopAddressVerified,
                "gstRequired" to application.gstRequired,
                "gstin" to application.gstin.trim().uppercase().take(20),
                "gstVerified" to existingGstVerified,
                "productCategories" to application.productCategories.take(15),
                "productsSold" to application.productsSold.trim().take(250),
                "brandInfo" to application.brandInfo.trim().take(160),
                "wholesaleRetail" to application.wholesaleRetail.trim().take(32),
                "deliveryAvailable" to application.deliveryAvailable,
                "shopPickupAvailable" to application.shopPickupAvailable,
                "returnPolicy" to application.returnPolicy.trim().take(120),
                "documentsVerified" to existingDocsVerified,
                "vehicleType" to application.vehicleType.trim().take(60),
                "vehicleBrand" to application.vehicleBrand.trim().take(60),
                "vehicleModel" to application.vehicleModel.trim().take(60),
                "vehicleRegNumber" to application.vehicleRegNumber.trim().uppercase().take(32),
                "vehicleColor" to application.vehicleColor.trim().take(40),
                "drivingLicenceNumber" to application.drivingLicenceNumber.trim().uppercase().take(32),
                "drivingLicenceVerified" to existingDlVerified,
                "vehicleDocVerified" to existingVehicleVerified,
                "preferredDeliveryArea" to application.preferredDeliveryArea.trim().take(120),
                "preferredDistrict" to application.preferredDistrict.trim().take(80),
                "preferredCity" to application.preferredCity.trim().take(80),
                "deliveryWorkingHours" to application.deliveryWorkingHours.trim().take(80),
                "employmentType" to application.employmentType.trim().take(40),
                "adminNotes" to (kycSnap.getString("adminNotes") ?: ""),
                "reviewedBy" to (kycSnap.getString("reviewedBy") ?: ""),
                "kycConsentAccepted" to application.kycConsentAccepted,
                "mobileOtpVerified" to application.mobileOtpVerified,
                "emailVerified" to (auth.currentUser?.isEmailVerified ?: false),
                "createdAt" to existingCreatedAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            kycRef.set(kycPayload).await()

            docRecords.forEach { doc ->
                val docId = doc.docType.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "doc_${System.currentTimeMillis()}" }
                val docRef = kycRef.collection("documents").document(docId)
                val docSnap = docRef.get().await()
                docRef.set(
                    mapOf(
                        "docType" to docId,
                        "docLabel" to doc.docLabel.take(100),
                        "fileName" to doc.fileName.take(180),
                        "mimeType" to doc.mimeType.take(80),
                        "byteSize" to doc.byteSize.coerceIn(0L, 52428800L),
                        "localPreviewPath" to doc.localPreviewPath.take(400),
                        "status" to "SUBMITTED",
                        "adminNote" to "",
                        "createdAt" to (docSnap.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            if (role in listOf("MECHANIC", "SELLER", "DELIVERY")) {
                val provRef = db.collection("providers").document(uid)
                val provSnap = provRef.get().await()
                val provName = application.fullName.trim().ifBlank { "Hunar Ji Partner" }.take(80)
                if (!provSnap.exists()) {
                    provRef.set(
                        mapOf(
                            "userId" to uid,
                            "name" to provName,
                            "providerType" to role,
                            "shopName" to application.shopName.trim().take(120),
                            "skills" to summarySkills.ifBlank { application.vehicleType.take(120) },
                            "experienceYears" to application.experienceYears.coerceIn(0L, 70L),
                            "rating" to 4.8,
                            "completedJobs" to 0L,
                            "city" to application.city.trim().ifBlank { "Patna" }.take(80),
                            "district" to application.district.trim().ifBlank { "Patna" }.take(80),
                            "state" to application.state.trim().ifBlank { "Bihar" }.take(80),
                            "online" to true,
                            "verified" to false,
                            "approved" to false,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } else {
                    provRef.update(
                        mapOf(
                            "name" to provName,
                            "shopName" to application.shopName.trim().take(120),
                            "skills" to summarySkills.ifBlank { application.vehicleType.take(120) },
                            "experienceYears" to application.experienceYears.coerceIn(0L, 70L),
                            "city" to application.city.trim().ifBlank { "Patna" }.take(80),
                            "district" to application.district.trim().ifBlank { "Patna" }.take(80),
                            "state" to application.state.trim().ifBlank { "Bihar" }.take(80),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }

            val histAction = if (kycSnap.exists()) "RESUBMITTED" else "SUBMITTED"
            val histId = "hist_${System.currentTimeMillis()}"
            kycRef.collection("verification_history").document(histId).set(
                mapOf(
                    "actorId" to uid,
                    "actorEmail" to email,
                    "action" to histAction,
                    "previousStatus" to prevStatus,
                    "newStatus" to submittedStatus,
                    "note" to "Role registration submitted ($role • $hunarUserId) with ${docRecords.size} document(s).",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            addNotification(
                uid = uid,
                title = "Registration Submitted ($hunarUserId)",
                message = if (role == "CUSTOMER") {
                    "Welcome to Hunar Ji! Your Customer ID is $hunarUserId."
                } else {
                    "Your $role KYC ($hunarUserId) is pending Admin review."
                },
                category = "KYC"
            )

            application.copy(
                id = uid,
                userId = uid,
                hunarUserId = hunarUserId,
                role = role,
                email = email,
                kycStatus = submittedStatus
            )
        }
    }

    suspend fun saveOrUpdateKycDocument(uid: String, doc: KycDocumentRecord): Result<Unit> {
        val actorUid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = doc.docType.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "doc_${System.currentTimeMillis()}" }
        return runSafe(OperationType.WRITE, "kyc_applications/$uid/documents/$docId") {
            val docRef = db.collection("kyc_applications").document(uid).collection("documents").document(docId)
            val snap = docRef.get().await()
            docRef.set(
                mapOf(
                    "docType" to docId,
                    "docLabel" to doc.docLabel.take(100),
                    "fileName" to doc.fileName.take(180),
                    "mimeType" to doc.mimeType.take(80),
                    "byteSize" to doc.byteSize.coerceIn(0L, 52428800L),
                    "localPreviewPath" to doc.localPreviewPath.take(400),
                    "status" to "SUBMITTED",
                    "adminNote" to "",
                    "createdAt" to (snap.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            db.collection("kyc_applications").document(uid).collection("verification_history")
                .document("hist_${System.currentTimeMillis()}").set(
                    mapOf(
                        "actorId" to actorUid,
                        "actorEmail" to (auth.currentUser?.email ?: ""),
                        "action" to "RESUBMITTED",
                        "previousStatus" to "",
                        "newStatus" to "SUBMITTED",
                        "note" to "Uploaded/replaced document: ${doc.docLabel}",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
        }
    }

    suspend fun deleteKycDocument(uid: String, docType: String, localPath: String): Result<Unit> {
        val actorUid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = docType.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return runSafe(OperationType.DELETE, "kyc_applications/$uid/documents/$docId") {
            PrivateKycStorage.deletePrivateDoc(localPath)
            db.collection("kyc_applications").document(uid).collection("documents").document(docId).delete().await()
            db.collection("kyc_applications").document(uid).collection("verification_history")
                .document("hist_${System.currentTimeMillis()}").set(
                    mapOf(
                        "actorId" to actorUid,
                        "actorEmail" to (auth.currentUser?.email ?: ""),
                        "action" to "DOC_REMOVED",
                        "previousStatus" to "",
                        "newStatus" to "PENDING",
                        "note" to "Removed document: $docId",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
        }
    }

    suspend fun verifyLocalCredentials(identifier: String, password: String): Result<UserProfile> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Sign in with Google first"))
        return runSafeValue(OperationType.GET, "users/$uid") {
            val snap = db.collection("users").document(uid).get().await()
            val profile = snap.toObject(UserProfile::class.java, est())
                ?: throw IllegalStateException("No registered profile found. Please register your account first.")
            if (profile.blocked || profile.accountStatus == "SUSPENDED") {
                throw IllegalStateException("Account suspended by Admin. Contact support@hunarji.in.")
            }
            val cleanInput = identifier.trim()
            val inputDigits = cleanInput.filter(Char::isDigit).takeLast(10)
            val storedDigits = profile.phone.filter(Char::isDigit).takeLast(10)
            val idMatches = cleanInput.isBlank() ||
                cleanInput.equals(profile.hunarUserId, ignoreCase = true) ||
                cleanInput.equals(profile.email, ignoreCase = true) ||
                (inputDigits.length >= 6 && inputDigits == storedDigits)
            if (!idMatches) {
                throw IllegalArgumentException("User ID or Mobile Number does not match your registered profile (${profile.hunarUserId.ifBlank { profile.phone }})")
            }
            if (profile.passwordHash.isBlank()) {
                throw IllegalStateException("No password configured yet. Please complete Registration or use Reset Password via OTP.")
            }
            if (!CredentialSecurityHelper.verifyPassword(password, profile.passwordHash)) {
                throw IllegalArgumentException("Incorrect password. Use Forgot Password? to reset via OTP.")
            }
            profile
        }
    }

    suspend fun resetPasswordWithOtp(identifier: String, newPassword: String): Result<UserProfile> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Sign in with Google first"))
        return runSafeValue(OperationType.UPDATE, "users/$uid") {
            val userRef = db.collection("users").document(uid)
            val snap = userRef.get().await()
            val profile = snap.toObject(UserProfile::class.java, est())
                ?: throw IllegalStateException("Profile not found")
            val newHash = CredentialSecurityHelper.hashPassword(newPassword)
            userRef.update(
                mapOf(
                    "passwordHash" to newHash,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            profile.copy(passwordHash = newHash)
        }
    }

    suspend fun adminUpdateKycStatus(
        targetUid: String,
        newKycStatus: String,
        accountStatus: String,
        verificationFlags: Map<String, Boolean>,
        adminNotes: String
    ): Result<Unit> {
        val adminUid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val adminEmail = auth.currentUser?.email ?: ""
        return runSafe(OperationType.UPDATE, "kyc_applications/$targetUid") {
            val kycRef = db.collection("kyc_applications").document(targetUid)
            val kycSnap = kycRef.get().await()
            val prevStatus = kycSnap.getString("kycStatus") ?: "PENDING"
            val isApproved = newKycStatus == "APPROVED" && accountStatus == "ACTIVE"

            val kycUpdates = mutableMapOf<String, Any>(
                "kycStatus" to newKycStatus,
                "accountStatus" to accountStatus,
                "adminNotes" to adminNotes.trim().take(1000),
                "reviewedBy" to adminEmail.take(160),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            verificationFlags.forEach { (k, v) -> kycUpdates[k] = v }
            kycRef.update(kycUpdates).await()

            val userRef = db.collection("users").document(targetUid)
            if (userRef.get().await().exists()) {
                userRef.update(
                    mapOf(
                        "kycStatus" to newKycStatus,
                        "accountStatus" to accountStatus,
                        "verified" to isApproved,
                        "blocked" to (accountStatus == "SUSPENDED"),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            val provRef = db.collection("providers").document(targetUid)
            if (provRef.get().await().exists()) {
                provRef.update(
                    mapOf(
                        "verified" to isApproved,
                        "approved" to isApproved,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            kycRef.collection("verification_history").document("hist_${System.currentTimeMillis()}").set(
                mapOf(
                    "actorId" to adminUid,
                    "actorEmail" to adminEmail,
                    "action" to "ADMIN_$newKycStatus",
                    "previousStatus" to prevStatus,
                    "newStatus" to newKycStatus,
                    "note" to adminNotes.trim().ifBlank { "Status updated to $newKycStatus ($accountStatus)" }.take(600),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            addNotification(
                uid = targetUid,
                title = "KYC Status: $newKycStatus",
                message = adminNotes.trim().ifBlank { "Your Hunar Ji KYC verification status is now $newKycStatus." },
                category = "KYC"
            )
            logAdminAction(adminUid, adminEmail, "KYC_$newKycStatus", "kyc_applications", targetUid, "Status $prevStatus -> $newKycStatus | $accountStatus")
        }
    }

    suspend fun adminUpdateDocumentStatus(
        targetUid: String,
        docId: String,
        status: String,
        adminNote: String
    ): Result<Unit> {
        val adminUid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val adminEmail = auth.currentUser?.email ?: ""
        return runSafe(OperationType.UPDATE, "kyc_applications/$targetUid/documents/$docId") {
            db.collection("kyc_applications").document(targetUid).collection("documents").document(docId)
                .update(
                    mapOf(
                        "status" to status,
                        "adminNote" to adminNote.trim().take(500),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            logAdminAction(adminUid, adminEmail, "KYC_DOC_$status", "documents", "$targetUid/$docId", adminNote.take(300))
        }
    }

    suspend fun logKycDocumentAccess(targetUid: String, docId: String, docLabel: String): Result<Unit> {
        val adminUid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val adminEmail = auth.currentUser?.email ?: ""
        return runSafe(OperationType.CREATE, "kyc_applications/$targetUid/verification_history") {
            db.collection("kyc_applications").document(targetUid).collection("verification_history")
                .document("hist_${System.currentTimeMillis()}").set(
                    mapOf(
                        "actorId" to adminUid,
                        "actorEmail" to adminEmail,
                        "action" to "VIEWED_DOCUMENT",
                        "previousStatus" to "",
                        "newStatus" to "UNDER_REVIEW",
                        "note" to "Admin inspected $docLabel ($docId)",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            logAdminAction(adminUid, adminEmail, "VIEWED_KYC_DOCUMENT", "kyc_applications", targetUid, "Inspected $docLabel ($docId)")
        }
    }

    suspend fun deleteUserAccountAndKyc(context: Context, targetUid: String): Result<Unit> {
        return runSafe(OperationType.DELETE, "users/$targetUid") {
            PrivateKycStorage.clearAllUserPrivateDocs(context, targetUid)
            val kycRef = db.collection("kyc_applications").document(targetUid)
            val docsSnap = kycRef.collection("documents").get().await()
            docsSnap.documents.forEach { it.reference.delete().await() }
            if (kycRef.get().await().exists()) {
                kycRef.delete().await()
            }
            val provRef = db.collection("providers").document(targetUid)
            if (provRef.get().await().exists()) {
                provRef.delete().await()
            }
            val userRef = db.collection("users").document(targetUid)
            if (userRef.get().await().exists()) {
                userRef.delete().await()
            }
        }
    }

    private suspend fun logAdminAction(
        actorId: String,
        actorEmail: String,
        action: String,
        targetType: String,
        targetId: String,
        details: String
    ) {
        val logId = "log_${System.currentTimeMillis()}"
        db.collection("audit_logs").document(logId).set(
            mapOf(
                "actorId" to actorId,
                "actorEmail" to actorEmail.take(160),
                "action" to action.take(120),
                "targetType" to targetType.take(60),
                "targetId" to targetId.take(128),
                "details" to details.take(500),
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    private suspend inline fun runSafe(
        op: OperationType,
        path: String,
        crossinline block: suspend () -> Unit
    ): Result<Unit> = try {
        block()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, op, path)
        Result.failure(e)
    }

    private suspend inline fun <T> runSafeValue(
        op: OperationType,
        path: String,
        crossinline block: suspend () -> T
    ): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, op, path)
        Result.failure(e)
    }
}
