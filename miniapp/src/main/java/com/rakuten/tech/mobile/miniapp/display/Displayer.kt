package com.rakuten.tech.mobile.miniapp.display

import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics
import com.rakuten.tech.mobile.miniapp.js.iap.MiniAppIAPVerifier
import com.rakuten.tech.mobile.miniapp.js.MessageBridgeRatDispatcher
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.MiniAppSecureStorageDispatcher
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache

@Suppress("LongParameterList", "LongMethod")
internal class Displayer(private val hostAppUserAgentInfo: String) {

    @Suppress("LongParameterList")
    fun createMiniAppDisplay(
        basePath: String,
        miniAppInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppFileChooser: MiniAppFileChooser?,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        downloadedManifestCache: DownloadedManifestCache,
        queryParams: String,
        miniAppAnalytics: MiniAppAnalytics,
        ratDispatcher: MessageBridgeRatDispatcher,
        secureStorageDispatcher: MiniAppSecureStorageDispatcher,
        enableH5Ads: Boolean,
        miniAppIAPVerifier: MiniAppIAPVerifier
    ): MiniAppDisplay = RealMiniAppDisplay(
        basePath = basePath,
        miniAppInfo = miniAppInfo,
        miniAppMessageBridge = miniAppMessageBridge,
        miniAppNavigator = miniAppNavigator,
        miniAppFileChooser = miniAppFileChooser,
        hostAppUserAgentInfo = hostAppUserAgentInfo,
        miniAppCustomPermissionCache = miniAppCustomPermissionCache,
        downloadedManifestCache = downloadedManifestCache,
        queryParams = queryParams,
        miniAppAnalytics = miniAppAnalytics,
        ratDispatcher = ratDispatcher,
        secureStorageDispatcher = secureStorageDispatcher,
        enableH5Ads = enableH5Ads,
        miniAppIAPVerifier = miniAppIAPVerifier
    )

    @Suppress("LongParameterList")
    fun createMiniAppDisplay(
        appUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppFileChooser: MiniAppFileChooser?,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        downloadedManifestCache: DownloadedManifestCache,
        queryParams: String,
        miniAppAnalytics: MiniAppAnalytics,
        ratDispatcher: MessageBridgeRatDispatcher,
        secureStorageDispatcher: MiniAppSecureStorageDispatcher,
        enableH5Ads: Boolean,
        miniAppIAPVerifier: MiniAppIAPVerifier
    ): MiniAppDisplay = RealMiniAppDisplay(
        appUrl = appUrl,
        miniAppMessageBridge = miniAppMessageBridge,
        miniAppNavigator = miniAppNavigator,
        miniAppFileChooser = miniAppFileChooser,
        hostAppUserAgentInfo = hostAppUserAgentInfo,
        miniAppCustomPermissionCache = miniAppCustomPermissionCache,
        downloadedManifestCache = downloadedManifestCache,
        queryParams = queryParams,
        miniAppAnalytics = miniAppAnalytics,
        ratDispatcher = ratDispatcher,
        secureStorageDispatcher = secureStorageDispatcher,
        enableH5Ads = enableH5Ads,
        miniAppIAPVerifier = miniAppIAPVerifier
    )

    @Suppress("LongParameterList")
    fun createMiniAppDisplayForBundle(
        basePath: String,
        miniAppInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        downloadedManifestCache: DownloadedManifestCache,
        queryParams: String,
    ): MiniAppDisplay = BundleMiniAppDisplay(
        basePath = basePath,
        miniAppInfo = miniAppInfo,
        miniAppMessageBridge = miniAppMessageBridge,
        miniAppNavigator = miniAppNavigator,
        hostAppUserAgentInfo = hostAppUserAgentInfo,
        miniAppCustomPermissionCache = miniAppCustomPermissionCache,
        downloadedManifestCache = downloadedManifestCache,
        queryParams = queryParams
    )
}
