package com.rakuten.tech.mobile.miniapp

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.storage.FileWriter
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage

/**
 * This represents the contract between the consuming application and the SDK
 * by which operations in the mini app ecosystem are exposed.
 * Should be accessed via [MiniApp.instance].
 */
@Suppress("UnnecessaryAbstractClass")
abstract class MiniApp internal constructor() {

    /**
     * Fetches and lists out the mini applications available in the MiniApp Ecosystem.
     * @return [List] of type [MiniAppInfo] when obtained successfully
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any reason.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun listMiniApp(): List<MiniAppInfo>

    /**
     * Creates a mini app.
     * The mini app is downloaded, saved and provides a [MiniAppDisplay] when successful.
     * @param appId mini app id.
     * @param miniAppMessageBridge the interface for communicating between host app & mini app
     * @throws MiniAppSdkException when there is some issue during fetching,
     * downloading or creating the view.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun create(
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge
    ): MiniAppDisplay

    /**
     * Same as {@link #create(String, MiniAppMessageBridge)}.
     * Use this to control external url loader.
     * @param miniAppNavigator allow host app to handle specific urls such as external link.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun create(
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator
    ): MiniAppDisplay

    /**
     * Fetches meta data information of a mini app.
     * @return [MiniAppInfo] for the provided appId of a mini app
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any reason.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun fetchInfo(appId: String): MiniAppInfo

    /**
     * Get custom permissions with grant results per MiniApp from this SDK.
     * @param miniAppId mini app id as the key to retrieve data from cache.
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
     * Update SDK interaction interface based on [MiniAppSdkConfig] configuration.
     */
    internal abstract fun updateConfiguration(newConfig: MiniAppSdkConfig)

    companion object {
        @VisibleForTesting
        internal lateinit var instance: MiniApp
        private lateinit var defaultConfig: MiniAppSdkConfig

        /**
         * Instance of [MiniApp] which uses the default config settings,
         * as defined in AndroidManifest.xml. For usual scenarios the default config suffices.
         * However, should it be required to change the config at runtime for QA purpose or similar,
         * another [MiniAppSdkConfig] can be provided for customization.
         * @return [MiniApp] instance
         */
        @JvmStatic
        fun instance(settings: MiniAppSdkConfig = defaultConfig): MiniApp =
            instance.apply { updateConfiguration(settings) }

        @Suppress("LongMethod")
        internal fun init(context: Context, miniAppSdkConfig: MiniAppSdkConfig) {
            defaultConfig = miniAppSdkConfig
            val apiClient = ApiClient(
                baseUrl = miniAppSdkConfig.baseUrl,
                rasAppId = miniAppSdkConfig.rasAppId,
                subscriptionKey = miniAppSdkConfig.subscriptionKey,
                hostAppVersionId = miniAppSdkConfig.hostAppVersionId,
                isTestMode = miniAppSdkConfig.isTestMode
            )
            val apiClientRepository = ApiClientRepository().apply {
                registerApiClient(defaultConfig.key, apiClient)
            }

            val miniAppStatus = MiniAppStatus(context)
            val storage = MiniAppStorage(FileWriter(), context.filesDir)

            instance = RealMiniApp(
                apiClientRepository = apiClientRepository,
                displayer = Displayer(context, defaultConfig.hostAppUserAgentInfo),
                miniAppDownloader = MiniAppDownloader(storage, apiClient, miniAppStatus),
                miniAppInfoFetcher = MiniAppInfoFetcher(apiClient),
                miniAppCustomPermissionCache = MiniAppCustomPermissionCache(context)
            )
        }
    }
}
