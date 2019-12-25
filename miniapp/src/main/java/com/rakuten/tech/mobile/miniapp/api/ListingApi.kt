package com.rakuten.tech.mobile.miniapp.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface ListingApi {
    @GET("oneapp/android/{hostAppVersion}/miniapps")
    fun list(
        @Path("hostAppVersion") hostAppVersion: String
    ): Call<List<ListingEntity>>
}

internal data class ListingEntity(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val versionId: String,
    val files: List<String>
)
