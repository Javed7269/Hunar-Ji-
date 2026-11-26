package com.example.ui.screens.auth

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.location.Geocoder
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.core.security.CredentialSecurityHelper
import com.example.core.storage.PrivateKycStorage
import com.example.core.storage.RegistrationDraftStore
import com.example.data.model.KycApplication
import com.example.data.model.KycCatalogConstants
import com.example.data.model.KycDocumentRecord
import com.example.ui.components.HunarJiBottomWaveDecoration
import com.example.ui.components.HunarJiCenteredLogoHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun RegistrationAndKycWizard(
    initialRole: String = "CUSTOMER",
    existingKyc: KycApplication? = null,
    existingDocs: List<KycDocumentRecord> = emptyList(),
    defaultName: String = "",
    defaultEmail: String = "",
    defaultPhone: String = "",
    onSubmitRegistration: (
        application: KycApplication,
        rawPassword: String,
        docs: List<KycDocumentRecord>,
        termsAccepted: Boolean,
        onSuccess: (KycApplication) -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    onAuthenticatedBeforeSubmit: (() -> Unit)? = null,
    onCompletedClose: (KycApplication) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BackHandler { onCancel() }

    var selectedRole by remember(initialRole) {
        mutableStateOf(
            (existingKyc?.role?.takeIf { it.isNotBlank() } ?: initialRole).uppercase()
                .let { if (it in listOf("CUSTOMER", "MECHANIC", "SELLER", "DELIVERY")) it else "CUSTOMER" }
        )
    }
    var stepIndex by remember(selectedRole) { mutableIntStateOf(0) }
    var submittedApp by remember { mutableStateOf<KycApplication?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var infoMsg by remember { mutableStateOf<String?>(null) }

    // Common fields
    var fullName by remember { mutableStateOf(existingKyc?.fullName?.ifBlank { defaultName } ?: defaultName) }
    var mobile by remember { mutableStateOf(existingKyc?.mobile?.ifBlank { defaultPhone } ?: defaultPhone) }
    var email by remember { mutableStateOf(existingKyc?.email?.ifBlank { defaultEmail } ?: defaultEmail) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf(existingKyc?.dob ?: "") }
    var gender by remember { mutableStateOf(existingKyc?.gender ?: "Male") }
    var referralCode by remember { mutableStateOf("") }
    var profilePhotoName by remember { mutableStateOf(existingKyc?.profilePhotoName ?: "") }
    var termsAccepted by remember { mutableStateOf(false) }

    // Address fields
    var houseNo by remember { mutableStateOf(existingKyc?.houseNumber ?: "") }
    var streetRoad by remember { mutableStateOf(existingKyc?.streetRoad ?: "") }
    var villageTown by remember { mutableStateOf(existingKyc?.villageTown ?: "") }
    var postOffice by remember { mutableStateOf(existingKyc?.postOffice ?: "") }
    var blockName by remember { mutableStateOf(existingKyc?.blockName ?: "") }
    var areaLocality by remember { mutableStateOf(existingKyc?.areaLocality ?: "") }
    var landmark by remember { mutableStateOf(existingKyc?.landmark ?: "") }
    var state by remember { mutableStateOf(existingKyc?.state?.ifBlank { "Bihar" } ?: "Bihar") }
    var district by remember { mutableStateOf(existingKyc?.district?.ifBlank { "Patna" } ?: "Patna") }
    var city by remember { mutableStateOf(existingKyc?.city?.ifBlank { "Patna" } ?: "Patna") }
    var pincode by remember { mutableStateOf(existingKyc?.pincode ?: "") }
    var gpsLocation by remember { mutableStateOf(existingKyc?.gpsLocation ?: "") }

    // KYC & Bank fields (NOT shown to Customer!)
    var aadhaarInput by remember { mutableStateOf("") }
    var panInput by remember { mutableStateOf("") }
    var bankHolder by remember { mutableStateOf(existingKyc?.bankHolderName ?: "") }
    var bankName by remember { mutableStateOf(existingKyc?.bankName ?: "") }
    var bankAccount by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf(existingKyc?.ifscCode ?: "") }
    var kycConsent by remember { mutableStateOf(existingKyc?.kycConsentAccepted ?: false) }

    // Mechanic fields
    var mainDepartment by remember { mutableStateOf(existingKyc?.mainDepartment?.ifBlank { KycCatalogConstants.MECHANIC_DEPARTMENTS.first() } ?: KycCatalogConstants.MECHANIC_DEPARTMENTS.first()) }
    var extraDepartments by remember { mutableStateOf(existingKyc?.departments ?: emptyList()) }
    var specialization by remember { mutableStateOf(existingKyc?.specialization ?: "") }
    var specificSkills by remember { mutableStateOf(existingKyc?.specificSkills ?: listOf("Engine Overhaul")) }
    var expYears by remember { mutableStateOf(existingKyc?.experienceYears?.takeIf { it > 0 }?.toString() ?: "3") }
    var prevWork by remember { mutableStateOf(existingKyc?.previousWorkExperience ?: "") }
    var certName by remember { mutableStateOf(existingKyc?.certificateName ?: "") }
    var serviceDesc by remember { mutableStateOf(existingKyc?.serviceDescription ?: "") }
    var serviceCharges by remember { mutableStateOf(existingKyc?.serviceCharges?.takeIf { it > 0 }?.toInt()?.toString() ?: "499") }
    var workingHours by remember { mutableStateOf(existingKyc?.workingHours ?: "09:00 AM - 08:00 PM") }
    var availableDays by remember { mutableStateOf(existingKyc?.availableDays?.ifEmpty { KycCatalogConstants.WEEK_DAYS } ?: KycCatalogConstants.WEEK_DAYS) }
    var emergencyAvailable by remember { mutableStateOf(existingKyc?.emergencyAvailable ?: false) }
    var serviceAreas by remember { mutableStateOf(existingKyc?.serviceAreas ?: "") }
    var serviceRadiusKm by remember { mutableStateOf(existingKyc?.serviceRadiusKm?.toString() ?: "10") }
    var preferredWorkingArea by remember { mutableStateOf(existingKyc?.preferredWorkingArea ?: "") }

    // Seller fields
    var shopName by remember { mutableStateOf(existingKyc?.shopName ?: "") }
    var businessName by remember { mutableStateOf(existingKyc?.businessName ?: "") }
    var businessType by remember { mutableStateOf(existingKyc?.businessType?.ifBlank { KycCatalogConstants.SELLER_BUSINESS_TYPES.first() } ?: KycCatalogConstants.SELLER_BUSINESS_TYPES.first()) }
    var shopCategory by remember { mutableStateOf(existingKyc?.shopCategory?.ifBlank { KycCatalogConstants.SHOP_CATEGORIES.first() } ?: KycCatalogConstants.SHOP_CATEGORIES.first()) }
    var shopDesc by remember { mutableStateOf(existingKyc?.shopDescription ?: "") }
    var shopContact by remember { mutableStateOf(existingKyc?.shopContactNumber ?: "") }
    var businessEmail by remember { mutableStateOf(existingKyc?.businessEmail ?: "") }
    var shopOpen by remember { mutableStateOf(existingKyc?.shopOpeningTime ?: "09:00 AM") }
    var shopClose by remember { mutableStateOf(existingKyc?.shopClosingTime ?: "09:00 PM") }
    var weeklyClose by remember { mutableStateOf(existingKyc?.weeklyClosingDay ?: "Sunday") }
    var gstin by remember { mutableStateOf(existingKyc?.gstin ?: "") }
    var productCategories by remember { mutableStateOf(existingKyc?.productCategories?.ifEmpty { listOf("Electronics & Mobiles") } ?: listOf("Electronics & Mobiles")) }
    var productsSold by remember { mutableStateOf(existingKyc?.productsSold ?: "") }
    var brandInfo by remember { mutableStateOf(existingKyc?.brandInfo ?: "") }
    var wholesaleRetail by remember { mutableStateOf(existingKyc?.wholesaleRetail ?: "Retail") }
    var deliveryAvailable by remember { mutableStateOf(existingKyc?.deliveryAvailable ?: true) }
    var shopPickupAvailable by remember { mutableStateOf(existingKyc?.shopPickupAvailable ?: true) }
    var returnPolicy by remember { mutableStateOf(existingKyc?.returnPolicy ?: "7 Days Replacement") }

    // Delivery fields
    var vehicleType by remember { mutableStateOf(existingKyc?.vehicleType?.ifBlank { KycCatalogConstants.VEHICLE_TYPES.first() } ?: KycCatalogConstants.VEHICLE_TYPES.first()) }
    var vehicleBrand by remember { mutableStateOf(existingKyc?.vehicleBrand ?: "") }
    var vehicleModel by remember { mutableStateOf(existingKyc?.vehicleModel ?: "") }
    var vehicleRegNo by remember { mutableStateOf(existingKyc?.vehicleRegNumber ?: "") }
    var vehicleColor by remember { mutableStateOf(existingKyc?.vehicleColor ?: "") }
    var dlNumber by remember { mutableStateOf(existingKyc?.drivingLicenceNumber ?: "") }
    var preferredDeliveryArea by remember { mutableStateOf(existingKyc?.preferredDeliveryArea ?: "") }
    var preferredDeliveryDistrict by remember { mutableStateOf(existingKyc?.preferredDistrict ?: "Patna") }
    var preferredDeliveryCity by remember { mutableStateOf(existingKyc?.preferredCity ?: "Patna") }
    var deliveryHours by remember { mutableStateOf(existingKyc?.deliveryWorkingHours ?: "08:00 AM - 09:00 PM") }
    var employmentType by remember { mutableStateOf(existingKyc?.employmentType ?: "Full Time") }

    // Documents map
    val docsMap = remember { mutableStateMapOf<String, KycDocumentRecord>() }
    LaunchedEffect(existingDocs) {
        existingDocs.forEach { doc ->
            docsMap[doc.docType] = doc
        }
    }

    // Load saved draft for role if no submitted KYC yet
    LaunchedEffect(selectedRole) {
        if (existingKyc == null) {
            val draft = RegistrationDraftStore.loadRoleDraft(context, selectedRole)
            if (draft.isNotEmpty()) {
                draft["fullName"]?.takeIf { it.isNotBlank() }?.let { fullName = it }
                draft["mobile"]?.takeIf { it.isNotBlank() }?.let { mobile = it }
                draft["email"]?.takeIf { it.isNotBlank() }?.let { email = it }
                draft["city"]?.takeIf { it.isNotBlank() }?.let { city = it }
                draft["district"]?.takeIf { it.isNotBlank() }?.let { district = it }
                draft["pincode"]?.takeIf { it.isNotBlank() }?.let { pincode = it }
                draft["shopName"]?.takeIf { it.isNotBlank() }?.let { shopName = it }
                draft["mainDepartment"]?.takeIf { it.isNotBlank() }?.let { mainDepartment = it }
                draft["vehicleType"]?.takeIf { it.isNotBlank() }?.let { vehicleType = it }
                infoMsg = "Saved draft restored for $selectedRole"
            }
        }
    }

    // OTP state
    var mobileOtpVerified by remember { mutableStateOf(existingKyc?.mobileOtpVerified ?: false) }

    val activeUid = Firebase.auth.currentUser?.uid ?: "draft_user"

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val stored = withContext(Dispatchers.IO) {
                    PrivateKycStorage.copyUriToPrivateStorage(context, activeUid, "profile_photo", uri)
                }
                profilePhotoName = stored.fileName
                docsMap["profile_photo"] = KycDocumentRecord(
                    id = "profile_photo",
                    docType = "profile_photo",
                    docLabel = "Profile Photo",
                    fileName = stored.fileName,
                    mimeType = stored.mimeType,
                    byteSize = stored.byteSize,
                    localPreviewPath = stored.privateFilePath
                )
            }
        }
    }

    fun updateAddressFromLocation(location: android.location.Location?) {
        if (location == null) {
            errorMsg = "Current location could not be detected. Turn on device location and try again."
            return
        }
        gpsLocation = "${location.latitude}, ${location.longitude}"
        scope.launch {
            val address = withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
                } catch (_: Exception) {
                    null
                }
            }
            if (address != null) {
                villageTown = address.subLocality ?: address.locality ?: villageTown
                areaLocality = address.thoroughfare ?: address.subThoroughfare ?: areaLocality
                city = address.locality ?: address.subAdminArea ?: city
                district = address.subAdminArea ?: district
                state = address.adminArea ?: state
                pincode = address.postalCode ?: pincode
            }
        }
    }

    val gpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                LocationServices.getFusedLocationProviderClient(context)
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                    .addOnSuccessListener(::updateAddressFromLocation)
                    .addOnFailureListener { errorMsg = it.localizedMessage ?: "Unable to get current location." }
            } catch (_: SecurityException) {
                errorMsg = "Location permission is required."
            }
        }
    }

    fun captureGps() {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            gpsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        try {
            LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener(::updateAddressFromLocation)
                .addOnFailureListener { errorMsg = it.localizedMessage ?: "Unable to get current location." }
        } catch (_: SecurityException) {
            errorMsg = "Location permission is required."
        }
    }

    fun openCurrentLocationInMaps() {
        val parts = gpsLocation.split(",")
        val latitude = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
        val longitude = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
        if (latitude == null || longitude == null) {
            errorMsg = "Capture your current location first."
            return
        }
        val mapUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Current%20Location)")
        val mapsIntent = Intent(Intent.ACTION_VIEW, mapUri).setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(mapsIntent)
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, mapUri))
            } catch (_: Exception) {
                errorMsg = "No map application is available on this device."
            }
        }
    }

    // If registration succeeded, show Success screen
    submittedApp?.let { saved ->
        RegistrationSuccessScreen(
            application = saved,
            onContinue = { onCompletedClose(saved) }
        )
        return
    }

    val totalSteps = if (selectedRole == "CUSTOMER") 1 else 5
    val stepTitle = when (selectedRole) {
        "CUSTOMER" -> "Customer Account Details"
        "MECHANIC" -> when (stepIndex) {
            0 -> "Step 1/5 • Personal Info"
            1 -> "Step 2/5 • Residential Address"
            2 -> "Step 3/5 • Identity & Bank KYC"
            3 -> "Step 4/5 • Trade & Service Details"
            else -> "Step 5/5 • Documents & OTP Verify"
        }
        "SELLER" -> when (stepIndex) {
            0 -> "Step 1/5 • Owner Details"
            1 -> "Step 2/5 • Shop & Business Details"
            2 -> "Step 3/5 • Shop & Owner Address"
            3 -> "Step 4/5 • PAN, GST & Bank Details"
            else -> "Step 5/5 • Documents & OTP Verify"
        }
        else -> when (stepIndex) {
            0 -> "Step 1/5 • Personal Info"
            1 -> "Step 2/5 • Residential Address"
            2 -> "Step 3/5 • Identity & Bank KYC"
            3 -> "Step 4/5 • Vehicle & Delivery Area"
            else -> "Step 5/5 • Documents & OTP Verify"
        }
    }

    val gstMandatory = selectedRole == "SELLER" && KycCatalogConstants.isGstMandatoryForBusinessType(businessType)

    fun validateCurrentStep(): String? {
        if (stepIndex == 0) {
            if (fullName.trim().length < 2) return "Enter your full name"
            if (!CredentialSecurityHelper.isValidMobile(mobile)) return "Enter a valid 10-digit mobile number"
            if (!CredentialSecurityHelper.isValidEmail(email)) return "Enter a valid email address"
            if (existingKyc == null && password.length < 6) return "Password must be at least 6 characters"
            if (password.isNotBlank() && password != confirmPassword) return "Passwords do not match"
            if (selectedRole == "CUSTOMER" && !termsAccepted) return "Please accept Terms & Conditions and Privacy Policy"
            return null
        }
        if (selectedRole == "MECHANIC") {
            return when (stepIndex) {
                1 -> when {
                    houseNo.isBlank() || streetRoad.isBlank() || areaLocality.isBlank() -> "Enter House, Street & Area/Locality"
                    pincode.length < 6 -> "Enter valid 6-digit Pincode"
                    else -> null
                }
                2 -> when {
                    !kycConsent -> "Accept KYC consent to verify identity documents"
                    !CredentialSecurityHelper.isValidAadhaarOrLast4(aadhaarInput) && existingKyc?.aadhaarMasked.isNullOrBlank() ->
                        "Enter 12-digit Aadhaar or last 4 digits"
                    !CredentialSecurityHelper.isValidPan(panInput) && existingKyc?.panNumberMasked.isNullOrBlank() ->
                        "Enter valid PAN (e.g. ABCDE1234F)"
                    bankHolder.isBlank() || (bankAccount.length < 6 && existingKyc?.bankAccountMasked.isNullOrBlank()) || !CredentialSecurityHelper.isValidIfsc(ifscCode) ->
                        "Enter valid Bank Holder Name, Account Number & IFSC"
                    else -> null
                }
                3 -> when {
                    mainDepartment.isBlank() -> "Select your Main Department"
                    serviceAreas.isBlank() -> "Enter your Service Areas"
                    else -> null
                }
                else -> when {
                    !termsAccepted -> "Accept Terms & Conditions to submit KYC"
                    else -> null
                }
            }
        }
        if (selectedRole == "SELLER") {
            return when (stepIndex) {
                1 -> when {
                    shopName.isBlank() -> "Enter your Shop Name"
                    !CredentialSecurityHelper.isValidMobile(shopContact.ifBlank { mobile }) -> "Enter valid Shop Contact Number"
                    else -> null
                }
                2 -> when {
                    houseNo.isBlank() || streetRoad.isBlank() -> "Enter Shop / Business Building & Street"
                    pincode.length < 6 -> "Enter valid 6-digit Shop Pincode"
                    else -> null
                }
                3 -> when {
                    !kycConsent -> "Accept KYC consent for Seller verification"
                    !CredentialSecurityHelper.isValidPan(panInput) && existingKyc?.panNumberMasked.isNullOrBlank() ->
                        "Enter valid PAN (e.g. ABCDE1234F)"
                    gstMandatory && !CredentialSecurityHelper.isValidGstin(gstin) ->
                        "Valid 15-character GSTIN is mandatory for $businessType"
                    bankHolder.isBlank() || (bankAccount.length < 6 && existingKyc?.bankAccountMasked.isNullOrBlank()) || !CredentialSecurityHelper.isValidIfsc(ifscCode) ->
                        "Enter valid Bank Account & IFSC for seller payouts"
                    else -> null
                }
                else -> if (!termsAccepted) "Accept Terms & Conditions to submit Seller KYC" else null
            }
        }
        // DELIVERY
        return when (stepIndex) {
            1 -> when {
                houseNo.isBlank() || streetRoad.isBlank() || areaLocality.isBlank() -> "Enter House, Street & Locality"
                pincode.length < 6 -> "Enter valid 6-digit Pincode"
                else -> null
            }
            2 -> when {
                !kycConsent -> "Accept KYC consent for Delivery Partner verification"
                !CredentialSecurityHelper.isValidAadhaarOrLast4(aadhaarInput) && existingKyc?.aadhaarMasked.isNullOrBlank() ->
                    "Enter 12-digit Aadhaar or last 4 digits"
                !CredentialSecurityHelper.isValidPan(panInput) && existingKyc?.panNumberMasked.isNullOrBlank() ->
                    "Enter valid PAN (e.g. ABCDE1234F)"
                bankHolder.isBlank() || (bankAccount.length < 6 && existingKyc?.bankAccountMasked.isNullOrBlank()) || !CredentialSecurityHelper.isValidIfsc(ifscCode) ->
                    "Enter valid Bank Account & IFSC"
                else -> null
            }
            3 -> when {
                KycCatalogConstants.isMotorVehicle(vehicleType) && (vehicleBrand.isBlank() || vehicleRegNo.isBlank() || dlNumber.isBlank()) ->
                    "Enter Vehicle Brand, Registration Number & Driving Licence for $vehicleType"
                preferredDeliveryArea.isBlank() -> "Enter Preferred Delivery Area / Zone"
                else -> null
            }
            else -> if (!termsAccepted) "Accept Terms & Conditions to submit Delivery KYC" else null
        }
    }

    fun performFinalSubmit() {
        val err = validateCurrentStep()
        if (err != null) {
            errorMsg = err
            return
        }
        if (selectedRole != "CUSTOMER" && !mobileOtpVerified) {
            errorMsg = "Please verify your mobile number via OTP before submitting KYC"
            return
        }
        errorMsg = null
        isSubmitting = true

        val composedFullAddr = listOf(houseNo, streetRoad, villageTown, postOffice, blockName, areaLocality, landmark, city, district, state, pincode)
            .filter { it.isNotBlank() }.joinToString(", ")

        val app = KycApplication(
            userId = activeUid,
            hunarUserId = existingKyc?.hunarUserId ?: "",
            role = selectedRole,
            fullName = fullName.trim(),
            mobile = mobile.trim(),
            email = email.trim(),
            dob = dob.trim(),
            gender = gender,
            profilePhotoName = profilePhotoName,
            houseNumber = houseNo.trim(),
            streetRoad = streetRoad.trim(),
            villageTown = villageTown.trim(),
            postOffice = postOffice.trim(),
            blockName = blockName.trim(),
            areaLocality = areaLocality.trim(),
            landmark = landmark.trim(),
            state = state.trim(),
            district = district.trim(),
            city = city.trim(),
            pincode = pincode.trim(),
            gpsLocation = gpsLocation,
            fullAddress = composedFullAddr,
            aadhaarMasked = if (aadhaarInput.isNotBlank()) CredentialSecurityHelper.maskAadhaar(aadhaarInput) else existingKyc?.aadhaarMasked ?: "",
            panNumberMasked = if (panInput.isNotBlank()) CredentialSecurityHelper.maskPan(panInput) else existingKyc?.panNumberMasked ?: "",
            bankHolderName = bankHolder.trim(),
            bankName = bankName.trim(),
            bankAccountMasked = if (bankAccount.isNotBlank()) CredentialSecurityHelper.maskBankAccount(bankAccount) else existingKyc?.bankAccountMasked ?: "",
            ifscCode = ifscCode.trim().uppercase(),
            mainDepartment = mainDepartment,
            departments = extraDepartments,
            specialization = specialization,
            specificSkills = specificSkills,
            experienceYears = expYears.toLongOrNull() ?: 0L,
            previousWorkExperience = prevWork,
            certificateName = certName,
            serviceDescription = serviceDesc,
            serviceCharges = serviceCharges.toDoubleOrNull() ?: 499.0,
            workingHours = workingHours,
            availableDays = availableDays,
            emergencyAvailable = emergencyAvailable,
            serviceAreas = serviceAreas,
            serviceRadiusKm = serviceRadiusKm.toLongOrNull() ?: 10L,
            preferredWorkingArea = preferredWorkingArea,
            shopName = shopName,
            businessName = businessName,
            businessType = businessType,
            shopCategory = shopCategory,
            shopDescription = shopDesc,
            shopContactNumber = shopContact.ifBlank { mobile },
            businessEmail = businessEmail.ifBlank { email },
            shopOpeningTime = shopOpen,
            shopClosingTime = shopClose,
            weeklyClosingDay = weeklyClose,
            shopAddressLine = composedFullAddr,
            shopState = state,
            shopDistrict = district,
            shopCity = city,
            shopPincode = pincode,
            shopGpsLocation = gpsLocation,
            gstRequired = gstMandatory,
            gstin = gstin,
            productCategories = productCategories,
            productsSold = productsSold,
            brandInfo = brandInfo,
            wholesaleRetail = wholesaleRetail,
            deliveryAvailable = deliveryAvailable,
            shopPickupAvailable = shopPickupAvailable,
            returnPolicy = returnPolicy,
            vehicleType = vehicleType,
            vehicleBrand = vehicleBrand,
            vehicleModel = vehicleModel,
            vehicleRegNumber = vehicleRegNo,
            vehicleColor = vehicleColor,
            drivingLicenceNumber = dlNumber,
            preferredDeliveryArea = preferredDeliveryArea,
            preferredDistrict = preferredDeliveryDistrict,
            preferredCity = preferredDeliveryCity,
            deliveryWorkingHours = deliveryHours,
            employmentType = employmentType,
            kycConsentAccepted = kycConsent,
            mobileOtpVerified = mobileOtpVerified
        )

        val executeUpload = {
            onSubmitRegistration(
                app,
                password,
                docsMap.values.toList(),
                termsAccepted,
                { saved ->
                    scope.launch { RegistrationDraftStore.clearRoleDraft(context, selectedRole) }
                    isSubmitting = false
                    submittedApp = saved
                },
                { msg ->
                    isSubmitting = false
                    errorMsg = msg
                }
            )
        }

        if (Firebase.auth.currentUser == null) {
            val mobileDigits = mobile.filter(Char::isDigit).takeLast(10)
            if (mobileDigits.length != 10) {
                isSubmitting = false
                errorMsg = "Enter a valid 10-digit mobile number to create your account."
            } else if (password.length < 6) {
                isSubmitting = false
                errorMsg = "Password must contain at least 6 characters."
            } else {
                Firebase.auth.createUserWithEmailAndPassword("$mobileDigits@mobile.hunarji.in", password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onAuthenticatedBeforeSubmit?.invoke()
                            executeUpload()
                        } else {
                            isSubmitting = false
                            errorMsg = when (task.exception) {
                                is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                                    "This mobile number is already registered. Please login instead."
                                else -> task.exception?.localizedMessage ?: "Account creation failed. Please try again."
                            }
                        }
                    }
            }
        } else {
            executeUpload()
        }
    }

    val stepLabels = when (selectedRole) {
        "CUSTOMER" -> listOf("Account Details")
        "MECHANIC" -> listOf("Personal", "Address", "KYC & Bank", "Services", "Verify")
        "SELLER" -> listOf("Owner", "Shop Info", "Address", "PAN & GST", "Verify")
        else -> listOf("Personal", "Address", "KYC & Bank", "Vehicle", "Verify")
    }

    Scaffold(
        containerColor = Color(0xFFF3F8FF),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = Color.Transparent) {
                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        TextButton(
                            onClick = {
                                scope.launch {
                                    RegistrationDraftStore.saveRoleDraft(
                                        context,
                                        selectedRole,
                                        mapOf(
                                            "fullName" to fullName,
                                            "mobile" to mobile,
                                            "email" to email,
                                            "city" to city,
                                            "district" to district,
                                            "pincode" to pincode,
                                            "shopName" to shopName,
                                            "mainDepartment" to mainDepartment,
                                            "vehicleType" to vehicleType
                                        )
                                    )
                                    infoMsg = "Draft saved locally! You can continue later."
                                }
                            }
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save draft", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Draft", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE0EDFF), Color(0xFFF5F9FF), Color.White)
                    )
                )
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        HunarJiCenteredLogoHeader(
                            subtitle = "Shop • Book • Earn • Grow",
                            logoSize = 58.dp
                        )
                    }
                }

                // Shared Role Selector + Dynamic Horizontal Stepper
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val roles = listOf(
                                    Triple("CUSTOMER", "Customer", Icons.Default.Person),
                                    Triple("MECHANIC", "Mechanic", Icons.Default.Build),
                                    Triple("SELLER", "Seller", Icons.Default.Storefront),
                                    Triple("DELIVERY", "Delivery", Icons.Default.DeliveryDining)
                                )
                                roles.forEach { (roleCode, label, icon) ->
                                    val selected = selectedRole == roleCode
                                    Surface(
                                        color = if (selected) BrandBlue else Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if (selected) BrandBlue else Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                selectedRole = roleCode
                                                stepIndex = 0
                                                errorMsg = null
                                            }
                                            .testTag("select_role_${roleCode.lowercase()}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(icon, contentDescription = label, tint = if (selected) Color.White else BrandBlueDark, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (selected) Color.White else TextPrimary,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            RegistrationStepper(
                                steps = stepLabels,
                                currentStep = stepIndex,
                                onStepSelected = { target ->
                                    if (target < stepIndex) {
                                        stepIndex = target
                                        errorMsg = null
                                    }
                                }
                            )

                            Text(
                                text = stepTitle,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrandBlue
                            )
                        }
                    }
                }

            if (infoMsg != null) {
                item {
                    Surface(color = AccentGreenLight, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(infoMsg ?: "", color = AccentGreen, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            if (errorMsg != null) {
                item {
                    Surface(color = AccentRedLight, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(errorMsg ?: "", color = AccentRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            // Role-Specific Form Content
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        when {
                            selectedRole == "CUSTOMER" -> {
                                RegPersonalInfoSection(
                                    fullName = fullName, onFullName = { fullName = it },
                                    mobile = mobile, onMobile = { mobile = it },
                                    email = email, onEmail = { email = it },
                                    password = password, onPassword = { password = it },
                                    confirmPassword = confirmPassword, onConfirmPassword = { confirmPassword = it },
                                    includeDobGender = false,
                                    dob = dob, onDob = {},
                                    gender = gender, onGender = {}
                                )
                                OutlinedTextField(
                                    value = referralCode,
                                    onValueChange = { referralCode = it.uppercase().take(16) },
                                    label = { Text("Referral Code (Optional)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedButton(
                                    onClick = {
                                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (profilePhotoName.isBlank()) "Optional Profile Photo" else "Photo: $profilePhotoName")
                                }
                                Surface(color = BrandBlueLight, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        "No Aadhaar, PAN, GST, or business details required for Customers. Delivery address is collected only during order or service checkout.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BrandBlueDark,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                TermsCheckboxRow(checked = termsAccepted, onCheckedChange = { termsAccepted = it })
                            }

                            stepIndex == 0 -> {
                                RegPersonalInfoSection(
                                    fullName = fullName, onFullName = { fullName = it },
                                    mobile = mobile, onMobile = { mobile = it },
                                    email = email, onEmail = { email = it },
                                    password = password, onPassword = { password = it },
                                    confirmPassword = confirmPassword, onConfirmPassword = { confirmPassword = it },
                                    includeDobGender = true,
                                    dob = dob, onDob = { dob = it },
                                    gender = gender, onGender = { gender = it }
                                )
                            }

                            selectedRole == "MECHANIC" && stepIndex == 1 -> {
                                RegAddressSection(
                                    title = "Mechanic Residential & Base Address",
                                    houseNo = houseNo, onHouseNo = { houseNo = it },
                                    streetRoad = streetRoad, onStreetRoad = { streetRoad = it },
                                    villageTown = villageTown, onVillageTown = { villageTown = it },
                                    postOffice = postOffice, onPostOffice = { postOffice = it },
                                    blockName = blockName, onBlockName = { blockName = it },
                                    areaLocality = areaLocality, onAreaLocality = { areaLocality = it },
                                    landmark = landmark, onLandmark = { landmark = it },
                                    state = state, onState = { state = it },
                                    district = district, onDistrict = { district = it },
                                    city = city, onCity = { city = it },
                                    pincode = pincode, onPincode = { pincode = it },
                                    gpsLocation = gpsLocation,
                                    onCaptureGps = ::captureGps,
                                    onOpenMaps = ::openCurrentLocationInMaps
                                )
                            }

                            selectedRole == "MECHANIC" && stepIndex == 2 -> {
                                RegIdentityAndBankSection(
                                    aadhaar = aadhaarInput, onAadhaar = { aadhaarInput = it },
                                    pan = panInput, onPan = { panInput = it },
                                    bankHolder = bankHolder, onBankHolder = { bankHolder = it },
                                    bankName = bankName, onBankName = { bankName = it },
                                    bankAcc = bankAccount, onBankAcc = { bankAccount = it },
                                    ifsc = ifscCode, onIfsc = { ifscCode = it },
                                    kycConsent = kycConsent, onKycConsent = { kycConsent = it }
                                )
                            }

                            selectedRole == "MECHANIC" && stepIndex == 3 -> {
                                MechanicProfessionalSection(
                                    mainDepartment = mainDepartment, onMainDepartment = { mainDepartment = it },
                                    departments = extraDepartments,
                                    onToggleDepartment = { d ->
                                        extraDepartments = if (d in extraDepartments) extraDepartments - d else extraDepartments + d
                                    },
                                    specialization = specialization, onSpecialization = { specialization = it },
                                    specificSkills = specificSkills,
                                    onToggleSkill = { s ->
                                        specificSkills = if (s in specificSkills) specificSkills - s else specificSkills + s
                                    },
                                    expYears = expYears, onExpYears = { expYears = it },
                                    prevWork = prevWork, onPrevWork = { prevWork = it },
                                    certName = certName, onCertName = { certName = it },
                                    serviceDesc = serviceDesc, onServiceDesc = { serviceDesc = it },
                                    serviceCharges = serviceCharges, onServiceCharges = { serviceCharges = it },
                                    workingHours = workingHours, onWorkingHours = { workingHours = it },
                                    availableDays = availableDays,
                                    onToggleDay = { day ->
                                        availableDays = if (day in availableDays) availableDays - day else availableDays + day
                                    },
                                    emergencyAvailable = emergencyAvailable, onEmergency = { emergencyAvailable = it },
                                    serviceAreas = serviceAreas, onServiceAreas = { serviceAreas = it },
                                    serviceRadiusKm = serviceRadiusKm, onServiceRadiusKm = { serviceRadiusKm = it },
                                    preferredArea = preferredWorkingArea, onPreferredArea = { preferredWorkingArea = it }
                                )
                            }

                            selectedRole == "SELLER" && stepIndex == 1 -> {
                                SellerBusinessSection(
                                    shopName = shopName, onShopName = { shopName = it },
                                    businessName = businessName, onBusinessName = { businessName = it },
                                    businessType = businessType, onBusinessType = { businessType = it },
                                    shopCategory = shopCategory, onShopCategory = { shopCategory = it },
                                    shopDesc = shopDesc, onShopDesc = { shopDesc = it },
                                    shopContact = shopContact, onShopContact = { shopContact = it },
                                    businessEmail = businessEmail, onBusinessEmail = { businessEmail = it },
                                    openTime = shopOpen, onOpenTime = { shopOpen = it },
                                    closeTime = shopClose, onCloseTime = { shopClose = it },
                                    weeklyClose = weeklyClose, onWeeklyClose = { weeklyClose = it },
                                    productCategories = productCategories,
                                    onToggleProductCat = { c ->
                                        productCategories = if (c in productCategories) productCategories - c else productCategories + c
                                    },
                                    productsSold = productsSold, onProductsSold = { productsSold = it },
                                    brandInfo = brandInfo, onBrandInfo = { brandInfo = it },
                                    wholesaleRetail = wholesaleRetail, onWholesaleRetail = { wholesaleRetail = it },
                                    deliveryAvailable = deliveryAvailable, onDeliveryAvailable = { deliveryAvailable = it },
                                    pickupAvailable = shopPickupAvailable, onPickupAvailable = { shopPickupAvailable = it },
                                    returnPolicy = returnPolicy, onReturnPolicy = { returnPolicy = it }
                                )
                            }

                            selectedRole == "SELLER" && stepIndex == 2 -> {
                                RegAddressSection(
                                    title = "Shop & Owner Address",
                                    houseNo = houseNo, onHouseNo = { houseNo = it },
                                    streetRoad = streetRoad, onStreetRoad = { streetRoad = it },
                                    villageTown = villageTown, onVillageTown = { villageTown = it },
                                    areaLocality = areaLocality, onAreaLocality = { areaLocality = it },
                                    landmark = landmark, onLandmark = { landmark = it },
                                    state = state, onState = { state = it },
                                    district = district, onDistrict = { district = it },
                                    city = city, onCity = { city = it },
                                    pincode = pincode, onPincode = { pincode = it },
                                    gpsLocation = gpsLocation,
                                    onCaptureGps = ::captureGps
                                )
                            }

                            selectedRole == "SELLER" && stepIndex == 3 -> {
                                RegIdentityAndBankSection(
                                    aadhaar = aadhaarInput, onAadhaar = { aadhaarInput = it },
                                    pan = panInput, onPan = { panInput = it },
                                    bankHolder = bankHolder, onBankHolder = { bankHolder = it },
                                    bankName = bankName, onBankName = { bankName = it },
                                    bankAcc = bankAccount, onBankAcc = { bankAccount = it },
                                    ifsc = ifscCode, onIfsc = { ifscCode = it },
                                    kycConsent = kycConsent, onKycConsent = { kycConsent = it },
                                    showGst = true,
                                    gstMandatory = gstMandatory,
                                    gstin = gstin, onGstin = { gstin = it }
                                )
                            }

                            selectedRole == "DELIVERY" && stepIndex == 1 -> {
                                RegAddressSection(
                                    title = "Delivery Partner Residential Address",
                                    houseNo = houseNo, onHouseNo = { houseNo = it },
                                    streetRoad = streetRoad, onStreetRoad = { streetRoad = it },
                                    villageTown = villageTown, onVillageTown = { villageTown = it },
                                    areaLocality = areaLocality, onAreaLocality = { areaLocality = it },
                                    landmark = landmark, onLandmark = { landmark = it },
                                    state = state, onState = { state = it },
                                    district = district, onDistrict = { district = it },
                                    city = city, onCity = { city = it },
                                    pincode = pincode, onPincode = { pincode = it },
                                    gpsLocation = gpsLocation,
                                    onCaptureGps = ::captureGps
                                )
                            }

                            selectedRole == "DELIVERY" && stepIndex == 2 -> {
                                RegIdentityAndBankSection(
                                    aadhaar = aadhaarInput, onAadhaar = { aadhaarInput = it },
                                    pan = panInput, onPan = { panInput = it },
                                    bankHolder = bankHolder, onBankHolder = { bankHolder = it },
                                    bankName = bankName, onBankName = { bankName = it },
                                    bankAcc = bankAccount, onBankAcc = { bankAccount = it },
                                    ifsc = ifscCode, onIfsc = { ifscCode = it },
                                    kycConsent = kycConsent, onKycConsent = { kycConsent = it }
                                )
                            }

                            selectedRole == "DELIVERY" && stepIndex == 3 -> {
                                DeliveryVehicleAndWorkSection(
                                    vehicleType = vehicleType, onVehicleType = { vehicleType = it },
                                    vehicleBrand = vehicleBrand, onVehicleBrand = { vehicleBrand = it },
                                    vehicleModel = vehicleModel, onVehicleModel = { vehicleModel = it },
                                    vehicleRegNo = vehicleRegNo, onVehicleRegNo = { vehicleRegNo = it },
                                    vehicleColor = vehicleColor, onVehicleColor = { vehicleColor = it },
                                    dlNumber = dlNumber, onDlNumber = { dlNumber = it },
                                    preferredArea = preferredDeliveryArea, onPreferredArea = { preferredDeliveryArea = it },
                                    preferredDistrict = preferredDeliveryDistrict, onPreferredDistrict = { preferredDeliveryDistrict = it },
                                    preferredCity = preferredDeliveryCity, onPreferredCity = { preferredDeliveryCity = it },
                                    workingHours = deliveryHours, onWorkingHours = { deliveryHours = it },
                                    employmentType = employmentType, onEmploymentType = { employmentType = it }
                                )
                            }

                            else -> {
                                RoleDocumentsUploadSection(
                                    uid = activeUid,
                                    role = selectedRole,
                                    gstRequired = gstMandatory,
                                    vehicleType = vehicleType,
                                    docsMap = docsMap,
                                    onDocSelected = { rec -> docsMap[rec.docType] = rec },
                                    onRemoveDoc = { rec -> docsMap.remove(rec.docType) }
                                )

                                // Mobile OTP Verification Widget
                                Surface(
                                    color = if (mobileOtpVerified) AccentGreenLight else BrandBlueLight,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Sms, contentDescription = null, tint = if (mobileOtpVerified) AccentGreen else BrandBlue)
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                if (mobileOtpVerified) "Mobile OTP Verified ($mobile)" else "Verify Mobile Number ($mobile)",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = if (mobileOtpVerified) AccentGreen else BrandBlueDark
                                            )
                                        }
                                        if (!mobileOtpVerified) {
                                            Text(
                                                "SMS verification will be enabled after the Admin configures a real SMS provider.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                                TermsCheckboxRow(checked = termsAccepted, onCheckedChange = { termsAccepted = it })
                            }
                        }
                    }
                }
            }

            // Bottom Step Navigation Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (stepIndex > 0) {
                        OutlinedButton(
                            onClick = { stepIndex--; errorMsg = null },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Previous")
                        }
                    }
                    Button(
                        onClick = {
                            if (stepIndex < totalSteps - 1) {
                                val err = validateCurrentStep()
                                if (err != null) {
                                    errorMsg = err
                                } else {
                                    errorMsg = null
                                    stepIndex++
                                }
                            } else {
                                performFinalSubmit()
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(50.dp)
                            .testTag("wizard_primary_action_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (stepIndex < totalSteps - 1) "Continue" else "Register Now",
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account? ", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.bodySmall.copy(color = BrandBlue, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .clickable { onCancel() }
                            .padding(4.dp)
                            .testTag("already_have_account_login")
                    )
                }
            }
        }
            HunarJiBottomWaveDecoration()
        }
    }
}

@Composable
fun RegistrationStepper(
    steps: List<String>,
    currentStep: Int,
    onStepSelected: (Int) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("registration_stepper"),
        verticalAlignment = Alignment.Top
    ) {
        steps.forEachIndexed { index, label ->
            val completed = index < currentStep
            val active = index == currentStep
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = completed) { onStepSelected(index) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (index == 0) Color.Transparent
                                else if (completed || active) BrandBlue
                                else Color(0xFFE2E8F0)
                            )
                    )
                    Surface(
                        color = when {
                            completed -> AccentGreen
                            active -> BrandBlue
                            else -> Color(0xFFF1F5F9)
                        },
                        shape = CircleShape,
                        border = BorderStroke(
                            1.5.dp,
                            when {
                                completed -> AccentGreen
                                active -> BrandBlue
                                else -> Color(0xFFCBD5E1)
                            }
                        ),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (completed) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (active) Color.White else TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (index == steps.lastIndex) Color.Transparent
                                else if (completed) BrandBlue
                                else Color(0xFFE2E8F0)
                            )
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = when {
                            active -> BrandBlueDark
                            completed -> AccentGreen
                            else -> TextMuted
                        },
                        fontWeight = if (active || completed) FontWeight.Bold else FontWeight.Normal
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TermsCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(4.dp))
        Text(
            text = "I agree to the Hunar Ji Terms & Conditions and Privacy Policy.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun RegistrationSuccessScreen(
    application: KycApplication,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BrandBlueDark, BrandBlue, SurfaceLight)))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HunarJiCenteredLogoHeader(subtitle = "", logoSize = 52.dp)
                Surface(color = AccentGreenLight, shape = CircleShape, modifier = Modifier.size(64.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = AccentGreen, modifier = Modifier.size(38.dp))
                    }
                }
                Text("Registration Submitted!", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = TextPrimary)
                Surface(color = BrandBlueLight, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Unique Hunar Ji User ID", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = application.hunarUserId,
                            style = MaterialTheme.typography.headlineSmall.copy(color = BrandBlueDark, fontWeight = FontWeight.ExtraBold),
                            modifier = Modifier.testTag("generated_hunar_user_id")
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusPill(application.role)
                            StatusPill(application.kycStatus)
                        }
                    }
                }
                Text(
                    text = if (application.role == "CUSTOMER") {
                        "Your Customer account is active! Save your User ID (${application.hunarUserId}) or Mobile Number for fast sign-in."
                    } else {
                        "Your ${application.role} KYC application is queued for Admin verification. You can track real-time verification progress in your dashboard."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onContinue,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("continue_to_dashboard_button")
                ) {
                    Text("Continue to ${application.role.lowercase().replaceFirstChar { it.uppercase() }} Dashboard")
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}
