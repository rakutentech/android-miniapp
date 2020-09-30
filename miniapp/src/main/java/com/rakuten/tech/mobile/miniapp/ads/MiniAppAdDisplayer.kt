package com.rakuten.tech.mobile.miniapp.ads

/**
 * Control ads load & display when want to use AdMob.
 */
interface MiniAppAdDisplayer {

    /** Load the interstitial ad when it is ready. **/
    fun loadInterstitial(adUnitId: String, onLoaded: () -> Unit, onFailed: (String) -> Unit)

    /** Show the interstitial ad when it is already loaded. **/
    fun showInterstitial(adUnitId: String, onClosed: () -> Unit, onFailed: (String) -> Unit)
}
