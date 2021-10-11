package com.rakuten.tech.mobile.miniapp.signatureverifier.api

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.IOException

internal class PublicKeyFetcher(private val client: SignatureApiClient) {

    @Throws(IOException::class)
    fun fetch(keyId: String): String {
        val response = client.fetchPath(keyId, null)

        if (!response.isSuccessful) {
            throw IOException("Unexpected response when fetching public key: $response")
        }

        val body = response.body!!.string() // Body is never null if request is successful

        return PublicKeyResponse.fromJsonString(body).ecKey
    }
}

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
