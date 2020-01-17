package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.api.MiniAppHttpException
import com.rakuten.tech.mobile.miniapp.display.Displayer

/**
 * This represents the contract between the consuming application and the SDK
 * by which operations in the mini app ecosystem are exposed.
 * Should be accessed via [MiniApp.instance].
 */
@Suppress("UnnecessaryAbstractClass")
abstract class MiniApp internal constructor() {

    /**
     * Provides a [List] of type [MiniAppInfo] when obtained successfully, and an
     * [error] when fetching fails from the backend server for any reason.
     */
    abstract suspend fun listMiniApp(
        success: (List<MiniAppInfo>) -> Unit,
        error: (MiniAppHttpException) -> Unit
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
