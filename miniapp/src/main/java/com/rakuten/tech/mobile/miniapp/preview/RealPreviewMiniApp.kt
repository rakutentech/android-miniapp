package com.rakuten.tech.mobile.miniapp.preview

import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.sdkExceptionForInvalidArguments

internal class RealPreviewMiniApp(
    private val miniAppInfoFetcher: PreviewMiniAppInfoFetcher
) : PreviewMiniApp() {

    override suspend fun getMiniAppInfoByPreviewCode(previewCode: String): MiniAppInfo = when {
        previewCode.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppInfoFetcher.getInfoByPreviewCode(previewCode)
    }
}
