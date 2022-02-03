package com.rakuten.tech.mobile.miniapp

import androidx.annotation.VisibleForTesting

/**
 * Check whether hostapp provides AdMob dependency.
 * If the dependency is provided then it means the AdMob app id has also been set from hostapp.
 */
@Suppress("SwallowedException")
fun isAdMobProvided(): Boolean =
    try {
        Class.forName(AdMobClassName)
        true
    } catch (e: ClassNotFoundException) {
        false
    }

/** Check whether hostapp provides admob dependency. */
@Suppress("EmptyCatchBlock", "SwallowedException")
fun <T> whenHasH5AdsWebViewClient(callback: () -> T) {
    try {
        Class.forName("com.rakuten.tech.mobile.miniapp.ads.MiniAppH5AdsWebViewClient")
        callback.invoke()
    } catch (e: ClassNotFoundException) {
    }
}

@VisibleForTesting
internal var AdMobClassName = "com.google.android.gms.ads.MobileAds"
