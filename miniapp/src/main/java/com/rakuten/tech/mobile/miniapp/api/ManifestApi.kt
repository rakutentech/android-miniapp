package com.rakuten.tech.mobile.miniapp.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface ManifestApi {
    // double slash is okay
    @GET("host/{hostappId}/miniapp/{miniappId}/version/{versionId}/{testPath}/manifest")
    fun fetchFileListFromManifest(
        @Path("hostappId") hostAppId: String,
        @Path("miniappId") miniAppId: String,
        @Path("versionId") versionId: String,
        @Path("testPath") testPath: String = "",
        @Query("hostVersion") hostAppVersionId: String
    ): Call<ManifestEntity>
}

internal data class ManifestEntity(
    @SerializedName("manifest") val files: List<String>
)
