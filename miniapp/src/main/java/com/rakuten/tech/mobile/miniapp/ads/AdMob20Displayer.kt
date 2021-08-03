package com.rakuten.tech.mobile.miniapp.ads

import android.app.Activity
import android.util.Log
import com.rakuten.tech.mobile.admob.AdmobDisplayerLatest
import com.rakuten.tech.mobile.admob.AdStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/** Check whether hostapp provides latest admob dependency. */
@Suppress("EmptyCatchBlock", "SwallowedException")
internal inline fun <T> whenHasAdmobLatest(callback: (isAvailable: Boolean) -> T) {
    try {
        Class.forName("com.rakuten.tech.mobile.admob.AdmobDisplayerLatest")
        callback.invoke(true)
    } catch (e: ClassNotFoundException) {
        callback.invoke(false)
    }
}

/**
 * The ad displayer for latest admob sdk 20.2.0.
 * @param context should use the same activity context for #MiniAppDisplay.getMiniAppView.
 * Support Interstitial, Reward ads.
 */
class AdMob20Displayer(private val context: Activity) : MiniAppAdDisplayer, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    init {
        /** init the sdk for latest admob version 20.2.0. **/
        whenHasAdmobLatest { isAvailable ->
            if (isAvailable)
                AdmobDisplayerLatest.init(context)
        }
    }

    override fun loadInterstitialAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) = whenHasAdmobLatest { isAvailable ->
        if (isAvailable) {
            launch {
                AdmobDisplayerLatest.getInstance()
                    .loadInterstitialAd(adUnitId = adUnitId) { loadStatus, errorMessage ->
                        when (loadStatus) {
                            AdStatus.LOADED -> onLoaded.invoke()
                            AdStatus.FAILED -> onFailed.invoke(errorMessage ?: "")
                            else -> Log.d("Ad", "Something went wrong.")
                        }
                    }
            }
        }
    }

    override fun showInterstitialAd(
        adUnitId: String,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit
    ) = whenHasAdmobLatest { isAvailable ->
        if (isAvailable) {
            launch {
                AdmobDisplayerLatest.getInstance()
                    .showInterstitialAd(adUnitId = adUnitId) { loadStatus, errorMessage ->
                        when (loadStatus) {
                            AdStatus.CLOSED -> onClosed.invoke()
                            AdStatus.FAILED -> onFailed.invoke(errorMessage ?: "")
                            AdStatus.LOADED -> {
                            }
                            AdStatus.SHOWED -> {
                            }
                        }
                    }
            }
        }
    }

    override fun loadRewardedAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) = whenHasAdmobLatest { isAvailable ->
        if (isAvailable) {
            launch {
                AdmobDisplayerLatest.getInstance()
                    .loadRewardedAd(adUnitId = adUnitId) { loadStatus, errorMessage ->
                        when (loadStatus) {
                            AdStatus.LOADED -> onLoaded.invoke()
                            AdStatus.FAILED -> onFailed.invoke(errorMessage ?: "")
                            else -> Log.d("Ad", "Something went wrong.")
                        }
                    }
            }
        }
    }

    override fun showRewardedAd(
        adUnitId: String,
        onClosed: (reward: Reward?) -> Unit,
        onFailed: (String) -> Unit
    ) = whenHasAdmobLatest { isAvailable ->
        if (isAvailable) {
            launch {
                var reward: Reward? = null
                AdmobDisplayerLatest.getInstance().showRewardedAd(
                    adUnitId = adUnitId,
                    onReward =
                    { amount: Int, type: String ->
                        reward = Reward(type = type, amount = amount)
                    }
                ) { loadStatus, errorMessage ->
                    when (loadStatus) {
                        AdStatus.FAILED -> onFailed.invoke(errorMessage ?: "")
                        AdStatus.CLOSED -> onClosed.invoke(reward)
                        else -> Log.d("Ad", "Something went wrong.")
                    }
                }
            }
        }
    }
}
