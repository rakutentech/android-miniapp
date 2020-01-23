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
     * Creates a mini app from the metadata [MiniAppInfo] object.
     * The mini app is downloaded, saved and provides a [MiniAppView] when successful
     * @throws MiniAppSdkException when there is some issue during fetching,
     * downloading or creating the view.
     */
    abstract suspend fun create(
        miniAppInfo: MiniAppInfo,
        success: (MiniAppView) -> Unit,
        error: (Exception) -> Unit
    )

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
            miniAppLister: MiniAppLister
        ) {
            instance = RealMiniApp(
                miniAppDownloader = miniAppDownloader,
                displayer = displayer,
                miniAppLister = miniAppLister
            )
        }
    }
}
