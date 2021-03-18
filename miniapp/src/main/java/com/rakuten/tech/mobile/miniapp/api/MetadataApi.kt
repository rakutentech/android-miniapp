package com.rakuten.tech.mobile.miniapp.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface MetadataApi {
    @GET("host/{hostId}/miniapp/{miniappId}/version/{versionId}/{testPath}/metadata")
    fun fetchMetadata(
        @Path("hostId") hostId: String,
        @Path("miniappId") miniAppId: String,
        @Path("versionId") versionId: String,
        @Path("testPath") testPath: String = ""
    ): Call<MetadataEntity>
}

@Keep
internal data class MetadataEntity(
    @SerializedName("bundleManifest") val metadata: MetadataResponse?
)

/** Metadata response object includes required and optional permissions. */
@Keep
data class MetadataResponse(
    // List of permissions requested by Mini App in their manifest
    @SerializedName("reqPermissions") val requiredPermissions: List<MetadataPermissionObj>?,
    @SerializedName("optPermissions") val optionalPermissions: List<MetadataPermissionObj>?,
    @SerializedName("customMetaData") val customMetaData: Map<String, String>?
)

/** Metadata permission object includes name and reason. */
@Keep
data class MetadataPermissionObj(
    val name: String?,
    val reason: String?
)
