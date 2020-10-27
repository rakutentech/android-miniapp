package com.rakuten.tech.mobile.miniapp.js

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.ads.MiniAppAdDisplayer
import com.rakuten.tech.mobile.miniapp.ads.isAdMobProvided

@Suppress("TooGenericExceptionCaught", "LongMethod")
internal class AdBridgeDispatcher {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var adDisplayer: MiniAppAdDisplayer
    private var isAdMobEnabled = false

    fun setAdMobDisplayer(adDisplayer: MiniAppAdDisplayer) {
        this.adDisplayer = adDisplayer
        onAdMobEnableCheck()
    }

    fun setBridgeExecutor(bridgeExecutor: MiniAppBridgeExecutor) {
        this.bridgeExecutor = bridgeExecutor
        onAdMobEnableCheck()
    }

    // Check ad enable when this class have all components initialized.
    private fun onAdMobEnableCheck() {
        if (this::bridgeExecutor.isInitialized && this::adDisplayer.isInitialized)
            isAdMobEnabled = isAdMobProvided()
    }

    fun onLoadAd(callbackId: String, jsonStr: String) {
        if (isAdMobEnabled) {
            try {
                val callbackObj = Gson().fromJson(jsonStr, AdCallbackObj::class.java)
                val adObj = callbackObj.param

                when (adObj.adType) {
                    AdType.INTERSTITIAL.value -> adDisplayer.loadInterstitialAd(
                        adUnitId = adObj.adUnitId,
                        onLoaded = { bridgeExecutor.postValue(callbackId, SUCCESS) },
                        onFailed = { errMsg ->
                            bridgeExecutor.postError(
                                callbackId,
                                "${ErrorBridgeMessage.ERR_LOAD_AD} $errMsg"
                            )
                        }
                    )
                    AdType.REWARDED.value -> adDisplayer.loadRewardedAd(
                        adUnitId = adObj.adUnitId,
                        onLoaded = { bridgeExecutor.postValue(callbackId, SUCCESS) },
                        onFailed = { errMsg ->
                            bridgeExecutor.postError(
                                callbackId,
                                "${ErrorBridgeMessage.ERR_LOAD_AD} $errMsg"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                bridgeExecutor.postError(callbackId, "${ErrorBridgeMessage.ERR_LOAD_AD} ${e.message}")
            }
        } else
            bridgeExecutor.postError(callbackId,
                "${ErrorBridgeMessage.ERR_LOAD_AD} ${ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP}")
    }

    fun onShowAd(callbackId: String, jsonStr: String) {
        if (isAdMobEnabled) {
            try {
                val callbackObj = Gson().fromJson(jsonStr, AdCallbackObj::class.java)
                val adObj = callbackObj.param

                when (adObj.adType) {
                    AdType.INTERSTITIAL.value -> adDisplayer.showInterstitialAd(
                        adUnitId = adObj.adUnitId,
                        onClosed = { bridgeExecutor.postValue(callbackId, CLOSED) },
                        onFailed = { errMsg ->
                            bridgeExecutor.postError(
                                callbackId,
                                "${ErrorBridgeMessage.ERR_SHOW_AD} $errMsg"
                            )
                        }
                    )
                    AdType.REWARDED.value -> adDisplayer.showRewardedAd(
                        adUnitId = adObj.adUnitId,
                        onClosed = { reward ->
                            if (reward == null)
                                bridgeExecutor.postValue(callbackId, "null")
                            else
                                bridgeExecutor.postValue(callbackId, Gson().toJson(reward).toString())
                        },
                        onFailed = { errMsg ->
                            bridgeExecutor.postError(
                                callbackId,
                                "${ErrorBridgeMessage.ERR_SHOW_AD} $errMsg"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                bridgeExecutor.postError(callbackId, "${ErrorBridgeMessage.ERR_SHOW_AD} ${e.message}")
            }
        } else
            bridgeExecutor.postError(callbackId,
                "${ErrorBridgeMessage.ERR_SHOW_AD} ${ErrorBridgeMessage.ERR_NO_SUPPORT_HOSTAPP}")
    }
}
