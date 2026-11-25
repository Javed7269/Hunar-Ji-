package com.example.ui.screens.customer

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun CustomerAccountScreen(
    profile: UserProfile?,
    fallbackName: String,
    fallbackEmail: String,
    selectedDistrict: String,
    languageName: String,
    ordersCount: Int,
    bookingsCount: Int,
    wishlistCount: Int,
    addressCount: Int,
    returnsCount: Int,
    unreadNotificationsCount: Int,
    isAdminUser: Boolean,
    onOpenOrders: () -> Unit,
    onOpenBookings: () -> Unit,
    onOpenWishlist: () -> Unit,
    onOpenAddresses: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenDistrictPicker: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onOpenWorkspaceSwitcher: () -> Unit,
    onOpenRegistrationWizard: () -> Unit,
    onDeleteAccount: () -> Unit,
    onSaveProfile: (String, String, String) -> Unit,
    onSignOut: () -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var nameInput by remember(profile) { mutableStateOf(profile?.displayName?.ifBlank { fallbackName } ?: fallbackName) }
    var phoneInput by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var cityInput by remember(profile) { mutableStateOf(profile?.city ?: selectedDistrict) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = BrandBlueLight,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(30.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profile?.displayName?.ifBlank { fallbackName } ?: fallbackName, style = MaterialTheme.typography.titleLarge)
                            Text(fallbackEmail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            if (!profile?.hunarUserId.isNullOrBlank()) {
                                Text("User ID: ${profile?.hunarUserId}", style = MaterialTheme.typography.labelSmall, color = BrandBlueDark)
                            }
                            Text(
                                "Referral: ${profile?.referralCode?.ifBlank { "HUNAR" } ?: "HUNAR"} • Reward Pts: ${profile?.rewardPoints ?: 0L}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandBlue
                            )
                        }
                        TextButton(onClick = { editing = !editing }) {
                            Text(if (editing) "Cancel" else "Edit")
                        }
                    }
                    if (editing) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = phoneInput, onValueChange = { phoneInput = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = cityInput, onValueChange = { cityInput = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onSaveProfile(nameInput, phoneInput, cityInput)
                                editing = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Profile")
                        }
                    }
                }
            }
        }

        val rows = buildList {
            add(Triple("My Orders ($ordersCount)", Icons.Default.ShoppingBag, onOpenOrders))
            add(Triple("Returns & Replacements ($returnsCount)", Icons.Default.AssignmentReturn, onOpenOrders))
            add(Triple("My Service Bookings ($bookingsCount)", Icons.Default.Build, onOpenBookings))
            add(Triple("My Wishlist ($wishlistCount)", Icons.Default.Favorite, onOpenWishlist))
            add(Triple("Saved Delivery Addresses ($addressCount)", Icons.Default.LocationOn, onOpenAddresses))
            add(Triple("Notifications ($unreadNotificationsCount unread)", Icons.Default.Notifications, onOpenNotifications))
            add(Triple("District & Location ($selectedDistrict)", Icons.Default.LocationOn, onOpenDistrictPicker))
            add(Triple("App Language ($languageName)", Icons.Default.Language, onOpenLanguagePicker))
            add(Triple("Become a Partner (Mechanic / Seller / Delivery KYC)", Icons.Default.Shield, onOpenRegistrationWizard))
            add(Triple("Customer Support & Tickets", Icons.Default.HeadsetMic, onOpenSupport))
            if (isAdminUser) {
                add(Triple("Admin Workspace Switcher", Icons.Default.AdminPanelSettings, onOpenWorkspaceSwitcher))
            }
        }

        items(rows) { (title, icon, action) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = action),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = title, tint = BrandBlue)
                    Spacer(Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("logout_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AccentRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Sign Out")
                }
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Delete Account", color = AccentRed)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account & Data?") },
            text = { Text("This permanently deletes your profile and any stored KYC records. Active orders cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) { Text("Delete Permanently") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
