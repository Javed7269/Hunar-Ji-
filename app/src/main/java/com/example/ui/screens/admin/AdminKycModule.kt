package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.KycApplication
import com.example.data.model.KycDocumentRecord
import com.example.data.model.VerificationHistoryEntry
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

@Composable
fun AdminKycManagementModule(
    applications: List<KycApplication>,
    inspectedDocs: List<KycDocumentRecord>,
    inspectedHistory: List<VerificationHistoryEntry>,
    globalSearch: String,
    isAdminUser: Boolean,
    onInspectApplicant: (String?) -> Unit,
    onUpdateKycStatus: (
        targetUid: String,
        newKycStatus: String,
        accountStatus: String,
        verificationFlags: Map<String, Boolean>,
        adminNotes: String
    ) -> Unit,
    onUpdateDocumentStatus: (targetUid: String, docId: String, status: String, adminNote: String) -> Unit,
    onLogDocumentAccess: (targetUid: String, docId: String, docLabel: String) -> Unit
) {
    var statusFilter by remember { mutableStateOf("ALL") }
    var roleFilter by remember { mutableStateOf("ALL") }
    var localSearch by remember { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<KycApplication?>(null) }

    val combinedSearch = (localSearch.ifBlank { globalSearch }).trim()

    val filtered = remember(applications, statusFilter, roleFilter, combinedSearch) {
        applications.filter { app ->
            val stOk = when (statusFilter) {
                "ALL" -> true
                "PENDING" -> app.kycStatus in listOf("PENDING", "SUBMITTED")
                else -> app.kycStatus.equals(statusFilter, ignoreCase = true)
            }
            val roleOk = roleFilter == "ALL" || app.role.equals(roleFilter, ignoreCase = true)
            val queryOk = combinedSearch.isBlank() ||
                app.fullName.contains(combinedSearch, true) ||
                app.mobile.contains(combinedSearch, true) ||
                app.hunarUserId.contains(combinedSearch, true) ||
                app.district.contains(combinedSearch, true) ||
                app.shopName.contains(combinedSearch, true) ||
                app.mainDepartment.contains(combinedSearch, true) ||
                app.panNumberMasked.contains(combinedSearch, true)
            stOk && roleOk && queryOk
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("KYC & Registration Verification", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                OutlinedTextField(
                    value = localSearch,
                    onValueChange = { localSearch = it },
                    placeholder = { Text("Search by Name, User ID (HJ-...), Mobile, District, Shop...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_kyc_search")
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val statuses = listOf("ALL", "PENDING", "UNDER_REVIEW", "APPROVED", "RESUBMISSION_REQUIRED", "REJECTED")
                    items(statuses) { st ->
                        FilterChip(
                            selected = statusFilter == st,
                            onClick = { statusFilter = st },
                            label = { Text(st.replace('_', ' ')) }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val roles = listOf("ALL", "CUSTOMER", "MECHANIC", "SELLER", "DELIVERY")
                    items(roles) { rl ->
                        FilterChip(
                            selected = roleFilter == rl,
                            onClick = { roleFilter = rl },
                            label = { Text(rl) }
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.VerifiedUser,
                    title = "No KYC Applications Match",
                    subtitle = "Newly submitted Customer, Mechanic, Seller, and Delivery KYC applications appear here in real time."
                )
            }
        } else {
            items(filtered, key = { it.userId.ifBlank { it.id } }) { app ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.fullName.ifBlank { "Unnamed Applicant" }, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${app.hunarUserId.ifBlank { app.userId }} • ${app.mobile}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandBlueDark
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusPill(app.role)
                                StatusPill(app.kycStatus)
                            }
                        }

                        val summary = when (app.role) {
                            "MECHANIC" -> "Dept: ${app.mainDepartment} • Exp: ${app.experienceYears} yrs • ${app.district}"
                            "SELLER" -> "Shop: ${app.shopName} (${app.businessType}) • ${app.district}"
                            "DELIVERY" -> "Vehicle: ${app.vehicleType} • ${app.preferredDistrict.ifBlank { app.district }}"
                            else -> "Customer • ${app.city}, ${app.district}"
                        }
                        Text(summary, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusPill(app.accountStatus)
                            Button(
                                onClick = {
                                    onInspectApplicant(app.userId.ifBlank { app.id })
                                    selectedApp = app
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("review_kyc_${app.hunarUserId.ifBlank { app.userId }}")
                            ) {
                                Text("Review & Verify")
                            }
                        }
                    }
                }
            }
        }
    }

    selectedApp?.let { app ->
        val uid = app.userId.ifBlank { app.id }
        val latest = applications.firstOrNull { it.userId == uid || it.id == uid } ?: app
        AdminKycReviewDialog(
            app = latest,
            docs = inspectedDocs,
            history = inspectedHistory,
            isAdminUser = isAdminUser,
            onUpdateKycStatus = { newKyc, accStatus, flags, notes ->
                onUpdateKycStatus(uid, newKyc, accStatus, flags, notes)
                selectedApp = null
                onInspectApplicant(null)
            },
            onUpdateDocumentStatus = { docId, st, note ->
                onUpdateDocumentStatus(uid, docId, st, note)
            },
            onLogDocumentAccess = { docId, docLabel ->
                onLogDocumentAccess(uid, docId, docLabel)
            },
            onDismiss = {
                selectedApp = null
                onInspectApplicant(null)
            }
        )
    }
}

@Composable
private fun AdminKycReviewDialog(
    app: KycApplication,
    docs: List<KycDocumentRecord>,
    history: List<VerificationHistoryEntry>,
    isAdminUser: Boolean,
    onUpdateKycStatus: (String, String, Map<String, Boolean>, String) -> Unit,
    onUpdateDocumentStatus: (String, String, String) -> Unit,
    onLogDocumentAccess: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var aadhaarVerified by remember(app.userId) { mutableStateOf(app.aadhaarVerified) }
    var panVerified by remember(app.userId) { mutableStateOf(app.panVerified) }
    var addressVerified by remember(app.userId) { mutableStateOf(app.addressVerified) }
    var bankVerified by remember(app.userId) { mutableStateOf(app.bankVerified) }
    var skillVerified by remember(app.userId) { mutableStateOf(app.skillVerified) }
    var shopVerified by remember(app.userId) { mutableStateOf(app.shopAddressVerified) }
    var gstVerified by remember(app.userId) { mutableStateOf(app.gstVerified) }
    var docsVerified by remember(app.userId) { mutableStateOf(app.documentsVerified) }
    var dlVerified by remember(app.userId) { mutableStateOf(app.drivingLicenceVerified) }
    var vehicleVerified by remember(app.userId) { mutableStateOf(app.vehicleDocVerified) }
    var adminNotes by remember(app.userId) { mutableStateOf(app.adminNotes) }
    var previewDoc by remember { mutableStateOf<KycDocumentRecord?>(null) }

    fun collectFlags(): Map<String, Boolean> = mapOf(
        "aadhaarVerified" to aadhaarVerified,
        "panVerified" to panVerified,
        "addressVerified" to addressVerified,
        "bankVerified" to bankVerified,
        "skillVerified" to skillVerified,
        "shopAddressVerified" to shopVerified,
        "gstVerified" to gstVerified,
        "documentsVerified" to docsVerified,
        "drivingLicenceVerified" to dlVerified,
        "vehicleDocVerified" to vehicleVerified
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.fullName.ifBlank { "Applicant" }, style = MaterialTheme.typography.titleLarge)
                    Text("${app.hunarUserId} • ${app.role}", style = MaterialTheme.typography.labelSmall, color = BrandBlueDark)
                }
                StatusPill(app.kycStatus)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Contact & Address
                item {
                    SectionBox("Personal & Contact") {
                        DetailLine("Mobile", "${app.mobile} (OTP: ${if (app.mobileOtpVerified) "Verified" else "Pending"})")
                        DetailLine("Email", app.email)
                        if (app.dob.isNotBlank()) DetailLine("DOB / Gender", "${app.dob} • ${app.gender}")
                        DetailLine("Address", "${app.fullAddress}, ${app.city}, ${app.district}, ${app.state} - ${app.pincode}")
                        if (app.gpsLocation.isNotBlank()) DetailLine("GPS", app.gpsLocation)
                    }
                }

                // Masked Identity & Bank (Workers only)
                if (app.role != "CUSTOMER") {
                    item {
                        SectionBox("Identity & Bank (Masked PII)") {
                            DetailLine("Aadhaar", app.aadhaarMasked.ifBlank { "Not provided" })
                            DetailLine("PAN", app.panNumberMasked.ifBlank { "Not provided" })
                            DetailLine("Bank Holder", app.bankHolderName)
                            DetailLine("Bank / Account", "${app.bankName} • ${app.bankAccountMasked}")
                            DetailLine("IFSC", app.ifscCode)
                        }
                    }
                }

                // Role Specific Section
                if (app.role == "MECHANIC") {
                    item {
                        SectionBox("Mechanic Department & Skills") {
                            DetailLine("Main Dept", app.mainDepartment)
                            if (app.departments.isNotEmpty()) DetailLine("Extra Depts", app.departments.joinToString(", "))
                            DetailLine("Skills", app.specificSkills.joinToString(", "))
                            DetailLine("Experience", "${app.experienceYears} yrs • Charges: ₹${app.serviceCharges.toInt()}")
                            DetailLine("Hours & Emergency", "${app.workingHours} • Emergency: ${if (app.emergencyAvailable) "Yes" else "No"}")
                            DetailLine("Service Area", "${app.serviceAreas} (${app.serviceRadiusKm} km radius)")
                        }
                    }
                } else if (app.role == "SELLER") {
                    item {
                        SectionBox("Shop & Business Details") {
                            DetailLine("Shop / Business", "${app.shopName} / ${app.businessName}")
                            DetailLine("Type / Category", "${app.businessType} • ${app.shopCategory}")
                            DetailLine("Timings", "${app.shopOpeningTime} - ${app.shopClosingTime} (Off: ${app.weeklyClosingDay})")
                            DetailLine("Mode", "${app.wholesaleRetail} • Delivery: ${app.deliveryAvailable} • Pickup: ${app.shopPickupAvailable}")
                            DetailLine("GST", if (app.gstRequired || app.gstin.isNotBlank()) app.gstin.ifBlank { "Required" } else "Not Applicable")
                            if (app.productCategories.isNotEmpty()) DetailLine("Categories", app.productCategories.joinToString(", "))
                        }
                    }
                } else if (app.role == "DELIVERY") {
                    item {
                        SectionBox("Vehicle & Delivery Preferences") {
                            DetailLine("Vehicle", "${app.vehicleType} • ${app.vehicleBrand} ${app.vehicleModel} (${app.vehicleColor})")
                            if (app.vehicleRegNumber.isNotBlank()) DetailLine("Reg Number", app.vehicleRegNumber)
                            if (app.drivingLicenceNumber.isNotBlank()) DetailLine("DL Number", app.drivingLicenceNumber)
                            DetailLine("Work Area", "${app.preferredDeliveryArea}, ${app.preferredCity}, ${app.preferredDistrict}")
                            DetailLine("Shift", "${app.employmentType} • ${app.deliveryWorkingHours}")
                        }
                    }
                }

                // Verification Toggles
                if (app.role != "CUSTOMER") {
                    item {
                        SectionBox("Verification Checklist") {
                            VerifyToggleRow("Aadhaar Verified", aadhaarVerified) { aadhaarVerified = it }
                            VerifyToggleRow("PAN Verified", panVerified) { panVerified = it }
                            VerifyToggleRow("Address Verified", addressVerified) { addressVerified = it }
                            VerifyToggleRow("Bank Verified", bankVerified) { bankVerified = it }
                            if (app.role == "MECHANIC") {
                                VerifyToggleRow("Mechanic Skill Verified", skillVerified) { skillVerified = it }
                            }
                            if (app.role == "SELLER") {
                                VerifyToggleRow("Shop Address Verified", shopVerified) { shopVerified = it }
                                VerifyToggleRow("Shop Documents Verified", docsVerified) { docsVerified = it }
                                if (app.gstRequired || app.gstin.isNotBlank()) {
                                    VerifyToggleRow("GSTIN Verified", gstVerified) { gstVerified = it }
                                }
                            }
                            if (app.role == "DELIVERY") {
                                VerifyToggleRow("Driving Licence Verified", dlVerified) { dlVerified = it }
                                VerifyToggleRow("Vehicle Doc Verified", vehicleVerified) { vehicleVerified = it }
                            }
                        }
                    }
                }

                // Uploaded Documents
                item {
                    SectionBox("Submitted KYC Documents (${docs.size})") {
                        if (docs.isEmpty()) {
                            Text("No document files uploaded.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        } else {
                            docs.forEach { doc ->
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (doc.mimeType.contains("pdf", true)) Icons.Default.PictureAsPdf else Icons.Default.Description,
                                                contentDescription = null,
                                                tint = BrandBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(doc.docLabel, style = MaterialTheme.typography.titleSmall)
                                                Text(doc.fileName, style = MaterialTheme.typography.labelSmall, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            StatusPill(doc.status)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    onLogDocumentAccess(doc.id, doc.docLabel)
                                                    previewDoc = doc
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("View", style = MaterialTheme.typography.labelSmall)
                                            }
                                            if (isAdminUser) {
                                                OutlinedButton(
                                                    onClick = { onUpdateDocumentStatus(doc.id, "VERIFIED", "Verified by Admin") },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) { Text("Verify", style = MaterialTheme.typography.labelSmall, color = AccentGreen) }
                                                OutlinedButton(
                                                    onClick = { onUpdateDocumentStatus(doc.id, "RESUBMIT_REQUIRED", "Clearer copy required") },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) { Text("Resubmit", style = MaterialTheme.typography.labelSmall, color = AccentRed) }
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Admin Remarks & Actions
                item {
                    OutlinedTextField(
                        value = adminNotes,
                        onValueChange = { adminNotes = it },
                        label = { Text("Admin Remarks / Rejection Reason") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_kyc_notes_input")
                    )
                }

                if (isAdminUser) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val allFlags = collectFlags().toMutableMap().apply {
                                            put("aadhaarVerified", true)
                                            put("addressVerified", true)
                                        }
                                        onUpdateKycStatus("APPROVED", "ACTIVE", allFlags, adminNotes.ifBlank { "Verified & Approved by Admin" })
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                    modifier = Modifier.weight(1f).testTag("admin_approve_kyc_button")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Approve KYC")
                                }
                                OutlinedButton(
                                    onClick = {
                                        onUpdateKycStatus("UNDER_REVIEW", app.accountStatus, collectFlags(), adminNotes.ifBlank { "Under Admin Review" })
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Under Review")
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        onUpdateKycStatus("RESUBMISSION_REQUIRED", "ACTIVE", collectFlags(), adminNotes.ifBlank { "Please upload clearer documents" })
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Resubmit")
                                }
                                OutlinedButton(
                                    onClick = {
                                        onUpdateKycStatus("REJECTED", "ACTIVE", collectFlags(), adminNotes.ifBlank { "KYC rejected by Admin" })
                                    },
                                    border = BorderStroke(1.dp, AccentRed),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                                    modifier = Modifier.weight(1f).testTag("admin_reject_kyc_button")
                                ) { Text("Reject") }
                            }
                            val nextAccountStatus = if (app.accountStatus == "SUSPENDED") "ACTIVE" else "SUSPENDED"
                            OutlinedButton(
                                onClick = {
                                    onUpdateKycStatus(app.kycStatus, nextAccountStatus, collectFlags(), adminNotes.ifBlank { "Account status -> $nextAccountStatus" })
                                },
                                border = BorderStroke(1.dp, if (nextAccountStatus == "SUSPENDED") AccentRed else AccentGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (app.accountStatus == "SUSPENDED") "Re-Activate Account" else "Suspend Account")
                            }
                        }
                    }
                }

                // Verification History Timeline
                if (history.isNotEmpty()) {
                    item {
                        SectionBox("Verification Audit History") {
                            history.take(8).forEach { entry ->
                                Text(
                                    text = "• ${entry.action}: ${entry.note} (${entry.actorEmail})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )

    previewDoc?.let { doc ->
        val file = doc.localPreviewPath.takeIf { it.isNotBlank() }?.let { File(it) }
        val isPdf = doc.mimeType.contains("pdf", true) || doc.fileName.endsWith(".pdf", true)
        AlertDialog(
            onDismissRequest = { previewDoc = null },
            title = { Text(doc.docLabel) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!isPdf && file != null && file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = doc.docLabel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(BrandBlueLight, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = AccentRed, modifier = Modifier.size(44.dp))
                                Spacer(Modifier.height(6.dp))
                                Text(doc.fileName, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                    Text("Type: ${doc.mimeType} • Size: ${(doc.byteSize / 1024L).coerceAtLeast(1L)} KB", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("Access logged to Verification Audit History.", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                }
            },
            confirmButton = {
                TextButton(onClick = { previewDoc = null }) { Text("Close Preview") }
            }
        )
    }
}

@Composable
private fun SectionBox(title: String, content: @Composable () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(color = BrandBlueDark, fontWeight = FontWeight.Bold))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            content()
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    if (value.isNotBlank()) {
        Text("$label: $value", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
    }
}

@Composable
private fun VerifyToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onToggle(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        Checkbox(checked = checked, onCheckedChange = onToggle)
    }
}
