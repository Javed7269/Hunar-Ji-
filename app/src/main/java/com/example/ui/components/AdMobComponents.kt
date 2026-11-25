package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.AdMobConfig
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberLight
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.UUID

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun AdMobBannerCard(
    config: AdMobConfig,
    placementEnabled: Boolean = true,
    onImpressionLogged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!config.adsEnabled || !config.bannerEnabled || !placementEnabled) return
    val unitId = remember(config.testMode, config.bannerUnitId) { config.effectiveBannerUnitId() }
    var loaded by remember(unitId) { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sponsored AdMob", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
                if (config.testMode) {
                    Surface(color = AccentAmberLight, shape = RoundedCornerShape(50)) {
                        Text(
                            "Test Ad",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            AndroidView(
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = unitId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                if (!loaded) {
                                    loaded = true
                                    onImpressionLogged(unitId)
                                }
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )
        }
    }
}

@Composable
fun RewardedAdWatchCard(
    config: AdMobConfig,
    todayClaimCount: Long,
    onRewardEarnedBySdk: (adUnitId: String, rewardToken: String) -> Unit,
    onStatusMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val unitId = remember(config.testMode, config.rewardedUnitId) { config.effectiveRewardedUnitId() }
    var rewardedAd by remember(unitId) { mutableStateOf<RewardedAd?>(null) }
    var isLoading by remember(unitId) { mutableStateOf(false) }

    fun loadAd() {
        if (!config.adsEnabled || !config.rewardedEnabled || isLoading) return
        isLoading = true
        RewardedAd.load(
            context,
            unitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(err: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                }
            }
        )
    }

    LaunchedEffect(unitId, config.adsEnabled, config.rewardedEnabled) {
        if (config.adsEnabled && config.rewardedEnabled && rewardedAd == null) {
            loadAd()
        }
    }

    val capReached = todayClaimCount >= config.dailyRewardCap

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = AccentAmberLight, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(44.dp)) {
                    Icon(
                        Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Watch Rewarded Ad & Earn +${config.rewardPointsPerAd} Points",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        "Daily limit: $todayClaimCount / ${config.dailyRewardCap} completed • Points never mix with cash wallet",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val activity = context.findActivity()
                    if (!config.adsEnabled || !config.rewardedEnabled) {
                        onStatusMessage("Rewarded ads are currently paused by Admin")
                        return@Button
                    }
                    if (capReached) {
                        onStatusMessage("Daily limit (${config.dailyRewardCap}) reached. Come back tomorrow!")
                        return@Button
                    }
                    val ad = rewardedAd
                    if (ad == null) {
                        loadAd()
                        onStatusMessage("Loading Google AdMob rewarded video. Please tap again in a moment.")
                        return@Button
                    }
                    if (activity == null) {
                        onStatusMessage("Unable to open ad overlay right now.")
                        return@Button
                    }
                    val sessionToken = "rwd_${UUID.randomUUID().toString().replace("-", "").take(16)}"
                    var callbackTriggered = false
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            loadAd()
                            if (!callbackTriggered) {
                                onStatusMessage("Ad closed early. Watch full video to earn reward points.")
                            }
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                            rewardedAd = null
                            loadAd()
                            onStatusMessage("Could not display ad: ${p0.message}")
                        }
                    }
                    ad.show(activity) { _ ->
                        // Credit strictly inside Google Mobile Ads SDK OnUserEarnedRewardListener callback
                        callbackTriggered = true
                        onRewardEarnedBySdk(unitId, sessionToken)
                    }
                },
                enabled = config.adsEnabled && config.rewardedEnabled && !capReached,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("watch_rewarded_ad_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading && rewardedAd == null) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Preparing Ad...")
                } else if (capReached) {
                    Text("Daily Limit Reached (${config.dailyRewardCap}/${config.dailyRewardCap})")
                } else if (!config.adsEnabled || !config.rewardedEnabled) {
                    Text("Rewarded Ads Paused")
                } else {
                    Icon(Icons.Default.PlayCircleFilled, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Watch Ad & Earn +${config.rewardPointsPerAd} Pts")
                }
            }
        }
    }
}
