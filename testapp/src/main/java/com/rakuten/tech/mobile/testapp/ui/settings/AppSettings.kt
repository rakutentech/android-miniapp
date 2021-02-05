package com.rakuten.tech.mobile.testapp.ui.settings

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.AppManifestConfig
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.js.userinfo.TokenData
import java.util.Date
import java.util.UUID
import kotlin.collections.ArrayList

class AppSettings private constructor(context: Context) {

    private val manifestConfig = AppManifestConfig(context)
    private val cache = Settings(context)

    var isPreviewMode: Boolean
        get() = cache.isPreviewMode ?: manifestConfig.isPreviewMode()
        set(isPreviewMode) {
            cache.isPreviewMode = isPreviewMode
        }

    var projectId: String
        get() = cache.projectId ?: manifestConfig.rasProjectId()
        set(projectId) {
            cache.projectId = projectId
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

    var profilePictureUrlBase64: String
        get() = cache.profilePictureUrlBase64 ?: ""
        set(profilePictureUrlBase64) {
            cache.profilePictureUrlBase64 = profilePictureUrlBase64
        }

    var tokenData: TokenData
        get() = cache.tokenData ?: TokenData("test_token", Date().time)
        set(tokenData) {
            cache.tokenData = tokenData
        }

    var contactNames: ArrayList<String>
        get() = cache.contactNames ?: arrayListOf()
        set(contactNames) {
            cache.contactNames = contactNames
        }

    val isContactsSaved: Boolean
        get() = cache.isContactsSaved

    var urlParameters: String
        get() = cache.urlParameters ?: ""
        set(urlParameters) {
            cache.urlParameters = urlParameters
        }

    val baseUrl = manifestConfig.baseUrl()

    val miniAppSettings: MiniAppSdkConfig
        get() = MiniAppSdkConfig(
            baseUrl = baseUrl,
            rasProjectId = projectId,
            subscriptionKey = subscriptionKey,
            // no update for hostAppUserAgentInfo because SDK does not allow changing it at runtime
            hostAppUserAgentInfo = manifestConfig.hostAppUserAgentInfo(),
            isPreviewMode = isPreviewMode
        )

    companion object {
        lateinit var instance: AppSettings

        fun init(context: Context) {
            instance = AppSettings(context)
        }
    }
}

private class Settings(context: Context) {

    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.sample.settings",
        Context.MODE_PRIVATE
    )

    var isPreviewMode: Boolean?
        get() =
            if (prefs.contains(IS_PREVIEW_MODE))
                prefs.getBoolean(IS_PREVIEW_MODE, true)
            else
                null
        set(isPreviewMode) = prefs.edit().putBoolean(IS_PREVIEW_MODE, isPreviewMode!!).apply()

    var projectId: String?
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

    var profilePictureUrlBase64: String?
        get() = prefs.getString(PROFILE_PICTURE_URL_BASE_64, null)
        set(profilePictureUrlBase64) = prefs.edit().putString(PROFILE_PICTURE_URL_BASE_64, profilePictureUrlBase64)
            .apply()

    var tokenData: TokenData?
        get() = gson.fromJson(prefs.getString(TOKEN_DATA, null), TokenData::class.java)
        set(tokenData) = prefs.edit().putString(TOKEN_DATA, gson.toJson(tokenData))
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

    var urlParameters: String?
        get() = prefs.getString(URL_PARAMETERS, null)
        set(urlParameters) = prefs.edit().putString(URL_PARAMETERS, urlParameters).apply()

    companion object {
        private const val IS_PREVIEW_MODE = "is_preview_mode"
        private const val APP_ID = "app_id"
        private const val SUBSCRIPTION_KEY = "subscription_key"
        private const val UNIQUE_ID = "unique_id"
        private const val IS_SETTING_SAVED = "is_setting_saved"
        private const val PROFILE_NAME = "profile_name"
        private const val PROFILE_PICTURE_URL = "profile_picture_url"
        private const val PROFILE_PICTURE_URL_BASE_64 = "profile_picture_url_base_64"
        private const val CONTACT_NAMES = "contact_names"
        private const val TOKEN_DATA = "token_data"
        private const val URL_PARAMETERS = "url_parameters"
    }
}
