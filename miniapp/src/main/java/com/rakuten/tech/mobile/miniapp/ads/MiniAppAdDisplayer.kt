package com.rakuten.tech.mobile.miniapp.ads

interface MiniAppAdDisplayer {
    fun loadInterstitial(onLoaded: () -> Unit, onFailed: (String) -> Unit)
    fun showInterstitial(onClosed: () -> Unit, onFailed: (String) -> Unit)
}
