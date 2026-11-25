package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.core.security.CredentialSecurityHelper
import com.example.core.storage.PrivateKycStorage
import com.example.core.storage.RegistrationDraftStore
import com.example.data.model.KycCatalogConstants
import com.example.data.model.KycStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HunarJiRegistrationKycTest {

  @Test
  fun `password hashing produces salted non-plaintext PBKDF2 output and verifies cleanly`() {
    val rawPassword = "HunarJi#2026!"
    val hash1 = CredentialSecurityHelper.hashPassword(rawPassword)
    val hash2 = CredentialSecurityHelper.hashPassword(rawPassword)

    assertFalse(hash1.contains(rawPassword))
    assertTrue(hash1.contains(":"))
    assertNotEquals(hash1, hash2)
    assertTrue(CredentialSecurityHelper.verifyPassword(rawPassword, hash1))
    assertTrue(CredentialSecurityHelper.verifyPassword(rawPassword, hash2))
    assertFalse(CredentialSecurityHelper.verifyPassword("WrongPassword1", hash1))
    assertFalse(CredentialSecurityHelper.verifyPassword(rawPassword, "malformed_hash"))
  }

  @Test
  fun `otp generation and PII masking protect sensitive numbers`() {
    val otp = CredentialSecurityHelper.generateSecureOtp()
    assertEquals(6, otp.length)
    assertTrue(otp.all(Char::isDigit))

    assertEquals("XXXX-XXXX-9012", CredentialSecurityHelper.maskAadhaar("123456789012"))
    assertEquals("ABXXX1234F", CredentialSecurityHelper.maskPan("ABCDE1234F"))
    assertEquals("XXXXXXXXX6789", CredentialSecurityHelper.maskBankAccount("1234567896789"))
  }

  @Test
  fun `indian regulatory and contact validators work accurately`() {
    assertTrue(CredentialSecurityHelper.isValidMobile("9876543210"))
    assertFalse(CredentialSecurityHelper.isValidMobile("12345"))

    assertTrue(CredentialSecurityHelper.isValidEmail("raju@hunarji.in"))
    assertFalse(CredentialSecurityHelper.isValidEmail("notanemail"))

    assertTrue(CredentialSecurityHelper.isValidPan("ABCDE1234F"))
    assertFalse(CredentialSecurityHelper.isValidPan("12345ABCD"))

    assertTrue(CredentialSecurityHelper.isValidIfsc("SBIN0001234"))
    assertFalse(CredentialSecurityHelper.isValidIfsc("SBIN1001234"))

    assertTrue(CredentialSecurityHelper.isValidGstin("10ABCDE1234F1Z5"))
    assertFalse(CredentialSecurityHelper.isValidGstin("INVALIDGST"))

    assertTrue(CredentialSecurityHelper.isValidAadhaarOrLast4("123456789012"))
    assertTrue(CredentialSecurityHelper.isValidAadhaarOrLast4("9012"))
    assertFalse(CredentialSecurityHelper.isValidAadhaarOrLast4("123456"))
  }

  @Test
  fun `kyc catalog constants contain all 18 mechanic departments, 6 vehicles, and role rules`() {
    assertEquals(18, KycCatalogConstants.MECHANIC_DEPARTMENTS.size)
    assertTrue(KycCatalogConstants.MECHANIC_DEPARTMENTS.contains("Car Mechanic"))
    assertTrue(KycCatalogConstants.MECHANIC_DEPARTMENTS.contains("AC and Refrigeration Technician"))
    assertTrue(KycCatalogConstants.MECHANIC_DEPARTMENTS.contains("Other Services"))

    assertEquals(6, KycCatalogConstants.VEHICLE_TYPES.size)
    assertTrue(KycCatalogConstants.isMotorVehicle("Motorcycle / Bike"))
    assertTrue(KycCatalogConstants.isMotorVehicle("Scooter"))
    assertFalse(KycCatalogConstants.isMotorVehicle("Bicycle"))
    assertFalse(KycCatalogConstants.isMotorVehicle("Electric Bike"))

    assertFalse(KycCatalogConstants.isGstMandatoryForBusinessType("Individual / Small Vendor (GST Not Required)"))
    assertTrue(KycCatalogConstants.isGstMandatoryForBusinessType("Partnership Firm (GST Required)"))

    assertEquals("CUS", KycCatalogConstants.rolePrefix("CUSTOMER"))
    assertEquals("MEC", KycCatalogConstants.rolePrefix("MECHANIC"))
    assertEquals("SEL", KycCatalogConstants.rolePrefix("SELLER"))
    assertEquals("DEL", KycCatalogConstants.rolePrefix("DELIVERY"))
    assertEquals(KycStatus.APPROVED, KycStatus.fromRaw("APPROVED"))
  }

  @Test
  fun `registration draft store saves remember-me identifier without password and role drafts`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()

    RegistrationDraftStore.saveRememberedLogin(context, rememberMe = true, identifier = "HJ-MEC-100001")
    val remembered = RegistrationDraftStore.observeRememberedLogin(context).first()
    assertTrue(remembered.rememberMe)
    assertEquals("HJ-MEC-100001", remembered.savedIdentifier)

    RegistrationDraftStore.saveRememberedLogin(context, rememberMe = false, identifier = "HJ-MEC-100001")
    val clearedRemembered = RegistrationDraftStore.observeRememberedLogin(context).first()
    assertFalse(clearedRemembered.rememberMe)
    assertEquals("", clearedRemembered.savedIdentifier)

    RegistrationDraftStore.saveRoleDraft(
      context,
      "MECHANIC",
      mapOf("fullName" to "Raju Mechanic", "city" to "Patna", "mainDept" to "Car Mechanic")
    )
    val loadedDraft = RegistrationDraftStore.loadRoleDraft(context, "MECHANIC")
    assertEquals("Raju Mechanic", loadedDraft["fullName"])
    assertEquals("Patna", loadedDraft["city"])

    RegistrationDraftStore.clearRoleDraft(context, "MECHANIC")
    assertTrue(RegistrationDraftStore.loadRoleDraft(context, "MECHANIC").isEmpty())
  }

  @Test
  fun `private kyc storage confines copied documents to app-private kyc_docs directory and cleans up`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sourceFile = File(context.cacheDir, "sample_pan.jpg").apply {
      writeBytes(byteArrayOf(1, 2, 3, 4, 5, 6))
    }

    val stored = PrivateKycStorage.copyUriToPrivateStorage(
      context = context,
      uid = "uid_worker_1",
      docType = "PAN_CARD",
      sourceUri = Uri.fromFile(sourceFile)
    )

    val destFile = File(stored.privateFilePath)
    assertTrue(destFile.exists())
    assertTrue(stored.privateFilePath.contains("kyc_docs"))
    assertTrue(stored.privateFilePath.contains("uid_worker_1"))
    assertEquals(6L, stored.byteSize)

    PrivateKycStorage.deletePrivateDoc(stored.privateFilePath)
    assertFalse(destFile.exists())

    val stored2 = PrivateKycStorage.copyUriToPrivateStorage(
      context = context,
      uid = "uid_worker_1",
      docType = "AADHAAR_FRONT",
      sourceUri = Uri.fromFile(sourceFile)
    )
    assertTrue(File(stored2.privateFilePath).exists())
    PrivateKycStorage.clearAllUserPrivateDocs(context, "uid_worker_1")
    assertFalse(File(stored2.privateFilePath).exists())
    sourceFile.delete()
  }
}
