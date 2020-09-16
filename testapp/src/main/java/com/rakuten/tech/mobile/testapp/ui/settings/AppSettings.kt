package com.rakuten.tech.mobile.testapp.ui.settings

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.AppManifestConfig
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import java.util.UUID

class AppSettings private constructor(context: Context) {

    private val manifestConfig = AppManifestConfig(context)
    private val cache = Settings(context)

    var isTestMode: Boolean
        get() = cache.isTestMode ?: manifestConfig.isTestMode()
        set(isTestMode) {
            cache.isTestMode = isTestMode
        }

    var appId: String
        get() = cache.appId ?: manifestConfig.rasAppId()
        set(appId) {
            cache.appId = appId
        }

    var subscriptionKey: String
        get() = cache.subscriptionKey ?: manifestConfig.subscriptionKey()
        set(subscriptionKey) {
            cache.subscriptionKey = subscriptionKey
        }

    var uniqueId: String
        get() {
            val uniqueId = cache.uniqueId ?: UUID.randomUUID().toString()
            cache.uniqueId = uniqueId
            return uniqueId
        }
        set(subscriptionKey) {
            cache.subscriptionKey = subscriptionKey
        }

    var isSettingSaved: Boolean
        get() = cache.isSettingSaved
        set(isSettingSaved) {
            cache.isSettingSaved = isSettingSaved
        }

    var profileName: String
        get() = cache.profileName ?: ""
        set(profileName) {
            cache.profileName = profileName
        }

    var profilePictureUrl: String
        get() = cache.profilePictureUrl ?: ""
        set(profilePictureUrl) {
            cache.profilePictureUrl = profilePictureUrl
        }

    var contactNames: ArrayList<String>
        get() = cache.contactNames ?: arrayListOf()
        set(contactNames) {
            cache.contactNames = contactNames
        }

    val isContactsSaved: Boolean
        get() = cache.isContactsSaved

    val baseUrl = manifestConfig.baseUrl()

    val hostAppVersionId = manifestConfig.hostAppVersion()

    val miniAppSettings: MiniAppSdkConfig
        get() = MiniAppSdkConfig(
            baseUrl = baseUrl,
            rasAppId = appId,
            subscriptionKey = subscriptionKey,
            hostAppVersionId = hostAppVersionId,
            // no update for hostAppUserAgentInfo because SDK does not allow changing it at runtime
            hostAppUserAgentInfo = manifestConfig.hostAppUserAgentInfo(),
            isTestMode = isTestMode
        )

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

    var isTestMode: Boolean?
        get() =
            if (prefs.contains(IS_TEST_MODE))
                prefs.getBoolean(IS_TEST_MODE, false)
            else
                null
        set(isTestMode) = prefs.edit().putBoolean(IS_TEST_MODE, isTestMode!!).apply()

    var appId: String?
        get() = prefs.getString(APP_ID, null)
        set(appId) = prefs.edit().putString(APP_ID, appId).apply()

    var subscriptionKey: String?
        get() = prefs.getString(SUBSCRIPTION_KEY, null)
        set(subscriptionKey) = prefs.edit().putString(SUBSCRIPTION_KEY, subscriptionKey).apply()

    var uniqueId: String?
        get() = prefs.getString(UNIQUE_ID, null)
        set(uuid) = prefs.edit().putString(UNIQUE_ID, uuid).apply()

    var isSettingSaved: Boolean
        get() = prefs.getBoolean(IS_SETTING_SAVED, false)
        set(isSettingSaved) = prefs.edit().putBoolean(IS_SETTING_SAVED, isSettingSaved).apply()

    var profileName: String?
        get() = prefs.getString(PROFILE_NAME, null)
        set(profileName) = prefs.edit().putString(PROFILE_NAME, profileName).apply()

    var profilePictureUrl: String?
        get() = prefs.getString(PROFILE_PICTURE_URL, null)
        set(profilePictureUrl) = prefs.edit().putString(PROFILE_PICTURE_URL, profilePictureUrl)
            .apply()

    var contactNames: ArrayList<String>?
        get() = Gson().fromJson(
            prefs.getString(CONTACT_NAMES, null),
            object : TypeToken<ArrayList<String>>() {}.type
        )
        set(contactNames) =
            prefs.edit().putString(CONTACT_NAMES, Gson().toJson(contactNames)).apply()

    val isContactsSaved: Boolean
        get() = prefs.contains(CONTACT_NAMES)

    companion object {
        private const val IS_TEST_MODE = "is_test_mode"
        private const val APP_ID = "app_id"
        private const val SUBSCRIPTION_KEY = "subscription_key"
        private const val UNIQUE_ID = "unique_id"
        private const val IS_SETTING_SAVED = "is_setting_saved"
        private const val PROFILE_NAME = "profile_name"
        private const val PROFILE_PICTURE_URL = "profile_picture_url"
        private const val CONTACT_NAMES = "contact_names"
    }
}
