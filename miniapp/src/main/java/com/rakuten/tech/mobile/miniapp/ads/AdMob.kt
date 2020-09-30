package com.rakuten.tech.mobile.miniapp.ads

import androidx.annotation.VisibleForTesting

/**
 * Check whether hostapp provides AdMob dependency.
 * If the dependency is provided then it means the AdMob app id has also been set from hostapp.
 */
@Suppress("SwallowedException")
internal fun isAdMobProvided(): Boolean =
    try {
        Class.forName(AdMobClassName)
        true
    } catch (e: ClassNotFoundException) {
        false
    }

@VisibleForTesting
internal var AdMobClassName = "com.google.android.gms.ads.MobileAds"
