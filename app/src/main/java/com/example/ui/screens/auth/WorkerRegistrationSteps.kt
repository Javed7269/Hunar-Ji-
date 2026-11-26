package com.example.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.KycCatalogConstants
import com.example.data.model.KycDocumentRecord
import com.example.ui.components.KycDocumentUploadCard
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegPersonalInfoSection(
    fullName: String, onFullName: (String) -> Unit,
    mobile: String, onMobile: (String) -> Unit,
    email: String, onEmail: (String) -> Unit,
    password: String, onPassword: (String) -> Unit,
    confirmPassword: String, onConfirmPassword: (String) -> Unit,
    includeDobGender: Boolean,
    dob: String, onDob: (String) -> Unit,
    gender: String, onGender: (String) -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = fullName,
            onValueChange = onFullName,
            label = { Text("Full Name *") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandBlue) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = mobile,
            onValueChange = { onMobile(it.filter { ch -> ch.isDigit() || ch == '+' }.take(13)) },
            label = { Text("Mobile Number *") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BrandBlue) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = onEmail,
            label = { Text("Email Address *") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandBlue) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (includeDobGender) {
            OutlinedTextField(
                value = dob,
                onValueChange = onDob,
                label = { Text("Date of Birth (DD/MM/YYYY)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Text("Gender (Optional)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Male", "Female", "Other").forEach { g ->
                    FilterChip(selected = gender == g, onClick = { onGender(g) }, label = { Text(g) })
                }
            }
        }
        OutlinedTextField(
            value = password,
            onValueChange = onPassword,
            label = { Text("Password *") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandBlue) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                        tint = TextMuted
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPassword,
            label = { Text("Confirm Password *") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandBlue) },
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showConfirmPassword) "Hide password" else "Show password",
                        tint = TextMuted
                    )
                }
            },
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun RegAddressSection(
    title: String,
    houseNo: String, onHouseNo: (String) -> Unit,
    streetRoad: String, onStreetRoad: (String) -> Unit,
    villageTown: String, onVillageTown: (String) -> Unit,
    postOffice: String, onPostOffice: (String) -> Unit,
    blockName: String, onBlockName: (String) -> Unit,
    areaLocality: String, onAreaLocality: (String) -> Unit,
    landmark: String, onLandmark: (String) -> Unit,
    state: String, onState: (String) -> Unit,
    district: String, onDistrict: (String) -> Unit,
    city: String, onCity: (String) -> Unit,
    pincode: String, onPincode: (String) -> Unit,
    gpsLocation: String,
    onCaptureGps: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = BrandBlueDark)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = houseNo, onValueChange = onHouseNo, label = { Text("House / Building *") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(value = pincode, onValueChange = { onPincode(it.filter(Char::isDigit).take(6)) }, label = { Text("Pincode *") }, singleLine = true, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = streetRoad, onValueChange = onStreetRoad, label = { Text("Street / Road *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = villageTown, onValueChange = onVillageTown, label = { Text("Village / Town") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(value = areaLocality, onValueChange = onAreaLocality, label = { Text("Area / Locality *") }, singleLine = true, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = postOffice, onValueChange = onPostOffice, label = { Text("Post Office") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = blockName, onValueChange = onBlockName, label = { Text("Block / Tehsil") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = landmark, onValueChange = onLandmark, label = { Text("Landmark") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = city, onValueChange = onCity, label = { Text("City *") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(value = district, onValueChange = onDistrict, label = { Text("District *") }, singleLine = true, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = state, onValueChange = onState, label = { Text("State *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = onCaptureGps, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (gpsLocation.isBlank()) "Capture Current GPS Location" else "GPS: $gpsLocation")
        }
    }
}

@Composable
fun RegIdentityAndBankSection(
    aadhaar: String, onAadhaar: (String) -> Unit,
    pan: String, onPan: (String) -> Unit,
    bankHolder: String, onBankHolder: (String) -> Unit,
    bankName: String, onBankName: (String) -> Unit,
    bankAcc: String, onBankAcc: (String) -> Unit,
    ifsc: String, onIfsc: (String) -> Unit,
    kycConsent: Boolean, onKycConsent: (Boolean) -> Unit,
    showGst: Boolean = false,
    gstMandatory: Boolean = false,
    gstin: String = "", onGstin: (String) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Identity, PAN & Bank KYC", style = MaterialTheme.typography.titleMedium, color = BrandBlueDark)
        Surface(color = BrandBlueLight, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = kycConsent, onCheckedChange = onKycConsent)
                Spacer(Modifier.width(6.dp))
                Text(
                    "I voluntarily consent to share my masked Aadhaar, PAN & Bank details for Hunar Ji KYC verification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandBlueDark
                )
            }
        }
        OutlinedTextField(
            value = aadhaar,
            onValueChange = { onAadhaar(it.filter(Char::isDigit).take(12)) },
            label = { Text("Aadhaar Number (12 digits or last 4) *") },
            supportingText = { Text("Stored masked (XXXX-XXXX-1234) for privacy protection") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = pan,
            onValueChange = { onPan(it.uppercase().take(10)) },
            label = { Text("PAN Number (e.g. ABCDE1234F) *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (showGst) {
            OutlinedTextField(
                value = gstin,
                onValueChange = { onGstin(it.uppercase().take(15)) },
                label = { Text(if (gstMandatory) "GSTIN Number (Required) *" else "GSTIN Number (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Text("Bank Account for Payouts", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
        OutlinedTextField(value = bankHolder, onValueChange = onBankHolder, label = { Text("Account Holder Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = bankName, onValueChange = onBankName, label = { Text("Bank Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = bankAcc, onValueChange = { onBankAcc(it.filter(Char::isDigit).take(20)) }, label = { Text("Account Number *") }, singleLine = true, modifier = Modifier.weight(1.3f))
            OutlinedTextField(value = ifsc, onValueChange = { onIfsc(it.uppercase().take(11)) }, label = { Text("IFSC Code *") }, singleLine = true, modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MechanicProfessionalSection(
    mainDepartment: String, onMainDepartment: (String) -> Unit,
    departments: List<String>, onToggleDepartment: (String) -> Unit,
    specialization: String, onSpecialization: (String) -> Unit,
    specificSkills: List<String>, onToggleSkill: (String) -> Unit,
    expYears: String, onExpYears: (String) -> Unit,
    prevWork: String, onPrevWork: (String) -> Unit,
    certName: String, onCertName: (String) -> Unit,
    serviceDesc: String, onServiceDesc: (String) -> Unit,
    serviceCharges: String, onServiceCharges: (String) -> Unit,
    workingHours: String, onWorkingHours: (String) -> Unit,
    availableDays: List<String>, onToggleDay: (String) -> Unit,
    emergencyAvailable: Boolean, onEmergency: (Boolean) -> Unit,
    serviceAreas: String, onServiceAreas: (String) -> Unit,
    serviceRadiusKm: String, onServiceRadiusKm: (String) -> Unit,
    preferredArea: String, onPreferredArea: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Mechanic Department & Skills", style = MaterialTheme.typography.titleMedium, color = BrandBlueDark)
        Text("Select Main Department * (18 Service Trades)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KycCatalogConstants.MECHANIC_DEPARTMENTS.forEach { dept ->
                FilterChip(
                    selected = mainDepartment == dept,
                    onClick = { onMainDepartment(dept) },
                    label = { Text(dept) }
                )
            }
        }
        Text("Additional Departments (Optional Multi-select)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KycCatalogConstants.MECHANIC_DEPARTMENTS.take(12).forEach { dept ->
                FilterChip(
                    selected = departments.contains(dept),
                    onClick = { onToggleDepartment(dept) },
                    label = { Text(dept, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        OutlinedTextField(value = specialization, onValueChange = onSpecialization, label = { Text("Specialization (e.g. BS6 Engine & AC)") }, modifier = Modifier.fillMaxWidth())
        Text("Specific Skills", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KycCatalogConstants.MECHANIC_SKILLS.forEach { sk ->
                FilterChip(
                    selected = specificSkills.contains(sk),
                    onClick = { onToggleSkill(sk) },
                    label = { Text(sk, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = expYears, onValueChange = { onExpYears(it.filter(Char::isDigit).take(2)) }, label = { Text("Experience (yrs) *") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = serviceCharges, onValueChange = { onServiceCharges(it.filter(Char::isDigit).take(6)) }, label = { Text("Base Visit Charge (₹) *") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = prevWork, onValueChange = onPrevWork, label = { Text("Previous Work Experience") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = certName, onValueChange = onCertName, label = { Text("ITI / Technical Certificate (Optional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = serviceDesc, onValueChange = onServiceDesc, label = { Text("Service Description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = workingHours, onValueChange = onWorkingHours, label = { Text("Working Hours") }, modifier = Modifier.fillMaxWidth())
        Text("Available Days", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KycCatalogConstants.WEEK_DAYS.forEach { day ->
                FilterChip(selected = availableDays.contains(day), onClick = { onToggleDay(day) }, label = { Text(day) })
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("24x7 Emergency Breakdown Service", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Switch(checked = emergencyAvailable, onCheckedChange = onEmergency)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = serviceAreas, onValueChange = onServiceAreas, label = { Text("Service Areas *") }, modifier = Modifier.weight(1.4f))
            OutlinedTextField(value = serviceRadiusKm, onValueChange = { onServiceRadiusKm(it.filter(Char::isDigit).take(3)) }, label = { Text("Radius (km)") }, modifier = Modifier.weight(0.8f))
        }
        OutlinedTextField(value = preferredArea, onValueChange = onPreferredArea, label = { Text("Preferred Working Area") }, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SellerBusinessSection(
    shopName: String, onShopName: (String) -> Unit,
    businessName: String, onBusinessName: (String) -> Unit,
    businessType: String, onBusinessType: (String) -> Unit,
    shopCategory: String, onShopCategory: (String) -> Unit,
    shopDesc: String, onShopDesc: (String) -> Unit,
    shopContact: String, onShopContact: (String) -> Unit,
    businessEmail: String, onBusinessEmail: (String) -> Unit,
    openTime: String, onOpenTime: (String) -> Unit,
    closeTime: String, onCloseTime: (String) -> Unit,
    weeklyClose: String, onWeeklyClose: (String) -> Unit,
    productCategories: List<String>, onToggleProductCat: (String) -> Unit,
    productsSold: String, onProductsSold: (String) -> Unit,
    brandInfo: String, onBrandInfo: (String) -> Unit,
    wholesaleRetail: String, onWholesaleRetail: (String) -> Unit,
    deliveryAvailable: Boolean, onDeliveryAvailable: (Boolean) -> Unit,
    pickupAvailable: Boolean, onPickupAvailable: (Boolean) -> Unit,
    returnPolicy: String, onReturnPolicy: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Shop & Business Profile", style = MaterialTheme.typography.titleMedium, color = BrandBlueDark)
        OutlinedTextField(value = shopName, onValueChange = onShopName, label = { Text("Shop Name *") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = businessName, onValueChange = onBusinessName, label = { Text("Registered Business Name") }, modifier = Modifier.fillMaxWidth())
        Text("Business Type *", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KycCatalogConstants.SELLER_BUSINESS_TYPES.forEach { bt ->
                FilterChip(selected = businessType == bt, onClick = { onBusinessType(bt) }, label = { Text(bt, style = MaterialTheme.typography.labelSmall) })
            }
        }
        Text("Primary Shop Category *", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KycCatalogConstants.SHOP_CATEGORIES.forEach { cat ->
                FilterChip(
                    selected = shopCategory == cat,
                    onClick = { onShopCategory(cat) },
                    label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        OutlinedTextField(value = shopDesc, onValueChange = onShopDesc, label = { Text("Shop Description") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = shopContact, onValueChange = { onShopContact(it.filter(Char::isDigit).take(13)) }, label = { Text("Shop Contact *") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = businessEmail, onValueChange = onBusinessEmail, label = { Text("Business Email") }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = openTime, onValueChange = onOpenTime, label = { Text("Opening Time") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = closeTime, onValueChange = onCloseTime, label = { Text("Closing Time") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = weeklyClose, onValueChange = onWeeklyClose, label = { Text("Weekly Closing Day") }, modifier = Modifier.fillMaxWidth())
        Text("Product Categories & Operations", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KycCatalogConstants.SHOP_CATEGORIES.forEach { cat ->
                FilterChip(selected = productCategories.contains(cat), onClick = { onToggleProductCat(cat) }, label = { Text(cat, style = MaterialTheme.typography.labelSmall) })
            }
        }
        OutlinedTextField(value = productsSold, onValueChange = onProductsSold, label = { Text("Products Sold (e.g. Spare parts, Chargers)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = brandInfo, onValueChange = onBrandInfo, label = { Text("Authorized Brands (Optional)") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Retail", "Wholesale", "Both").forEach { wr ->
                FilterChip(selected = wholesaleRetail == wr, onClick = { onWholesaleRetail(wr) }, label = { Text(wr) })
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Home Delivery Available", modifier = Modifier.weight(1f))
            Switch(checked = deliveryAvailable, onCheckedChange = onDeliveryAvailable)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Store Pickup Available", modifier = Modifier.weight(1f))
            Switch(checked = pickupAvailable, onCheckedChange = onPickupAvailable)
        }
        OutlinedTextField(value = returnPolicy, onValueChange = onReturnPolicy, label = { Text("Return / Replacement Policy") }, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeliveryVehicleAndWorkSection(
    vehicleType: String, onVehicleType: (String) -> Unit,
    vehicleBrand: String, onVehicleBrand: (String) -> Unit,
    vehicleModel: String, onVehicleModel: (String) -> Unit,
    vehicleRegNo: String, onVehicleRegNo: (String) -> Unit,
    vehicleColor: String, onVehicleColor: (String) -> Unit,
    dlNumber: String, onDlNumber: (String) -> Unit,
    preferredArea: String, onPreferredArea: (String) -> Unit,
    preferredDistrict: String, onPreferredDistrict: (String) -> Unit,
    preferredCity: String, onPreferredCity: (String) -> Unit,
    workingHours: String, onWorkingHours: (String) -> Unit,
    employmentType: String, onEmploymentType: (String) -> Unit
) {
    val isMotor = KycCatalogConstants.isMotorVehicle(vehicleType)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Vehicle & Delivery Preferences", style = MaterialTheme.typography.titleMedium, color = BrandBlueDark)
        Text("Select Vehicle Type * (6 Approved Options)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KycCatalogConstants.VEHICLE_TYPES.forEach { vt ->
                FilterChip(selected = vehicleType == vt, onClick = { onVehicleType(vt) }, label = { Text(vt, style = MaterialTheme.typography.labelSmall) })
            }
        }
        if (isMotor) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = vehicleBrand, onValueChange = onVehicleBrand, label = { Text("Vehicle Brand *") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = vehicleModel, onValueChange = onVehicleModel, label = { Text("Vehicle Model *") }, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = vehicleRegNo, onValueChange = { onVehicleRegNo(it.uppercase()) }, label = { Text("Reg. Number *") }, modifier = Modifier.weight(1.2f))
                OutlinedTextField(value = vehicleColor, onValueChange = onVehicleColor, label = { Text("Color") }, modifier = Modifier.weight(0.8f))
            }
            OutlinedTextField(value = dlNumber, onValueChange = { onDlNumber(it.uppercase()) }, label = { Text("Driving Licence Number *") }, modifier = Modifier.fillMaxWidth())
        } else {
            Surface(color = BrandBlueLight, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "$vehicleType selected: Driving Licence, RC, Insurance & Pollution certificates are not mandatory.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandBlueDark,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        OutlinedTextField(value = preferredArea, onValueChange = onPreferredArea, label = { Text("Preferred Delivery Area / Zone *") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = preferredCity, onValueChange = onPreferredCity, label = { Text("Preferred City") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = preferredDistrict, onValueChange = onPreferredDistrict, label = { Text("Preferred District") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = workingHours, onValueChange = onWorkingHours, label = { Text("Available Working Hours") }, modifier = Modifier.fillMaxWidth())
        Text("Shift Preference", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Full Time", "Part Time").forEach { emp ->
                FilterChip(selected = employmentType == emp, onClick = { onEmploymentType(emp) }, label = { Text(emp) })
            }
        }
    }
}

@Composable
fun RoleDocumentsUploadSection(
    uid: String,
    role: String,
    gstRequired: Boolean,
    vehicleType: String,
    docsMap: Map<String, KycDocumentRecord>,
    onDocSelected: (KycDocumentRecord) -> Unit,
    onRemoveDoc: (KycDocumentRecord) -> Unit
) {
    val specs: List<Triple<String, String, Boolean>> = when (role.uppercase()) {
        "MECHANIC" -> listOf(
            Triple("profile_photo", "Profile Photo / Selfie", true),
            Triple("aadhaar_card", "Aadhaar Card (Front/Back)", true),
            Triple("pan_card", "PAN Card", true),
            Triple("bank_proof", "Bank Passbook / Cancelled Cheque", false),
            Triple("skill_certificate", "ITI / Trade Skill Certificate", false),
            Triple("previous_work_photo", "Previous Work / Workshop Photo", false)
        )
        "SELLER" -> buildList {
            add(Triple("owner_photo", "Owner Profile Photo", true))
            add(Triple("shop_photo", "Shop Front / Store Photo", true))
            add(Triple("pan_card", "Business / Owner PAN Card", true))
            add(Triple("aadhaar_card", "Owner Aadhaar Card", true))
            add(Triple("bank_proof", "Bank Proof / Cancelled Cheque", false))
            add(Triple("gst_certificate", "GST Registration Certificate", gstRequired))
            add(Triple("shop_address_proof", "Shop Address / Trade License", false))
        }
        "DELIVERY" -> buildList {
            add(Triple("profile_photo", "Rider Profile Photo / Selfie", true))
            add(Triple("aadhaar_card", "Aadhaar Card", true))
            add(Triple("pan_card", "PAN Card", true))
            add(Triple("bank_proof", "Bank Proof / Cancelled Cheque", false))
            if (KycCatalogConstants.isMotorVehicle(vehicleType)) {
                add(Triple("driving_licence", "Driving Licence (DL)", true))
                add(Triple("vehicle_rc", "Vehicle Registration (RC)", true))
                add(Triple("vehicle_insurance", "Vehicle Insurance", true))
                add(Triple("pollution_cert", "Pollution Certificate (PUC)", false))
            }
        }
        else -> emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("KYC Document Uploads", style = MaterialTheme.typography.titleMedium, color = BrandBlueDark)
        }
        Text(
            "Capture via Camera, choose from Gallery, or upload PDF. Files are saved privately and reviewed by Hunar Ji Admin.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        specs.forEach { (type, label, req) ->
            KycDocumentUploadCard(
                uid = uid,
                docType = type,
                docLabel = label,
                required = req,
                existingDoc = docsMap[type],
                onDocumentSelected = onDocSelected,
                onRemoveDocument = onRemoveDoc
            )
        }
    }
}
