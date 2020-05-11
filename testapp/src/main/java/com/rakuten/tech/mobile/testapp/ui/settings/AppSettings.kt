package com.rakuten.tech.mobile.testapp.ui.settings

import android.content.Context
import android.content.SharedPreferences
import com.rakuten.tech.mobile.miniapp.AppManifestConfig
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import java.util.*

class AppSettings private constructor(context: Context) {

    private val manifestConfig = AppManifestConfig(context)
    private val cache = Settings(context)

    var appId: String
        get() = cache.appId ?: manifestConfig.rasAppId()
        set(appId) { cache.appId = appId }

    var subscriptionKey: String
        get() = cache.subscriptionKey ?: manifestConfig.subscriptionKey()
        set(subscriptionKey) { cache.subscriptionKey = subscriptionKey }

    var uniqueId: String
        get() {
            val uniqueId = cache.uniqueId ?: UUID.randomUUID().toString()
            cache.uniqueId = uniqueId
            return uniqueId
        }
        set(subscriptionKey) { cache.subscriptionKey = subscriptionKey }

    val baseUrl = manifestConfig.baseUrl()

    val hostAppVersionId = manifestConfig.hostAppVersion()

    val miniAppSettings: MiniAppSdkConfig get() = MiniAppSdkConfig(
        baseUrl = baseUrl,
        rasAppId = appId,
        subscriptionKey = subscriptionKey,
        hostAppVersionId = hostAppVersionId
    )

    fun isSettingSaved() = cache.appId != null

    companion object {
        lateinit var instance: AppSettings

        fun init(context: Context) {
            instance = AppSettings(context)
        }
    }
}

private class Settings(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.sample.settings",
        Context.MODE_PRIVATE
    )

    var appId: String?
        get() = prefs.getString(APP_ID, null)
        set(appId) = prefs.edit().putString(APP_ID, appId).apply()

    var subscriptionKey: String?
        get() = prefs.getString(SUBSCRIPTION_KEY, null)
        set(subscriptionKey) = prefs.edit().putString(SUBSCRIPTION_KEY, subscriptionKey).apply()

    var uniqueId: String?
        get() = prefs.getString(UNIQUE_ID, null)
        set(uuid) = prefs.edit().putString(UNIQUE_ID, uuid).apply()

    companion object {
        private const val APP_ID = "app_id"
        private const val SUBSCRIPTION_KEY = "subscription_key"
        private const val UNIQUE_ID = "unique_id"
    }
}
