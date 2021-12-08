package com.rakuten.tech.mobile.miniapp.api

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface PublicKeyApi {

    @GET("keys/{keyId}")
    fun fetchPath(
        @Path("keyId") keyId: String
    ): Call<PublicKeyResponse>
}

@SuppressWarnings("ParameterListWrapping")
@Keep
internal data class PublicKeyResponse(
    val id: String = "",
    val ecKey: String = "",
    val pemKey: String = ""
) {

    companion object {
        @SuppressWarnings("SwallowedException")
        fun fromJsonString(body: String): PublicKeyResponse {
            return try {
                val response = Gson().fromJson(body, PublicKeyResponse::class.java)
                if (response.id.isEmpty()) {
                    PublicKeyResponse()
                } else {
                    response
                }
            } catch (jex: JsonSyntaxException) {
                PublicKeyResponse()
            }
        }
    }
}
