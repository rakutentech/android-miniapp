package com.rakuten.tech.mobile.miniapp.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface ManifestApi {
    @GET("miniapp/{miniappId}/version/{versionId}/manifest")
    fun fetchFileListFromManifest(
        @Path("miniappId") miniAppId: String,
        @Path("versionId") versionId: String
    ): Call<ManifestEntity>
}

internal data class ManifestEntity(
    val files: List<String>
)
