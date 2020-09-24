package com.rakuten.tech.mobile.miniapp.ads

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.LoadAdError

internal class AdMobDisplayer(val context: Context) : MiniAppAdDisplayer {
    private val interstitialAdMap = HashMap<String, InterstitialAd>()

    override fun loadInterstitial(adUnitId: String, onLoaded: () -> Unit, onFailed: (String) -> Unit) {
        val ad = InterstitialAd(context)
        ad.adUnitId = adUnitId
        interstitialAdMap[adUnitId] = ad

        ad.adListener = object : AdListener() {
            override fun onAdLoaded() { onLoaded.invoke() }

            override fun onAdFailedToLoad(adError: LoadAdError) { onFailed.invoke(adError.message) }
        }

        ad.loadAd(AdRequest.Builder().build())
    }

    override fun showInterstitial(adUnitId: String, onClosed: () -> Unit, onFailed: (String) -> Unit) {
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
