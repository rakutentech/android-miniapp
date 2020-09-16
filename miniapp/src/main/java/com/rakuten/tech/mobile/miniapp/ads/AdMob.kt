package com.rakuten.tech.mobile.miniapp.ads

/**
 * Check whether hostapp provides AdMob dependency.
 * If the dependency is provided then it means the AdMob app id has also been set from hostapp.
 */
inline fun <T> whenAdMobProvided(callback: () -> T) {
    try {
        Class.forName("com.google.android.gms.ads.MobileAds")
        callback.invoke()
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    }
}
