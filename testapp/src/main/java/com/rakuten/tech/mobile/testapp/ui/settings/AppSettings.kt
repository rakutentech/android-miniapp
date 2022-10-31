package com.rakuten.tech.mobile.testapp.ui.settings

import android.content.Context
import com.rakuten.tech.mobile.miniapp.AppManifestConfig
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalyticsConfig
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.js.userinfo.Points
import com.rakuten.tech.mobile.miniapp.js.userinfo.TokenData
import com.rakuten.tech.mobile.miniapp.testapp.BuildConfig
import java.util.*

@Suppress("TooManyFunctions")
class AppSettings private constructor(context: Context) {

    private val manifestConfig = AppManifestConfig(context)
    private val cache = Cache(context, manifestConfig)

    val projectIdForAnalytics: String = if (isSettingSaved)
        cache.rasCredentialData.getTab1Data().projectId
    else manifestConfig.rasProjectId()

    var isDisplayInputPreviewMode: Boolean
        get() = cache.rasCredentialData.isDisplayByInputPreviewMode
            ?: manifestConfig.isPreviewMode()
        set(isPreviewMode) {
            cache.rasCredentialData.isDisplayByInputPreviewMode = isPreviewMode
        }

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

    var newMiniAppSdkConfig: MiniAppSdkConfig = miniAppSettings1

    val miniAppSettings1: MiniAppSdkConfig
        get() {
            val tab1Data = cache.rasCredentialData.getTab1Data()
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
                        BuildConfig.ADDITIONAL_ANALYTICS_ACC, BuildConfig.ADDITIONAL_ANALYTICS_AID
                    )
                ),
                maxStorageSizeLimitInBytes = maxStorageSizeLimitInBytes
            )
        }

    val miniAppSettings2: MiniAppSdkConfig
        get() {
            val tab2Data = cache.rasCredentialData.getTab2Data()
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
                        BuildConfig.ADDITIONAL_ANALYTICS_ACC, BuildConfig.ADDITIONAL_ANALYTICS_AID
                    )
                ),
                maxStorageSizeLimitInBytes = maxStorageSizeLimitInBytes
            )
        }

    fun getCurrentTab1CredentialData(): MiniAppCredentialData {
        return cache.rasCredentialData.getTab1CurrentData()
    }

    fun getDefaultCredentialData(): MiniAppCredentialData {
        return if (isSettingSaved) cache.rasCredentialData.getTab1Data() else cache.rasCredentialData.getDefaultData()
    }

    fun getCurrentTab2CredentialData(): MiniAppCredentialData {
        return cache.rasCredentialData.getTab2CurrentData()
    }

    fun saveData() {
        cache.rasCredentialData.saveTab1Data()
        cache.rasCredentialData.saveTab2Data()
        cache.rasCredentialData.isTempCleared = true
    }


    fun setTempTab1CredentialData(
        credentialData: MiniAppCredentialData
    ) {
        cache.rasCredentialData.setTempTab1Data(
            credentialData
        )
    }

    fun setTempTab1IsProduction(isProduction: Boolean) {
        cache.rasCredentialData.setTempTab1IsProduction(isProduction)
    }

    fun setTempTab1IsVerificationRequired(isVerificationRequired: Boolean) {
        cache.rasCredentialData.setTempTab1IsVerificationRequired(isVerificationRequired)
    }

    fun setTempTab1IsPreviewMode(isPreviewMode: Boolean) {
        cache.rasCredentialData.setTempTab1IsPreviewMode(isPreviewMode)
    }

    fun setTempTab2IsProduction(isProduction: Boolean) {
        cache.rasCredentialData.setTempTab2IsProduction(isProduction)
    }

    fun setTempTab2IsVerificationRequired(isVerificationRequired: Boolean) {
        cache.rasCredentialData.setTempTab2IsVerificationRequired(isVerificationRequired)
    }

    fun setTempTab2IsPreviewMode(isPreviewMode: Boolean) {
        cache.rasCredentialData.setTempTab2IsPreviewMode(isPreviewMode)
    }

    fun setTempTab2CredentialData(
        credentialData: MiniAppCredentialData
    ) {
        cache.rasCredentialData.setTempTab2Data(
            credentialData
        )
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
