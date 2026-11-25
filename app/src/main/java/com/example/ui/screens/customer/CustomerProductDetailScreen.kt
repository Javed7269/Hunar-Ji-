package com.example.ui.screens.customer

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.AdMobConfig
import com.example.data.model.MarketplaceReview
import com.example.data.model.Product
import com.example.data.model.ProductMediaItem
import com.example.data.model.ProviderProfile
import com.example.ui.components.AdMobBannerCard
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.StatusPill
import com.example.ui.components.VideoPlayerCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberLight
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CustomerProductDetailScreen(
    product: Product,
    saved: Boolean,
    reviews: List<MarketplaceReview>,
    relatedProducts: List<Product>,
    sellerProfile: ProviderProfile?,
    adMobConfig: AdMobConfig,
    onAddToCart: (Int, String) -> Unit,
    onBuyNow: (String) -> Unit,
    onToggleWishlist: () -> Unit,
    onToggleCompare: () -> Unit,
    isCompared: Boolean,
    onSelectRelatedProduct: (Product) -> Unit,
    onSubmitReview: (Long, String, String) -> Unit,
    onLogAdImpression: (String) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    val context = LocalContext.current
    val mediaList = remember(product) {
        if (product.mediaGallery.isNotEmpty()) {
            product.mediaGallery.sortedBy { it.sortOrder }
        } else {
            listOf(
                ProductMediaItem(
                    id = "fallback_main",
                    type = "IMAGE",
                    angleTag = "Front",
                    uriOrUrl = "",
                    caption = product.title
                )
            )
        }
    }
    var selectedMediaIndex by remember(product.id) { mutableIntStateOf(0) }
    var zoomImageItem by remember { mutableStateOf<ProductMediaItem?>(null) }
    var selectedColor by remember(product.id) {
        mutableStateOf(product.colorOptions.firstOrNull() ?: "Standard")
    }
    var selectedSize by remember(product.id) {
        mutableStateOf(product.sizeOptions.firstOrNull() ?: "")
    }
    var qty by remember(product.id) { mutableIntStateOf(1) }
    var pincodeInput by remember { mutableStateOf("800001") }
    var pincodeStatus by remember { mutableStateOf("Delivery in ${product.estimatedDeliveryDays} days • ${if (product.codAvailable) "COD Available" else "Prepaid Only"}") }
    var showReviewDialog by remember { mutableStateOf(false) }
    var expandedFaqIndex by remember { mutableIntStateOf(-1) }

    val activeVariant = remember(selectedColor, selectedSize) {
        listOf(selectedColor, selectedSize).filter { it.isNotBlank() }.joinToString(" / ").ifBlank { "Standard" }
    }
    val productReviews = remember(reviews, product.id) {
        reviews.filter { it.targetId == product.id }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            containerColor = SurfaceLight,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("pdp_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                product.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${product.category} • ${product.subcategory}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        IconButton(onClick = onToggleCompare, modifier = Modifier.testTag("pdp_compare_button")) {
                            Icon(
                                Icons.Default.CompareArrows,
                                contentDescription = "Compare Product",
                                tint = if (isCompared) BrandBlue else TextMuted
                            )
                        }
                        IconButton(onClick = onToggleWishlist, modifier = Modifier.testTag("pdp_wishlist_button")) {
                            Icon(
                                if (saved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (saved) AccentRed else TextMuted
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = Color.White,
                    shadowElevation = 10.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onAddToCart(qty, activeVariant) },
                            enabled = product.stock > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("pdp_add_to_cart_button")
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add to Cart")
                        }
                        Button(
                            onClick = { onBuyNow(activeVariant) },
                            enabled = product.stock > 0,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("pdp_buy_now_button")
                        ) {
                            Text(if (product.stock > 0) "Buy Now • ₹${(product.price * qty).toInt()}" else "Out of Stock")
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SECTION 1 & 2: Multi-Image / Multi-Video Gallery + Thumbnails + Zoom
                item {
                    val activeMedia = mediaList.getOrNull(selectedMediaIndex) ?: mediaList.first()
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (activeMedia.type == "VIDEO") {
                                VideoPlayerCard(
                                    videoUrl = activeMedia.uriOrUrl,
                                    title = "${activeMedia.angleTag} Video",
                                    subtitle = activeMedia.caption,
                                    thumbnailUrl = activeMedia.thumbnailUrl,
                                    youtubeId = if (activeMedia.youtubeVideo) activeMedia.uriOrUrl else "",
                                    height = 230.dp
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(230.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { zoomImageItem = activeMedia }
                                ) {
                                    ProductThumbnail(
                                        iconKey = product.iconKey,
                                        imageUrl = activeMedia.uriOrUrl,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.65f),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            activeMedia.angleTag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.65f),
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(10.dp)
                                            .size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.ZoomIn, contentDescription = "Pinch zoom photo", tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                            if (mediaList.size > 1) {
                                Spacer(Modifier.height(10.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(mediaList.size) { idx ->
                                        val m = mediaList[idx]
                                        val selected = idx == selectedMediaIndex
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(
                                                width = if (selected) 2.dp else 1.dp,
                                                color = if (selected) BrandBlue else Color(0xFFE2E8F0)
                                            ),
                                            color = if (selected) BrandBlueLight else Color.White,
                                            modifier = Modifier
                                                .width(74.dp)
                                                .clickable { selectedMediaIndex = idx }
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(4.dp)
                                            ) {
                                                if (m.type == "VIDEO") {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(44.dp)
                                                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("VIDEO", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                                    }
                                                } else {
                                                    ProductThumbnail(
                                                        iconKey = product.iconKey,
                                                        imageUrl = m.uriOrUrl,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(44.dp)
                                                    )
                                                }
                                                Spacer(Modifier.height(3.dp))
                                                Text(m.angleTag, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 3 & 4: Core Header, Price, Stock, Brand/Model/SKU, Variants & Qty
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (product.brand.isNotBlank()) {
                                    Surface(color = BrandBlueLight, shape = RoundedCornerShape(50)) {
                                        Text(product.brand, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BrandBlueDark, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                                    }
                                }
                                StatusPill("${product.discountPercent}% OFF")
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.Star, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                                Text("${product.rating} (${product.reviewCount + productReviews.size})", style = MaterialTheme.typography.labelMedium)
                            }
                            Text(product.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("₹${product.price.toInt()}", style = MaterialTheme.typography.headlineMedium.copy(color = BrandBlueDark, fontWeight = FontWeight.ExtraBold))
                                Spacer(Modifier.width(10.dp))
                                Text("MRP ₹${product.originalPrice.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted, textDecoration = TextDecoration.LineThrough))
                            }
                            Text(
                                text = if (product.stock > 0) "In Stock (${product.stock} units available) • Sold: ${product.soldCount}" else "Currently Out of Stock",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (product.stock > 0) AccentGreen else AccentRed
                            )
                            if (product.modelNumber.isNotBlank() || product.sku.isNotBlank()) {
                                Text("Model: ${product.modelNumber.ifBlank { "-" }}  •  SKU: ${product.sku.ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }

                            if (product.colorOptions.isNotEmpty()) {
                                Text("Color / Option", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    product.colorOptions.forEach { colorOpt ->
                                        val active = selectedColor == colorOpt
                                        Surface(
                                            color = if (active) BrandBlue else Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.clickable { selectedColor = colorOpt }
                                        ) {
                                            Text(
                                                colorOpt,
                                                color = if (active) Color.White else TextPrimary,
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (product.sizeOptions.isNotEmpty()) {
                                Text("Size / Pack", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    product.sizeOptions.forEach { sizeOpt ->
                                        val active = selectedSize == sizeOpt
                                        Surface(
                                            color = if (active) BrandBlue else Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.clickable { selectedSize = sizeOpt }
                                        ) {
                                            Text(
                                                sizeOpt,
                                                color = if (active) Color.White else TextPrimary,
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Quantity:", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.width(10.dp))
                                IconButton(onClick = { if (qty > 1) qty-- }) { Icon(Icons.Default.Remove, contentDescription = "Decrease") }
                                Text("$qty", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                IconButton(onClick = { if (qty < 10 && qty < product.stock.coerceAtLeast(1)) qty++ }) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }
                        }
                    }
                }

                // SECTION 5: Delivery Pincode & COD Checker
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = BrandBlue)
                                Spacer(Modifier.width(8.dp))
                                Text("Delivery & Availability", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = pincodeInput,
                                    onValueChange = { pincodeInput = it.filter(Char::isDigit).take(6) },
                                    label = { Text("Enter Pincode") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(onClick = {
                                    pincodeStatus = if (pincodeInput.length == 6) {
                                        "Available in $pincodeInput • Est. ${product.estimatedDeliveryDays} days • ${if (product.codAvailable) "COD Supported" else "Online Pay"}"
                                    } else {
                                        "Enter a valid 6-digit pincode"
                                    }
                                }) { Text("Check") }
                            }
                            Text(pincodeStatus, style = MaterialTheme.typography.bodySmall, color = AccentGreen)
                        }
                    }
                }

                // SECTION 6 & 7: Highlights & Full Description
                item {
                    PdpSectionCard(title = "Product Highlights & Description") {
                        if (product.highlights.isNotEmpty()) {
                            product.highlights.forEach { hl ->
                                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(hl, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(product.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }

                // SECTION 8: Features & Benefits
                if (product.features.isNotEmpty() || product.benefits.isNotEmpty()) {
                    item {
                        PdpSectionCard(title = "Key Features & Benefits") {
                            product.features.forEach { feat ->
                                Text("• $feat", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }
                            if (product.benefits.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Text("Benefits", style = MaterialTheme.typography.labelLarge.copy(color = BrandBlueDark))
                                product.benefits.forEach { ben ->
                                    Text("✓ $ben", style = MaterialTheme.typography.bodyMedium, color = AccentGreen)
                                }
                            }
                        }
                    }
                }

                // SECTION 9: Usage, Installation & Safety Instructions
                if (product.usageInstructions.isNotBlank() || product.installationInstructions.isNotBlank() || product.safetyInfo.isNotBlank()) {
                    item {
                        PdpSectionCard(title = "Usage, Installation & Safety") {
                            if (product.usageInstructions.isNotBlank()) {
                                Text("How to Use", style = MaterialTheme.typography.labelLarge.copy(color = BrandBlueDark))
                                Text(product.usageInstructions, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Spacer(Modifier.height(6.dp))
                            }
                            if (product.installationInstructions.isNotBlank()) {
                                Text("Installation Guide", style = MaterialTheme.typography.labelLarge.copy(color = BrandBlueDark))
                                Text(product.installationInstructions, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Spacer(Modifier.height(6.dp))
                            }
                            if (product.safetyInfo.isNotBlank()) {
                                Surface(color = AccentAmberLight, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text("Safety Note: ${product.safetyInfo}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF92400E), modifier = Modifier.padding(10.dp))
                                }
                            }
                        }
                    }
                }

                // SECTION 10: Technical Specifications Table
                item {
                    PdpSectionCard(title = "Technical Specifications") {
                        val combinedSpecs = buildList {
                            if (product.brand.isNotBlank()) add("Brand" to product.brand)
                            if (product.modelNumber.isNotBlank()) add("Model Number" to product.modelNumber)
                            if (product.weight.isNotBlank()) add("Weight" to product.weight)
                            if (product.dimensions.isNotBlank()) add("Dimensions" to product.dimensions)
                            if (product.material.isNotBlank()) add("Material" to product.material)
                            if (product.manufacturer.isNotBlank()) add("Manufacturer" to product.manufacturer)
                            if (product.countryOfOrigin.isNotBlank()) add("Country of Origin" to product.countryOfOrigin)
                            if (product.packageContents.isNotBlank()) add("In the Box" to product.packageContents)
                            product.specs.forEach { (k, v) -> add(k to v) }
                        }
                        if (combinedSpecs.isEmpty()) {
                            Text("Standard Hunar Ji verified specification", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        } else {
                            combinedSpecs.forEachIndexed { idx, (k, v) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (idx % 2 == 0) Color(0xFFF8FAFC) else Color.White)
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(k, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = TextSecondary, modifier = Modifier.weight(0.42f))
                                    Text(v, style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(0.58f))
                                }
                            }
                        }
                    }
                }

                // SECTION 11: Step-by-Step Builder Guide
                if (product.stepByStepSections.isNotEmpty()) {
                    item {
                        PdpSectionCard(title = "Step-by-Step Setup & Guide") {
                            product.stepByStepSections.sortedBy { it.stepNumber }.forEach { step ->
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Step ${step.stepNumber}: ${step.heading}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BrandBlueDark))
                                        if (step.description.isNotBlank()) {
                                            Text(step.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        }
                                        if (step.instructions.isNotBlank()) {
                                            Text("Tip: ${step.instructions}", style = MaterialTheme.typography.labelSmall, color = AccentGreen)
                                        }
                                        if (step.imageUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = step.imageUrl,
                                                contentDescription = step.heading,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp))
                                            )
                                        }
                                        if (step.videoUrl.isNotBlank()) {
                                            VideoPlayerCard(videoUrl = step.videoUrl, title = step.heading, height = 140.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 12: Warranty, Guarantee, Return Policy & PDF Documents
                item {
                    PdpSectionCard(title = "Warranty, Returns & Documents") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = BrandBlue)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (product.warrantyAvailable) "${product.warrantyDuration} • ${product.warrantyType}" else "No Manufacturer Warranty",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        if (product.warrantyConditions.isNotBlank()) {
                            Text("Coverage: ${product.warrantyConditions}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        if (product.warrantyServiceCentre.isNotBlank()) {
                            Text("Service Centre: ${product.warrantyServiceCentre}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        if (product.warrantyRegistrationSteps.isNotBlank()) {
                            Text("Registration: ${product.warrantyRegistrationSteps}", style = MaterialTheme.typography.bodySmall, color = BrandBlue)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Text("Return Policy: ${product.returnPolicy}", style = MaterialTheme.typography.bodySmall, color = AccentGreen)
                        Text("Replacement: ${product.replacementPolicy}", style = MaterialTheme.typography.bodySmall, color = AccentGreen)

                        product.warrantyDocs.forEach { doc ->
                            Spacer(Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(doc.uriOrUrl)))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("${doc.label.ifBlank { doc.fileName }} (${doc.docType})")
                            }
                        }
                    }
                }

                // SECTION 13: Expandable FAQs
                if (product.faqs.isNotEmpty()) {
                    item {
                        PdpSectionCard(title = "Frequently Asked Questions") {
                            product.faqs.forEachIndexed { idx, faq ->
                                val q = faq["q"] ?: faq["question"] ?: ""
                                val a = faq["a"] ?: faq["answer"] ?: ""
                                val expanded = expandedFaqIndex == idx
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable { expandedFaqIndex = if (expanded) -1 else idx }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Q: $q", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                                        }
                                        AnimatedVisibility(visible = expanded) {
                                            Text("A: $a", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = 6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 14: Seller Shop Info Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = BrandBlueLight, shape = CircleShape, modifier = Modifier.size(46.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = BrandBlue)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(product.sellerName.ifBlank { "Verified Hunar Ji Seller" }, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Default.Verified, contentDescription = "Verified Seller", tint = AccentGreen, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    "District: ${sellerProfile?.district ?: product.district} • Rating ${sellerProfile?.rating ?: 4.7}★",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // SECTION 15: Customer Reviews & Rating Submission
                item {
                    PdpSectionCard(title = "Verified Customer Reviews (${productReviews.size})") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Overall Rating: ${product.rating} ★", style = MaterialTheme.typography.titleMedium.copy(color = AccentAmber, fontWeight = FontWeight.Bold))
                            OutlinedButton(onClick = { showReviewDialog = true }, modifier = Modifier.testTag("write_review_button")) {
                                Text("Write a Review")
                            }
                        }
                        if (productReviews.isEmpty()) {
                            Text("No customer reviews yet. Be the first to review this product!", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        } else {
                            productReviews.take(6).forEach { rev ->
                                Surface(color = Color(0xFFF8FAFC), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(rev.userName, style = MaterialTheme.typography.labelLarge)
                                            Text("${rev.rating} ★", style = MaterialTheme.typography.labelMedium, color = AccentAmber)
                                        }
                                        if (rev.title.isNotBlank()) {
                                            Text(rev.title, style = MaterialTheme.typography.titleSmall)
                                        }
                                        Text(rev.comment, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                // AdMob Placement
                item {
                    AdMobBannerCard(
                        config = adMobConfig,
                        placementEnabled = adMobConfig.placementProductDetail,
                        onImpressionLogged = onLogAdImpression
                    )
                }

                // SECTION 16: Related Products
                if (relatedProducts.isNotEmpty()) {
                    item {
                        Text("Related Products", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(relatedProducts, key = { it.id }) { rel ->
                                Card(
                                    modifier = Modifier
                                        .width(150.dp)
                                        .clickable { onSelectRelatedProduct(rel) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        ProductThumbnail(
                                            iconKey = rel.iconKey,
                                            imageUrl = rel.mediaGallery.firstOrNull { it.type == "IMAGE" }?.uriOrUrl ?: "",
                                            modifier = Modifier.fillMaxWidth().height(76.dp)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(rel.title, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("₹${rel.price.toInt()}", style = MaterialTheme.typography.titleSmall.copy(color = BrandBlueDark, fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Pinch-to-Zoom Photo Modal
    zoomImageItem?.let { item ->
        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        Dialog(onDismissRequest = { zoomImageItem = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f; offsetY = 0f
                            }
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offsetX, translationY = offsetY),
                    contentAlignment = Alignment.Center
                ) {
                    ProductThumbnail(
                        iconKey = product.iconKey,
                        imageUrl = item.uriOrUrl,
                        modifier = Modifier.fillMaxWidth().height(360.dp)
                    )
                }
                IconButton(
                    onClick = { zoomImageItem = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Zoom", tint = Color.White)
                }
                Text(
                    "${item.angleTag} • Pinch to Zoom (${"%.1f".format(scale)}x)",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp)
                )
            }
        }
    }

    if (showReviewDialog) {
        var revRating by remember { mutableStateOf(5L) }
        var revTitle by remember { mutableStateOf("") }
        var revComment by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Rate & Review ${product.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1L..5L).forEach { star ->
                            IconButton(onClick = { revRating = star }) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= revRating) AccentAmber else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                    OutlinedTextField(value = revTitle, onValueChange = { revTitle = it }, label = { Text("Review Headline") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = revComment, onValueChange = { revComment = it }, label = { Text("Your experience") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (revComment.isNotBlank()) {
                        onSubmitReview(revRating, revTitle, revComment)
                        showReviewDialog = false
                    }
                }) { Text("Publish Review") }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun PdpSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            content()
        }
    }
}
