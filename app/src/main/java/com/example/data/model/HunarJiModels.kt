package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AppWorkspace(val label: String) {
    CUSTOMER("Customer App"),
    MECHANIC("Mechanic App"),
    SELLER("Seller App"),
    DELIVERY("Delivery Boy App"),
    ADMIN("Admin Panel")
}

enum class PaymentMethod(val displayName: String) {
    UPI("UPI"),
    RAZORPAY("Razorpay"),
    CARD("Debit / Credit Card"),
    NET_BANKING("Net Banking"),
    WALLET("Hunar Ji Wallet"),
    COD("Cash on Delivery");

    companion object {
        fun fromRaw(raw: String): PaymentMethod =
            entries.firstOrNull { it.name == raw } ?: UPI
    }
}

enum class OrderStatus(val label: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    RETURNED("Returned"),
    REFUNDED("Refunded");

    companion object {
        fun fromRaw(raw: String): OrderStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class BookingStatus(val label: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    ON_THE_WAY("On the Way"),
    STARTED("Started"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    companion object {
        fun fromRaw(raw: String): BookingStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "CUSTOMER",
    val walletBalance: Double = 0.0,
    val loyaltyPoints: Long = 0L,
    val verified: Boolean = false,
    val online: Boolean = true,
    val blocked: Boolean = false,
    val city: String = "Patna",
    val district: String = "Patna",
    val state: String = "Bihar",
    val referralCode: String = "",
    val shopName: String = "",
    val vehicleInfo: String = "",
    val skills: String = "",
    val hunarUserId: String = "",
    val kycStatus: String = "NOT_SUBMITTED",
    val accountStatus: String = "ACTIVE",
    val dob: String = "",
    val gender: String = "",
    val profilePhotoUri: String = "",
    val passwordHash: String = "",
    val termsAccepted: Boolean = false,
    val kycConsentAccepted: Boolean = false,
    val rewardPoints: Long = 0L,
    val dailyRewardCount: Long = 0L,
    val dailyRewardDate: String = "",
    val shopDescription: String = "",
    val shopLogoUrl: String = "",
    val shopBannerUrl: String = "",
    val shopHours: String = "09:00 AM - 09:00 PM",
    val shopReturnPolicy: String = "7 Days Easy Return",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CartItem(
    @DocumentId val id: String = "",
    val productId: String = "",
    val title: String = "",
    val category: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val quantity: Long = 1L,
    val variant: String = "Standard",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WishlistItem(
    @DocumentId val id: String = "",
    val productId: String = "",
    val title: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val discountPercent: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Address(
    @DocumentId val id: String = "",
    val label: String = "Home",
    val recipientName: String = "",
    val mobile: String = "",
    val houseFlatNo: String = "",
    val streetRoad: String = "",
    val areaLocality: String = "",
    val fullAddress: String = "",
    val landmark: String = "",
    val city: String = "",
    val district: String = "",
    val state: String = "",
    val pincode: String = "",
    val gpsLocation: String = "",
    val defaultAddress: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WalletTransaction(
    @DocumentId val id: String = "",
    val type: String = "CREDIT",
    val amount: Double = 0.0,
    val title: String = "",
    val referenceId: String = "",
    val status: String = "COMPLETED",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "GENERAL",
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ProviderProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val providerType: String = "MECHANIC",
    val shopName: String = "",
    val skills: String = "",
    val experienceYears: Long = 3L,
    val rating: Double = 4.8,
    val completedJobs: Long = 0L,
    val city: String = "Patna",
    val district: String = "Patna",
    val state: String = "Bihar",
    val online: Boolean = true,
    val verified: Boolean = true,
    val approved: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val title: String = "",
    val category: String = "Electronics",
    val subcategory: String = "General",
    val description: String = "",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val discountPercent: Long = 0L,
    val stock: Long = 0L,
    val rating: Double = 4.5,
    val reviewCount: Long = 0L,
    val iconKey: String = "earbuds",
    val approved: Boolean = true,
    val featured: Boolean = false,
    val trending: Boolean = false,
    val active: Boolean = true,
    val district: String = "Patna",
    val mediaGallery: List<ProductMediaItem> = emptyList(),
    val brand: String = "",
    val modelNumber: String = "",
    val sku: String = "",
    val weight: String = "",
    val dimensions: String = "",
    val material: String = "",
    val manufacturer: String = "",
    val countryOfOrigin: String = "India",
    val packageContents: String = "",
    val specs: Map<String, String> = emptyMap(),
    val highlights: List<String> = emptyList(),
    val features: List<String> = emptyList(),
    val benefits: List<String> = emptyList(),
    val usageInstructions: String = "",
    val installationInstructions: String = "",
    val safetyInfo: String = "",
    val faqs: List<Map<String, String>> = emptyList(),
    val colorOptions: List<String> = emptyList(),
    val sizeOptions: List<String> = emptyList(),
    val stepByStepSections: List<ProductGuideStep> = emptyList(),
    val warrantyAvailable: Boolean = true,
    val warrantyDuration: String = "1 Year",
    val warrantyType: String = "Manufacturer Warranty",
    val warrantyConditions: String = "",
    val warrantyServiceCentre: String = "",
    val warrantyRegistrationSteps: String = "",
    val returnPolicy: String = "7 Days Easy Return",
    val replacementPolicy: String = "7 Days Free Replacement",
    val warrantyDocs: List<ProductWarrantyDoc> = emptyList(),
    val soldCount: Long = 0L,
    val viewCount: Long = 0L,
    val newArrival: Boolean = false,
    val codAvailable: Boolean = true,
    val estimatedDeliveryDays: Long = 3L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ServiceItem(
    @DocumentId val id: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val title: String = "",
    val category: String = "Car Repair",
    val description: String = "",
    val price: Double = 0.0,
    val durationMinutes: Long = 45L,
    val rating: Double = 4.8,
    val iconKey: String = "car",
    val approved: Boolean = true,
    val active: Boolean = true,
    val district: String = "Patna",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Order(
    @DocumentId val id: String = "",
    val userId: String = "",
    val sellerId: String = "",
    val deliveryPartnerId: String = "",
    val customerName: String = "",
    val sellerName: String = "",
    val deliveryPartnerName: String = "",
    val itemsSummary: String = "",
    val itemCount: Long = 1L,
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val couponCode: String = "",
    val paymentMethod: String = "UPI",
    val paymentStatus: String = "PENDING",
    val status: String = "PENDING",
    val pickupAddress: String = "",
    val dropAddress: String = "",
    val district: String = "Patna",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ServiceBooking(
    @DocumentId val id: String = "",
    val userId: String = "",
    val mechanicId: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val customerName: String = "",
    val mechanicName: String = "",
    val vehicleInfo: String = "",
    val problemDescription: String = "",
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val serviceAddress: String = "",
    val district: String = "Patna",
    val amount: Double = 0.0,
    val status: String = "PENDING",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LiveStream(
    @DocumentId val id: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val title: String = "",
    val category: String = "Car Engine Repair",
    val viewerCount: Long = 1200L,
    val live: Boolean = true,
    val approved: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class StreamMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val senderName: String = "",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Banner(
    @DocumentId val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val ctaText: String = "Shop Now",
    val targetCategory: String = "All",
    val active: Boolean = true,
    val sortOrder: Long = 0L,
    val description: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val linkUrl: String = "",
    val targetApp: String = "CUSTOMER_HOME",
    val placement: String = "MAIN_SLIDER",
    val targetProductId: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val impressions: Long = 0L,
    val clicks: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Coupon(
    @DocumentId val id: String = "",
    val code: String = "",
    val title: String = "",
    val discountPercent: Long = 10L,
    val maxDiscount: Double = 500.0,
    val minOrderAmount: Double = 499.0,
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WithdrawalRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val role: String = "SELLER",
    val amount: Double = 0.0,
    val payoutMethod: String = "",
    val status: String = "PENDING",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SupportTicket(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val role: String = "CUSTOMER",
    val category: String = "Order",
    val subject: String = "",
    val description: String = "",
    val status: String = "OPEN",
    val adminReply: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PlatformConfig(
    @DocumentId val id: String = "platform_settings",
    val customerAppEnabled: Boolean = true,
    val mechanicAppEnabled: Boolean = true,
    val sellerAppEnabled: Boolean = true,
    val deliveryAppEnabled: Boolean = true,
    val maintenanceMode: Boolean = false,
    val razorpayEnabled: Boolean = true,
    val razorpayMode: String = "TEST",
    val razorpayKeyId: String = "",
    val upiEnabled: Boolean = true,
    val cardEnabled: Boolean = true,
    val netBankingEnabled: Boolean = true,
    val walletEnabled: Boolean = true,
    val codEnabled: Boolean = true,
    val globalCommissionPercent: Double = 10.0,
    val productCommissionPercent: Double = 8.0,
    val serviceCommissionPercent: Double = 12.0,
    val deliveryCommissionPercent: Double = 5.0,
    val gstPercent: Double = 18.0,
    val smsProvider: String = "MSG91",
    val smsEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val pushEnabled: Boolean = true,
    val otpExpiryMinutes: Long = 5L,
    val supportEmail: String = "support@hunarji.in",
    val supportPhone: String = "+91 1800-123-4567",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ServiceLocation(
    @DocumentId val id: String = "",
    val state: String = "Bihar",
    val district: String = "Patna",
    val city: String = "Patna",
    val pincode: String = "800001",
    val serviceAvailable: Boolean = true,
    val deliveryAvailable: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AuditLog(
    @DocumentId val id: String = "",
    val actorId: String = "",
    val actorEmail: String = "",
    val action: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val details: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

enum class KycStatus(val label: String) {
    NOT_SUBMITTED("Not Submitted"),
    SUBMITTED("Registration Submitted"),
    PENDING("Verification Pending"),
    UNDER_REVIEW("Under Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    RESUBMISSION_REQUIRED("Resubmission Required");

    companion object {
        fun fromRaw(raw: String): KycStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: NOT_SUBMITTED
    }
}

enum class KycDocStatus(val label: String) {
    NOT_SUBMITTED("Not Submitted"),
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    VERIFIED("Verified"),
    REJECTED("Rejected"),
    RESUBMIT_REQUIRED("Resubmit Required");

    companion object {
        fun fromRaw(raw: String): KycDocStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: NOT_SUBMITTED
    }
}

@Keep
@IgnoreExtraProperties
data class KycApplication(
    @DocumentId val id: String = "",
    val userId: String = "",
    val hunarUserId: String = "",
    val role: String = "CUSTOMER",
    val fullName: String = "",
    val mobile: String = "",
    val referralCode: String = "",
    val email: String = "",
    val dob: String = "",
    val gender: String = "",
    val profilePhotoName: String = "",
    val kycStatus: String = "PENDING",
    val accountStatus: String = "ACTIVE",
    val houseNumber: String = "",
    val streetRoad: String = "",
    val villageTown: String = "",
    val postOffice: String = "",
    val blockName: String = "",
    val areaLocality: String = "",
    val landmark: String = "",
    val state: String = "Bihar",
    val district: String = "Patna",
    val city: String = "Patna",
    val pincode: String = "",
    val gpsLocation: String = "",
    val fullAddress: String = "",
    val aadhaarMasked: String = "",
    val aadhaarVerified: Boolean = false,
    val panNumberMasked: String = "",
    val panVerified: Boolean = false,
    val addressVerified: Boolean = false,
    val bankHolderName: String = "",
    val bankName: String = "",
    val bankAccountMasked: String = "",
    val ifscCode: String = "",
    val bankVerified: Boolean = false,
    // Mechanic fields
    val mainDepartment: String = "",
    val departments: List<String> = emptyList(),
    val specialization: String = "",
    val specificSkills: List<String> = emptyList(),
    val experienceYears: Long = 0L,
    val previousWorkExperience: String = "",
    val certificateName: String = "",
    val serviceDescription: String = "",
    val serviceCharges: Double = 0.0,
    val workingHours: String = "09:00 AM - 08:00 PM",
    val availableDays: List<String> = emptyList(),
    val emergencyAvailable: Boolean = false,
    val serviceAreas: String = "",
    val serviceRadiusKm: Long = 10L,
    val preferredWorkingArea: String = "",
    val skillVerified: Boolean = false,
    // Seller fields
    val shopName: String = "",
    val businessName: String = "",
    val businessType: String = "Individual / Small Vendor",
    val shopCategory: String = "",
    val shopDescription: String = "",
    val shopContactNumber: String = "",
    val businessEmail: String = "",
    val shopOpeningTime: String = "09:00 AM",
    val shopClosingTime: String = "09:00 PM",
    val weeklyClosingDay: String = "Sunday",
    val shopAddressLine: String = "",
    val shopState: String = "",
    val shopDistrict: String = "",
    val shopCity: String = "",
    val shopPincode: String = "",
    val shopGpsLocation: String = "",
    val shopAddressVerified: Boolean = false,
    val gstRequired: Boolean = false,
    val gstin: String = "",
    val gstVerified: Boolean = false,
    val productCategories: List<String> = emptyList(),
    val productsSold: String = "",
    val brandInfo: String = "",
    val wholesaleRetail: String = "Retail",
    val deliveryAvailable: Boolean = true,
    val shopPickupAvailable: Boolean = true,
    val returnPolicy: String = "7 Days Replacement",
    val documentsVerified: Boolean = false,
    // Delivery fields
    val vehicleType: String = "",
    val vehicleBrand: String = "",
    val vehicleModel: String = "",
    val vehicleRegNumber: String = "",
    val vehicleColor: String = "",
    val drivingLicenceNumber: String = "",
    val drivingLicenceVerified: Boolean = false,
    val vehicleDocVerified: Boolean = false,
    val preferredDeliveryArea: String = "",
    val preferredDistrict: String = "",
    val preferredCity: String = "",
    val deliveryWorkingHours: String = "08:00 AM - 09:00 PM",
    val employmentType: String = "Full Time",
    // Admin & consent
    val adminNotes: String = "",
    val reviewedBy: String = "",
    val kycConsentAccepted: Boolean = false,
    val mobileOtpVerified: Boolean = false,
    val emailVerified: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class KycDocumentRecord(
    @DocumentId val id: String = "",
    val docType: String = "",
    val docLabel: String = "",
    val fileName: String = "",
    val mimeType: String = "image/jpeg",
    val byteSize: Long = 0L,
    val localPreviewPath: String = "",
    val status: String = "SUBMITTED",
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class VerificationHistoryEntry(
    @DocumentId val id: String = "",
    val actorId: String = "",
    val actorEmail: String = "",
    val action: String = "",
    val previousStatus: String = "",
    val newStatus: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

object KycCatalogConstants {
    val MECHANIC_DEPARTMENTS = listOf(
        "Car Mechanic",
        "Bike / Motorcycle Mechanic",
        "Electrician",
        "Plumber",
        "Carpenter",
        "AC and Refrigeration Technician",
        "Washing Machine Repair",
        "Refrigerator Repair",
        "Television Repair",
        "Mobile Repair Technician",
        "Computer / Laptop Repair",
        "Electronics Repair",
        "Home Appliance Repair",
        "Painter",
        "Welder",
        "Construction Worker",
        "Home Cleaning",
        "Other Services"
    )

    val VEHICLE_TYPES = listOf(
        "Motorcycle / Bike",
        "Scooter",
        "Electric Scooter",
        "Electric Bike",
        "Bicycle",
        "Other Approved Vehicle"
    )

    val SELLER_BUSINESS_TYPES = listOf(
        "Individual / Small Vendor (GST Not Required)",
        "Sole Proprietorship (Optional GST)",
        "Partnership Firm (GST Required)",
        "Private Limited / LLP (GST Required)",
        "Wholesale / Interstate Trader (GST Required)"
    )

    val SHOP_CATEGORIES = listOf(
        "Electronics & Mobiles",
        "Fashion & Footwear",
        "Home & Kitchen",
        "Hardware & Tools",
        "Auto Spare Parts",
        "Grocery & General Store",
        "Health & Personal Care"
    )

    val WEEK_DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val MECHANIC_SKILLS = listOf(
        "Engine Overhaul",
        "Brake & Clutch",
        "AC Gas Refill",
        "House Wiring",
        "Pipe Fitting",
        "Compressor Repair",
        "PCB Soldering",
        "Screen Replacement",
        "Motor Winding",
        "Emergency Breakdown"
    )

    fun isMotorVehicle(vehicleType: String): Boolean {
        return vehicleType in listOf("Motorcycle / Bike", "Scooter", "Other Approved Vehicle")
    }

    fun isGstMandatoryForBusinessType(businessType: String): Boolean {
        return businessType.contains("GST Required", ignoreCase = true)
    }

    fun rolePrefix(role: String): String = when (role.uppercase()) {
        "CUSTOMER" -> "CUS"
        "MECHANIC" -> "MEC"
        "SELLER" -> "SEL"
        "DELIVERY" -> "DEL"
        else -> "ADM"
    }
}

