package com.rakuten.tech.mobile.miniapp.api

import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface AppInfoApi {

    @GET("host/{hostappId}/miniapps/{testPath}")
    fun list(
        @Path("hostappId") hostAppId: String,
        @Path("testPath") testPath: String = "",
        @Query("hostVersion") hostAppVersionId: String
    ): Call<List<MiniAppInfo>>

    @GET("host/{hostappId}/miniapps/{testPath}")
    fun fetchInfo(
        @Path("hostappId") hostAppId: String,
        @Path("testPath") testPath: String = "",
        @Query("hostVersion") hostAppVersionId: String,
        @Query("miniAppId") miniAppId: String
    ): Call<List<MiniAppInfo>>
}
