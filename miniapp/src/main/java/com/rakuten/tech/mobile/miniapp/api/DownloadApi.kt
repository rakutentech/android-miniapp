package com.rakuten.tech.mobile.miniapp.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

internal interface DownloadApi {
    @GET
    fun downloadFile(@Url url: String): Call<ResponseBody>
}
