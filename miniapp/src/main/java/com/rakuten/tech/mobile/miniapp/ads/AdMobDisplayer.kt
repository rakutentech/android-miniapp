package com.rakuten.tech.mobile.miniapp.ads

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class AdMobDisplayer(private val context: Context) : MiniAppAdDisplayer, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    private val interstitialAdMap = HashMap<String, InterstitialAd>()

    @VisibleForTesting
    internal fun isAdReady(adUnitId: String) =
        interstitialAdMap.containsKey(adUnitId) && interstitialAdMap[adUnitId]!!.isLoaded

    override fun loadInterstitial(adUnitId: String, onLoaded: () -> Unit, onFailed: (String) -> Unit) {
        launch {
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

    override fun showInterstitial(adUnitId: String, onClosed: () -> Unit, onFailed: (String) -> Unit) {
        launch {
            if (isAdReady(adUnitId)) {
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
}
