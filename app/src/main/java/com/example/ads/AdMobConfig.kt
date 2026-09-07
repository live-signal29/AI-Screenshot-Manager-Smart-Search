package com.example.ads

import com.example.BuildConfig

object AdMobConfig {

    // Official Google Mobile Ads Test Unit IDs for development
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    // Production Placeholders: Users replace these with their own AdMob IDs before Play Store release
    private const val PROD_BANNER_ID = "YOUR_BANNER_AD_UNIT_ID"
    private const val PROD_INTERSTITIAL_ID = "YOUR_INTERSTITIAL_AD_UNIT_ID"
    private const val PROD_REWARDED_ID = "YOUR_REWARDED_AD_UNIT_ID"

    val BANNER_AD_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID

    val INTERSTITIAL_AD_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID

    val REWARDED_AD_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID

    // Safety policy: Check if production ID is still a placeholder
    val isBannerConfigured: Boolean
        get() = BuildConfig.DEBUG || BANNER_AD_UNIT_ID.startsWith("ca-app-pub-")

    val isInterstitialConfigured: Boolean
        get() = BuildConfig.DEBUG || INTERSTITIAL_AD_UNIT_ID.startsWith("ca-app-pub-")

    val isRewardedConfigured: Boolean
        get() = BuildConfig.DEBUG || REWARDED_AD_UNIT_ID.startsWith("ca-app-pub-")
}
