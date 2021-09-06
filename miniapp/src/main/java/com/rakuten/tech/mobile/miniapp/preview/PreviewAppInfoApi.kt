package com.rakuten.tech.mobile.miniapp.preview

import com.rakuten.tech.mobile.miniapp.PreviewMiniAppInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface PreviewAppInfoApi {
    @GET("host/{hostId}/preview-codes/{previewCode}")
    fun fetchInfoByPreviewCode(
        @Path("hostId") hostId: String,
        @Path("previewCode") previewCode: String = ""
    ): Call<PreviewMiniAppInfo>
}
