package com.example.core.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object CredentialSecurityHelper {

    private const val PBKDF2_ITERATIONS = 65536
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    fun hashPassword(password: String): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES)
        random.nextBytes(salt)
        val hash = pbkdf2(password.toCharArray(), salt)
        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashB64 = Base64.encodeToString(hash, Base64.NO_WRAP)
        return "$saltB64:$hashB64"
    }

    fun verifyPassword(password: String, stored: String): Boolean {
        if (stored.isBlank() || !stored.contains(":")) return false
        val parts = stored.split(":", limit = 2)
        if (parts.size != 2) return false
        return try {
            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val expectedHash = Base64.decode(parts[1], Base64.NO_WRAP)
            val actualHash = pbkdf2(password.toCharArray(), salt)
            MessageDigest.isEqual(expectedHash, actualHash)
        } catch (_: Exception) {
            false
        }
    }

    private fun pbkdf2(chars: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(chars, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } catch (_: Exception) {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun generateSecureOtp(): String {
        val code = 100000 + SecureRandom().nextInt(900000)
        return code.toString()
    }

    fun maskAadhaar(raw: String): String {
        val digits = raw.filter(Char::isDigit)
        if (digits.length < 4) return "XXXX-XXXX-XXXX"
        val last4 = digits.takeLast(4)
        return "XXXX-XXXX-$last4"
    }

    fun maskPan(raw: String): String {
        val clean = raw.trim().uppercase()
        if (clean.length < 5) return "XXXXX"
        return "${clean.take(2)}XXX${clean.drop(5)}"
    }

    fun maskBankAccount(raw: String): String {
        val clean = raw.filter(Char::isDigit)
        if (clean.length <= 4) return "XXXX$clean"
        return "X".repeat((clean.length - 4).coerceAtLeast(4)) + clean.takeLast(4)
    }

    fun isValidMobile(mobile: String): Boolean {
        val digits = mobile.filter(Char::isDigit)
        return digits.length in 10..13
    }

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.contains("@") && trimmed.contains(".") && trimmed.length >= 5
    }

    fun isValidPan(pan: String): Boolean {
        return pan.trim().uppercase().matches(Regex("^[A-Z]{5}[0-9]{4}[A-Z]$"))
    }

    fun isValidIfsc(ifsc: String): Boolean {
        return ifsc.trim().uppercase().matches(Regex("^[A-Z]{4}0[A-Z0-9]{6}$"))
    }

    fun isValidGstin(gstin: String): Boolean {
        return gstin.trim().uppercase().matches(Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$"))
    }

    fun isValidAadhaarOrLast4(aadhaar: String): Boolean {
        val digits = aadhaar.filter(Char::isDigit)
        return digits.length == 12 || digits.length == 4
    }
}
