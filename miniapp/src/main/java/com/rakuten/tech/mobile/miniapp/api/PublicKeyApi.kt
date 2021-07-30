package com.rakuten.tech.mobile.miniapp.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface PublicKeyApi {
    @GET("host/keys/{keyId}")
    fun fetchPublicKey(
        @Path("keyId") keyId: String
    ): Call<PublicKeyEntity>
}

internal data class PublicKeyEntity(
    @SerializedName("id") val id: String,
    @SerializedName("pemKey") val pemKey: String,
    @SerializedName("ecKey") val ecKey: String
)
