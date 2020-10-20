package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

internal class KeyboardBridgeDispatcher(
    private val bridgeExecutor: MiniAppBridgeExecutor,
    private val activity: Activity
) {

    fun onGetKeyboardVisibility(callbackId: String) {
        try {
            val inputMethodManager: InputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            if (inputMethodManager.isActive && inputMethodManager.isAcceptingText)
                bridgeExecutor.postValue(callbackId, VISIBLE)
            else
                bridgeExecutor.postValue(callbackId, INVISIBLE)
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, "$ERR_KEYBOARD_VISIBILITY ${e.message}")
        }
    }

    private companion object {
        const val VISIBLE = "visible"
        const val INVISIBLE = "invisible"
        const val ERR_KEYBOARD_VISIBILITY = "Cannot execute keyboard visibility:"
    }
}
