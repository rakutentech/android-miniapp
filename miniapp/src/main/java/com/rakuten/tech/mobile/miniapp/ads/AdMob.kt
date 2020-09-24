package com.rakuten.tech.mobile.miniapp.ads

/**
 * Check whether hostapp provides AdMob dependency.
 * If the dependency is provided then it means the AdMob app id has also been set from hostapp.
 */
@Suppress("EmptyCatchBlock", "SwallowedException")
inline fun whenAdMobProvided(successExec: () -> Unit, errorExec: (String) -> Unit = {}) {
    try {
        Class.forName("com.google.android.gms.ads.MobileAds")
        successExec.invoke()
    } catch (e: ClassNotFoundException) {
        errorExec.invoke("No AdMob support from HostApp")
    }
}
