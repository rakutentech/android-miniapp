package com.rakuten.tech.mobile.admob

import android.app.Activity
import androidx.annotation.VisibleForTesting
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener

import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

internal class RealAdmobDisplayerLatest(private val context: Activity) : AdmobDisplayerLatest() {

    private val interstitialAdMap = HashMap<String, InterstitialAd>()
    internal val rewardedAdMap = HashMap<String, RewardedAd>()

    @VisibleForTesting
    internal fun initAdMap(
        interstitialAdMap: Map<String, InterstitialAd> = HashMap(),
        rewardedAdMap: Map<String, RewardedAd> = HashMap()
    ) {
        this.interstitialAdMap.clear()
        this.interstitialAdMap.putAll(interstitialAdMap)

        this.rewardedAdMap.clear()
        this.rewardedAdMap.putAll(rewardedAdMap)
    }

    override fun loadInterstitialAd(
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    ) {
        if (interstitialAdMap.containsKey(adUnitId)) {
            onCallback(AdStatus.FAILED, createLoadReqError(adUnitId))
        } else {
            val adRequest = AdRequest.Builder().build()
            val adLoadCallback = object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAdMap.remove(adUnitId)
                    onCallback(AdStatus.FAILED, adError.message)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialAdMap[adUnitId] = interstitialAd
                    onCallback(AdStatus.LOADED, null)
                }
            }
            InterstitialAd.load(context, adUnitId, adRequest, adLoadCallback)
        }
    }

    override fun loadRewardedAd(
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    ) {
        if (rewardedAdMap.containsKey(adUnitId))
            onCallback(AdStatus.FAILED, createLoadReqError(adUnitId))
        else {
            val adRequest = AdRequest.Builder().build()
            val adLoadCallback = object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAdMap.remove(adUnitId)
                    onCallback(AdStatus.FAILED, adError.message)
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    rewardedAdMap[adUnitId] = rewardedAd
                    onCallback(AdStatus.LOADED, null)
                }
            }
            RewardedAd.load(context, adUnitId, adRequest, adLoadCallback)
        }
    }

    override fun showInterstitialAd(
        adUnitId: String,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    ) {
        if (interstitialAdMap.containsKey(adUnitId)) {
            val ad = interstitialAdMap[adUnitId]
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
                // Show interstitial ad.
                ad.show(context)
                ad.fullScreenContentCallback = adListener
            } else {
                onCallback(AdStatus.FAILED, ERR_AD_NOT_LOADED)
            }
        } else
            onCallback(AdStatus.FAILED, ERR_AD_NOT_LOADED)
    }

    override fun showRewardedAd(
        adUnitId: String,
        onReward: (amount: Int, type: String) -> Unit,
        onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit
    ) {
        if (rewardedAdMap.containsKey(adUnitId)) {
            val ad = rewardedAdMap[adUnitId]
            val rewardListener = OnUserEarnedRewardListener {
                val rewardAmount = it.amount
                val rewardType = it.type
                onReward(rewardAmount, rewardType)
            }
            val adListener = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onCallback(AdStatus.CLOSED, null)
                    rewardedAdMap.remove(adUnitId)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    onCallback(AdStatus.FAILED, ERR_FAILED_T0_SHOW_AD)
                    rewardedAdMap.remove(adUnitId)
                }

                override fun onAdShowedFullScreenContent() {
                    onCallback(AdStatus.SHOWED, null)
                    rewardedAdMap.remove(adUnitId)
                }
            }
            if (ad != null) {
                // Show interstitial ad.
                ad.fullScreenContentCallback = adListener
                ad.show(context, rewardListener)
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

    internal fun createLoadReqError(adUnitId: String) = "Previous $adUnitId already loaded."
}
