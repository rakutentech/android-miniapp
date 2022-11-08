package com.rakuten.tech.mobile.testapp.ui.settings.cache

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.js.userinfo.Points
import com.rakuten.tech.mobile.miniapp.js.userinfo.TokenData
import com.rakuten.tech.mobile.miniapp.testapp.R

internal class Cache(
    context: Context,
    isVerificationRequired: Boolean,
    isPreviewMode: Boolean
) {

    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.sample.settings",
        Context.MODE_PRIVATE
    )
    private val productionBaseUrl = context.getString(R.string.prodBaseUrl)
    private val stagingBaseUrl = context.getString(R.string.stagingBaseUrl)

    val rasConfigData = RasConfigData(
        context = context,
        requireSignatureVerification = isVerificationRequired,
        isPreviewMode = isPreviewMode
    )

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
        private const val TAB_1_DATA_KEY = "tab_1_data"
        private const val TAB_2_DATA_KEY = "tab_2_data"
        private const val TAB_1_TEMP_DATA_KEY = "tab_1_temp_data"
        private const val TAB_2_TEMP_DATA_KEY = "tab_2_temp_data"
        internal const val TAB_1_MINIAPP_INFO_LIST_KEY = "tab_1_list"
        internal const val TAB_2_MINIAPP_INFO_LIST_KEY = "tab_2_list"

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
    inner class RasConfigData(
        context: Context,
        requireSignatureVerification: Boolean,
        isPreviewMode: Boolean
    ) {
        private val isDefaultProductionEnabled = true
        val gson = Gson()

        private val tab1MiniAppConfigCache = MiniAppConfigCache(
            TAB_1_DATA_KEY
        )

        private val tab1TempMiniAppConfigCache = MiniAppConfigCache(
            TAB_1_TEMP_DATA_KEY
        )

        private val tab2MiniAppConfigCache = MiniAppConfigCache(
            TAB_2_DATA_KEY
        )

        private val tab2TempMiniAppConfigCache = MiniAppConfigCache(
            TAB_2_TEMP_DATA_KEY
        )

        private fun isTempCleared() = prefs.getBoolean(IS_TEMP_CLEARED, true)

        private val defaultProductionData = MiniAppConfigData(
            isProduction = isDefaultProductionEnabled,
            isVerificationRequired = requireSignatureVerification,
            isPreviewMode = isPreviewMode,
            projectId = context.getString(R.string.prodProjectId),
            subscriptionId = context.getString(R.string.prodSubscriptionKey)
        )

        private val defaultStagingData = MiniAppConfigData(
            isProduction = isDefaultProductionEnabled,
            isVerificationRequired = requireSignatureVerification,
            isPreviewMode = isPreviewMode,
            projectId = context.getString(R.string.stagingProjectId),
            subscriptionId = context.getString(R.string.stagingSubscriptionKey)
        )

        private fun getDefaultData(
        ): MiniAppConfigData {
            return if (isDefaultProductionEnabled) defaultProductionData
            else defaultStagingData
        }

        private fun setTempCleared(isCleared: Boolean) {
            prefs.edit().putBoolean(IS_TEMP_CLEARED, isCleared).commit()
        }

        fun clearTempData() {
            val editor = prefs.edit()
            tab1TempMiniAppConfigCache.clear(editor)
            tab2TempMiniAppConfigCache.clear(editor)
            setTempCleared(true)
        }

        fun getTab1Data(): MiniAppConfigData =
            tab1MiniAppConfigCache.getData(gson, prefs) ?: getDefaultData()

        private fun getTab1TempData(): MiniAppConfigData =
            tab1TempMiniAppConfigCache.getData(gson, prefs) ?: getDefaultData()

        fun getTab1CurrentData(): MiniAppConfigData =
            if (isTempCleared() && isSettingSaved) getTab1Data() else getTab1TempData()

        fun getTab2Data(): MiniAppConfigData =
            tab2MiniAppConfigCache.getData(gson, prefs) ?: getDefaultData()

        fun getTab2CurrentData(): MiniAppConfigData =
            if (isTempCleared() && isSettingSaved) getTab2Data() else getTab2TempData()

        private fun getTab2TempData(): MiniAppConfigData =
            tab2TempMiniAppConfigCache.getData(gson, prefs) ?: getDefaultData()


        fun saveTab1Data() {
            tab1MiniAppConfigCache.setData(
                prefs.edit(),
                getTab1TempData()
            )
        }

        fun getTabMiniAppInfoList(key: String): List<MiniAppInfo>? =
            MiniAppListCache(key).getData(prefs)

        fun saveCurrentMiniAppInfoList(miniAppInfoList: List<MiniAppInfo>, key: String){
            MiniAppListCache(key).setData(prefs.edit(), miniAppInfoList)
        }

        fun saveTab1MiniAppInfoList(miniAppInfoList: List<MiniAppInfo>) {
            MiniAppListCache(TAB_1_MINIAPP_INFO_LIST_KEY).setData(prefs.edit(), miniAppInfoList)
        }

        fun saveTab2MiniAppInfoList(miniAppInfoList: List<MiniAppInfo>) {
            MiniAppListCache(TAB_2_MINIAPP_INFO_LIST_KEY).setData(prefs.edit(), miniAppInfoList)
        }

        fun clearAllMiniAppInfoList(){
            MiniAppListCache(TAB_1_MINIAPP_INFO_LIST_KEY).clear(prefs.edit())
            MiniAppListCache(TAB_2_MINIAPP_INFO_LIST_KEY).clear(prefs.edit())
        }

        fun setTempTab1IsProduction(isProduction: Boolean) {
            val projectIdSubsKeyPair = getDefaultProjectIdAndSubsKeyPair(isProduction)
            val newData = getTab1TempData().copy(
                isProduction = isProduction,
                projectId = projectIdSubsKeyPair.first,
                subscriptionId = projectIdSubsKeyPair.second,
            )
            tab1TempMiniAppConfigCache.setData(
                prefs.edit(),
                newData
            )
            setTempCleared(false)
        }

        fun setTempTab1IsVerificationRequired(isVerificationRequired: Boolean) {
            val configData = getTab1TempData()
            val newData = configData.copy(isVerificationRequired = isVerificationRequired)
            tab1TempMiniAppConfigCache.setData(
                prefs.edit(),
                newData
            )
            setTempCleared(false)
        }

        fun setTempTab1IsPreviewMode(isPreviewMode: Boolean) {
            val configData = getTab1TempData()
            val newData = configData.copy(isPreviewMode = isPreviewMode)
            tab1TempMiniAppConfigCache.setData(prefs.edit(), newData)
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
        }

        private fun getDefaultProjectIdAndSubsKeyPair(isProduction: Boolean): Pair<String, String> {
            return if (isProduction) Pair(
                defaultProductionData.projectId,
                defaultProductionData.subscriptionId
            )
            else Pair(defaultStagingData.projectId, defaultStagingData.subscriptionId)
        }

        fun setTempTab2IsProduction(isProduction: Boolean) {
            val projectIdSubsKeyPair = getDefaultProjectIdAndSubsKeyPair(isProduction)
            val configData = getTab2TempData().copy(
                isProduction = isProduction,
                projectId = projectIdSubsKeyPair.first,
                subscriptionId = projectIdSubsKeyPair.second
            )
            tab2TempMiniAppConfigCache.setData(
                prefs.edit(),
                configData
            )
            setTempCleared(false)
        }

        fun setTempTab2IsVerificationRequired(isVerificationRequired: Boolean) {
            val configData = getTab2TempData().copy(isVerificationRequired = isVerificationRequired)
            tab2TempMiniAppConfigCache.setData(
                prefs.edit(),
                configData
            )
            setTempCleared(false)
        }

        fun setTempTab2IsPreviewMode(isPreviewMode: Boolean) {
            val newData = getTab2TempData().copy(isPreviewMode = isPreviewMode)
            tab2TempMiniAppConfigCache.setData(prefs.edit(), newData)
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
