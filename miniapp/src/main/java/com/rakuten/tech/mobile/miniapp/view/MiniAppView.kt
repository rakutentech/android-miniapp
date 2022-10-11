package com.rakuten.tech.mobile.miniapp.view

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppNotFoundException
import com.rakuten.tech.mobile.miniapp.MiniAppHasNoPublishedVersionException
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.RequiredPermissionsNotGrantedException
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig

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
     * @param onComplete parameters needed to callback when the miniapp is successfully loaded.
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
        RequiredPermissionsNotGrantedException::class,
    )
    abstract fun load(queryParams: String = "", onComplete: (MiniAppDisplay) -> Unit)
}
