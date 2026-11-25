package com.example.ui.screens.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Address
import com.example.data.model.AppNotification
import com.example.data.model.CartItem
import com.example.data.model.Coupon
import com.example.data.model.LiveStream
import com.example.data.model.Order
import com.example.data.model.PaymentMethod
import com.example.data.model.PlatformConfig
import com.example.data.model.Product
import com.example.data.model.ServiceBooking
import com.example.data.model.ServiceItem
import com.example.data.model.ServiceLocation
import com.example.data.model.StreamMessage
import com.example.data.model.SupportTicket
import com.example.data.model.WishlistItem
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.StatusPill
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ProductDetailDialog(
    product: Product,
    saved: Boolean,
    onAddToCart: (Int, String) -> Unit,
    onBuyNow: (String) -> Unit,
    onToggleWishlist: () -> Unit,
    onDismiss: () -> Unit
) {
    var qty by remember { mutableIntStateOf(1) }
    var variant by remember { mutableStateOf("Standard") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(product.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onToggleWishlist) {
                    Icon(
                        if (saved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (saved) AccentRed else TextMuted
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProductThumbnail(
                    iconKey = product.iconKey,
                    modifier = Modifier.fillMaxWidth().height(110.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${product.price.toInt()}", style = MaterialTheme.typography.headlineSmall.copy(color = BrandBlueDark))
                    Spacer(Modifier.width(8.dp))
                    StatusPill("${product.discountPercent}% OFF")
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Star, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                    Text("${product.rating} (${product.reviewCount})", style = MaterialTheme.typography.bodySmall)
                }
                Text("Seller: ${product.sellerName} • Stock: ${product.stock} units", style = MaterialTheme.typography.bodySmall, color = BrandBlue)
                Text(product.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Standard", "Pro Pack", "Black").forEach { v ->
                        Surface(
                            color = if (variant == v) BrandBlueLight else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { variant = v }
                        ) {
                            Text(v, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Quantity:", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.width(12.dp))
                    IconButton(onClick = { if (qty > 1) qty-- }) { Icon(Icons.Default.Remove, contentDescription = "Decrease") }
                    Text("$qty", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { if (qty < 5) qty++ }) { Icon(Icons.Default.Add, contentDescription = "Increase") }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onAddToCart(qty, variant) }) {
                    Text("Add to Cart")
                }
                Button(onClick = { onBuyNow(variant) }) {
                    Text("Buy Now")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun CartAndCheckoutDialog(
    cartItems: List<CartItem>,
    addresses: List<Address>,
    coupons: List<Coupon>,
    platformConfig: PlatformConfig,
    walletBalance: Double,
    defaultDistrict: String,
    onUpdateQty: (String, Long) -> Unit,
    onSaveAddress: (Address) -> Unit,
    onPlaceOrder: (String, PaymentMethod, String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var couponCode by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf<Coupon?>(null) }
    var customAddress by remember(addresses) {
        mutableStateOf(
            addresses.firstOrNull()?.let { "${it.fullAddress}, ${it.city} - ${it.pincode}" }
                ?: "Civil Lines, $defaultDistrict"
        )
    }

    val enabledPayments = remember(platformConfig) {
        buildList {
            if (platformConfig.upiEnabled) add(PaymentMethod.UPI)
            if (platformConfig.razorpayEnabled) add(PaymentMethod.RAZORPAY)
            if (platformConfig.cardEnabled) add(PaymentMethod.CARD)
            if (platformConfig.netBankingEnabled) add(PaymentMethod.NET_BANKING)
            if (platformConfig.walletEnabled) add(PaymentMethod.WALLET)
            if (platformConfig.codEnabled) add(PaymentMethod.COD)
        }.ifEmpty { listOf(PaymentMethod.COD) }
    }
    var selectedPayment by remember { mutableStateOf(enabledPayments.first()) }

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val discount = appliedCoupon?.let { c ->
        if (subtotal >= c.minOrderAmount) ((subtotal * c.discountPercent) / 100.0).coerceAtMost(c.maxDiscount)
        else 0.0
    } ?: 0.0
    val deliveryFee = if (subtotal >= 999.0 || cartItems.isEmpty()) 0.0 else 40.0
    val tax = ((subtotal - discount).coerceAtLeast(0.0) * (platformConfig.gstPercent / 100.0))
    val finalTotal = (subtotal - discount + deliveryFee + tax).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cart & Checkout (${cartItems.size})") },
        text = {
            if (cartItems.isEmpty()) {
                Text("Your cart is empty. Add products to place an order.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(
                    modifier = Modifier.height(390.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cartItems, key = { it.id }) { item ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, style = MaterialTheme.typography.titleSmall)
                                    Text("₹${item.price.toInt()} • ${item.variant}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                IconButton(onClick = { onUpdateQty(item.id, item.quantity - 1) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                }
                                Text("${item.quantity}", style = MaterialTheme.typography.titleSmall)
                                IconButton(onClick = { onUpdateQty(item.id, item.quantity + 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = customAddress,
                            onValueChange = { customAddress = it },
                            label = { Text("Delivery Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = couponCode,
                                onValueChange = { couponCode = it.uppercase() },
                                label = { Text("Coupon (e.g. HUNAR10)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                appliedCoupon = coupons.firstOrNull { it.code.equals(couponCode.trim(), true) }
                            }) {
                                Text("Apply")
                            }
                        }
                    }

                    item {
                        Text("Payment Method", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            enabledPayments.forEach { method ->
                                val selected = selectedPayment == method
                                Surface(
                                    color = if (selected) BrandBlueLight else Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (selected) BrandBlue else Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPayment = method }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (method == PaymentMethod.WALLET) "${method.displayName} (₹${walletBalance.toInt()})" else method.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (selected) BrandBlueDark else TextPrimary
                                        )
                                        if (selected) Text("Selected", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider()
                        Spacer(Modifier.height(6.dp))
                        SummaryRow("Subtotal", "₹${subtotal.toInt()}")
                        SummaryRow("Discount", "- ₹${discount.toInt()}")
                        SummaryRow("Delivery Fee", if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}")
                        SummaryRow("GST (${platformConfig.gstPercent.toInt()}%)", "₹${tax.toInt()}")
                        SummaryRow("Final Total", "₹${finalTotal.toInt()}", bold = true)
                    }
                }
            }
        },
        confirmButton = {
            if (cartItems.isNotEmpty()) {
                Button(
                    onClick = {
                        onPlaceOrder(
                            customAddress,
                            selectedPayment,
                            appliedCoupon?.code ?: "",
                            discount
                        )
                    },
                    modifier = Modifier.testTag("place_order_button")
                ) {
                    Text("Place Order • ₹${finalTotal.toInt()}")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodySmall,
            color = if (bold) TextPrimary else TextSecondary
        )
        Text(
            value,
            style = if (bold) MaterialTheme.typography.titleMedium.copy(color = BrandBlueDark) else MaterialTheme.typography.bodySmall,
            color = if (bold) BrandBlueDark else TextPrimary
        )
    }
}

@Composable
fun ServiceBookingDialog(
    service: ServiceItem,
    defaultAddress: String,
    onConfirm: (String, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var vehicleInfo by remember { mutableStateOf("Honda City / Hero Splendor") }
    var problem by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("Tomorrow") }
    var time by remember { mutableStateOf("10:30 AM") }
    var address by remember { mutableStateOf(defaultAddress) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Book ${service.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Provider: ${service.providerName} • ₹${service.price.toInt()}", style = MaterialTheme.typography.bodySmall, color = BrandBlue)
                OutlinedTextField(value = vehicleInfo, onValueChange = { vehicleInfo = it }, label = { Text("Vehicle / Appliance Model") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = problem, onValueChange = { problem = it }, label = { Text("Describe Problem") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Service Address") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(vehicleInfo, problem.ifBlank { "General service inspection" }, date, time, address)
                },
                modifier = Modifier.testTag("confirm_booking_button")
            ) {
                Text("Confirm Booking")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun LiveStreamViewerDialog(
    stream: LiveStream,
    messages: List<StreamMessage>,
    onSendMessage: (String) -> Unit,
    onBookMechanic: () -> Unit,
    onDismiss: () -> Unit
) {
    var chatText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = AccentRed, shape = RoundedCornerShape(50)) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RadioButtonChecked, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("LIVE", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(stream.hostName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("${stream.viewerCount} watching", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_live_mechanic),
                        contentDescription = stream.title,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                    Image(
                        painter = painterResource(id = R.drawable.img_worker_avatar),
                        contentDescription = stream.hostName,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(stream.title, style = MaterialTheme.typography.titleSmall)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Text("Ask the mechanic a live question below...", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    } else {
                        items(messages, key = { it.id }) { m ->
                            Text("${m.senderName}: ${m.text}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = chatText,
                        onValueChange = { chatText = it },
                        placeholder = { Text("Send live question...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (chatText.isNotBlank()) {
                            onSendMessage(chatText)
                            chatText = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = BrandBlue)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onBookMechanic) {
                Text("Book Mechanic")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun CustomerOrdersDialog(
    orders: List<Order>,
    onCancelOrder: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My Orders (${orders.size})") },
        text = {
            if (orders.isEmpty()) {
                Text("No orders placed yet. Add items to cart and checkout to track orders live.")
            } else {
                LazyColumn(
                    modifier = Modifier.height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(orders.sortedByDescending { it.createdAt?.seconds ?: 0L }, key = { it.id }) { ord ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("#${ord.id}", style = MaterialTheme.typography.titleSmall)
                                    StatusPill(ord.status)
                                }
                                Text(ord.itemsSummary, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                Text("Total: ₹${ord.totalAmount.toInt()} • ${ord.paymentMethod} (${ord.paymentStatus})", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                                Text("Drop: ${ord.dropAddress}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                if (ord.deliveryPartnerName.isNotBlank()) {
                                    Text("Delivery Partner: ${ord.deliveryPartnerName}", style = MaterialTheme.typography.labelSmall, color = AccentGreen)
                                }
                                if (ord.status == "PENDING" || ord.status == "CONFIRMED") {
                                    TextButton(onClick = { onCancelOrder(ord.id) }, contentPadding = PaddingValuesZero) {
                                        Text("Cancel Order", color = AccentRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

private val PaddingValuesZero = androidx.compose.foundation.layout.PaddingValues(0.dp)

@Composable
fun CustomerBookingsDialog(
    bookings: List<ServiceBooking>,
    onCancelBooking: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My Service Bookings (${bookings.size})") },
        text = {
            if (bookings.isEmpty()) {
                Text("No service bookings yet.")
            } else {
                LazyColumn(modifier = Modifier.height(340.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(bookings.sortedByDescending { it.createdAt?.seconds ?: 0L }, key = { it.id }) { bk ->
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("#${bk.id}", style = MaterialTheme.typography.titleSmall)
                                    StatusPill(bk.status)
                                }
                                Text(bk.serviceTitle, style = MaterialTheme.typography.titleSmall, color = BrandBlueDark)
                                Text("${bk.scheduledDate} at ${bk.scheduledTime} • ₹${bk.amount.toInt()}", style = MaterialTheme.typography.bodySmall)
                                Text("Mechanic: ${bk.mechanicName}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                if (bk.status == "PENDING") {
                                    TextButton(onClick = { onCancelBooking(bk.id) }, contentPadding = PaddingValuesZero) {
                                        Text("Cancel Booking", color = AccentRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun WishlistDialog(
    items: List<WishlistItem>,
    products: List<Product>,
    onMoveToCart: (Product) -> Unit,
    onRemove: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My Wishlist (${items.size})") },
        text = {
            if (items.isEmpty()) {
                Text("No saved wishlist items yet.")
            } else {
                LazyColumn(modifier = Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.id }) { item ->
                        val matched = products.firstOrNull { it.id == item.productId } ?: Product(
                            id = item.productId,
                            title = item.title,
                            category = item.category,
                            price = item.price,
                            originalPrice = item.originalPrice,
                            discountPercent = item.discountPercent
                        )
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.titleSmall)
                                Text("₹${item.price.toInt()}", style = MaterialTheme.typography.bodySmall, color = BrandBlue)
                            }
                            TextButton(onClick = { onMoveToCart(matched) }) { Text("To Cart") }
                            IconButton(onClick = { onRemove(matched) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = AccentRed)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun NotificationsDialog(
    notifications: List<AppNotification>,
    onMarkRead: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notifications") },
        text = {
            if (notifications.isEmpty()) {
                Text("You are all caught up! Order, booking, and wallet alerts appear here.")
            } else {
                LazyColumn(modifier = Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notifications.sortedByDescending { it.createdAt?.seconds ?: 0L }, key = { it.id }) { n ->
                        Surface(
                            color = if (n.read) Color(0xFFF8FAFC) else BrandBlueLight,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().clickable { if (!n.read) onMarkRead(n.id) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(n.title, style = MaterialTheme.typography.titleSmall)
                                Text(n.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun AddressesDialog(
    addresses: List<Address>,
    defaultDistrict: String,
    onSaveAddress: (Address) -> Unit,
    onDeleteAddress: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var addressType by remember { mutableStateOf("Home") }
    var recipient by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var houseFlatNo by remember { mutableStateOf("") }
    var streetRoad by remember { mutableStateOf("") }
    var areaLocality by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var city by remember { mutableStateOf(defaultDistrict) }
    var state by remember { mutableStateOf("Bihar") }
    var pincode by remember { mutableStateOf("800001") }
    var gpsLocation by remember { mutableStateOf("") }

    val locationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context).lastLocation
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            gpsLocation = "${"%.4f".format(loc.latitude)}, ${"%.4f".format(loc.longitude)}"
                        }
                    }
            } catch (_: SecurityException) {}
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Saved Delivery Addresses") },
        text = {
            LazyColumn(modifier = Modifier.height(400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(addresses, key = { it.id }) { a ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${a.label} • ${a.recipientName} (${a.mobile})", style = MaterialTheme.typography.titleSmall)
                                Text("${a.fullAddress}, ${a.city}, ${a.district} - ${a.pincode}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                if (a.gpsLocation.isNotBlank()) {
                                    Text("GPS: ${a.gpsLocation}", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                                }
                            }
                            IconButton(onClick = { onDeleteAddress(a.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed)
                            }
                        }
                    }
                }
                item {
                    HorizontalDivider()
                    Spacer(Modifier.height(6.dp))
                    Text("Add New Delivery Address", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Home", "Office", "Other").forEach { t ->
                            val selected = addressType == t
                            Surface(
                                color = if (selected) BrandBlue else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.clickable { addressType = t }
                            ) {
                                Text(
                                    t,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.White else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(value = recipient, onValueChange = { recipient = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = mobile, onValueChange = { mobile = it.filter(Char::isDigit).take(10) }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = houseFlatNo, onValueChange = { houseFlatNo = it }, label = { Text("House / Flat Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = streetRoad, onValueChange = { streetRoad = it }, label = { Text("Street / Road") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = areaLocality, onValueChange = { areaLocality = it }, label = { Text("Area / Locality") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = landmark, onValueChange = { landmark = it }, label = { Text("Landmark (Optional)") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = pincode, onValueChange = { pincode = it.filter(Char::isDigit).take(6) }, label = { Text("Pincode") }, modifier = Modifier.weight(1f))
                        OutlinedButton(
                            onClick = {
                                val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    try {
                                        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context).lastLocation
                                            .addOnSuccessListener { loc ->
                                                if (loc != null) {
                                                    gpsLocation = "${"%.4f".format(loc.latitude)}, ${"%.4f".format(loc.longitude)}"
                                                }
                                            }
                                    } catch (_: SecurityException) {}
                                } else {
                                    locationLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                }
                            },
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(if (gpsLocation.isBlank()) "GPS (Opt)" else "GPS ✓", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val composed = listOf(houseFlatNo, streetRoad, areaLocality, landmark)
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .joinToString(", ")
                            if (recipient.isNotBlank() && mobile.length >= 6 && composed.length >= 3) {
                                onSaveAddress(
                                    Address(
                                        label = addressType,
                                        recipientName = recipient.trim(),
                                        mobile = mobile.trim(),
                                        houseFlatNo = houseFlatNo.trim(),
                                        streetRoad = streetRoad.trim(),
                                        areaLocality = areaLocality.trim(),
                                        landmark = landmark.trim(),
                                        fullAddress = composed,
                                        city = city.ifBlank { defaultDistrict },
                                        district = defaultDistrict,
                                        state = state.ifBlank { "Bihar" },
                                        pincode = pincode.ifBlank { "800001" },
                                        gpsLocation = gpsLocation,
                                        defaultAddress = addresses.isEmpty()
                                    )
                                )
                                recipient = ""; mobile = ""; houseFlatNo = ""; streetRoad = ""; areaLocality = ""; landmark = ""; gpsLocation = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_customer_address_button")
                    ) {
                        Text("Save Delivery Address")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun DistrictPickerDialog(
    locations: List<ServiceLocation>,
    currentDistrict: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Service District") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                locations.forEach { loc ->
                    val selected = loc.district.equals(currentDistrict, true)
                    Surface(
                        color = if (selected) BrandBlueLight else Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            onSelect(loc.district)
                            onDismiss()
                        }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${loc.district}, ${loc.state}", style = MaterialTheme.typography.titleSmall)
                            Text("Pincode ${loc.pincode} • Services: ${if (loc.serviceAvailable) "Active" else "Off"}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun SupportTicketDialog(
    tickets: List<SupportTicket>,
    onSubmit: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var category by remember { mutableStateOf("Order") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customer Support & Tickets") },
        text = {
            LazyColumn(modifier = Modifier.height(340.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (Order/Booking/Payment)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Describe issue") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = {
                            if (subject.isNotBlank() && description.isNotBlank()) {
                                onSubmit(category, subject, description)
                                subject = ""; description = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Submit Support Ticket") }
                }
                items(tickets, key = { it.id }) { t ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(t.subject, style = MaterialTheme.typography.titleSmall)
                                StatusPill(t.status)
                            }
                            Text(t.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            if (t.adminReply.isNotBlank()) {
                                Text("Admin Reply: ${t.adminReply}", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
