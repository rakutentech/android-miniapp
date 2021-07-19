package com.rakuten.tech.mobile.admob

import android.app.Activity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


internal class RealAdmobDisplayerSdk : AdmobDisplayerSdk() {

    private val interstitialAdMap = HashMap<String, InterstitialAd>()

    override fun loadInterstitialAdForSdk(
        context: Activity,
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    ) {
        var adRequest = AdRequest.Builder().build()

        val adLoadCallback = object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAdMap.remove(adUnitId)
                onCallback(AdStatus.FAILED, adError?.message)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                interstitialAdMap[adUnitId] = interstitialAd
                onCallback(AdStatus.LOADED, null)
            }
        }
        InterstitialAd.load(context, adUnitId, adRequest, adLoadCallback)
    }

    override fun showInterstitialAdForSdk(
        context: Activity,
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    ) {
        if (interstitialAdMap.containsKey(adUnitId)) {
            val ad = interstitialAdMap[adUnitId]!!
            val adListener = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onCallback(AdStatus.CLOSED, null)
                    interstitialAdMap.remove(adUnitId)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    onCallback(AdStatus.FAILED, ERR_FAILED_T0_SHOW_AD)
                    interstitialAdMap.remove(adUnitId)
                }

                override fun onAdShowedFullScreenContent() {
                    onCallback(AdStatus.SHOWED, null)
                    interstitialAdMap.remove(adUnitId)
                }
            }
            if (ad != null) {
                //Show interstitial ad.
                ad.show(context)
                ad.fullScreenContentCallback = adListener
            } else {
                onCallback(AdStatus.FAILED, ERR_AD_NOT_LOADED)
            }

        } else
            onCallback(AdStatus.FAILED, ERR_AD_NOT_LOADED)
    }

    internal companion object {
        const val ERR_AD_NOT_LOADED = "Ad is not loaded yet"
        const val ERR_FAILED_T0_SHOW_AD = "Failed to show ad"
    }
}
