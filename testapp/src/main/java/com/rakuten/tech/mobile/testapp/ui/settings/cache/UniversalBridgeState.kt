package com.rakuten.tech.mobile.testapp.ui.settings.cache

import android.content.SharedPreferences
import com.google.gson.Gson

class UniversalBridgeState(
    var shouldSendMessage: Boolean = false,
    var message: String = ""
) {
    companion object {
        internal fun fromJson(prefs: SharedPreferences): UniversalBridgeState {
            val universalBridgeStateData = prefs.getString(Cache.UNIVERSAL_BRIDGE_MESSAGE, null)
                ?: return UniversalBridgeState()
            return Gson().fromJson(universalBridgeStateData, UniversalBridgeState::class.java)
        }
    }

    internal fun toJsonString() = Gson().toJson(this)

    fun handleOnMiniAppLoaded(onMessageReady: (String) -> Unit) {
        if (shouldSendMessage) {
            onMessageReady(message)
        }
    }
}
