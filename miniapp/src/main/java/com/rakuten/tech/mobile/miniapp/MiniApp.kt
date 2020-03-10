package com.rakuten.tech.mobile.miniapp

import android.content.Context
import com.rakuten.tech.mobile.miniapp.api.ApiRepos
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
     * @param appId application ID of the mini app
     * @param versionId a version ID associated with the mini app
     * The mini app is downloaded, saved and provides a [MiniAppDisplay] when successful
     * @throws MiniAppSdkException when there is some issue during fetching,
     * downloading or creating the view.
     */
    abstract suspend fun create(
        appId: String,
        versionId: String
    ): MiniAppDisplay

    /**
     * Update SDK interaction interface based on [MiniAppSdkConfig] configuration.
      */
    internal abstract fun updateConfiguration(config: MiniAppSdkConfig?)

    /**
     * Fetches meta data information of a mini app.
     * @return [MiniAppInfo] for the provided appId of a mini app
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any reason.
     */
    abstract suspend fun fetchInfo(appId: String): MiniAppInfo

    companion object {
        private lateinit var instance: MiniApp

        /**
         * Instance of [MiniApp] which uses the default config settings as defined in AndroidManifest.xml.
         * For usual scenarios the default config suffices.
         * However, should it be required to change the config at runtime for QA purpose or similar,
         * another [MiniAppSdkConfig] can be provided for customization.
         *
         * @return [MiniApp] instance
         */
        @JvmStatic
        fun instance(config: MiniAppSdkConfig? = null): MiniApp =
            instance.apply { updateConfiguration(config) }

        @Suppress("LongMethod")
        internal fun init(
            context: Context,
            baseUrl: String,
            rasAppId: String,
            subscriptionKey: String,
            hostAppVersionId: String
        ) {
            val miniAppStatus = MiniAppStatus(context)
            val storage = MiniAppStorage(FileWriter(), context.filesDir)

            val defaultConfig = MiniAppSdkConfig(
                baseUrl = baseUrl,
                rasAppId = rasAppId,
                subscriptionKey = subscriptionKey,
                hostAppVersionId = hostAppVersionId
            )
            val apiRepos = ApiRepos(defaultConfig)
            val apiClient = apiRepos.get(defaultConfig)

            instance = RealMiniApp(
                apiRepos = apiRepos,
                displayer = Displayer(context),
                miniAppDownloader = MiniAppDownloader(storage, apiClient, miniAppStatus),
                miniAppInfoFetcher = MiniAppInfoFetcher(apiClient)
            )
        }
    }
}
