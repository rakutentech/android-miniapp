package com.rakuten.tech.mobile.miniapp.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface ManifestApi {
    @GET("host/{hostappId}/miniapp/{miniappId}/version/{versionId}/manifest")
    fun fetchFileListFromManifest(
        @Path("hostappId") hostAppId: String,
        @Path("miniappId") miniAppId: String,
        @Path("versionId") versionId: String,
        @Query("hostVersion") hostAppVersionId: String
    ): Call<ManifestEntity>
}

internal data class ManifestEntity(
    val files: List<String>
)
