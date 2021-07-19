package com.rakuten.tech.mobile.miniapp.ads

/**
 * Control ads load & display when want to use AdMob.
 */
interface MiniAppAdDisplayer {

    /** Load the interstitial ad when it is ready. **/
    fun loadInterstitialAd(adUnitId: String, onLoaded: () -> Unit, onFailed: (String) -> Unit)

    /** Load the interstitial ad or version 20.0.0 when it is ready. **/
    fun loadInterstitialAdSdk(adUnitId: String, onLoaded: () -> Unit, onFailed: (String) -> Unit)

    /** Show the interstitial ad when it is already loaded. **/
    fun showInterstitialAd(adUnitId: String, onClosed: () -> Unit, onFailed: (String) -> Unit)

    /** Show the interstitial ad for version 20.0.0 when it is already loaded. **/
    fun showInterstitialAdSdk(adUnitId: String, onClosed: () -> Unit, onFailed: (String) -> Unit)

    /** Load the rewarded ad when it is ready. **/
    fun loadRewardedAd(adUnitId: String, onLoaded: () -> Unit, onFailed: (String) -> Unit)

    /** Show the rewarded ad when it is already loaded. **/
    fun showRewardedAd(adUnitId: String, onClosed: (reward: Reward?) -> Unit, onFailed: (String) -> Unit)
}
