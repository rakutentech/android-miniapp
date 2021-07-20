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
import com.rakuten.tech.mobile.admob.AdmobDisplayerSdk
import com.rakuten.tech.mobile.admob.AdStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/** Check whether hostapp provides latest admob dependency. */
@Suppress("EmptyCatchBlock", "SwallowedException")
private inline fun <T> whenHasAdmobLatest(callback: () -> T) {
    try {
        Class.forName("com.rakuten.tech.mobile.admob.AdmobDisplayerSdk")
        callback.invoke()
    } catch (e: ClassNotFoundException) {}
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
        /** init the sdk for latest admob version 20.0.0. **/
        whenHasAdmobLatest {
            AdmobDisplayerSdk.init(context)
        }
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
    }

    /** Load the interstitial ad or version 20.0.0 when it is ready. **/
    override fun loadInterstitialAdSdk(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) = whenHasAdmobLatest {
        launch {
            AdmobDisplayerSdk.getInstance().loadInterstitialAdForSdk(adUnitId = adUnitId) { loadStatus, errorMessage ->
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
    }

    /** Show the interstitial ad when it is already loaded. **/
    override fun showInterstitialAd(
        adUnitId: String,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit
    ) {
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

    /** Show the interstitial ad for version 20.0.0 when it is already loaded. **/
    override fun showInterstitialAdSdk(
        adUnitId: String,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit
    ) = whenHasAdmobLatest {
        launch {
            AdmobDisplayerSdk.getInstance().showInterstitialAdForSdk(adUnitId = adUnitId) { loadStatus, errorMessage ->
                when (loadStatus) {
                    AdStatus.CLOSED -> onClosed.invoke()
                    AdStatus.FAILED -> onFailed.invoke(errorMessage ?: "")
                    AdStatus.LOADED -> Log.d("Ad", "Loaded")
                    AdStatus.SHOWED -> Log.d("Ad", "Showed")
                }
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
            if (rewardedAdMap.containsKey(adUnitId))
                onFailed.invoke(createLoadReqError(adUnitId))
            else {
                val ad = RewardedAd(context, adUnitId)
                rewardedAdMap[adUnitId] = ad

                val adLoadCallback = createRewardedAdLoadCallback(adUnitId, onLoaded, onFailed)

                ad.loadAd(AdRequest.Builder().build(), adLoadCallback)
            }
        }
    }

    /** Load the interstitial ad or version 20.0.0 when it is ready. **/
    override fun loadRewardedAdSdk(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) = whenHasAdmobLatest {
        launch {
            AdmobDisplayerSdk.getInstance().loadRewardedAdSdk(adUnitId = adUnitId) { loadStatus, errorMessage ->
                when (loadStatus) {
                    AdStatus.LOADED -> onLoaded.invoke()
                    AdStatus.FAILED -> onFailed.invoke(errorMessage ?: "")
                    else -> Log.d("Ad", "Something went wrong.")
                }
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
            if (rewardedAdMap.containsKey(adUnitId) && rewardedAdMap[adUnitId]!!.isLoaded) {
                val ad = rewardedAdMap[adUnitId]!!
                val adCallback = createRewardedAdShowCallback(adUnitId, onClosed, onFailed)

                ad.show(context, adCallback)
            } else
                onFailed.invoke("Ad is not loaded yet")
        }
    }

    /**
     * Show the rewarded ad when it is already loaded.
     * @param onClosed When the ad is closed, forward the reward earned by the user.
     * Reward will be null if the user did not earn the reward.
     */
    override fun showRewardedAdSdk(
        adUnitId: String,
        onClosed: (reward: Reward?) -> Unit,
        onFailed: (String) -> Unit
    ) {
        launch {
            var reward: Reward? = null
            AdmobDisplayerSdk.getInstance().showRewardedAdForSdk(
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
