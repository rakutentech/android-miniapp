package com.rakuten.tech.mobile.miniapp

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.api.ManifestApiCache
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.js.MessageBridgeRatDispatcher
import com.rakuten.tech.mobile.miniapp.js.MiniAppSecureStorageDispatcher
import com.rakuten.tech.mobile.miniapp.signatureverifier.SignatureVerifier
import com.rakuten.tech.mobile.miniapp.storage.verifier.CachedMiniAppVerifier
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import com.rakuten.tech.mobile.miniapp.storage.FileWriter
import com.rakuten.tech.mobile.miniapp.storage.verifier.MiniAppManifestVerifier
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage

/**
 * This represents the contract between the consuming application and the SDK
 * by which operations in the mini app ecosystem are exposed.
 * Should be accessed via [MiniApp.instance].
 */
@Suppress("UnnecessaryAbstractClass", "LongMethod", "TooManyFunctions")
abstract class MiniApp internal constructor() {

    /**
     * Fetches and lists out the mini applications available in the MiniApp Ecosystem.
     * @return [List] of type [MiniAppInfo] when obtained successfully
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any reason.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun listMiniApp(): List<MiniAppInfo>

    /**
     * Fetches MiniappInfo by preview code.
     * @return of type [MiniAppInfo] when obtained successfully
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any reason.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun getMiniAppInfoByPreviewCode(previewCode: String): PreviewMiniAppInfo

    /**
     * Creates a mini app.
     * The mini app is downloaded, saved and provides a [MiniAppDisplay] when successful.
     * @param appId mini app id.
     * @param miniAppMessageBridge the interface for communicating between host app & mini app.
     * @param miniAppNavigator allow host app to handle specific urls such as external link.
     * @param miniAppFileChooser allow host app to get the file path while choosing file inside the webview.
     * @param queryParams the parameters will be appended with the miniapp url scheme.
     * @param fromCache allow host app to load miniapp from cache.
     * @throws [MiniAppNotFoundException] when the specified project ID does not have any mini app exist on the server.
     * @throws [MiniAppHasNoPublishedVersionException] when the specified mini app ID exists on the
     * server but has no published versions
     * @throws [MiniAppSdkException] when there is any other issue during fetching,
     * downloading or creating the view.
     * @throws [RequiredPermissionsNotGrantedException] when the required permissions of the manifest are not granted.
     */
    @Throws(
        MiniAppNotFoundException::class,
        MiniAppHasNoPublishedVersionException::class,
        MiniAppSdkException::class,
        RequiredPermissionsNotGrantedException::class
    )
    @Suppress("LongParameterList", "LongMethod")
    @Deprecated(
        "This function has been deprecated.",
        ReplaceWith(
            "MiniAppView.init(param: MiniAppParameters).load {" +
                    "miniAppDisplay: MiniAppDisplay ->" +
                    "}"
        )
    )
    abstract suspend fun create(
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator? = null,
        miniAppFileChooser: MiniAppFileChooser? = null,
        queryParams: String = "",
        fromCache: Boolean = false
    ): MiniAppDisplay

    /**
     * Creates a mini app using the mini app ID and version specified in [MiniAppInfo].
     * This should only be used in "Preview Mode".
     * The mini app is downloaded, saved and provides a [MiniAppDisplay] when successful.
     * @param appInfo metadata of a mini app.
     * @param miniAppMessageBridge the interface for communicating between host app & mini app.
     * @param miniAppNavigator allow host app to handle specific urls such as external link.
     * @param miniAppFileChooser allow host app to get the file path while choosing file inside the webview.
     * @param queryParams the parameters will be appended with the miniapp url scheme.
     * @param fromCache allow host app to load miniapp from cache.
     * @throws [MiniAppNotFoundException] when the specified project ID does not have any mini app exist on the server.
     * @throws [MiniAppHasNoPublishedVersionException] when the specified mini app ID exists on the
     * server but has no published versions
     * @throws [MiniAppSdkException] when there is any other issue during fetching,
     * downloading or creating the view.
     * @throws [RequiredPermissionsNotGrantedException] when the required permissions of the manifest are not granted.
     */
    @Throws(
        MiniAppNotFoundException::class,
        MiniAppHasNoPublishedVersionException::class,
        MiniAppSdkException::class,
        RequiredPermissionsNotGrantedException::class
    )
    @Suppress("LongParameterList", "LongMethod")
    @Deprecated(
        "This function has been deprecated.",
        ReplaceWith(
            "MiniAppView.init(param: MiniAppParameters).load {" +
                    "miniAppDisplay: MiniAppDisplay ->" +
                    "}"
        )
    )
    abstract suspend fun create(
        appInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator? = null,
        miniAppFileChooser: MiniAppFileChooser? = null,
        queryParams: String = "",
        fromCache: Boolean = false
    ): MiniAppDisplay

    /**
     * Creates a mini app using provided url.
     * Mini app is NOT downloaded and cached in local, its content are read directly from the url.
     * This should only be used for previewing a mini app from a local server.
     * @param appUrl a HTTP url containing Mini App content.
     * @param miniAppMessageBridge the interface for communicating between host app & mini app.
     * @param miniAppNavigator allow host app to handle specific urls such as external link.
     * @param miniAppFileChooser allow host app to get the file path while choosing file inside the webview.
     * @param queryParams the parameters will be appended with the miniapp url scheme.
     * @throws [MiniAppNotFoundException] when the specified Mini App URL cannot be reached.
     * @throws [MiniAppSdkException] when there is any other issue during loading or creating the view.
     */
    @Throws(MiniAppNotFoundException::class, MiniAppSdkException::class)
    @Deprecated(
        "This function has been deprecated.",
        ReplaceWith(
            "MiniAppView.init(param: MiniAppParameters).load {" +
                    "miniAppDisplay: MiniAppDisplay ->" +
                    "}"
        )
    )
    abstract suspend fun createWithUrl(
        appUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator? = null,
        miniAppFileChooser: MiniAppFileChooser? = null,
        queryParams: String = ""
    ): MiniAppDisplay

    /**
     * Fetches meta data information of a mini app.
     * @return [MiniAppInfo] for the provided appId of a mini app
     * @throws [MiniAppNotFoundException] when the specified project ID does not have any mini app exist on the server.
     * @throws [MiniAppHasNoPublishedVersionException] when the specified mini app ID exists on the
     * server but has no published versions
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any other reason.
     */
    @Throws(
        MiniAppNotFoundException::class,
        MiniAppHasNoPublishedVersionException::class,
        MiniAppSdkException::class
    )
    abstract suspend fun fetchInfo(appId: String): MiniAppInfo

    /**
     * Get custom permissions with grant results per MiniApp from this SDK.
     * @param miniAppId mini app id as the key to retrieve data from cache.
     * @return [MiniAppCustomPermission] an object contains the grant results per mini app.
     */
    abstract fun getCustomPermissions(
        miniAppId: String
    ): MiniAppCustomPermission

    /**
     * Store custom permissions with grant results per MiniApp inside this SDK.
     * @param miniAppCustomPermission the supplied custom permissions to be stored in cache.
     */
    abstract fun setCustomPermissions(
        miniAppCustomPermission: MiniAppCustomPermission
    )

    /**
     * lists out the mini applications available with custom permissions in cache.
     * @return [List<MiniAppInfo>] list of MiniApp what is downloaded and containing
     * custom permissions data.
     */
    @Suppress("FunctionMaxLength")
    abstract fun listDownloadedWithCustomPermissions(): List<Pair<MiniAppInfo, MiniAppCustomPermission>>

    /**
     * Clears all secure storage items for all mini apps.
     * @param context will be used to find the storage to be deleted.
     * Host App should call this when they want to clear all sensitive and session data such as when a user logs out.
     */
    abstract fun clearSecureStorages(context: Context)

    /**
     * Clears the secure storage for a particular Mini App ID.
     * @param context will be used to find the storage to be deleted.
     * @param miniAppId will be used to find the storage to be deleted.
     */
    abstract fun clearSecureStorage(context: Context, miniAppId: String): Boolean

    /**
     * Get the manifest information e.g. required and optional permissions.
     * @param appId mini app id.
     * @param versionId of mini app.
     * @param languageCode of mini app.
     * @return MiniAppManifest an object contains manifest information of a mini app.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun getMiniAppManifest(
        appId: String,
        versionId: String,
        languageCode: String = ""
    ): MiniAppManifest

    /**
     * Get the currently downloaded manifest information e.g. required and optional permissions.
     * @param appId mini app id.
     * @return MiniAppManifest an object contains manifest information of a miniapp.
     */
    abstract fun getDownloadedManifest(appId: String): MiniAppManifest?

    /**
     * Update SDK interaction interface based on [MiniAppSdkConfig] configuration.
     * [setConfigAsDefault] for use the [MiniAppSdkConfig] as default.
     */
    internal abstract fun updateConfiguration(newConfig: MiniAppSdkConfig, setConfigAsDefault: Boolean)

    companion object {
        @VisibleForTesting
        internal lateinit var instance: MiniApp
        private lateinit var defaultConfig: MiniAppSdkConfig

        /**
         * Instance of [MiniApp] which uses the default config settings,
         * as defined in AndroidManifest.xml. For usual scenarios the default config suffices.
         * However, should it be required to change the config at runtime for QA purpose or similar,
         * another [MiniAppSdkConfig] can be provided for customization.
         * [setConfigAsDefault] is to use the config as default.
         * @return [MiniApp] instance
         */
        @JvmStatic
        fun instance(settings: MiniAppSdkConfig = defaultConfig, setConfigAsDefault: Boolean = true): MiniApp =
            instance.apply { updateConfiguration(settings, setConfigAsDefault) }

        internal fun init(context: Context, miniAppSdkConfig: MiniAppSdkConfig) {
            defaultConfig = miniAppSdkConfig
            val apiClient = ApiClient(
                baseUrl = miniAppSdkConfig.baseUrl,
                rasProjectId = miniAppSdkConfig.rasProjectId,
                subscriptionKey = miniAppSdkConfig.subscriptionKey,
                isPreviewMode = miniAppSdkConfig.isPreviewMode,
                sslPublicKeyList = miniAppSdkConfig.sslPinningPublicKeyList
            )
            val apiClientRepository = ApiClientRepository().apply {
                registerApiClient(defaultConfig, apiClient)
            }
            val signatureVerifier: SignatureVerifier? = SignatureVerifier.init(
                context = context,
                baseUrl = miniAppSdkConfig.baseUrl + "keys/",
                subscriptionKey = miniAppSdkConfig.subscriptionKey
            )
            val miniAppAnalytics = MiniAppAnalytics(
                miniAppSdkConfig.rasProjectId,
                miniAppSdkConfig.miniAppAnalyticsConfigList
            )
            instance = RealMiniApp(
                apiClientRepository = apiClientRepository,
                displayer = Displayer(defaultConfig.hostAppUserAgentInfo),
                miniAppDownloader = MiniAppDownloader(
                    apiClient = apiClient,
                    miniAppAnalytics = miniAppAnalytics,
                    requireSignatureVerification = miniAppSdkConfig.requireSignatureVerification,
                    initStorage = { MiniAppStorage(FileWriter(), context.filesDir) },
                    initStatus = { MiniAppStatus(context) },
                    initVerifier = { CachedMiniAppVerifier(context) },
                    initManifestApiCache = { ManifestApiCache(context) },
                    initSignatureVerifier = { signatureVerifier }
                ),
                miniAppInfoFetcher = MiniAppInfoFetcher(apiClient),
                initCustomPermissionCache = { MiniAppCustomPermissionCache(context) },
                initDownloadedManifestCache = { DownloadedManifestCache(context) },
                initManifestVerifier = { MiniAppManifestVerifier(context) },
                miniAppAnalytics = miniAppAnalytics,
                ratDispatcher = MessageBridgeRatDispatcher(miniAppAnalytics = miniAppAnalytics),
                secureStorageDispatcher = MiniAppSecureStorageDispatcher(
                    context,
                    miniAppSdkConfig.maxStorageSizeLimitInBytes.toLong()
                ),
                enableH5Ads = miniAppSdkConfig.enableH5Ads
            )
        }
    }
}
