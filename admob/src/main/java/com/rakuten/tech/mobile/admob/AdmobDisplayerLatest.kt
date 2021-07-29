package com.rakuten.tech.mobile.admob

import android.app.Activity

/**
 * This represents the exposed apis for latest admob version 20.2.0.
 * Should be accessed via [AdmobDisplayerLatest.getInstance].
 */
abstract class AdmobDisplayerLatest internal constructor() {

    /** Load the interstitial ad when it is ready.
     * @param adUnitId the id the ad will load upon.
     * @param onCallback is the callback status of the ad.
     */
    abstract fun loadInterstitialAd(
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    )

    /** Load the rewarded ad when it is ready.
     * @param adUnitId the id the ad will load upon.
     * @param onCallback is the callback status of the ad.
     */
    abstract fun loadRewardedAd(
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    )

    /** show the interstitial ad when it is ready.
     * @param adUnitId the id the ad will load upon.
     * @param onCallback is the callback status of the ad.
     */
    abstract fun showInterstitialAd(
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    )

    /** show the interstitial ad when it is ready.
     * @param adUnitId the id the ad will load upon.
     * @param onReward is the callback for getting reward of the ad.
     * @param onCallback is the callback status of the ad.
     */
    abstract fun showRewardedAd(
        adUnitId: String,
        onReward: (amount: Int, type: String) -> Unit,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    )

    companion object {
        private lateinit var instance: AdmobDisplayerLatest
        fun getInstance(): AdmobDisplayerLatest {
            return instance
        }

        fun init(context: Activity) {
            instance = RealAdmobDisplayerLatest(context)
        }
    }
}
