package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.content.pm.ActivityInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.ERR_SCREEN_ACTION

internal class ScreenBridgeDispatcher(
    private val activity: Activity,
    private val bridgeExecutor: MiniAppBridgeExecutor,
    var allowScreenOrientation: Boolean
) {
    private var isLocked = false
    private val originalOrientation = activity.requestedOrientation

    @Suppress("TooGenericExceptionCaught", "LongMethod")
    fun onScreenRequest(callbackObj: CallbackObj) {
        if (allowScreenOrientation) {
            try {
                val requestAction = Gson().fromJson<Screen>(
                    callbackObj.param.toString(),
                    object : TypeToken<Screen>() {}.type
                ).action

                when (requestAction) {
                    ScreenOrientation.LOCK_LANDSCAPE.value -> {
                        activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        isLocked = true
                    }
                    ScreenOrientation.LOCK_PORTRAIT.value -> {
                        activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                        isLocked = true
                    }
                    ScreenOrientation.LOCK_RELEASE.value -> releaseLock()
                }
                bridgeExecutor.postValue(callbackObj.id, SUCCESS)
            } catch (e: Exception) {
                bridgeExecutor.postError(callbackObj.id, "$ERR_SCREEN_ACTION ${e.message}")
            }
        }
    }

    fun releaseLock() {
        if (allowScreenOrientation && isLocked) {
            activity.requestedOrientation = originalOrientation
            isLocked = false
        }
    }
}
