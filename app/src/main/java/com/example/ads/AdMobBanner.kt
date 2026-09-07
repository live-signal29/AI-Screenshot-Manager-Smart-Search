package com.example.ads

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier
) {
    if (LocalInspectionMode.current || !AdMobConfig.isBannerConfigured) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AdMob Banner Placement",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdMobConfig.BANNER_AD_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

object InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null
    private var lastAdShownTimestamp: Long = 0L
    private const val COOLDOWN_MS = 60_000L // Minimum 60 seconds between interstitials for policy compliance

    fun loadAd(context: Context) {
        if (!AdMobConfig.isInterstitialConfigured) return
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdMobConfig.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    /**
     * Shows an interstitial ad only at natural workflow transitions (e.g. after a complete scan or after storage clean).
     * Strictly avoids interrupting viewing, zoom, editing, or active operations.
     */
    fun showIfAvailable(activity: Activity, onDismissed: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        if (now - lastAdShownTimestamp < COOLDOWN_MS) {
            onDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastAdShownTimestamp = System.currentTimeMillis()
                    loadAd(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            loadAd(activity)
            onDismissed()
        }
    }
}

object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null

    fun loadAd(context: Context) {
        if (!AdMobConfig.isRewardedConfigured) return
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            AdMobConfig.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun showIfAvailable(activity: Activity, onUserEarnedReward: () -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(activity) {
                onUserEarnedReward()
            }
            rewardedAd = null
            loadAd(activity)
        } else {
            loadAd(activity)
            // If ad not loaded yet, still grant action gracefully or show notice
            onUserEarnedReward()
        }
    }
}
