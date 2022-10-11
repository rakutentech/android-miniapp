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

    var requireSignatureVerification: Boolean
        get() = cache.requireSignatureVerification ?: manifestConfig.requireSignatureVerification()
        set(isRequired) {
            cache.requireSignatureVerification = isRequired
        }

    var isProdVersionEnabled: Boolean
        get() = cache.isProdVersionEnabled ?: false
        set(isRequired) {
            cache.isProdVersionEnabled = isRequired
        }

    var baseUrl: String
        get() = cache.baseUrl ?: manifestConfig.baseUrl()
        set(baseUrl) {
            cache.baseUrl = baseUrl
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

    var projectId2: String
        get() = cache.projectId2 ?: manifestConfig.rasProjectId()
        set(projectId2) {
            cache.projectId2 = projectId2
        }

    var subscriptionKey2: String
        get() = cache.subscriptionKey2 ?: manifestConfig.subscriptionKey()
        set(subscriptionKey2) {
            cache.subscriptionKey2 = subscriptionKey2
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

    var messagingUniqueIdError: String
        get() = cache.messagingUniqueIdError ?: ""
        set(messagingUniqueIdError) {
            cache.messagingUniqueIdError = messagingUniqueIdError
        }

    var mauIdError: String
        get() = cache.mauIdError ?: ""
        set(mauIdError) {
            cache.mauIdError = mauIdError
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

    var dynamicDeeplinks: ArrayList<String>
        get() = cache.dynamicDeeplinks ?: arrayListOf()
        set(deeplinks) { cache.dynamicDeeplinks = deeplinks }

    val isDynamicDeeplinksSaved: Boolean
        get() = cache.isDynamicDeeplinksSaved

    var maxStorageSizeLimitInBytes: String
        get() = cache.maxStorageSizeLimitInBytes
        set(maxStorageSizeLimitInBytes) { cache.maxStorageSizeLimitInBytes = maxStorageSizeLimitInBytes }

    var newMiniAppSdkConfig: MiniAppSdkConfig = miniAppSettings1

    val miniAppSettings1: MiniAppSdkConfig
        get() = MiniAppSdkConfig(
            baseUrl = baseUrl,
            rasProjectId = projectId,
            subscriptionKey = subscriptionKey,
            // no update for hostAppUserAgentInfo because SDK does not allow changing it at runtime
            hostAppUserAgentInfo = manifestConfig.hostAppUserAgentInfo(),
            isPreviewMode = isPreviewMode,
            requireSignatureVerification = requireSignatureVerification,
            // temporarily taking values from buildConfig, we may add UI for this later.
            miniAppAnalyticsConfigList = listOf(
                MiniAppAnalyticsConfig(
                    BuildConfig.ADDITIONAL_ANALYTICS_ACC,
                    BuildConfig.ADDITIONAL_ANALYTICS_AID
                )
            ),
            maxStorageSizeLimitInBytes = maxStorageSizeLimitInBytes
        )

    val miniAppSettings2: MiniAppSdkConfig
        get() = MiniAppSdkConfig(
            baseUrl = baseUrl,
            rasProjectId = projectId2,
            subscriptionKey = subscriptionKey2,
            // no update for hostAppUserAgentInfo because SDK does not allow changing it at runtime
            hostAppUserAgentInfo = manifestConfig.hostAppUserAgentInfo(),
            isPreviewMode = isPreviewMode,
            requireSignatureVerification = requireSignatureVerification,
            // temporarily taking values from buildConfig, we may add UI for this later.
            miniAppAnalyticsConfigList = listOf(
                MiniAppAnalyticsConfig(
                    BuildConfig.ADDITIONAL_ANALYTICS_ACC,
                    BuildConfig.ADDITIONAL_ANALYTICS_AID
                )
            ),
            maxStorageSizeLimitInBytes = maxStorageSizeLimitInBytes
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

    var requireSignatureVerification: Boolean?
        get() =
            if (prefs.contains(REQUIRE_SIGNATURE_VERIFICATION))
                prefs.getBoolean(REQUIRE_SIGNATURE_VERIFICATION, true)
            else
                null
        set(isRequired) = prefs.edit().putBoolean(REQUIRE_SIGNATURE_VERIFICATION, isRequired!!).apply()

    var isProdVersionEnabled: Boolean?
        get() =
            if (prefs.contains(IS_PROD_VERSION_ENABLED))
                prefs.getBoolean(IS_PROD_VERSION_ENABLED, false)
            else
                null
        set(isRequired) = prefs.edit().putBoolean(IS_PROD_VERSION_ENABLED, isRequired!!).apply()

    var baseUrl: String?
        get() = prefs.getString(BASE_URL, null)
        set(baseUrl) = prefs.edit().putString(BASE_URL, baseUrl).apply()

    var projectId: String?
        get() = prefs.getString(APP_ID, null)
        set(appId) = prefs.edit().putString(APP_ID, appId).apply()

    var projectId2: String?
        get() = prefs.getString(APP_ID_2, null)
        set(appId2) = prefs.edit().putString(APP_ID_2, appId2).apply()

    var subscriptionKey: String?
        get() = prefs.getString(SUBSCRIPTION_KEY, null)
        set(subscriptionKey) = prefs.edit().putString(SUBSCRIPTION_KEY, subscriptionKey).apply()

    var subscriptionKey2: String?
        get() = prefs.getString(SUBSCRIPTION_KEY_2, null)
        set(subscriptionKey2) = prefs.edit().putString(SUBSCRIPTION_KEY_2, subscriptionKey2).apply()

    var uniqueId: String?
        get() = prefs.getString(UNIQUE_ID, null)
        set(uuid) = prefs.edit().putString(UNIQUE_ID, uuid).apply()

    var uniqueIdError: String?
        get() = prefs.getString(UNIQUE_ID_ERROR, null)
        set(uniqueIdError) = prefs.edit().putString(UNIQUE_ID_ERROR, uniqueIdError).apply()

    var messagingUniqueIdError: String?
        get() = prefs.getString(MESSAGING_UNIQUE_ID_ERROR, null)
        set(messagingUniqueIdError) = prefs.edit()
            .putString(MESSAGING_UNIQUE_ID_ERROR, messagingUniqueIdError).apply()

    var mauIdError: String?
        get() = prefs.getString(MAUID_ERROR, null)
        set(mauIdError) = prefs.edit().putString(MAUID_ERROR, mauIdError).apply()

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

    var dynamicDeeplinks: ArrayList<String>?
        get() = Gson().fromJson(
                prefs.getString(DYNAMIC_DEEPLINKS, null),
                object : TypeToken<ArrayList<String>>() {}.type
        )
        set(deeplinks) = prefs.edit().putString(DYNAMIC_DEEPLINKS, Gson().toJson(deeplinks)).apply()

    val isDynamicDeeplinksSaved: Boolean
        get() = prefs.contains(DYNAMIC_DEEPLINKS)

    var maxStorageSizeLimitInBytes: String
        get() = prefs.getString(MAX_STORAGE_SIZE_LIMIT, "5242880").toString() // Default max storage is 5MB
        set(maxStorageSizeLimitInBytes) = prefs.edit().putString(MAX_STORAGE_SIZE_LIMIT, maxStorageSizeLimitInBytes).apply()

    companion object {
        private const val IS_PREVIEW_MODE = "is_preview_mode"
        private const val REQUIRE_SIGNATURE_VERIFICATION = "require_signature_verification"
        private const val IS_PROD_VERSION_ENABLED = "is_prod_version_enabled"
        private const val BASE_URL = "base_url"
        private const val APP_ID = "app_id"
        private const val APP_ID_2 = "app_id_2"
        private const val SUBSCRIPTION_KEY = "subscription_key"
        private const val SUBSCRIPTION_KEY_2 = "subscription_key_2"
        private const val UNIQUE_ID = "unique_id"
        private const val UNIQUE_ID_ERROR = "unique_id_error"
        private const val MESSAGING_UNIQUE_ID_ERROR = "messaging_unique_id_error"
        private const val MAUID_ERROR = "mauid_error"
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
        private const val DYNAMIC_DEEPLINKS = "dynamic_deeplinks"
        private const val MAX_STORAGE_SIZE_LIMIT = "max_storage_size_limit"
    }
}
