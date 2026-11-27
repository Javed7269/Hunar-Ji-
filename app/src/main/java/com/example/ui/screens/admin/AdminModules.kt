package com.example.ui.screens.admin

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditLog
import com.example.data.model.Banner
import com.example.data.model.BookingStatus
import com.example.data.model.Coupon
import com.example.data.model.LiveStream
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PlatformConfig
import com.example.data.model.Product
import com.example.data.model.ProviderProfile
import com.example.data.model.ServiceBooking
import com.example.data.model.ServiceItem
import com.example.data.model.ServiceLocation
import com.example.data.model.SupportTicket
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalRequest
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SalesByAppDonutChart
import com.example.ui.components.SalesOverviewLineChart
import com.example.ui.components.StatusPill
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AdminOrdersModule(
    orders: List<Order>,
    globalSearch: String,
    onUpdateStatus: (String, OrderStatus) -> Unit
) {
    val context = LocalContext.current
    var statusFilter by remember { mutableStateOf("ALL") }
    var inspectOrder by remember { mutableStateOf<Order?>(null) }

    val filtered = remember(orders, statusFilter, globalSearch) {
        orders.filter { ord ->
            val matchesStatus = statusFilter == "ALL" || ord.status.equals(statusFilter, true)
            val matchesQuery = globalSearch.isBlank() ||
                ord.id.contains(globalSearch, true) ||
                ord.customerName.contains(globalSearch, true) ||
                ord.itemsSummary.contains(globalSearch, true)
            matchesStatus && matchesQuery
        }.sortedByDescending { it.createdAt?.seconds ?: 0L }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val tabs = listOf("ALL", "PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED")
                items(tabs) { tab ->
                    FilterChip(
                        selected = statusFilter == tab,
                        onClick = { statusFilter = tab },
                        label = { Text(tab.replace('_', ' ')) }
                    )
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.ReceiptLong,
                    title = "No matching orders",
                    subtitle = "Orders placed from the Customer App appear here in real time."
                )
            }
        } else {
            items(filtered, key = { it.id }) { ord ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("#${ord.id} • ${ord.customerName}", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            StatusPill(ord.status)
                        }
                        Text(ord.itemsSummary, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            "Amount: ₹${ord.totalAmount.toInt()} • ${ord.paymentMethod} (${ord.paymentStatus}) • ${ord.district}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandBlue
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { inspectOrder = ord }, modifier = Modifier.weight(1f)) {
                                Text("Manage / Invoice")
                            }
                            if (ord.status == "PENDING") {
                                Button(
                                    onClick = { onUpdateStatus(ord.id, OrderStatus.CONFIRMED) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Confirm")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inspectOrder?.let { ord ->
        OrderDetailAdminDialog(
            order = ord,
            onUpdateStatus = { st ->
                onUpdateStatus(ord.id, st)
                inspectOrder = null
            },
            onShareInvoice = {
                val invoiceText = buildString {
                    appendLine("HUNHAR JI TAX INVOICE")
                    appendLine("Order ID: #${ord.id}")
                    appendLine("Customer: ${ord.customerName}")
                    appendLine("Seller: ${ord.sellerName}")
                    appendLine("Items: ${ord.itemsSummary}")
                    appendLine("Subtotal: ₹${ord.subtotal.toInt()}")
                    appendLine("Discount: -₹${ord.discountAmount.toInt()}")
                    appendLine("GST: ₹${ord.taxAmount.toInt()}")
                    appendLine("Delivery: ₹${ord.deliveryFee.toInt()}")
                    appendLine("Total Paid: ₹${ord.totalAmount.toInt()} (${ord.paymentMethod})")
                    appendLine("Drop Address: ${ord.dropAddress}")
                }
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "HUNHAR JI Invoice #${ord.id}")
                    putExtra(Intent.EXTRA_TEXT, invoiceText)
                }
                context.startActivity(Intent.createChooser(share, "Share Invoice"))
            },
            onDismiss = { inspectOrder = null }
        )
    }
}

@Composable
fun OrderDetailAdminDialog(
    order: Order,
    onUpdateStatus: (OrderStatus) -> Unit,
    onShareInvoice: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Order #${order.id}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Customer: ${order.customerName}", style = MaterialTheme.typography.titleSmall)
                Text("Seller: ${order.sellerName}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                if (order.deliveryPartnerName.isNotBlank()) {
                    Text("Delivery Rider: ${order.deliveryPartnerName}", style = MaterialTheme.typography.bodySmall, color = AccentGreen)
                }
                Text("Items: ${order.itemsSummary}", style = MaterialTheme.typography.bodySmall)
                Text("Drop: ${order.dropAddress}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Total: ₹${order.totalAmount.toInt()} • ${order.paymentMethod}", style = MaterialTheme.typography.titleSmall, color = BrandBlueDark)
                Spacer(Modifier.height(4.dp))
                Text("Update Order Status:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(OrderStatus.entries) { st ->
                        FilterChip(
                            selected = order.status == st.name,
                            onClick = { onUpdateStatus(st) },
                            label = { Text(st.label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onShareInvoice) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Invoice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun AdminProductsModule(
    products: List<Product>,
    globalSearch: String,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit
) {
    var editing by remember { mutableStateOf<Product?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    val filtered = remember(products, globalSearch) {
        products.filter {
            globalSearch.isBlank() || it.title.contains(globalSearch, true) || it.category.contains(globalSearch, true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(
                onClick = { showAdd = true },
                modifier = Modifier.fillMaxWidth().testTag("admin_add_product_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Add Marketplace Product")
            }
        }
        items(filtered, key = { it.id }) { prod ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prod.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("${prod.category} • ₹${prod.price.toInt()} • Stock: ${prod.stock}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("Seller: ${prod.sellerName}", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                    }
                    StatusPill(if (prod.active) "ACTIVE" else "OFFLINE")
                    IconButton(onClick = { editing = prod }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit product", tint = BrandBlue)
                    }
                    IconButton(onClick = { onDeleteProduct(prod.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete product", tint = AccentRed)
                    }
                }
            }
        }
    }

    if (showAdd || editing != null) {
        AdminProductEditorDialog(
            initial = editing,
            onSave = {
                onSaveProduct(it)
                showAdd = false
                editing = null
            },
            onDismiss = {
                showAdd = false
                editing = null
            }
        )
    }
}

@Composable
private fun AdminProductEditorDialog(
    initial: Product?,
    onSave: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "Electronics") }
    var price by remember { mutableStateOf(initial?.price?.toInt()?.toString() ?: "999") }
    var origPrice by remember { mutableStateOf(initial?.originalPrice?.toInt()?.toString() ?: "1499") }
    var stock by remember { mutableStateOf(initial?.stock?.toString() ?: "25") }
    var active by remember { mutableStateOf(initial?.active ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Product" else "Edit Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (₹)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = origPrice, onValueChange = { origPrice = it }, label = { Text("MRP (₹)") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock Qty") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Published / Active", modifier = Modifier.weight(1f))
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    val p = price.toDoubleOrNull() ?: 499.0
                    val op = origPrice.toDoubleOrNull() ?: p
                    onSave(
                        (initial ?: Product()).copy(
                            title = title,
                            category = category,
                            price = p,
                            originalPrice = op.coerceAtLeast(p),
                            stock = stock.toLongOrNull() ?: 10L,
                            active = active,
                            approved = true
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AdminServicesModule(
    services: List<ServiceItem>,
    bookings: List<ServiceBooking>,
    onSaveService: (ServiceItem) -> Unit,
    onUpdateBooking: (String, BookingStatus) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Car Repair") }
    var price by remember { mutableStateOf("499") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Add Mistri Ji Service")
            }
        }
        item {
            Text("Service Catalog (${services.size})", style = MaterialTheme.typography.titleMedium)
        }
        items(services, key = { it.id }) { srv ->
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = BrandBlue)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(srv.title, style = MaterialTheme.typography.titleSmall)
                        Text("${srv.category} • ₹${srv.price.toInt()} • ${srv.durationMinutes} min", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    StatusPill(if (srv.active) "ACTIVE" else "OFFLINE")
                }
            }
        }
        item {
            Text("Customer Service Bookings (${bookings.size})", style = MaterialTheme.typography.titleMedium)
        }
        if (bookings.isEmpty()) {
            item {
                EmptyStateCard(icon = Icons.Default.Build, title = "No bookings yet", subtitle = "Mechanic service requests appear here.")
            }
        } else {
            items(bookings, key = { it.id }) { bk ->
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("#${bk.id} • ${bk.serviceTitle}", style = MaterialTheme.typography.titleSmall)
                            StatusPill(bk.status)
                        }
                        Text("${bk.customerName} -> ${bk.mechanicName} • ₹${bk.amount.toInt()}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        if (bk.status == "PENDING") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onUpdateBooking(bk.id, BookingStatus.CANCELLED) }) { Text("Cancel") }
                                Button(onClick = { onUpdateBooking(bk.id, BookingStatus.ACCEPTED) }) { Text("Approve/Assign") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add Service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Service Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Rate (₹)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onSaveService(ServiceItem(title = title, category = category, price = price.toDoubleOrNull() ?: 499.0))
                        title = ""; showAdd = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AdminUsersOrProvidersModule(
    title: String,
    users: List<UserProfile>,
    providers: List<ProviderProfile>,
    roleFilter: String,
    globalSearch: String
) {
    val matchingUsers = users.filter {
        (roleFilter == "ALL" || it.role.equals(roleFilter, true)) &&
            (globalSearch.isBlank() || it.displayName.contains(globalSearch, true) || it.email.contains(globalSearch, true))
    }
    val matchingProviders = providers.filter {
        (roleFilter == "CUSTOMER" || roleFilter == "ALL" || it.providerType.equals(roleFilter, true)) &&
            (globalSearch.isBlank() || it.name.contains(globalSearch, true) || it.district.contains(globalSearch, true))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("$title (${matchingUsers.size + matchingProviders.size})", style = MaterialTheme.typography.titleMedium)
        }
        items(matchingUsers, key = { "u_${it.id}" }) { u ->
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(u.displayName.ifBlank { u.email }, style = MaterialTheme.typography.titleSmall)
                            if (u.verified) {
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = BrandBlue, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("${u.email} • ${u.district}, ${u.state}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("Wallet: ₹${u.walletBalance.toInt()} • Loyalty: ${u.loyaltyPoints} pts", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                    }
                    StatusPill(if (u.online) "ONLINE" else "OFFLINE")
                }
            }
        }
        items(matchingProviders, key = { "p_${it.id}" }) { p ->
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${p.name} (${p.providerType})", style = MaterialTheme.typography.titleSmall)
                        Text(p.shopName.ifBlank { p.skills }, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("${p.city}, ${p.district} • ⭐ ${p.rating} (${p.completedJobs} jobs)", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                    }
                    StatusPill(if (p.approved) "APPROVED" else "PENDING")
                }
            }
        }
    }
}

@Composable
fun AdminWalletModule(
    withdrawals: List<WithdrawalRequest>,
    totalRevenue: Double,
    onUpdateWithdrawal: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BrandBlue)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Platform Gross Transaction Volume", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    Text("₹${totalRevenue.toInt()}", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    Text("Pending Withdrawals: ${withdrawals.count { it.status == "PENDING" }}", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }
        }
        if (withdrawals.isEmpty()) {
            item {
                EmptyStateCard(icon = Icons.Default.Inventory2, title = "No withdrawal requests", subtitle = "Seller, Mechanic, and Delivery payouts requested appear here.")
            }
        } else {
            items(withdrawals, key = { it.id }) { wd ->
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${wd.userName} (${wd.role})", style = MaterialTheme.typography.titleSmall)
                            StatusPill(wd.status)
                        }
                        Text("₹${wd.amount.toInt()} -> ${wd.payoutMethod}", style = MaterialTheme.typography.bodySmall, color = BrandBlueDark)
                        if (wd.status == "PENDING") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onUpdateWithdrawal(wd.id, "REJECTED") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = AccentRed, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reject", color = AccentRed)
                                }
                                Button(
                                    onClick = { onUpdateWithdrawal(wd.id, "APPROVED") },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Approve Payout")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPaymentGatewayModule(
    config: PlatformConfig,
    onSaveConfig: (PlatformConfig) -> Unit
) {
    var razorpayEnabled by remember(config) { mutableStateOf(config.razorpayEnabled) }
    var mode by remember(config) { mutableStateOf(config.razorpayMode) }
    var keyId by remember(config) { mutableStateOf(config.razorpayKeyId) }
    var upi by remember(config) { mutableStateOf(config.upiEnabled) }
    var card by remember(config) { mutableStateOf(config.cardEnabled) }
    var netBanking by remember(config) { mutableStateOf(config.netBankingEnabled) }
    var wallet by remember(config) { mutableStateOf(config.walletEnabled) }
    var cod by remember(config) { mutableStateOf(config.codEnabled) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Razorpay & Payment Controls", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Razorpay Gateway", modifier = Modifier.weight(1f))
                        Switch(checked = razorpayEnabled, onCheckedChange = { razorpayEnabled = it })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = mode == "TEST", onClick = { mode = "TEST" }, label = { Text("TEST Mode") })
                        FilterChip(selected = mode == "LIVE", onClick = { mode = "LIVE" }, label = { Text("LIVE Mode") })
                    }
                    OutlinedTextField(
                        value = keyId,
                        onValueChange = { keyId = it },
                        label = { Text("Razorpay Public Key ID (rzp_...)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Surface(color = BrandBlueLight, shape = RoundedCornerShape(10.dp)) {
                        Text(
                            text = "Security Notice: Razorpay Key Secret & Webhook Secret must be configured exclusively on your server / Cloud Functions environment, never inside mobile client code.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandBlueDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Allowed Customer Checkout Methods", style = MaterialTheme.typography.titleMedium)
                    ToggleSettingRow("UPI Payments (GPay / PhonePe / Paytm)", upi) { upi = it }
                    ToggleSettingRow("Credit / Debit Cards", card) { card = it }
                    ToggleSettingRow("Net Banking", netBanking) { netBanking = it }
                    ToggleSettingRow("HUNHAR JI Wallet", wallet) { wallet = it }
                    ToggleSettingRow("Cash on Delivery (COD)", cod) { cod = it }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onSaveConfig(
                                config.copy(
                                    razorpayEnabled = razorpayEnabled,
                                    razorpayMode = mode,
                                    razorpayKeyId = keyId,
                                    upiEnabled = upi,
                                    cardEnabled = card,
                                    netBankingEnabled = netBanking,
                                    walletEnabled = wallet,
                                    codEnabled = cod
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_payment_gateway_button")
                    ) {
                        Text("Save Payment Gateway Settings")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCommissionModule(
    config: PlatformConfig,
    onSaveConfig: (PlatformConfig) -> Unit
) {
    var globalPct by remember(config) { mutableStateOf(config.globalCommissionPercent.toString()) }
    var productPct by remember(config) { mutableStateOf(config.productCommissionPercent.toString()) }
    var servicePct by remember(config) { mutableStateOf(config.serviceCommissionPercent.toString()) }
    var deliveryPct by remember(config) { mutableStateOf(config.deliveryCommissionPercent.toString()) }
    var gstPct by remember(config) { mutableStateOf(config.gstPercent.toString()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Commission & GST Rates (%)", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = globalPct, onValueChange = { globalPct = it }, label = { Text("Global Commission (%)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = productPct, onValueChange = { productPct = it }, label = { Text("Product Seller Commission (%)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = servicePct, onValueChange = { servicePct = it }, label = { Text("Mechanic Service Commission (%)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = deliveryPct, onValueChange = { deliveryPct = it }, label = { Text("Delivery Partner Fee (%)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = gstPct, onValueChange = { gstPct = it }, label = { Text("Platform GST (%)") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            onSaveConfig(
                                config.copy(
                                    globalCommissionPercent = globalPct.toDoubleOrNull() ?: config.globalCommissionPercent,
                                    productCommissionPercent = productPct.toDoubleOrNull() ?: config.productCommissionPercent,
                                    serviceCommissionPercent = servicePct.toDoubleOrNull() ?: config.serviceCommissionPercent,
                                    deliveryCommissionPercent = deliveryPct.toDoubleOrNull() ?: config.deliveryCommissionPercent,
                                    gstPercent = gstPct.toDoubleOrNull() ?: config.gstPercent
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save Commission & GST") }
                }
            }
        }
    }
}

@Composable
fun AdminLiveStreamingModule(
    streams: List<LiveStream>,
    onStopStream: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(streams, key = { it.id }) { stream ->
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stream.title, style = MaterialTheme.typography.titleSmall)
                        Text("${stream.hostName} • ${stream.category} • ${stream.viewerCount} viewers", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    StatusPill(if (stream.live) "LIVE" else "OFFLINE")
                    if (stream.live) {
                        IconButton(onClick = { onStopStream(stream.id) }) {
                            Icon(Icons.Default.StopCircle, contentDescription = "Stop stream", tint = AccentRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBannersAndCouponsModule(
    banners: List<Banner>,
    coupons: List<Coupon>,
    onSaveBanner: (Banner) -> Unit,
    onSaveCoupon: (Coupon) -> Unit
) {
    var bTitle by remember { mutableStateOf("") }
    var bSub by remember { mutableStateOf("") }
    var cCode by remember { mutableStateOf("") }
    var cPct by remember { mutableStateOf("15") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Promotional Banners (${banners.size})", style = MaterialTheme.typography.titleMedium)
                    banners.forEach { b ->
                        Text("• ${b.title} (${b.targetCategory}) — ${b.subtitle}", style = MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider()
                    OutlinedTextField(value = bTitle, onValueChange = { bTitle = it }, label = { Text("New Banner Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = bSub, onValueChange = { bSub = it }, label = { Text("Subtitle") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (bTitle.isNotBlank()) {
                                onSaveBanner(Banner(title = bTitle, subtitle = bSub.ifBlank { "Special Offer" }))
                                bTitle = ""; bSub = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Publish Banner") }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Discount Coupons (${coupons.size})", style = MaterialTheme.typography.titleMedium)
                    coupons.forEach { c ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${c.code} (${c.discountPercent}% OFF)", style = MaterialTheme.typography.titleSmall, color = BrandBlueDark)
                            StatusPill(if (c.active) "ACTIVE" else "OFFLINE")
                        }
                    }
                    HorizontalDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = cCode, onValueChange = { cCode = it.uppercase() }, label = { Text("Code") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = cPct, onValueChange = { cPct = it }, label = { Text("% Off") }, modifier = Modifier.width(100.dp))
                    }
                    Button(
                        onClick = {
                            if (cCode.isNotBlank()) {
                                onSaveCoupon(Coupon(code = cCode, title = "$cPct% Festival Discount", discountPercent = cPct.toLongOrNull() ?: 10L))
                                cCode = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Create Coupon") }
                }
            }
        }
    }
}

@Composable
fun AdminNotificationsModule(
    config: PlatformConfig,
    onSaveConfig: (PlatformConfig) -> Unit
) {
    var smsProvider by remember(config) { mutableStateOf(config.smsProvider) }
    var smsEnabled by remember(config) { mutableStateOf(config.smsEnabled) }
    var emailEnabled by remember(config) { mutableStateOf(config.emailEnabled) }
    var pushEnabled by remember(config) { mutableStateOf(config.pushEnabled) }
    var otpExpiry by remember(config) { mutableStateOf(config.otpExpiryMinutes.toString()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SMS, OTP, Email & Push Settings", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("MSG91", "Twilio", "Fast2SMS").forEach { prov ->
                            FilterChip(selected = smsProvider == prov, onClick = { smsProvider = prov }, label = { Text(prov) })
                        }
                    }
                    OutlinedTextField(value = otpExpiry, onValueChange = { otpExpiry = it }, label = { Text("OTP Expiry (Minutes)") }, modifier = Modifier.fillMaxWidth())
                    ToggleSettingRow("SMS OTP Alerts", smsEnabled) { smsEnabled = it }
                    ToggleSettingRow("Transactional Email Alerts", emailEnabled) { emailEnabled = it }
                    ToggleSettingRow("FCM Push Notifications", pushEnabled) { pushEnabled = it }
                    Surface(color = BrandBlueLight, shape = RoundedCornerShape(10.dp)) {
                        Text(
                            text = "Note: SMS API tokens & SMTP credentials are managed strictly in server-side Cloud Functions secrets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandBlueDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Button(
                        onClick = {
                            onSaveConfig(
                                config.copy(
                                    smsProvider = smsProvider,
                                    smsEnabled = smsEnabled,
                                    emailEnabled = emailEnabled,
                                    pushEnabled = pushEnabled,
                                    otpExpiryMinutes = otpExpiry.toLongOrNull() ?: 5L
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save Notification Settings") }
                }
            }
        }
    }
}

@Composable
fun AdminLocationManagerModule(
    locations: List<ServiceLocation>,
    onSaveLocation: (ServiceLocation) -> Unit
) {
    var state by remember { mutableStateOf("Bihar") }
    var district by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add District Service Zone", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = pincode, onValueChange = { pincode = it }, label = { Text("Pincode") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (district.isNotBlank() && pincode.length >= 6) {
                                onSaveLocation(ServiceLocation(state = state, district = district, city = district, pincode = pincode))
                                district = ""; pincode = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Enable District Zone") }
                }
            }
        }
        items(locations, key = { it.id }) { loc ->
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${loc.district}, ${loc.state}", style = MaterialTheme.typography.titleSmall)
                        Text("PIN: ${loc.pincode}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    StatusPill(if (loc.serviceAvailable && loc.deliveryAvailable) "ACTIVE" else "PARTIAL")
                }
            }
        }
    }
}

@Composable
fun AdminReportsModule(
    orders: List<Order>,
    bookings: List<ServiceBooking>,
    config: PlatformConfig
) {
    val context = LocalContext.current
    val orderRev = orders.sumOf { it.totalAmount }
    val bookingRev = bookings.sumOf { it.amount }
    val totalRev = orderRev + bookingRev
    val commission = totalRev * (config.globalCommissionPercent / 100.0)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Platform Financial Analytics", style = MaterialTheme.typography.titleMedium)
                    Text("Gross Revenue: ₹${totalRev.toInt()} • Est. Commission (${config.globalCommissionPercent}%): ₹${commission.toInt()}", style = MaterialTheme.typography.bodySmall, color = BrandBlueDark)
                    SalesOverviewLineChart(
                        thisMonthPoints = listOf(5.2f, 9.4f, 11.8f, 13.4f, 15.6f, 18.2f),
                        lastMonthPoints = listOf(3.1f, 6.8f, 6.4f, 9.2f, 9.8f, 13.0f),
                        labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    )
                    Button(
                        onClick = {
                            val csv = buildString {
                                appendLine("Type,Count,Amount")
                                appendLine("Orders,${orders.size},${orderRev.toInt()}")
                                appendLine("Bookings,${bookings.size},${bookingRev.toInt()}")
                                appendLine("Commission,${config.globalCommissionPercent}%,${commission.toInt()}")
                            }
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_SUBJECT, "HUNHAR JI Platform Report CSV")
                                putExtra(Intent.EXTRA_TEXT, csv)
                            }
                            context.startActivity(Intent.createChooser(share, "Export CSV Report"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Export CSV Financial Report")
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Revenue Share by App", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    SalesByAppDonutChart(
                        segments = listOf(
                            Triple("Customer App", 40f, BrandBlue),
                            Triple("Mechanic App", 25f, AccentGreen),
                            Triple("Seller App", 20f, AccentOrange),
                            Triple("Delivery App", 15f, AccentPurple)
                        ),
                        centerText = "100%"
                    )
                }
            }
        }
    }
}

@Composable
fun AdminAppAndSettingsModule(
    config: PlatformConfig,
    onSaveConfig: (PlatformConfig) -> Unit,
    onSeedCatalog: () -> Unit
) {
    var custApp by remember(config) { mutableStateOf(config.customerAppEnabled) }
    var mechApp by remember(config) { mutableStateOf(config.mechanicAppEnabled) }
    var sellerApp by remember(config) { mutableStateOf(config.sellerAppEnabled) }
    var deliveryApp by remember(config) { mutableStateOf(config.deliveryAppEnabled) }
    var maint by remember(config) { mutableStateOf(config.maintenanceMode) }
    var supportEmail by remember(config) { mutableStateOf(config.supportEmail) }
    var supportPhone by remember(config) { mutableStateOf(config.supportPhone) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Four Applications Master Control", style = MaterialTheme.typography.titleMedium)
                    ToggleSettingRow("Customer App Enabled", custApp) { custApp = it }
                    ToggleSettingRow("Mechanic App (Mistri Ji) Enabled", mechApp) { mechApp = it }
                    ToggleSettingRow("Seller App Enabled", sellerApp) { sellerApp = it }
                    ToggleSettingRow("Delivery Boy App Enabled", deliveryApp) { deliveryApp = it }
                    HorizontalDivider()
                    ToggleSettingRow("Global Maintenance Mode", maint) { maint = it }
                    OutlinedTextField(value = supportEmail, onValueChange = { supportEmail = it }, label = { Text("Support Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = supportPhone, onValueChange = { supportPhone = it }, label = { Text("Support Helpline") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            onSaveConfig(
                                config.copy(
                                    customerAppEnabled = custApp,
                                    mechanicAppEnabled = mechApp,
                                    sellerAppEnabled = sellerApp,
                                    deliveryAppEnabled = deliveryApp,
                                    maintenanceMode = maint,
                                    supportEmail = supportEmail,
                                    supportPhone = supportPhone
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save App & General Settings") }
                    OutlinedButton(
                        onClick = onSeedCatalog,
                        modifier = Modifier.fillMaxWidth().testTag("seed_catalog_button")
                    ) { Text("Sync Starter Catalog to Firestore") }
                }
            }
        }
    }
}

@Composable
fun AdminSupportModule(
    tickets: List<SupportTicket>,
    onReplyTicket: (String, String, String) -> Unit
) {
    var replyingTo by remember { mutableStateOf<SupportTicket?>(null) }
    var replyText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (tickets.isEmpty()) {
            item {
                EmptyStateCard(icon = Icons.Default.Lock, title = "No open complaints", subtitle = "Support tickets from users and partners appear here.")
            }
        } else {
            items(tickets, key = { it.id }) { t ->
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${t.subject} (${t.role})", style = MaterialTheme.typography.titleSmall)
                            StatusPill(t.status)
                        }
                        Text("${t.userName}: ${t.description}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        if (t.adminReply.isNotBlank()) {
                            Text("Admin Reply: ${t.adminReply}", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { replyingTo = t; replyText = t.adminReply }) { Text("Reply") }
                            if (t.status != "RESOLVED") {
                                Button(onClick = { onReplyTicket(t.id, "RESOLVED", t.adminReply.ifBlank { "Resolved by HUNHAR JI Support." }) }) {
                                    Text("Mark Resolved")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    replyingTo?.let { t ->
        AlertDialog(
            onDismissRequest = { replyingTo = null },
            title = { Text("Reply to ${t.userName}") },
            text = {
                OutlinedTextField(value = replyText, onValueChange = { replyText = it }, label = { Text("Admin Response") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = {
                    onReplyTicket(t.id, "RESOLVED", replyText.ifBlank { "Resolved." })
                    replyingTo = null
                }) { Text("Send & Resolve") }
            },
            dismissButton = { TextButton(onClick = { replyingTo = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AdminSecurityModule(
    logs: List<AuditLog>,
    isAdminUser: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Zero-Trust Firestore Security", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (isAdminUser) "Verified Super Admin session active (hunarji47@gmail.com). All admin actions are logged."
                        else "Viewer mode: Sign in with verified admin account (hunarji47@gmail.com) for full write permissions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
        items(logs.sortedByDescending { it.createdAt?.seconds ?: 0L }, key = { it.id }) { log ->
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${log.action} • ${log.targetType}/${log.targetId}", style = MaterialTheme.typography.titleSmall, color = BrandBlueDark)
                    Text("${log.actorEmail}: ${log.details}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun ToggleSettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
