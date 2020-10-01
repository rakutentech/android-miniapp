package com.rakuten.tech.mobile.miniapp.ads

import android.app.Activity
import androidx.annotation.VisibleForTesting
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * The ad displayer.
 * @param context should use the same activity context for #MiniAppDisplay.getMiniAppView.
 * Support Interstitial, Reward ads.
 */
class AdMobDisplayer(private val context: Activity) : MiniAppAdDisplayer, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    @VisibleForTesting
    internal val interstitialAdMap = HashMap<String, InterstitialAd>()
    @VisibleForTesting
    internal val rewardedAdMap = HashMap<String, RewardedAd>()

    /** Load the interstitial ad when it is ready. **/
    override fun loadInterstitial(adUnitId: String, onLoaded: () -> Unit, onFailed: (String) -> Unit) {
        launch {
            if (interstitialAdMap.containsKey(adUnitId))
                onFailed.invoke(createLoadReqError(adUnitId))
            else {
                val ad = InterstitialAd(context)
                ad.adUnitId = adUnitId
                interstitialAdMap[adUnitId] = ad

                ad.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        onLoaded.invoke()
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        onFailed.invoke(adError.message)
                    }
                }

                ad.loadAd(AdRequest.Builder().build())
            }
        }
    }

    /** Show the interstitial ad when it is already loaded. **/
    override fun showInterstitial(adUnitId: String, onClosed: () -> Unit, onFailed: (String) -> Unit) {
        launch {
            if (interstitialAdMap.containsKey(adUnitId) && interstitialAdMap[adUnitId]!!.isLoaded) {
                val ad = interstitialAdMap[adUnitId]!!
                ad.adListener = object : AdListener() {

                    override fun onAdClosed() {
                        onClosed.invoke()
                        interstitialAdMap.remove(adUnitId)
                    }
                }

                ad.show()
            } else
                onFailed.invoke("Ad is not loaded yet")
        }
    }

    /** Load the rewarded ad when it is ready. **/
    override fun loadRewarded(adUnitId: String, onLoaded: () -> Unit, onFailed: (String) -> Unit) {
        launch {
            if (rewardedAdMap.containsKey(adUnitId))
                onFailed.invoke(createLoadReqError(adUnitId))
            else {
                val ad = RewardedAd(context, adUnitId)
                rewardedAdMap[adUnitId] = ad

                val adLoadCallback = object : RewardedAdLoadCallback() {
                    override fun onRewardedAdLoaded() {
                        onLoaded.invoke()
                    }

                    override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                        onFailed.invoke(adError.message)
                    }
                }

                ad.loadAd(AdRequest.Builder().build(), adLoadCallback)
            }
        }
    }

    /**
     * Show the rewarded ad when it is already loaded.
     * @param onClosed When the ad is closed, forward the reward earned by the user.
     * Reward will be null if the user did not earn the reward.
     */
    override fun showRewarded(adUnitId: String, onClosed: (reward: Reward?) -> Unit, onFailed: (String) -> Unit) {
        launch {
            if (rewardedAdMap.containsKey(adUnitId) && rewardedAdMap[adUnitId]!!.isLoaded) {
                val ad = rewardedAdMap[adUnitId]!!
                val adCallback = object : RewardedAdCallback() {
                    var reward: Reward? = null

                    override fun onRewardedAdClosed() {
                        onClosed.invoke(reward)
                        rewardedAdMap.remove(adUnitId)
                    }

                    override fun onUserEarnedReward(rewardItem: RewardItem) {
                        reward = Reward(type = rewardItem.type, amount = rewardItem.amount)
                    }

                    override fun onRewardedAdFailedToShow(adError: AdError) {
                        onFailed.invoke(adError.message)
                    }
                }

                ad.show(context, adCallback)
            } else
                onFailed.invoke("Ad is not loaded yet")
        }
    }

    private fun createLoadReqError(adUnitId: String) = "Previous $adUnitId is still in progress"
}
