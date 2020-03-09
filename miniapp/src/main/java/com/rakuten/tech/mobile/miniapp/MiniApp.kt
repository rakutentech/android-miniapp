package com.rakuten.tech.mobile.miniapp

import android.content.Context
import com.rakuten.tech.mobile.miniapp.api.ApiClient
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

    protected abstract fun updateConfiguration(settings: MiniAppSdkConfig)

    /**
     * Fetches meta data information of a mini app.
     * @return [MiniAppInfo] for the provided appId of a mini app
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any reason.
     */
    abstract suspend fun fetchInfo(appId: String): MiniAppInfo

    companion object {
        private lateinit var instance: MiniApp
        private lateinit var defaultSettings: MiniAppSdkConfig

        /**
         * Instance of [MiniApp] with custom [MiniAppSdkConfig].
         * This function should only be used if you wish to use the SDK with custom settings
         * which are changed at runtime for QA purposes, etc.
         * Note that the default [instance] without [settings] uses the config settings from AndroidManifest.xml.
         *
         * @return [MiniApp] instance
         */
        @JvmStatic
        fun instance(settings: MiniAppSdkConfig = defaultSettings): MiniApp =
            instance.apply { updateConfiguration(settings) }

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
            val apiClient = ApiClient(
                baseUrl = baseUrl,
                rasAppId = rasAppId,
                subscriptionKey = subscriptionKey,
                hostAppVersionId = hostAppVersionId
            )
            defaultSettings = MiniAppSdkConfig(
                baseUrl = baseUrl,
                rasAppId = rasAppId,
                subscriptionKey = subscriptionKey,
                hostAppVersionId = hostAppVersionId
            )

            instance = RealMiniApp(
                displayer = Displayer(context),
                miniAppDownloader = MiniAppDownloader(storage, apiClient, miniAppStatus),
                miniAppInfoFetcher = MiniAppInfoFetcher(apiClient)
            )
        }
    }
}

/**
 * Config for the Mini App SDK.
 * Contains settings which are used when sending requests to the Mini App API.
 */
data class MiniAppSdkConfig(
    var baseUrl: String,
    var rasAppId: String,
    var subscriptionKey: String,
    var hostAppVersionId: String
)
