package com.rakuten.tech.mobile.miniapp.preview

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException

abstract class PreviewMiniApp internal constructor() {
    /**
     * Fetches MiniappInfo by preview code.
     * @return of type [MiniAppInfo] when obtained successfully
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any reason.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun getMiniAppInfoByPreviewCode(previewCode: String): MiniAppInfo

    companion object {
        @VisibleForTesting
        internal lateinit var instance: PreviewMiniApp

        /**
         * @return [PreviewMiniApp] instance
         */
        @JvmStatic
        fun instance(): PreviewMiniApp = instance

        internal fun init(pubKey: String, miniAppSdkConfig: MiniAppSdkConfig) {
            val apiClient = ApiClient(
                baseUrl = miniAppSdkConfig.baseUrl,
                pubKey = pubKey,
                rasProjectId = miniAppSdkConfig.rasProjectId,
                subscriptionKey = miniAppSdkConfig.subscriptionKey
            )

            instance = RealPreviewMiniApp(miniAppInfoFetcher = PreviewMiniAppInfoFetcher(apiClient))
        }
    }
}

