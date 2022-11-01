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
import com.rakuten.tech.mobile.testapp.BuildVariant
import java.util.*

class AppSettings private constructor(context: Context) {

    private val manifestConfig = AppManifestConfig(context)
    private val cache = Cache(context)

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
        get() = cache.isProdVersionEnabled
            ?: (BuildConfig.BUILD_TYPE == BuildVariant.RELEASE.value && !isSettingSaved)
        set(isRequired) {
            cache.isProdVersionEnabled = isRequired
        }

    var baseUrl: String
        get() = cache.baseUrl ?: manifestConfig.baseUrl()
        set(baseUrl) {
            cache.baseUrl = baseUrl
        }

    var projectId: String
        get() = cache.rasCredentialData.projectId ?: manifestConfig.rasProjectId()
        set(projectId) {
            cache.rasCredentialData.projectId = projectId
        }

    var subscriptionKey: String
        get() = cache.rasCredentialData.subscriptionKey ?: manifestConfig.subscriptionKey()
        set(subscriptionKey) {
            cache.rasCredentialData.subscriptionKey = subscriptionKey
        }

    var projectId2: String
        get() = cache.rasCredentialData.projectId2 ?: manifestConfig.rasProjectId()
        set(projectId2) {
            cache.rasCredentialData.projectId2 = projectId2
        }

    var subscriptionKey2: String
        get() = cache.rasCredentialData.subscriptionKey2 ?: manifestConfig.subscriptionKey()
        set(subscriptionKey2) {
            cache.rasCredentialData.subscriptionKey2 = subscriptionKey2
        }

    var uniqueId: String
        get() {
            val uniqueId = cache.uniqueId ?: UUID.randomUUID().toString()
            cache.uniqueId = uniqueId
            return uniqueId
        }
        set(subscriptionKey) {
            cache.rasCredentialData.subscriptionKey = subscriptionKey
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

    fun getCurrentTab1ProjectIdSubscriptionKeyPair(isProduction: Boolean): Pair<String, String> {
        return if (isTab1TempCredentialDataValid()) {
            cache.rasCredentialData.getTab1TempData()
        } else {
            cache.rasCredentialData.getDefaultData(isProduction)
        }
    }

    fun getDefaultProjectIdSubscriptionKeyPair(): Pair<String, String> {
        return if (isProdVersionEnabled) cache.rasCredentialData.defaultProdPair else Pair(
            manifestConfig.rasProjectId(),
            manifestConfig.subscriptionKey()
        )
    }

    fun getCurrentTab2ProjectIdSubscriptionKeyPair(isProduction: Boolean): Pair<String, String> {
        return if (isTab2TempCredentialDataValid()) {
            cache.rasCredentialData.getTab2TempData()
        } else {
            cache.rasCredentialData.getDefaultData(isProduction)
        }
    }

    private fun isTab1TempCredentialDataValid(): Boolean {
        return cache.rasCredentialData.isTab1TempDataValid()
    }

    private fun isTab2TempCredentialDataValid(): Boolean {
        return cache.rasCredentialData.isTab2TempDataValid()
    }

    fun setTab1CredentialData(
        projectIdSubscriptionKeyPair: Pair<String, String>,
    ) {
        cache.rasCredentialData.setTab1Data(
            projectIdSubscriptionKeyPair.first,
            projectIdSubscriptionKeyPair.second,
        )
    }

    fun setTab2CredentialData(
        projectIdSubscriptionKeyPair: Pair<String, String>,
    ) {
        cache.rasCredentialData.setTab2Data(
            projectIdSubscriptionKeyPair.first,
            projectIdSubscriptionKeyPair.second,
        )
    }

    companion object {
        lateinit var instance: AppSettings
        const val DEFAULT_PROFILE_NAME = "MiniAppUser"
        val DEFAULT_POINTS = Points(10, 20, 30)
        val fakeFirstNames = arrayOf("Yvonne", "Jamie", "Leticia", "Priscilla", "Sidney", "Nancy", "Edmund", "Bill", "Megan")
        val fakeLastNames = arrayOf("Andrews", "Casey", "Gross", "Lane", "Thomas", "Patrick", "Strickland", "Nicolas", "Freeman")
        fun init(context: Context) {
            instance = AppSettings(context)
        }
    }
}
