package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.miniapp.Downloader
import com.rakuten.tech.mobile.miniapp.miniapp.Lister

/**
 * This represents the contract between the consuming application and the SDK
 * by which operations in the mini app ecosystem are exposed.
 * Should be accessed via [MiniApp.instance].
 */
abstract class MiniApp internal constructor() {

    /**
     * Provides a [List] of type [MiniAppInfo] when obtained successfully, and an
     * [error] when fetching fails from the backend server for any reason.
     */
    abstract suspend fun listMiniApp(
        success: (List<MiniAppInfo>) -> Unit,
        error: (Exception) -> Unit
    )

    /**
     * Creates a mini app for the given [appId]. The mini app is downloaded and saved.
     * Provides [MiniAppView] when successful and an [error] when there is some issue
     * during fetching, downloading or creating a view.
     */
    abstract suspend fun create(
        appId: String,
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
            downloader: Downloader,
            displayer: Displayer,
            lister: Lister
        ) {
            instance = RealMiniApp(
                downloader = downloader,
                displayer = displayer,
                lister = lister
            )
        }
    }
}
