package com.rakuten.tech.mobile.testapp.ui.settings

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalyticsConfig
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.js.userinfo.Points
import com.rakuten.tech.mobile.miniapp.js.userinfo.TokenData
import com.rakuten.tech.mobile.miniapp.testapp.R


internal class Cache(context: Context) {

    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.sample.settings",
        Context.MODE_PRIVATE
    )

    val rasCredentialData = RasCredentialData(context)

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
        set(isRequired) = prefs.edit().putBoolean(REQUIRE_SIGNATURE_VERIFICATION, isRequired!!)
            .apply()

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
        set(profilePictureUrlBase64) = prefs.edit()
            .putString(PROFILE_PICTURE_URL_BASE_64, profilePictureUrlBase64)
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
        set(miniAppAnalyticsConfigs) = prefs.edit()
            .putString(ANALYTIC_CONFIGS, Gson().toJson(miniAppAnalyticsConfigs)).apply()

    var accessTokenError: MiniAppAccessTokenError?
        get() = gson.fromJson(
            prefs.getString(ACCESS_TOKEN_ERROR, null),
            MiniAppAccessTokenError::class.java
        )
        set(accessTokenError) = prefs.edit()
            .putString(ACCESS_TOKEN_ERROR, gson.toJson(accessTokenError))
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
        get() = prefs.getString(MAX_STORAGE_SIZE_LIMIT, "52428800")
            .toString() // Default max storage is 5MB
        set(maxStorageSizeLimitInBytes) = prefs.edit()
            .putString(MAX_STORAGE_SIZE_LIMIT, maxStorageSizeLimitInBytes).apply()


    companion object {
        private const val IS_PREVIEW_MODE = "is_preview_mode"
        private const val REQUIRE_SIGNATURE_VERIFICATION = "require_signature_verification"
        private const val IS_PROD_VERSION_ENABLED = "is_prod_version_enabled"
        private const val BASE_URL = "base_url"
        private const val APP_ID = "app_id"
        private const val APP_ID_2 = "app_id_2"
        const val TEMP_APP_ID = "temp_app_id"
        const val TEMP_APP_ID_2 = "temp_app_id_2"
        private const val SUBSCRIPTION_KEY = "subscription_key"
        private const val SUBSCRIPTION_KEY_2 = "subscription_key_2"
        const val TEMP_SUBSCRIPTION_KEY = "temp_subscription_key"
        const val TEMP_SUBSCRIPTION_KEY_2 = "temp_subscription_key_2"
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

    inner class RasCredentialData(context: Context) {


        val defaultProdPair = Pair(
            context.getString(R.string.prodProjectId),
            context.getString(R.string.prodSubscriptionKey)
        )

        val defaultStagingPair = Pair(
            context.getString(R.string.stagingProjectId),
            context.getString(R.string.stagingSubscriptionKey)
        )

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
            set(subscriptionKey2) = prefs.edit().putString(SUBSCRIPTION_KEY_2, subscriptionKey2)
                .apply()


        fun getDefaultData(isProduction: Boolean): Pair<String, String> {
            return if (isProduction) defaultProdPair else defaultStagingPair
        }

        fun getTab1TempData(): Pair<String, String> {
            return Pair(
                prefs.getString(TEMP_APP_ID, null) ?: "",
                prefs.getString(TEMP_SUBSCRIPTION_KEY, null) ?: ""
            )
        }

        fun getTab2TempData(): Pair<String, String> {
            return Pair(
                prefs.getString(TEMP_APP_ID_2, null) ?: "",
                prefs.getString(TEMP_SUBSCRIPTION_KEY_2, null) ?: ""
            )
        }

        fun isTab1TempDataValid(): Boolean {
            val tempData = getTab1TempData()
            return tempData.first.isNotBlank() && tempData.second.isNotBlank()
        }

        fun isTab2TempDataValid(): Boolean {
            val tempData = getTab2TempData()
            return tempData.first.isNotBlank() && tempData.second.isNotBlank()
        }

        /**
         * no OnSharedPreferenceChangeListener added, thus requires immediate value
         */

        fun setTab1Data(
            tempAppId: String,
            tempSubscriptionKey: String,
        ) {
            val edit = prefs.edit()
            edit.putString(TEMP_APP_ID, tempAppId).commit()
            edit.putString(TEMP_SUBSCRIPTION_KEY, tempSubscriptionKey).commit()
        }

        fun setTab2Data(
            tempAppId: String,
            tempSubscriptionKey: String,
        ) {
            val edit = prefs.edit()
            edit.putString(TEMP_APP_ID_2, tempAppId).commit()
            edit.putString(TEMP_SUBSCRIPTION_KEY_2, tempSubscriptionKey).commit()
        }
    }
}
