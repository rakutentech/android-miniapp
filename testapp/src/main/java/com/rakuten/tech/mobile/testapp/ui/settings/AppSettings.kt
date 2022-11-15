package com.rakuten.tech.mobile.testapp.ui.settings

import android.content.Context
import com.rakuten.tech.mobile.miniapp.AppManifestConfig
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalyticsConfig
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.js.userinfo.Points
import com.rakuten.tech.mobile.miniapp.js.userinfo.TokenData
import com.rakuten.tech.mobile.miniapp.testapp.BuildConfig
import com.rakuten.tech.mobile.testapp.ui.settings.cache.Cache
import com.rakuten.tech.mobile.testapp.ui.settings.cache.MiniAppConfigData
import java.util.*

@Suppress("TooManyFunctions")
class AppSettings private constructor(context: Context) {

    private val manifestConfig = AppManifestConfig(context)
    private val cache = Cache(
        context,
        manifestConfig.requireSignatureVerification(),
        manifestConfig.isPreviewMode()
    )

    val projectIdForAnalytics: String = if (isSettingSaved)
        cache.rasConfigData.getTab1Data().projectId
    else manifestConfig.rasProjectId()

    val uniqueId: String
        get() {
            val uniqueId = cache.uniqueId ?: UUID.randomUUID().toString()
            cache.uniqueId = uniqueId
            return uniqueId
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
        get() = cache.profileName ?: DEFAULT_PROFILE_NAME
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
        set(contacts) {
            cache.contacts = contacts
        }

    val isContactsSaved: Boolean
        get() = cache.isContactsSaved

    var urlParameters: String
        get() = cache.urlParameters ?: ""
        set(urlParameters) {
            cache.urlParameters = urlParameters
        }

    var accessTokenError: MiniAppAccessTokenError?
        get() = cache.accessTokenError
        set(accessTokenError) {
            cache.accessTokenError = accessTokenError
        }

    var points: Points
        get() = cache.points ?: DEFAULT_POINTS
        set(points) {
            cache.points = points
        }

    var dynamicDeeplinks: ArrayList<String>
        get() = cache.dynamicDeeplinks ?: arrayListOf()
        set(deeplinks) {
            cache.dynamicDeeplinks = deeplinks
        }

    val isDynamicDeeplinksSaved: Boolean
        get() = cache.isDynamicDeeplinksSaved

    var maxStorageSizeLimitInBytes: String
        get() = cache.maxStorageSizeLimitInBytes
        set(maxStorageSizeLimitInBytes) {
            cache.maxStorageSizeLimitInBytes = maxStorageSizeLimitInBytes
        }

    var isTab1Checked: Boolean
        get() = cache.isTab1Checked
        set(isTab1Checked) {
            cache.isTab1Checked = isTab1Checked
        }

    var newMiniAppSdkConfig: MiniAppSdkConfig = miniAppSettings1
    var miniAppInfoListKey = Cache.TAB_1_MINIAPP_INFO_LIST_KEY

    fun setTab1MiniAppSdkConfig() {
        newMiniAppSdkConfig = miniAppSettings1
        miniAppInfoListKey = Cache.TAB_1_MINIAPP_INFO_LIST_KEY
    }

    fun setTab2MiniAppSdkConfig() {
        newMiniAppSdkConfig = miniAppSettings2
        miniAppInfoListKey = Cache.TAB_2_MINIAPP_INFO_LIST_KEY
    }

    val miniAppSettings1: MiniAppSdkConfig
        get() {
            val tab1Data = cache.rasConfigData.getTab1Data()
            return MiniAppSdkConfig(
                baseUrl = cache.getBaseUrl(tab1Data.isProduction),
                rasProjectId = tab1Data.projectId,
                subscriptionKey = tab1Data.subscriptionId,
                // no update for hostAppUserAgentInfo because SDK does not allow changing it at runtime
                hostAppUserAgentInfo = manifestConfig.hostAppUserAgentInfo(),
                isPreviewMode = tab1Data.isPreviewMode,
                requireSignatureVerification = tab1Data.isVerificationRequired,
                // temporarily taking values from buildConfig, we may add UI for this later.
                miniAppAnalyticsConfigList = listOf(
                    MiniAppAnalyticsConfig(
                        BuildConfig.ADDITIONAL_ANALYTICS_ACC,
                        BuildConfig.ADDITIONAL_ANALYTICS_AID
                    )
                ),
                maxStorageSizeLimitInBytes = maxStorageSizeLimitInBytes
            )
        }

    val miniAppSettings2: MiniAppSdkConfig
        get() {
            val tab2Data = cache.rasConfigData.getTab2Data()
            return MiniAppSdkConfig(
                baseUrl = cache.getBaseUrl(tab2Data.isProduction),
                rasProjectId = tab2Data.projectId,
                subscriptionKey = tab2Data.subscriptionId,
                // no update for hostAppUserAgentInfo because SDK does not allow changing it at runtime
                hostAppUserAgentInfo = manifestConfig.hostAppUserAgentInfo(),
                isPreviewMode = tab2Data.isPreviewMode,
                requireSignatureVerification = tab2Data.isVerificationRequired,
                // temporarily taking values from buildConfig, we may add UI for this later.
                miniAppAnalyticsConfigList = listOf(
                    MiniAppAnalyticsConfig(
                        BuildConfig.ADDITIONAL_ANALYTICS_ACC,
                        BuildConfig.ADDITIONAL_ANALYTICS_AID
                    )
                ),
                maxStorageSizeLimitInBytes = maxStorageSizeLimitInBytes
            )
        }

    fun getCurrentTab1ConfigData(): MiniAppConfigData {
        return cache.rasConfigData.getTab1CurrentData()
    }

    fun getDefaultConfigData(isTab1Checked: Boolean): MiniAppConfigData {
        return when {
            isTab1Checked -> getCurrentTab1ConfigData()
            else -> {
                getCurrentTab2ConfigData()
            }
        }
    }

    fun getCurrentTab2ConfigData(): MiniAppConfigData {
        return cache.rasConfigData.getTab2CurrentData()
    }

    fun saveData() {
        cache.rasConfigData.saveTab1Data()
        cache.rasConfigData.saveTab2Data()
    }

    fun saveCurrentAppInfoList(miniAppInfoList: List<MiniAppInfo>){
        cache.rasConfigData.saveCurrentMiniAppInfoList(miniAppInfoList, miniAppInfoListKey)
    }

    fun saveTab1MiniAppInfoList(miniAppInfoList: List<MiniAppInfo>) {
        cache.rasConfigData.saveTab1MiniAppInfoList(miniAppInfoList)
    }

    fun getMiniAppinfoList(key: String): List<MiniAppInfo> =
        cache.rasConfigData.getTabMiniAppInfoList(key) ?: emptyList()

    fun saveTab2MiniAppInfoList(miniAppInfoList: List<MiniAppInfo>) {
        cache.rasConfigData.saveTab2MiniAppInfoList(miniAppInfoList)
    }

    fun setTempTab1ConfigData(
        credentialData: MiniAppConfigData
    ) {
        cache.rasConfigData.setTempTab1Data(
            credentialData
        )
    }

    fun setTempTab1IsProduction(isProduction: Boolean) {
        cache.rasConfigData.setTempTab1IsProduction(isProduction)
    }

    fun setTempTab1IsVerificationRequired(isVerificationRequired: Boolean) {
        cache.rasConfigData.setTempTab1IsVerificationRequired(isVerificationRequired)
    }

    fun setTempTab1IsPreviewMode(isPreviewMode: Boolean) {
        cache.rasConfigData.setTempTab1IsPreviewMode(isPreviewMode)
    }

    fun setTempTab2IsProduction(isProduction: Boolean) {
        cache.rasConfigData.setTempTab2IsProduction(isProduction)
    }

    fun setTempTab2IsVerificationRequired(isVerificationRequired: Boolean) {
        cache.rasConfigData.setTempTab2IsVerificationRequired(isVerificationRequired)
    }

    fun setTempTab2IsPreviewMode(isPreviewMode: Boolean) {
        cache.rasConfigData.setTempTab2IsPreviewMode(isPreviewMode)
    }

    fun setTempTab2ConfigData(
        credentialData: MiniAppConfigData
    ) {
        cache.rasConfigData.setTempTab2Data(
            credentialData
        )
    }

    fun clearTempData() {
        cache.rasConfigData.clearTempData()
    }

    fun clearAllMiniAppInfoList(){
        cache.rasConfigData.clearAllMiniAppInfoList()
    }

    companion object {
        lateinit var instance: AppSettings
        const val DEFAULT_PROFILE_NAME = "MiniAppUser"
        val DEFAULT_POINTS = Points(10, 20, 30)
        val fakeFirstNames = arrayOf(
            "Yvonne",
            "Jamie",
            "Leticia",
            "Priscilla",
            "Sidney",
            "Nancy",
            "Edmund",
            "Bill",
            "Megan"
        )
        val fakeLastNames = arrayOf(
            "Andrews",
            "Casey",
            "Gross",
            "Lane",
            "Thomas",
            "Patrick",
            "Strickland",
            "Nicolas",
            "Freeman"
        )

        fun init(context: Context) {
            instance = AppSettings(context)
        }
    }
}
