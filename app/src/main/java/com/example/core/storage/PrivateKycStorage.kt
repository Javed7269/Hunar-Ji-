package com.example.core.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

data class KycStoredFile(
    val fileName: String,
    val mimeType: String,
    val byteSize: Long,
    val privateFilePath: String
)

object PrivateKycStorage {

    private fun userKycDir(context: Context, uid: String): File {
        val safeUid = uid.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "draft_user" }
        return File(File(context.filesDir, "kyc_docs"), safeUid).apply { mkdirs() }
    }

    fun createTempCameraCaptureUri(context: Context, uid: String, docType: String): Pair<Uri, File> {
        val dir = userKycDir(context, uid)
        val safeType = docType.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val outFile = File(dir, "${safeType}_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outFile
        )
        return uri to outFile
    }

    fun copyUriToPrivateStorage(
        context: Context,
        uid: String,
        docType: String,
        sourceUri: Uri
    ): KycStoredFile {
        val resolver = context.contentResolver
        val mime = resolver.getType(sourceUri) ?: "image/jpeg"
        val ext = when {
            mime.contains("pdf", ignoreCase = true) -> "pdf"
            mime.contains("png", ignoreCase = true) -> "png"
            else -> "jpg"
        }
        val originalName = queryDisplayName(context, sourceUri)
            ?: "${docType.lowercase()}_${System.currentTimeMillis()}.$ext"
        val dir = userKycDir(context, uid)
        val safeDocName = "${docType}_${System.currentTimeMillis()}.$ext"
        val destFile = File(dir, safeDocName)

        resolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Unable to open selected document")

        return KycStoredFile(
            fileName = originalName,
            mimeType = mime,
            byteSize = destFile.length(),
            privateFilePath = destFile.absolutePath
        )
    }

    fun deletePrivateDoc(privateFilePath: String) {
        if (privateFilePath.isBlank()) return
        try {
            File(privateFilePath).takeIf { it.exists() }?.delete()
        } catch (_: Exception) {
        }
    }

    fun clearAllUserPrivateDocs(context: Context, uid: String) {
        try {
            userKycDir(context, uid).deleteRecursively()
        } catch (_: Exception) {
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }
}
