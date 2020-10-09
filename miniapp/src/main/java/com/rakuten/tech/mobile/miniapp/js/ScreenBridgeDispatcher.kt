package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.content.pm.ActivityInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
                ScreenAction.LOCK_LANDSCAPE.action ->
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                ScreenAction.LOCK_PORTRAIT.action ->
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                ScreenAction.LOCK_RELEASE.action ->
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
            }
            bridgeExecutor.postValue(callbackObj.id, SUCCESS)
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackObj.id, "$ERR_SCREEN_ACTION ${e.message}")
        }
    }

    private companion object {
        const val ERR_SCREEN_ACTION = "Cannot request screen action:"
    }
}
