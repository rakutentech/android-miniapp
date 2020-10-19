package com.rakuten.tech.mobile.miniapp.js

import android.graphics.Rect
import android.webkit.WebView

internal class KeyboardBridgeDispatcher(
    private val bridgeExecutor: MiniAppBridgeExecutor
) {

    fun onGetKeyboardVisibility(callbackId: String, webView: WebView) {
        try {
            webView.viewTreeObserver
                .addOnGlobalLayoutListener {
                    val r = Rect()
                    webView.getWindowVisibleDisplayFrame(r)
                    val screenHeight: Int = webView.rootView.height
                    val keypadHeight: Int = screenHeight - r.bottom
                    if (keypadHeight > screenHeight * 0.15) {
                        bridgeExecutor.postValue(callbackId, "visible")
                    } else {
                        bridgeExecutor.postValue(callbackId, "invisible")
                    }
                }
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "$ERR_KEYBOARD_VISIBILITY ${e.message}")
        }
    }

    private companion object {
        const val ERR_KEYBOARD_VISIBILITY = "Cannot execute keyboard visibility:"
    }
}
