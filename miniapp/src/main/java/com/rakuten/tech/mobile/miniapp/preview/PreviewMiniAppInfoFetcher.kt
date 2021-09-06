package com.rakuten.tech.mobile.miniapp.preview

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException

internal class PreviewMiniAppInfoFetcher(private var apiClient: ApiClient) {
    @Throws(MiniAppSdkException::class)
    suspend fun getInfoByPreviewCode(previewCode: String) =
        apiClient.fetchInfoByPreviewCode(previewCode)

}
