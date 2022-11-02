package com.rakuten.tech.mobile.testapp.ui.settings

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.AppManifestConfig
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.js.userinfo.Points
import com.rakuten.tech.mobile.miniapp.js.userinfo.TokenData
import com.rakuten.tech.mobile.miniapp.testapp.R

internal class Cache(context: Context, manifestConfig: AppManifestConfig) {

    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.sample.settings",
        Context.MODE_PRIVATE
    )
    val productionBaseUrl = context.getString(R.string.prodBaseUrl)
    val stagingBaseUrl = context.getString(R.string.stagingBaseUrl)

    val rasConfigData = RasConfigData(context, manifestConfig)

    fun getBaseUrl(isProduction: Boolean) = if (isProduction) {
        productionBaseUrl
    } else stagingBaseUrl

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
        get() = prefs.getString(MAX_STORAGE_SIZE_LIMIT, "5242880")
            .toString() // Default max storage is 5MB
        set(maxStorageSizeLimitInBytes) = prefs.edit()
            .putString(MAX_STORAGE_SIZE_LIMIT, maxStorageSizeLimitInBytes).apply()

    var isTab1Checked: Boolean
        get() = prefs.getBoolean(IS_TAB_1_CHECKED, true)
        set(isTab1Checked) = prefs.edit()
            .putBoolean(IS_TAB_1_CHECKED, isTab1Checked).apply()

    companion object {
        private const val IS_PREVIEW_MODE = "is_preview_mode"
        private const val TEMP_IS_PREVIEW_MODE = "temp_is_preview_mode"
        private const val IS_PREVIEW_MODE_2 = "is_preview_mode_2"
        private const val TEMP_IS_PREVIEW_MODE_2 = "temp_is_preview_mode_2"
        private const val REQUIRE_SIGNATURE_VERIFICATION = "require_signature_verification"
        private const val TEMP_REQUIRE_SIGNATURE_VERIFICATION =
            "temp_require_signature_verification"
        private const val REQUIRE_SIGNATURE_VERIFICATION_2 = "require_signature_verification_2"
        private const val TEMP_REQUIRE_SIGNATURE_VERIFICATION_2 =
            "temp_require_signature_verification_2"
        private const val IS_PROD_VERSION_ENABLED = "is_prod_version_enabled"
        private const val TEMP_IS_PROD_VERSION_ENABLED = "temp_is_prod_version_enabled"
        private const val IS_PROD_VERSION_ENABLED_2 = "is_prod_version_enabled_2"
        private const val TEMP_IS_PROD_VERSION_ENABLED_2 = "temp_is_prod_version_enabled_2"
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
        private const val ACCESS_TOKEN_ERROR = "access_token_error"
        private const val POINTS = "points"
        private const val DYNAMIC_DEEPLINKS = "dynamic_deeplinks"
        private const val MAX_STORAGE_SIZE_LIMIT = "max_storage_size_limit"
        private const val IS_TEMP_CLEARED = "is_temp_cleared"
        private const val IS_TAB_1_CHECKED = "is_tab_1_checked"
    }

    @Suppress("TooManyFunctions")
    inner class RasConfigData(context: Context, manifestConfig: AppManifestConfig) {
        private val isDefaultProductionEnabled = true

        private val tab1MiniAppConfigCache = MiniAppConfigCache(
            IS_PROD_VERSION_ENABLED,
            IS_PREVIEW_MODE,
            REQUIRE_SIGNATURE_VERIFICATION,
            APP_ID,
            SUBSCRIPTION_KEY
        )

        private val tab1TempMiniAppConfigCache = MiniAppConfigCache(
            TEMP_IS_PROD_VERSION_ENABLED,
            TEMP_IS_PREVIEW_MODE,
            TEMP_REQUIRE_SIGNATURE_VERIFICATION,
            TEMP_APP_ID,
            TEMP_SUBSCRIPTION_KEY
        )

        private val tab2MiniAppConfigCache = MiniAppConfigCache(
            IS_PROD_VERSION_ENABLED_2,
            IS_PREVIEW_MODE_2,
            REQUIRE_SIGNATURE_VERIFICATION_2,
            APP_ID_2,
            SUBSCRIPTION_KEY_2
        )

        private val tab2TempMiniAppConfigCache = MiniAppConfigCache(
            TEMP_IS_PROD_VERSION_ENABLED_2,
            TEMP_IS_PREVIEW_MODE_2,
            TEMP_REQUIRE_SIGNATURE_VERIFICATION_2,
            TEMP_APP_ID_2,
            TEMP_SUBSCRIPTION_KEY_2
        )

        fun isTempCleared() = prefs.getBoolean(IS_TEMP_CLEARED, true)

        val defaultProductionData = MiniAppConfigData(
            isProduction = isDefaultProductionEnabled,
            isVerificationRequired = manifestConfig.requireSignatureVerification(),
            isPreviewMode = manifestConfig.isPreviewMode(),
            projectId = context.getString(R.string.prodProjectId),
            subscriptionId = context.getString(R.string.prodSubscriptionKey)
        )

        val defaultStagingData = MiniAppConfigData(
            isProduction = isDefaultProductionEnabled,
            isVerificationRequired = manifestConfig.requireSignatureVerification(),
            isPreviewMode = manifestConfig.isPreviewMode(),
            projectId = context.getString(R.string.stagingProjectId),
            subscriptionId = context.getString(R.string.stagingSubscriptionKey)
        )

        fun getDefaultData(
        ): MiniAppConfigData {
            return if (isDefaultProductionEnabled) defaultProductionData
            else defaultStagingData
        }

        fun setTempCleared(isCleared: Boolean) {
            prefs.edit().putBoolean(IS_TEMP_CLEARED, isCleared).commit()
        }

        fun getTab1Data(): MiniAppConfigData =
            tab1MiniAppConfigCache.getData(prefs, getDefaultData())

        private fun getTab1TempData(): MiniAppConfigData =
            tab1TempMiniAppConfigCache.getData(prefs, getDefaultData())

        fun getTab1CurrentData(): MiniAppConfigData =
            if (isTempCleared() && isSettingSaved) getTab1Data() else getTab1TempData()

        fun getTab2Data(): MiniAppConfigData =
            tab2MiniAppConfigCache.getData(prefs, getDefaultData())

        fun getTab2CurrentData(): MiniAppConfigData =
            if (isTempCleared() && isSettingSaved) getTab2Data() else getTab2TempData()

        private fun getTab2TempData(): MiniAppConfigData =
            tab2TempMiniAppConfigCache.getData(prefs, getDefaultData())


        fun saveTab1Data() {
            tab1MiniAppConfigCache.setData(
                prefs.edit(),
                getTab1TempData()
            )
            tab1TempMiniAppConfigCache.clear(prefs.edit())
        }


        fun setTempTab1IsProduction(isProduction: Boolean) {
            tab1TempMiniAppConfigCache.setIsProduction(
                prefs.edit(),
                isProduction = isProduction,
                defaultProjectIdSubsKeyPair = getDefaultProjectIdAndSubsKeyPair(isProduction)
            )
            setTempCleared(false)
        }

        fun setTempTab1IsVerificationRequired(isVerificationRequired: Boolean) {
            tab1TempMiniAppConfigCache.setIsVerificationRequired(
                prefs.edit(),
                isVerificationRequired
            )
            setTempCleared(false)
        }

        fun setTempTab1IsPreviewMode(isPreviewMode: Boolean) {
            tab1TempMiniAppConfigCache.setIsPreviewMode(prefs.edit(), isPreviewMode)
            setTempCleared(false)
        }

        fun setTempTab1Data(
            credentialData: MiniAppConfigData
        ) {
            tab1TempMiniAppConfigCache.setData(
                prefs.edit(),
                credentialData
            )
        }

        fun saveTab2Data() {
            tab2MiniAppConfigCache.setData(
                prefs.edit(),
                getTab2TempData()
            )
            tab2TempMiniAppConfigCache.clear(prefs.edit())
        }

        fun getDefaultProjectIdAndSubsKeyPair(isProduction: Boolean): Pair<String, String> {
            return if (isProduction) Pair(
                defaultProductionData.projectId,
                defaultProductionData.subscriptionId
            )
            else Pair(defaultStagingData.projectId, defaultStagingData.subscriptionId)
        }

        fun setTempTab2IsProduction(isProduction: Boolean) {
            tab2TempMiniAppConfigCache.setIsProduction(
                prefs.edit(),
                isProduction = isProduction,
                defaultProjectIdSubsKeyPair = getDefaultProjectIdAndSubsKeyPair(isProduction)
            )
            setTempCleared(false)
        }

        fun setTempTab2IsVerificationRequired(isSignatureVerificationRequired: Boolean) {
            tab2TempMiniAppConfigCache.setIsVerificationRequired(
                prefs.edit(),
                isSignatureVerificationRequired
            )
            setTempCleared(false)
        }

        fun setTempTab2IsPreviewMode(isPreviewMode: Boolean) {
            tab2TempMiniAppConfigCache.setIsPreviewMode(prefs.edit(), isPreviewMode)
            setTempCleared(false)
        }

        fun setTempTab2Data(
            credentialData: MiniAppConfigData
        ) {
            tab2TempMiniAppConfigCache.setData(
                prefs.edit(),
                credentialData
            )
        }
    }
}
