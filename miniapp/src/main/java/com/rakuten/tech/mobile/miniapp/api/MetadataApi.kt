package com.rakuten.tech.mobile.miniapp.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
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

@Keep
internal data class MetadataEntity(
    @SerializedName("metadata") val metadata: MetadataResponse
)

@Keep
data class MetadataResponse(
    // List of permissions requested by Mini App in their manifest
    @SerializedName("reqPermissions") val requiredPermissions: List<MetadataPermissionObj>?,
    @SerializedName("optPermissions") val optionalPermissions: List<MetadataPermissionObj>?
)

@Keep
data class MetadataPermissionObj(
    val name: String,
    val reason: String?
)
