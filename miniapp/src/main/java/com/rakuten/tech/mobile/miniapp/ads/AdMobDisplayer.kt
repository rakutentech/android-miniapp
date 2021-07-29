package com.rakuten.tech.mobile.miniapp.ads

import android.app.Activity
import android.util.Log
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
import com.rakuten.tech.mobile.admob.AdmobDisplayerLatest
import com.rakuten.tech.mobile.admob.AdStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/** Check whether hostapp provides latest admob dependency. */
@Suppress("EmptyCatchBlock", "SwallowedException")
private inline fun <T> whenHasAdmobLatest(callback: (success: Boolean) -> T) {
    try {
        Class.forName("com.rakuten.tech.mobile.admob.AdmobDisplayerLatest")
        callback.invoke(true)
    } catch (e: ClassNotFoundException) {
        callback.invoke(false)
    }
}

/**
 * The ad displayer.
 * @param context should use the same activity context for #MiniAppDisplay.getMiniAppView.
 * Support Interstitial, Reward ads.
 */
class AdMobDisplayer(private val context: Activity) : MiniAppAdDisplayer, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    private val interstitialAdMap = HashMap<String, InterstitialAd>()

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
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

    init {
        /** init the sdk for latest admob version 20.2.0. **/
        whenHasAdmobLatest { available ->
            if (available) AdmobDisplayerLatest.init(context)
        }
    }

    /** Load the interstitial ad when it is ready. **/
    override fun loadInterstitialAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        launch {
            whenHasAdmobLatest { available ->
                if (available)
                    loadLatestInterstitialAd(adUnitId, onLoaded, onFailed)
                else
                    loadOldInterstitialAd(adUnitId, onLoaded, onFailed)
            }
        }
    }

    private fun loadOldInterstitialAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
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
                    interstitialAdMap.remove(adUnitId)
                    onFailed.invoke(adError.message)
                }
            }

            ad.loadAd(AdRequest.Builder().build())
        }
    }

    /** Load the interstitial ad or version 20.2.0 when it is ready. **/
    private fun loadLatestInterstitialAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        AdmobDisplayerLatest.getInstance()
            .loadInterstitialAd(adUnitId = adUnitId) { loadStatus, errorMessage ->
                when (loadStatus) {
                    AdStatus.LOADED -> onLoaded.invoke()
                    AdStatus.FAILED -> {
                        interstitialAdMap.remove(adUnitId)
                        onFailed.invoke(errorMessage ?: "")
                    }
                    else -> Log.d("Ad", "Something went wrong.")
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
            whenHasAdmobLatest { available ->
                if (available)
                    showLatestInterstitialAd(adUnitId, onClosed, onFailed)
                else
                    showOldInterstitialAd(adUnitId, onClosed, onFailed)
            }
        }
    }

    private fun showOldInterstitialAd(
        adUnitId: String,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit
    ) {
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

    /** Show the interstitial ad for version 20.2.0 when it is already loaded. **/
    private fun showLatestInterstitialAd(
        adUnitId: String,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        AdmobDisplayerLatest.getInstance()
            .showInterstitialAd(adUnitId = adUnitId) { loadStatus, errorMessage ->
                when (loadStatus) {
                    AdStatus.CLOSED -> onClosed.invoke()
                    AdStatus.FAILED -> onFailed.invoke(errorMessage ?: "")
                    AdStatus.LOADED -> {}
                    AdStatus.SHOWED -> {}
                }
            }
    }

    /** Load the rewarded ad when it is ready. **/
    override fun loadRewardedAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        launch {
            whenHasAdmobLatest { available ->
                if (available)
                    loadLatestRewardedAd(adUnitId, onLoaded, onFailed)
                else
                    loadOldRewardedAd(adUnitId, onLoaded, onFailed)
            }
        }
    }

    private fun loadOldRewardedAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        if (rewardedAdMap.containsKey(adUnitId))
            onFailed.invoke(createLoadReqError(adUnitId))
        else {
            val ad = RewardedAd(context, adUnitId)
            rewardedAdMap[adUnitId] = ad

            val adLoadCallback = createRewardedAdLoadCallback(adUnitId, onLoaded, onFailed)

            ad.loadAd(AdRequest.Builder().build(), adLoadCallback)
        }
    }

    /** Load the interstitial ad or version 20.2.0 when it is ready. **/
    private fun loadLatestRewardedAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        AdmobDisplayerLatest.getInstance()
            .loadRewardedAd(adUnitId = adUnitId) { loadStatus, errorMessage ->
                when (loadStatus) {
                    AdStatus.LOADED -> onLoaded.invoke()
                    AdStatus.FAILED -> onFailed.invoke(errorMessage ?: "")
                    else -> Log.d("Ad", "Something went wrong.")
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
            whenHasAdmobLatest { available ->
                if (available)
                    showLatestRewardedAd(adUnitId, onClosed, onFailed)
                else
                    showOldRewardedAd(adUnitId, onClosed, onFailed)
            }
        }
    }

    private fun showOldRewardedAd(
        adUnitId: String,
        onClosed: (reward: Reward?) -> Unit,
        onFailed: (String) -> Unit
    ) {
        if (rewardedAdMap.containsKey(adUnitId) && rewardedAdMap[adUnitId]!!.isLoaded) {
            val ad = rewardedAdMap[adUnitId]!!
            val adCallback = createRewardedAdShowCallback(adUnitId, onClosed, onFailed)

            ad.show(context, adCallback)
        } else
            onFailed.invoke("Ad is not loaded yet")
    }

    /**
     * Show the rewarded ad when it is already loaded.
     * @param onClosed When the ad is closed, forward the reward earned by the user.
     * Reward will be null if the user did not earn the reward.
     */
    private fun showLatestRewardedAd(
        adUnitId: String,
        onClosed: (reward: Reward?) -> Unit,
        onFailed: (String) -> Unit
    ) {
        var reward: Reward? = null
        AdmobDisplayerLatest.getInstance().showRewardedAd(
            adUnitId = adUnitId,
            onReward = { amount: Int, type: String ->
                reward = Reward(type = type, amount = amount)
            }
        ) { loadStatus, errorMessage ->
            when (loadStatus) {
                AdStatus.FAILED -> {
                    interstitialAdMap.remove(adUnitId)
                    onFailed.invoke(errorMessage ?: "")
                }
                AdStatus.CLOSED -> onClosed.invoke(reward)
                else -> Log.d("Ad", "Something went wrong.")
            }
        }
    }

    @VisibleForTesting
    internal fun createRewardedAdLoadCallback(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ): RewardedAdLoadCallback = object : RewardedAdLoadCallback() {

        override fun onRewardedAdLoaded() {
            onLoaded.invoke()
        }

        override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
            rewardedAdMap.remove(adUnitId)
            onFailed.invoke(adError.message)
        }
    }

    @VisibleForTesting
    internal fun createRewardedAdShowCallback(
        adUnitId: String,
        onClosed: (reward: Reward?) -> Unit,
        onFailed: (String) -> Unit
    ): RewardedAdCallback = object : RewardedAdCallback() {
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

    private fun createLoadReqError(adUnitId: String) = "Previous $adUnitId is still in progress"
}
