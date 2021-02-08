package com.rakuten.tech.mobile.miniapp.api

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface MetadataApi {
    @GET("host/{hostappId}/miniapp/{miniappId}/version/{versionId}/{testPath}/metadata")
    fun fetchMetadata(
        @Path("hostappId") hostAppId: String,
        @Path("miniappId") miniAppId: String,
        @Path("versionId") versionId: String,
        @Path("testPath") testPath: String = ""
    ): Call<MetadataEntity>
}

internal data class MetadataEntity(
    @SerializedName("metadata") val manifest: MiniAppManifest
)
