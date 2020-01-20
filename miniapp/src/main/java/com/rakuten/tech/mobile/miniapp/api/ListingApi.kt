package com.rakuten.tech.mobile.miniapp.api

import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface ListingApi {
    @GET("oneapp/android/{hostAppVersion}/miniapps")
    fun list(
        @Path("hostAppVersion") hostAppVersion: String
    ): Call<List<MiniAppInfo>>
}

