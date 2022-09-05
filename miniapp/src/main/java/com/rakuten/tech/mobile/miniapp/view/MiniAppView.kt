package com.rakuten.tech.mobile.miniapp.view

import android.content.Context
import com.rakuten.tech.mobile.miniapp.*

/**
 * This class can be used in the HostApp to create the miniapp views independently.
 */
abstract class MiniAppView internal constructor() {
    companion object {
        internal lateinit var instance: MiniAppView
        /**
         * Creates a mini app view.
         * The mini app is downloaded, saved and provides a view when successful.
         * @param miniAppParameters parameters needed to create a mini app.
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
            instance = MiniAppViewimpl(miniAppParameters) {
                MiniAppViewHandler(context, config.miniAppSdkConfig)
            }
            return instance
        }
    }

    abstract fun load(queryParameter: String = "", onComplete: (MiniAppDisplay) -> Unit)

}




