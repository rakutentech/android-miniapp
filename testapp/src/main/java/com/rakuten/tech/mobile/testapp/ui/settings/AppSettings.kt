package com.rakuten.tech.mobile.testapp.ui.settings

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.AppManifestConfig
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalyticsConfig
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.js.userinfo.Points
import com.rakuten.tech.mobile.miniapp.js.userinfo.TokenData
import com.rakuten.tech.mobile.miniapp.testapp.BuildConfig
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

    var isRequireSignatureVerification: Boolean
        get() = cache.isRequireSignatureVerification ?: manifestConfig.isRequireSignatureVerification()
        set(isRequireSignatureVerification) {
            cache.isRequireSignatureVerification = isRequireSignatureVerification
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

    var uniqueIdError: String
        get() = cache.uniqueIdError ?: ""
        set(uniqueIdError) {
            cache.uniqueIdError = uniqueIdError
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

    var contacts: ArrayList<Contact>
        get() = cache.contacts ?: arrayListOf()
        set(contacts) { cache.contacts = contacts }

    val isContactsSaved: Boolean
        get() = cache.isContactsSaved

    var urlParameters: String
        get() = cache.urlParameters ?: ""
        set(urlParameters) {
            cache.urlParameters = urlParameters
        }

    var miniAppAnalyticsConfigs: List<MiniAppAnalyticsConfig>
        get() = cache.miniAppAnalyticsConfigs ?: emptyList()
        set(miniAppAnalyticsConfigs) {
            cache.miniAppAnalyticsConfigs = miniAppAnalyticsConfigs
        }

    var accessTokenError: MiniAppAccessTokenError?
        get() = cache.accessTokenError
        set(accessTokenError) {
            cache.accessTokenError = accessTokenError
        }

    var points: Points?
        get() = cache.points
        set(points) {
            cache.points = points
        }

    val miniAppSettings: MiniAppSdkConfig
        get() = MiniAppSdkConfig(
            baseUrl = manifestConfig.baseUrl(),
            rasProjectId = projectId,
            subscriptionKey = subscriptionKey,
            // no update for hostAppUserAgentInfo because SDK does not allow changing it at runtime
            hostAppUserAgentInfo = manifestConfig.hostAppUserAgentInfo(),
            isPreviewMode = isPreviewMode,
            isRequireSignatureVerification = isRequireSignatureVerification,
            // temporarily taking values from buildConfig, we may add UI for this later.
            miniAppAnalyticsConfigList = listOf(
                MiniAppAnalyticsConfig(
                    BuildConfig.ADDITIONAL_ANALYTICS_ACC,
                    BuildConfig.ADDITIONAL_ANALYTICS_AID
                )
            ),
            sslPinningPublicKey = manifestConfig.sslPinningPublicKey()
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

    var isRequireSignatureVerification: Boolean?
        get() =
            if (prefs.contains(IS_REQUIRE_SIGNATURE_VERIFICATION))
                prefs.getBoolean(IS_REQUIRE_SIGNATURE_VERIFICATION, true)
            else
                null
        set(isRequire) = prefs.edit().putBoolean(IS_REQUIRE_SIGNATURE_VERIFICATION, isRequire!!).apply()

    var projectId: String?
        get() = prefs.getString(APP_ID, null)
        set(appId) = prefs.edit().putString(APP_ID, appId).apply()

    var subscriptionKey: String?
        get() = prefs.getString(SUBSCRIPTION_KEY, null)
        set(subscriptionKey) = prefs.edit().putString(SUBSCRIPTION_KEY, subscriptionKey).apply()

    var uniqueId: String?
        get() = prefs.getString(UNIQUE_ID, null)
        set(uuid) = prefs.edit().putString(UNIQUE_ID, uuid).apply()

    var uniqueIdError: String?
        get() = prefs.getString(UNIQUE_ID_ERROR, null)
        set(uniqueIdError) = prefs.edit().putString(UNIQUE_ID_ERROR, uniqueIdError).apply()

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

    var contacts: ArrayList<Contact>?
        get() = Gson().fromJson(
            prefs.getString(CONTACTS, null),
            object : TypeToken<ArrayList<Contact>>() {}.type
        )
        set(contacts) = prefs.edit().putString(CONTACTS, Gson().toJson(contacts)).apply()

    val isContactsSaved: Boolean
        get() = prefs.contains(CONTACTS)

    var urlParameters: String?
        get() = prefs.getString(URL_PARAMETERS, null)
        set(urlParameters) = prefs.edit().putString(URL_PARAMETERS, urlParameters).apply()

    var miniAppAnalyticsConfigs: List<MiniAppAnalyticsConfig>?
        get() = Gson().fromJson(
            prefs.getString(ANALYTIC_CONFIGS, null),
            object : TypeToken<List<MiniAppAnalyticsConfig>>() {}.type
        )
        set(miniAppAnalyticsConfigs) = prefs.edit().putString(ANALYTIC_CONFIGS, Gson().toJson(miniAppAnalyticsConfigs)).apply()

    var accessTokenError: MiniAppAccessTokenError?
        get() = gson.fromJson(prefs.getString(ACCESS_TOKEN_ERROR, null), MiniAppAccessTokenError::class.java)
        set(accessTokenError) = prefs.edit().putString(ACCESS_TOKEN_ERROR, gson.toJson(accessTokenError))
            .apply()

    var points: Points?
        get() = gson.fromJson(prefs.getString(POINTS, null), Points::class.java)
        set(points) = prefs.edit().putString(POINTS, gson.toJson(points))
                .apply()

    companion object {
        private const val IS_PREVIEW_MODE = "is_preview_mode"
        private const val IS_REQUIRE_SIGNATURE_VERIFICATION = "is_require_signature_verification"
        private const val APP_ID = "app_id"
        private const val SUBSCRIPTION_KEY = "subscription_key"
        private const val UNIQUE_ID = "unique_id"
        private const val UNIQUE_ID_ERROR = "unique_id_error"
        private const val IS_SETTING_SAVED = "is_setting_saved"
        private const val PROFILE_NAME = "profile_name"
        private const val PROFILE_PICTURE_URL = "profile_picture_url"
        private const val PROFILE_PICTURE_URL_BASE_64 = "profile_picture_url_base_64"
        private const val CONTACTS = "contacts"
        private const val TOKEN_DATA = "token_data"
        private const val URL_PARAMETERS = "url_parameters"
        private const val ANALYTIC_CONFIGS = "analytic_configs"
        private const val ACCESS_TOKEN_ERROR = "access_token_error"
        private const val POINTS = "points"
    }
}
