package com.rakuten.tech.mobile.miniapp.api

import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface ListingApi {
    @GET("host/{hostappId}/miniapps")
    fun list(
        @Path("hostappId") hostAppId: String,
        @Query("hostVersion") hostAppVersionId: String
    ): Call<List<MiniAppInfo>>
}
