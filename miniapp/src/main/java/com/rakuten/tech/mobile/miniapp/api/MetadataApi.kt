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
    // TODO: MetaDataResponse --> check value with api response
    /**
     * {
    // The mini app should use "reqPermissions" for setting which permissions it requires.
    // These permissions will be requested by the host app before launching and downloading the mini app.
    // The user MUST accept these permissions before the mini app can be launched.
    "reqPermissions": [
    {
    "name": "rakuten.miniapp.user.USER_NAME",
    "reason": "Describe your reason here (optional)."
    },
    {
    "name": "rakuten.miniapp.user.PROFILE_PHOTO",
    "reason": "Describe your reason here (optional)."
    }
    ],
    // The mini app should use "optPermissions" for setting which permissions it will optionally use.
    // These permissions will be requested by the host app before launching and downloading the mini app.
    // The user can choose to either accept or deny these permissions before the mini app is launched.
    "optPermissions": [
    {
    "name": "rakuten.miniapp.user.CONTACT_LIST",
    "reason": "Describe your reason here (optional)."
    },
    {
    "name": "rakuten.miniapp.device.LOCATION",
    "reason": "Describe your reason here (optional)."
    }
    ],
    // The Host App can require additional keys that the mini app developer must set
    "exampleHostAppMetaData": {
    "exampleKey": "test"
    }
    }
     */
    @SerializedName("metadata") val manifest: MiniAppManifest
)
