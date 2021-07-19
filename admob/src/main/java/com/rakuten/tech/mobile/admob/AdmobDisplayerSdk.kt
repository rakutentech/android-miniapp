package com.rakuten.tech.mobile.admob

import android.app.Activity

/**
 * This represents the exposed apis for latest admob version 20.0.0.
 * Should be accessed via [AdmobDisplayerSdk.getInstance].
 */
abstract class AdmobDisplayerSdk internal constructor() {

    /** Load the interstitial ad when it is ready.
     * @param context the activity the ad will load.
     * @param adUnitId the id the ad will load upon.
     * @param onCallback is the callback status of th ad.
     */
    abstract fun loadInterstitialAdForSdk(
        context: Activity,
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    )

    /** show the interstitial ad when it is ready.
     * @param context the activity the ad will load.
     * @param adUnitId the id the ad will load upon.
     * @param onCallback is the callback status of th ad.
     */
    abstract fun showInterstitialAdForSdk(
        context: Activity,
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    )

    companion object {
        private lateinit var instance: AdmobDisplayerSdk
        fun getInstance(): AdmobDisplayerSdk {
            return instance
        }

        fun init() {
            instance = RealAdmobDisplayerSdk()
        }
    }
}
