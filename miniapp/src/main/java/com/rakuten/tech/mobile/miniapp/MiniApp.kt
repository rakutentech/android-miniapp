package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.display.Displayer

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
     * Fetches meta data information of a mini app.
     * @return [MiniAppInfo] for the provided appId of a mini app
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any reason.
     */
    abstract suspend fun fetchMiniAppInfo(appId: String): MiniAppInfo

    companion object {
        private lateinit var instance: MiniApp

        /**
         * Instance of [MiniApp].
         *
         * @return [MiniApp] instance
         */
        @JvmStatic
        fun instance(): MiniApp = instance

        internal fun init(
            miniAppDownloader: MiniAppDownloader,
            displayer: Displayer,
            miniAppInfoFetcher: MiniAppInfoFetcher
        ) {
            instance = RealMiniApp(
                miniAppDownloader = miniAppDownloader,
                displayer = displayer,
                miniAppInfoFetcher = miniAppInfoFetcher
            )
        }
    }
}
