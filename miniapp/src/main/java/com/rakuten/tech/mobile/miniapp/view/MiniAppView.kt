package com.rakuten.tech.mobile.miniapp.view

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException

/**
 * This class can be used in the HostApp to create the miniapp views independently.
 */
@Suppress("UnnecessaryAbstractClass")
abstract class MiniAppView internal constructor() {
    companion object {
        internal lateinit var instance: MiniAppView

        /**
         * initialize a mini app view.
         * @param miniAppParameters parameters needed to create a mini app.
         */
        fun init(miniAppParameters: MiniAppParameters): MiniAppView {
            var context: Context? = null
            var config: MiniAppConfig? = null
            when (miniAppParameters) {
                is MiniAppParameters.DefaultParams -> {
                    context = miniAppParameters.context
                    config = miniAppParameters.config
                }
                is MiniAppParameters.InfoParams -> {
                    context = miniAppParameters.context
                    config = miniAppParameters.config
                }
                is MiniAppParameters.UrlParams -> {
                    context = miniAppParameters.context
                    config = miniAppParameters.config
                }
            }
            return createMiniAppView(context, miniAppParameters, config.miniAppSdkConfig)
        }

        @VisibleForTesting
        internal fun createMiniAppView(
            context: Context,
            miniAppParameters: MiniAppParameters,
            miniAppSdkConfig: MiniAppSdkConfig
        ): MiniAppView {
            instance = MiniAppViewImpl(miniAppParameters) {
                MiniAppViewHandler(context, miniAppSdkConfig)
            }
            return instance
        }
    }

    /**
     * load a mini app view.
     * The mini app is downloaded, saved and provides a view when successful.
     * @param queryParams the parameters will be appended with the miniapp url scheme.
     * @param fromCache the parameters will be appended with cached miniapp.
     * @param onComplete parameters needed to callback when the miniapp is successfully loaded.
     */
    abstract fun load(
        queryParams: String = "",
        fromCache: Boolean = false,
        onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit
    )

    /**
     * send a generic message
     * to MiniApp using [com.rakuten.tech.mobile.miniapp.js.NativeEventType.MINIAPP_RECEIVE_JSON_INFO].
     * @param message the content that will send to the MiniApp
     */
    abstract fun sendJsonToMiniApp(message: String)
}
