package com.rakuten.tech.mobile.miniapp.ads

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * The ad displayer for admob sdk version 20.2.0.
 * @param context should use the same activity context for #MiniAppDisplay.getMiniAppView.
 * Support Interstitial, Reward ads.
 */
class AdMobDisplayer(private val context: Activity) : MiniAppAdDisplayer, CoroutineScope {

    @VisibleForTesting
    internal val loadInterstitialAd: (
        context: Activity,
        adUnitId: String,
        adRequest: AdRequest,
        adLoadCallback: InterstitialAdLoadCallback
    ) -> Unit = InterstitialAd::load

    @VisibleForTesting
    internal val showInterstitialAd: (
        context: Activity,
        ad: InterstitialAd
    ) -> Unit = { context, ad ->
        ad.show(context)
    }

    @VisibleForTesting
    internal val loadRewardedAd: (
        context: Activity,
        adUnitId: String,
        adRequest: AdRequest,
        RewardedAdLoadCallback
    ) -> Unit = RewardedAd::load

    @VisibleForTesting
    internal val showRewardedAd: (
        context: Activity,
        ad: RewardedAd,
        rewardListener: OnUserEarnedRewardListener
    ) -> Unit = { context, ad, rewardListener ->
        ad.show(context, rewardListener)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val interstitialAdMap = HashMap<String, InterstitialAd>()
    internal val rewardedAdMap = HashMap<String, RewardedAd>()
    private var reward: Reward? = null

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

    /** Load the interstitial ad when it is ready. **/
    override fun loadInterstitialAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        launch {
            if (interstitialAdMap.containsKey(adUnitId))
                onFailed.invoke(createLoadReqError(adUnitId))
            else {
                val adRequest = AdRequest.Builder().build()
                val adLoadCallback = object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        interstitialAdMap.remove(adUnitId)
                        onFailed.invoke(adError.message)
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        interstitialAdMap[adUnitId] = interstitialAd
                        onLoaded.invoke()
                    }
                }
                loadInterstitialAd(context, adUnitId, adRequest, adLoadCallback)
            }
        }
    }

    /** Show the interstitial ad when it is already loaded. **/
    override fun showInterstitialAd(
        adUnitId: String,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        launch {
            if (interstitialAdMap.containsKey(adUnitId)) {
                val ad = interstitialAdMap[adUnitId]
                val adListener = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        onClosed.invoke()
                        interstitialAdMap.remove(adUnitId)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                        onFailed.invoke(ERR_FAILED_T0_SHOW_AD)
                        interstitialAdMap.remove(adUnitId)
                    }

                    override fun onAdShowedFullScreenContent() {
                        interstitialAdMap.remove(adUnitId)
                    }
                }
                if (ad != null) {
                    showInterstitialAd(context, ad)
                    ad.fullScreenContentCallback = adListener
                } else {
                    onFailed.invoke(ERR_AD_NOT_LOADED)
                }
            } else
                onFailed.invoke(ERR_AD_NOT_LOADED)
        }
    }

    /** Load the rewarded ad when it is ready. **/
    override fun loadRewardedAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        launch {
            if (rewardedAdMap.containsKey(adUnitId))
                onFailed.invoke(createLoadReqError(adUnitId))
            else {
                val adRequest = AdRequest.Builder().build()
                val adLoadCallback = object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        rewardedAdMap.remove(adUnitId)
                        onFailed.invoke(adError.message)
                    }

                    override fun onAdLoaded(rewardedAd: RewardedAd) {
                        rewardedAdMap[adUnitId] = rewardedAd
                        onLoaded.invoke()
                    }
                }
                loadRewardedAd(context, adUnitId, adRequest, adLoadCallback)
            }
        }
    }

    /**
     * Show the rewarded ad when it is already loaded.
     * @param onClosed When the ad is closed, forward the reward earned by the user.
     * Reward will be null if the user did not earn the reward.
     */
    override fun showRewardedAd(
        adUnitId: String,
        onClosed: (reward: Reward?) -> Unit,
        onFailed: (String) -> Unit
    ) {
        launch {
            if (rewardedAdMap.containsKey(adUnitId)) {
                val ad = rewardedAdMap[adUnitId]
                val rewardListener = createRewardedAdShowCallback()

                val adListener = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        onClosed.invoke(reward)
                        rewardedAdMap.remove(adUnitId)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                        onFailed.invoke(ERR_FAILED_T0_SHOW_AD)
                        rewardedAdMap.remove(adUnitId)
                    }

                    override fun onAdShowedFullScreenContent() {
                        rewardedAdMap.remove(adUnitId)
                    }
                }
                if (ad != null) {
                    showRewardedAd(context, ad, rewardListener)
                    ad.fullScreenContentCallback = adListener
                } else {
                    onFailed.invoke(ERR_AD_NOT_LOADED)
                }
            } else
                onFailed.invoke(ERR_AD_NOT_LOADED)
        }
    }

    @VisibleForTesting
    internal fun createRewardedAdShowCallback(): OnUserEarnedRewardListener =
        OnUserEarnedRewardListener { rewardItem ->
            reward = Reward(type = rewardItem.type, amount = rewardItem.amount)
        }

    private fun createLoadReqError(adUnitId: String) = "Previous $adUnitId already loaded."

    internal companion object {
        const val ERR_AD_NOT_LOADED = "Ad is not loaded yet"
        const val ERR_FAILED_T0_SHOW_AD = "Failed to show ad"
    }
}
