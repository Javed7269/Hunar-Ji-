package com.example

import com.example.core.security.CredentialSecurityHelper
import com.example.data.firebase.extractIsAdminClaim
import com.example.data.model.KycCatalogConstants
import com.example.data.model.KycDocStatus
import com.example.data.model.KycStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HunarJiUnitTest {

  @Test
  fun adminCustomClaimExtractionChecksIsAdminStrictly() {
    assertTrue(extractIsAdminClaim(mapOf("is_admin" to true)))
    assertTrue(extractIsAdminClaim(mapOf("is_admin" to "true")))
    assertFalse(extractIsAdminClaim(mapOf("is_admin" to false)))
    assertFalse(extractIsAdminClaim(mapOf("is_admin" to "false")))
    assertFalse(extractIsAdminClaim(mapOf("is_admin" to 1)))
    assertFalse(extractIsAdminClaim(mapOf("role" to "ADMIN")))
    assertFalse(extractIsAdminClaim(emptyMap()))
  }

  @Test
  fun otpGenerationAndMaskingProtectSensitiveNumbers() {
    val otp = CredentialSecurityHelper.generateSecureOtp()
    assertEquals(6, otp.length)
    assertTrue(otp.all(Char::isDigit))

    assertEquals("XXXX-XXXX-9012", CredentialSecurityHelper.maskAadhaar("123456789012"))
    assertEquals("ABXXX1234F", CredentialSecurityHelper.maskPan("ABCDE1234F"))
    assertEquals("XXXXXXXXX6789", CredentialSecurityHelper.maskBankAccount("1234567896789"))
  }

  @Test
  fun indianRegulatoryAndContactValidatorsWorkAccurately() {
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
  fun kycCatalogConstantsContainAllDepartmentsVehiclesAndRoleRules() {
    assertEquals(18, KycCatalogConstants.MECHANIC_DEPARTMENTS.size)
    assertTrue(KycCatalogConstants.MECHANIC_DEPARTMENTS.contains("Car Mechanic"))
    assertTrue(KycCatalogConstants.MECHANIC_DEPARTMENTS.contains("AC and Refrigeration Technician"))
    assertTrue(KycCatalogConstants.MECHANIC_DEPARTMENTS.contains("Other Services"))

    assertEquals(6, KycCatalogConstants.VEHICLE_TYPES.size)
    assertTrue(KycCatalogConstants.isMotorVehicle("Motorcycle / Bike"))
    assertTrue(KycCatalogConstants.isMotorVehicle("Scooter"))
    assertFalse(KycCatalogConstants.isMotorVehicle("Bicycle"))
    assertFalse(KycCatalogConstants.isMotorVehicle("Electric Bike"))

    assertFalse(
      KycCatalogConstants.isGstMandatoryForBusinessType(
        "Individual / Small Vendor (GST Not Required)"
      )
    )
    assertTrue(
      KycCatalogConstants.isGstMandatoryForBusinessType("Partnership Firm (GST Required)")
    )

    assertEquals("CUS", KycCatalogConstants.rolePrefix("CUSTOMER"))
    assertEquals("MEC", KycCatalogConstants.rolePrefix("MECHANIC"))
    assertEquals("SEL", KycCatalogConstants.rolePrefix("SELLER"))
    assertEquals("DEL", KycCatalogConstants.rolePrefix("DELIVERY"))

    assertEquals(KycStatus.APPROVED, KycStatus.fromRaw("APPROVED"))
    assertEquals(KycDocStatus.VERIFIED, KycDocStatus.fromRaw("VERIFIED"))
  }
}
