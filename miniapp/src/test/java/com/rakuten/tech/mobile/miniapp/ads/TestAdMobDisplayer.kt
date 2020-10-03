package com.rakuten.tech.mobile.miniapp.ads

import com.rakuten.tech.mobile.miniapp.TEST_ERROR_MSG

@Suppress("TooGenericExceptionThrown")
internal class TestAdMobDisplayer : MiniAppAdDisplayer {
    override fun loadInterstitial(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        onLoaded.invoke()
        onFailed.invoke(TEST_ERROR_MSG)
        throw Exception()
    }

    override fun showInterstitial(
        adUnitId: String,
        onClosed: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        onClosed.invoke()
        onFailed.invoke(TEST_ERROR_MSG)
        throw Exception()
    }

    override fun loadRewarded(adUnitId: String, onLoaded: () -> Unit, onFailed: (String) -> Unit) {
        onLoaded.invoke()
        onFailed.invoke(TEST_ERROR_MSG)
        throw Exception()
    }

    override fun showRewarded(
        adUnitId: String,
        onClosed: (reward: Reward?) -> Unit,
        onFailed: (String) -> Unit
    ) {
        onClosed.invoke(null)
        onClosed.invoke(Reward("", 1))
        onFailed.invoke(TEST_ERROR_MSG)
        throw Exception()
    }
}
