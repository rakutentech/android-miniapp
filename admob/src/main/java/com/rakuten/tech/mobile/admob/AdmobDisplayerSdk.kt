package com.rakuten.tech.mobile.admob

import android.app.Activity

/**
 * This represents the exposed apis for latest admob version 20.0.0.
 * Should be accessed via [AdmobDisplayerSdk.getInstance].
 */
abstract class AdmobDisplayerSdk internal constructor() {

    /** Load the interstitial ad when it is ready.
     * @param adUnitId the id the ad will load upon.
     * @param onCallback is the callback status of th ad.
     */
    abstract fun loadInterstitialAdForSdk(
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    )

    /** Load the rewarded ad when it is ready.
     * @param adUnitId the id the ad will load upon.
     * @param onCallback is the callback status of th ad.
     */
    abstract fun loadRewardedAdSdk(
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    )


    /** show the interstitial ad when it is ready.
     * @param adUnitId the id the ad will load upon.
     * @param onCallback is the callback status of th ad.
     */
    abstract fun showInterstitialAdForSdk(
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    )

    /** show the interstitial ad when it is ready.
     * @param adUnitId the id the ad will load upon.
     * @param onReward is the callback for getting reward of th ad.
     * @param onCallback is the callback status of th ad.
     */
    abstract fun showRewardedAdForSdk(
        adUnitId: String,
        onReward: (amount: Int, type: String) -> Unit,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    )

    companion object {
        private lateinit var instance: AdmobDisplayerSdk
        fun getInstance(): AdmobDisplayerSdk {
            return instance
        }

        fun init(context: Activity) {
            instance = RealAdmobDisplayerSdk(context)
        }
    }
}
