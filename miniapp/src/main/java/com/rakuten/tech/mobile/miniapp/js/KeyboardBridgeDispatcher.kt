package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.ResultReceiver
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.ERR_KEYBOARD_VISIBILITY

internal class KeyboardBridgeDispatcher(
    private val bridgeExecutor: MiniAppBridgeExecutor,
    private val activity: Activity
) {

    fun onGetKeyboardVisibility(callbackId: String) {
        try {
            val immResultReceiver = IMMResultReceiver()
            val view = activity.findViewById(android.R.id.content) as ViewGroup
            val inputMethodManager: InputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view.getChildAt(0), 0, immResultReceiver)
            val result = immResultReceiver.findResult()
            if (result == InputMethodManager.RESULT_UNCHANGED_SHOWN ||
                result == InputMethodManager.RESULT_UNCHANGED_HIDDEN
            ) {
                bridgeExecutor.postValue(callbackId, VISIBLE)
            } else {
                bridgeExecutor.postValue(callbackId, INVISIBLE)
            }
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "$ERR_KEYBOARD_VISIBILITY ${e.message}")
        }
    }

    private class IMMResultReceiver : ResultReceiver(null) {
        var currentResult = -1

        override fun onReceiveResult(r: Int, data: Bundle?) {
            currentResult = r
        }

        fun findResult(): Int {
            try {
                var sleep = 0
                while (currentResult == -1 && sleep < 500) {
                    Thread.sleep(100)
                    sleep += 100
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            return currentResult
        }
    }

    private companion object {
        const val VISIBLE = "visible"
        const val INVISIBLE = "invisible"
    }
}
