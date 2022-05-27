package com.rakuten.tech.mobile.miniapp.js

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception

internal class CloseAlertInfoCache constructor(
    private val prefs: SharedPreferences
) {

    constructor(context: Context) : this(
        prefs = context.getSharedPreferences(
            "com.rakuten.tech.mobile.miniapp.close.alert.cache", Context.MODE_PRIVATE
        )
    )

    fun get(miniAppId: String): MiniAppCloseAlertInfo? {
        return if (prefs.contains(miniAppId)) {
            try {
                val persisted: PersistedCloseAlertInfo = Gson().fromJson(
                    prefs.getString(miniAppId, ""),
                    object : TypeToken<PersistedCloseAlertInfo>() {}.type
                )
                persisted.closeAlertInfo
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun store(alertInfo: MiniAppCloseAlertInfo, callBackId: String, miniAppId: String) {
        val persistedAlertInfo = PersistedCloseAlertInfo(alertInfo, callBackId)
        val jsonToStore: String = Gson().toJson(persistedAlertInfo)
        prefs.edit().putString(miniAppId, jsonToStore).apply()
    }

    fun remove(miniAppId: String) = prefs.edit().remove(miniAppId).apply()
}

@Keep
private data class PersistedCloseAlertInfo(
    var closeAlertInfo: MiniAppCloseAlertInfo,
    var callBackId: String
)
