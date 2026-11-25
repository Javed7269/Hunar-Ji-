package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.storage.PrivateKycStorage
import com.example.data.model.KycApplication
import com.example.data.model.KycDocStatus
import com.example.data.model.KycDocumentRecord
import com.example.data.model.KycStatus
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberLight
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun KycDocumentUploadCard(
    uid: String,
    docType: String,
    docLabel: String,
    required: Boolean,
    allowPdf: Boolean = true,
    existingDoc: KycDocumentRecord?,
    onDocumentSelected: (KycDocumentRecord) -> Unit,
    onRemoveDocument: (KycDocumentRecord) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var uploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val captured = pendingCameraFile
        if (success && captured != null && captured.exists()) {
            val record = KycDocumentRecord(
                id = docType,
                docType = docType,
                docLabel = docLabel,
                fileName = captured.name,
                mimeType = "image/jpeg",
                byteSize = captured.length(),
                localPreviewPath = captured.absolutePath,
                status = KycDocStatus.SUBMITTED.name
            )
            onDocumentSelected(record)
        }
        pendingCameraFile = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                uploading = true
                uploadError = null
                try {
                    val stored = withContext(Dispatchers.IO) {
                        PrivateKycStorage.copyUriToPrivateStorage(context, uid, docType, uri)
                    }
                    onDocumentSelected(
                        KycDocumentRecord(
                            id = docType,
                            docType = docType,
                            docLabel = docLabel,
                            fileName = stored.fileName,
                            mimeType = stored.mimeType,
                            byteSize = stored.byteSize,
                            localPreviewPath = stored.privateFilePath,
                            status = KycDocStatus.SUBMITTED.name
                        )
                    )
                } catch (e: Exception) {
                    uploadError = e.localizedMessage ?: "Failed to load selected photo"
                } finally {
                    uploading = false
                }
            }
        }
    }

    val pdfDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                uploading = true
                uploadError = null
                try {
                    val stored = withContext(Dispatchers.IO) {
                        PrivateKycStorage.copyUriToPrivateStorage(context, uid, docType, uri)
                    }
                    onDocumentSelected(
                        KycDocumentRecord(
                            id = docType,
                            docType = docType,
                            docLabel = docLabel,
                            fileName = stored.fileName,
                            mimeType = stored.mimeType,
                            byteSize = stored.byteSize,
                            localPreviewPath = stored.privateFilePath,
                            status = KycDocStatus.SUBMITTED.name
                        )
                    )
                } catch (e: Exception) {
                    uploadError = e.localizedMessage ?: "Failed to load selected document"
                } finally {
                    uploading = false
                }
            }
        }
    }

    val statusEnum = KycDocStatus.fromRaw(existingDoc?.status ?: KycDocStatus.NOT_SUBMITTED.name)
    val locked = statusEnum == KycDocStatus.VERIFIED

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = 1.dp,
            color = when (statusEnum) {
                KycDocStatus.VERIFIED -> AccentGreen.copy(alpha = 0.5f)
                KycDocStatus.REJECTED, KycDocStatus.RESUBMIT_REQUIRED -> AccentRed.copy(alpha = 0.55f)
                KycDocStatus.SUBMITTED, KycDocStatus.UNDER_REVIEW -> BrandBlue.copy(alpha = 0.4f)
                else -> Color(0xFFE2E8F0)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kyc_doc_card_${docType.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = docLabel,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        if (required) {
                            Spacer(Modifier.width(4.dp))
                            Text("*", color = AccentRed, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = if (required) "Mandatory KYC Document" else "Optional Supporting Document",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                StatusPill(statusEnum.label)
            }

            if (uploading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Encrypting & saving to private app storage...", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
            }

            if (uploadError != null) {
                Text(uploadError ?: "", style = MaterialTheme.typography.labelSmall, color = AccentRed)
            }

            if (existingDoc != null) {
                val isPdf = existingDoc.mimeType.contains("pdf", ignoreCase = true) ||
                    existingDoc.fileName.endsWith(".pdf", ignoreCase = true)
                val localFile = existingDoc.localPreviewPath.takeIf { it.isNotBlank() }?.let { File(it) }

                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isPdf && localFile != null && localFile.exists()) {
                            AsyncImage(
                                model = localFile,
                                contentDescription = docLabel,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isPdf) AccentRedLight else BrandBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Description,
                                    contentDescription = docLabel,
                                    tint = if (isPdf) AccentRed else BrandBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = existingDoc.fileName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val kb = (existingDoc.byteSize / 1024L).coerceAtLeast(1L)
                            Text(
                                text = "${if (isPdf) "PDF Document" else "Image"} • ${kb} KB",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        if (!locked) {
                            IconButton(
                                onClick = {
                                    PrivateKycStorage.deletePrivateDoc(existingDoc.localPreviewPath)
                                    onRemoveDocument(existingDoc)
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove document", tint = AccentRed)
                            }
                        }
                    }
                }
            }

            if (!existingDoc?.adminNote.isNullOrBlank()) {
                Surface(color = AccentAmberLight, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Reviewer Note: ${existingDoc?.adminNote}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (!locked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val (uri, file) = PrivateKycStorage.createTempCameraCaptureUri(context, uid, docType)
                            pendingCameraFile = file
                            cameraLauncher.launch(uri)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (existingDoc == null) Icons.Default.CameraAlt else Icons.Default.Refresh,
                            contentDescription = "Capture photo",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (existingDoc == null) "Camera" else "Retake", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Gallery", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                    if (allowPdf) {
                        OutlinedButton(
                            onClick = {
                                pdfDocLauncher.launch(arrayOf("application/pdf", "image/*"))
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = "Upload PDF", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PDF/Doc", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KycVerificationStatusCard(
    kyc: KycApplication?,
    roleLabel: String,
    hunarUserId: String,
    fallbackStatus: String,
    onOpenKycForm: () -> Unit
) {
    val status = KycStatus.fromRaw(kyc?.kycStatus ?: fallbackStatus)
    val (bg, fg, icon) = when (status) {
        KycStatus.APPROVED -> Triple(AccentGreenLight, AccentGreen, Icons.Default.CheckCircle)
        KycStatus.REJECTED, KycStatus.RESUBMISSION_REQUIRED -> Triple(AccentRedLight, AccentRed, Icons.Default.WarningAmber)
        KycStatus.UNDER_REVIEW, KycStatus.SUBMITTED, KycStatus.PENDING -> Triple(AccentAmberLight, Color(0xFF92400E), Icons.Default.HourglassTop)
        else -> Triple(BrandBlueLight, BrandBlueDark, Icons.Default.VerifiedUser)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, fg.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kyc_status_card")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(color = bg, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(38.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("$roleLabel KYC Verification", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text(
                            text = if (hunarUserId.isNotBlank()) "ID: $hunarUserId" else "Registration incomplete",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandBlue
                        )
                    }
                }
                StatusPill(status.name)
            }

            // Progress bar tracker
            val progress = when (status) {
                KycStatus.NOT_SUBMITTED -> 0.1f
                KycStatus.SUBMITTED, KycStatus.PENDING -> 0.45f
                KycStatus.UNDER_REVIEW -> 0.72f
                KycStatus.APPROVED -> 1f
                KycStatus.REJECTED, KycStatus.RESUBMISSION_REQUIRED -> 0.35f
            }
            LinearProgressIndicator(
                progress = { progress },
                color = fg,
                trackColor = Color(0xFFE2E8F0),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
            )

            if (kyc != null) {
                val checks = buildList {
                    add("Identity" to kyc.aadhaarVerified)
                    add("PAN" to kyc.panVerified)
                    add("Address" to kyc.addressVerified)
                    add("Bank" to kyc.bankVerified)
                    if (kyc.role == "MECHANIC") add("Skills" to kyc.skillVerified)
                    if (kyc.role == "SELLER") {
                        add("Shop" to kyc.shopAddressVerified)
                        if (kyc.gstRequired || kyc.gstin.isNotBlank()) add("GST" to kyc.gstVerified)
                    }
                    if (kyc.role == "DELIVERY") {
                        add("DL" to kyc.drivingLicenceVerified)
                        add("Vehicle" to kyc.vehicleDocVerified)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    checks.take(4).forEach { (lbl, ok) ->
                        Surface(
                            color = if (ok) AccentGreenLight else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${if (ok) "✓" else "•"} $lbl",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (ok) AccentGreen else TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
                if (kyc.aadhaarMasked.isNotBlank() || kyc.panNumberMasked.isNotBlank()) {
                    Text(
                        text = listOfNotNull(
                            kyc.aadhaarMasked.takeIf { it.isNotBlank() }?.let { "Aadhaar: $it" },
                            kyc.panNumberMasked.takeIf { it.isNotBlank() }?.let { "PAN: $it" },
                            kyc.bankAccountMasked.takeIf { it.isNotBlank() }?.let { "Bank: $it" }
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            if (!kyc?.adminNotes.isNullOrBlank()) {
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Text(
                    text = "Admin Remarks: ${kyc?.adminNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = fg
                )
            }

            if (status != KycStatus.APPROVED) {
                Button(
                    onClick = onOpenKycForm,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_kyc_wizard_button")
                ) {
                    Text(
                        text = when (status) {
                            KycStatus.NOT_SUBMITTED -> "Complete $roleLabel Registration & KYC"
                            KycStatus.RESUBMISSION_REQUIRED, KycStatus.REJECTED -> "Fix & Re-submit KYC Documents"
                            else -> "View / Update KYC Application"
                        }
                    )
                }
            }
        }
    }
}
