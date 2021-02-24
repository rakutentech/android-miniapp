package com.rakuten.tech.mobile.miniapp.api

import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface AppInfoApi {

    @GET("host/{hostId}/miniapps/{testPath}")
    fun list(
        @Path("hostId") hostId: String,
        @Path("testPath") testPath: String = ""
    ): Call<List<MiniAppInfo>>

    @GET("host/{hostId}/miniapps/{testPath}")
    fun fetchInfo(
        @Path("hostId") hostId: String,
        @Path("testPath") testPath: String = "",
        @Query("miniAppId") miniAppId: String
    ): Call<List<MiniAppInfo>>
}
