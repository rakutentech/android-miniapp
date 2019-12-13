package com.rakuten.mobile.miniapp.download.network.api

import com.rakuten.mobile.miniapp.download.data.response.ManifestResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

/**
 * Retrofit framework needed API interface.
 */
interface MiniAppServiceApi {

    /**
     * Get request for /manifest endpoint.
     */
    @GET("/{manifest}")
    fun getManifest(@Path("manifest", encoded = true) manifestEndpoint: String):
            Call<ManifestResponse.Manifest>

    /**
     * Get request for downloading files from file storage. This method will be invoked once per
     * file download.
     */
    @GET
    fun getFile(@Url url: String): Call<ResponseBody>
}
