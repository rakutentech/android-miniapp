package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.content.pm.ActivityInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.ERR_SCREEN_ACTION

@Suppress("TooGenericExceptionCaught")
internal class ScreenBridgeDispatcher(
    private val activity: Activity,
    private val bridgeExecutor: MiniAppBridgeExecutor
) {

    fun onScreenRequest(callbackObj: CallbackObj) {
        try {
            val requestAction = Gson().fromJson<Screen>(
                callbackObj.param.toString(),
                object : TypeToken<Screen>() {}.type
            ).action

            when (requestAction) {
                ScreenOrientation.LOCK_LANDSCAPE.value ->
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                ScreenOrientation.LOCK_PORTRAIT.value ->
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                ScreenOrientation.LOCK_RELEASE.value ->
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
            }
            bridgeExecutor.postValue(callbackObj.id, SUCCESS)
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackObj.id, "$ERR_SCREEN_ACTION ${e.message}")
        }
    }
}
