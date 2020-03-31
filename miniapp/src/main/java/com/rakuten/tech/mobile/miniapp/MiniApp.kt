package com.rakuten.tech.mobile.miniapp

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
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
     * @param info metadata of a mini app.
     * The mini app is downloaded, saved and provides a [MiniAppDisplay] when successful
     * @throws MiniAppSdkException when there is some issue during fetching,
     * downloading or creating the view.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun create(
        info: MiniAppInfo
    ): MiniAppDisplay

    /**
     * Fetches meta data information of a mini app.
     * @return [MiniAppInfo] for the provided appId of a mini app
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any reason.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun fetchInfo(appId: String): MiniAppInfo

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
        internal fun init(
            context: Context,
            baseUrl: String,
            rasAppId: String,
            subscriptionKey: String,
            hostAppVersionId: String
        ) {
            defaultConfig = MiniAppSdkConfig(
                baseUrl = baseUrl,
                rasAppId = rasAppId,
                subscriptionKey = subscriptionKey,
                hostAppVersionId = hostAppVersionId
            )
            val apiClient = ApiClient(
                baseUrl = baseUrl,
                rasAppId = rasAppId,
                subscriptionKey = subscriptionKey,
                hostAppVersionId = hostAppVersionId
            )
            val apiClientRepository = ApiClientRepository().apply {
                registerApiClient(defaultConfig.key, apiClient)
            }

            val miniAppStatus = MiniAppStatus(context)
            val storage = MiniAppStorage(FileWriter(), context.filesDir)

            instance = RealMiniApp(
                apiClientRepository = apiClientRepository,
                displayer = Displayer(context),
                miniAppDownloader = MiniAppDownloader(storage, apiClient, miniAppStatus),
                miniAppInfoFetcher = MiniAppInfoFetcher(apiClient)
            )
        }
    }
}
