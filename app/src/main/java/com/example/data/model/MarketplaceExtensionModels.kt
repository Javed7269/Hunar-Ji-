package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class ProductMediaItem(
    val id: String = "",
    val type: String = "IMAGE", // IMAGE or VIDEO
    val angleTag: String = "Front",
    val uriOrUrl: String = "",
    val thumbnailUrl: String = "",
    val caption: String = "",
    val sortOrder: Long = 0L,
    val youtubeVideo: Boolean = false
)

@Keep
@IgnoreExtraProperties
data class ProductGuideStep(
    val stepNumber: Long = 1L,
    val heading: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val instructions: String = ""
)

@Keep
@IgnoreExtraProperties
data class ProductWarrantyDoc(
    val id: String = "",
    val label: String = "",
    val docType: String = "WARRANTY_CARD", // WARRANTY_CARD, MANUAL, CERTIFICATE
    val fileName: String = "",
    val mimeType: String = "application/pdf",
    val uriOrUrl: String = ""
)

@Keep
@IgnoreExtraProperties
data class CategoryItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val iconKey: String = "box",
    val subcategories: List<String> = emptyList(),
    val active: Boolean = true,
    val sortOrder: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class VideoContentItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val sectionType: String = "HOME_PROMO", // HOME_PROMO, FEATURED, SERVICE, LIVE_SERVICE, PRODUCT_PROMO, TUTORIAL, TRAINING, ADVERTISEMENT
    val videoUrl: String = "",
    val youtubeId: String = "",
    val thumbnailUrl: String = "",
    val targetCategory: String = "All",
    val active: Boolean = true,
    val sortOrder: Long = 0L,
    val views: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdMobConfig(
    @DocumentId val id: String = "admob_settings",
    val adsEnabled: Boolean = true,
    val testMode: Boolean = true,
    val appId: String = TEST_APP_ID,
    val bannerUnitId: String = TEST_BANNER_UNIT_ID,
    val interstitialUnitId: String = TEST_INTERSTITIAL_UNIT_ID,
    val rewardedUnitId: String = TEST_REWARDED_UNIT_ID,
    val nativeUnitId: String = TEST_NATIVE_UNIT_ID,
    val bannerEnabled: Boolean = true,
    val interstitialEnabled: Boolean = false,
    val rewardedEnabled: Boolean = true,
    val nativeEnabled: Boolean = true,
    val interstitialFrequency: Long = 5L,
    val rewardPointsPerAd: Long = 25L,
    val dailyRewardCap: Long = 5L,
    val placementHome: Boolean = true,
    val placementCategories: Boolean = true,
    val placementDeals: Boolean = true,
    val placementProductDetail: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun effectiveBannerUnitId(): String =
        if (testMode || bannerUnitId.isBlank()) TEST_BANNER_UNIT_ID else bannerUnitId

    fun effectiveInterstitialUnitId(): String =
        if (testMode || interstitialUnitId.isBlank()) TEST_INTERSTITIAL_UNIT_ID else interstitialUnitId

    fun effectiveRewardedUnitId(): String =
        if (testMode || rewardedUnitId.isBlank()) TEST_REWARDED_UNIT_ID else rewardedUnitId

    companion object {
        const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
        const val TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        const val TEST_NATIVE_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    }
}

@Keep
@IgnoreExtraProperties
data class RewardTransaction(
    @DocumentId val id: String = "",
    val points: Long = 0L,
    val type: String = "REWARDED_AD",
    val title: String = "",
    val rewardToken: String = "",
    val adUnitId: String = "",
    val status: String = "CREDITED",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdEventLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val eventType: String = "IMPRESSION", // IMPRESSION, REWARDED_COMPLETE, BANNER_CLICK
    val adFormat: String = "BANNER",
    val adUnitId: String = "",
    val rewardPoints: Long = 0L,
    val rewardToken: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WalletTopUpRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val amount: Double = 0.0,
    val method: String = "UPI",
    val paymentReference: String = "",
    val status: String = "PENDING", // PENDING, VERIFIED, REJECTED
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ReturnRequest(
    @DocumentId val id: String = "",
    val orderId: String = "",
    val userId: String = "",
    val sellerId: String = "",
    val customerName: String = "",
    val reason: String = "",
    val requestType: String = "RETURN", // RETURN, REPLACEMENT, REFUND
    val status: String = "PENDING", // PENDING, APPROVED, PICKUP_SCHEDULED, REFUNDED, REJECTED
    val refundAmount: Double = 0.0,
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class MarketplaceReview(
    @DocumentId val id: String = "",
    val targetId: String = "",
    val targetType: String = "PRODUCT", // PRODUCT or SERVICE
    val userId: String = "",
    val userName: String = "",
    val rating: Long = 5L,
    val title: String = "",
    val comment: String = "",
    val verifiedPurchase: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class RecentlyViewedRecord(
    @DocumentId val id: String = "",
    val productId: String = "",
    @ServerTimestamp val viewedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ApiIntegrationConfig(
    @DocumentId val id: String = "",
    val providerKey: String = "",
    val providerName: String = "",
    val category: String = "", // PAYMENT, SMS, EMAIL, PUSH, MAPS, GEOCODING, LIVE_VIDEO, STORAGE, ADMOB, WEBHOOK
    val publicKey: String = "",
    val secretMasked: String = "",
    val webhookUrl: String = "",
    val endpointUrl: String = "",
    val mode: String = "TEST", // TEST or LIVE
    val enabled: Boolean = false,
    val status: String = "DISABLED", // CONFIGURED, VERIFIED, ERROR, DISABLED
    val lastTestedAt: String = "",
    val lastError: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TranslationOverride(
    @DocumentId val id: String = "",
    val languageCode: String = "hi",
    val key: String = "",
    val value: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object MarketplaceCatalogConstants {
    val PRODUCT_ANGLE_TAGS = listOf(
        "Front",
        "Back",
        "Left Side",
        "Right Side",
        "Top",
        "Bottom",
        "Close-up",
        "Detail",
        "Packaging",
        "Accessories",
        "Real Photo",
        "Installation",
        "Other"
    )

    val VIDEO_ANGLE_TAGS = listOf(
        "Product Demo",
        "Working Video",
        "Unboxing",
        "Installation",
        "Review",
        "Features",
        "Tutorial"
    )

    val BANNER_TARGET_APPS = listOf(
        "CUSTOMER_HOME",
        "CUSTOMER_CATEGORY",
        "CUSTOMER_DEALS",
        "MECHANIC",
        "SELLER",
        "DELIVERY",
        "PRODUCT_DETAIL",
        "SPECIAL_OFFERS"
    )

    val BANNER_PLACEMENTS = listOf(
        "MAIN_SLIDER",
        "TOP_BANNER",
        "MIDDLE_PROMO",
        "BOTTOM_BANNER",
        "CATEGORY_BANNER",
        "PRODUCT_OFFER",
        "FULL_SCREEN_PROMO"
    )

    val VIDEO_SECTION_TYPES = listOf(
        "HOME_PROMO",
        "FEATURED",
        "SERVICE",
        "LIVE_SERVICE",
        "PRODUCT_PROMO",
        "TUTORIAL",
        "TRAINING",
        "ADVERTISEMENT"
    )
}

/**
 * Pure domain validation & security helpers tested via JUnit4.
 */
object MarketplaceDomainRules {
    fun canSwitchWorkspace(
        requested: AppWorkspace,
        registeredRole: String,
        isAdminVerified: Boolean
    ): Boolean {
        if (isAdminVerified) return true
        if (requested == AppWorkspace.ADMIN) return true // Routes only to AdminLoginScreen unless verified
        val ownWorkspace = when (registeredRole.uppercase()) {
            "MECHANIC" -> AppWorkspace.MECHANIC
            "SELLER" -> AppWorkspace.SELLER
            "DELIVERY" -> AppWorkspace.DELIVERY
            else -> AppWorkspace.CUSTOMER
        }
        return requested == ownWorkspace
    }

    fun evaluateRewardedAdClaim(
        adsEnabled: Boolean,
        rewardedEnabled: Boolean,
        rewardEarnedBySdkCallback: Boolean,
        todayCount: Long,
        dailyCap: Long,
        pointsPerAd: Long,
        rewardToken: String,
        usedTokens: Set<String>
    ): Result<Long> {
        if (!adsEnabled || !rewardedEnabled) {
            return Result.failure(IllegalStateException("Rewarded ads are currently paused by Admin."))
        }
        if (!rewardEarnedBySdkCallback) {
            return Result.failure(IllegalStateException("Ad must be watched completely before earning reward points."))
        }
        if (rewardToken.isBlank() || usedTokens.contains(rewardToken)) {
            return Result.failure(IllegalStateException("Duplicate or invalid reward session token."))
        }
        if (todayCount >= dailyCap) {
            return Result.failure(IllegalStateException("Daily rewarded ads limit ($dailyCap) reached. Come back tomorrow!"))
        }
        return Result.success(pointsPerAd.coerceIn(1L, 500L))
    }

    fun calculateOrderTotals(
        subtotal: Double,
        discountPercent: Long,
        maxDiscount: Double,
        minOrderAmount: Double,
        gstPercent: Double
    ): OrderTotals {
        val discount = if (subtotal >= minOrderAmount && discountPercent > 0L) {
            ((subtotal * discountPercent) / 100.0).coerceAtMost(maxDiscount)
        } else 0.0
        val deliveryFee = if (subtotal >= 999.0 || subtotal <= 0.0) 0.0 else 40.0
        val taxable = (subtotal - discount).coerceAtLeast(0.0)
        val tax = taxable * (gstPercent.coerceIn(0.0, 40.0) / 100.0)
        val total = (taxable + deliveryFee + tax).coerceAtLeast(0.0)
        return OrderTotals(subtotal, discount, deliveryFee, tax, total)
    }

    data class OrderTotals(
        val subtotal: Double,
        val discount: Double,
        val deliveryFee: Double,
        val tax: Double,
        val finalTotal: Double
    )
}
