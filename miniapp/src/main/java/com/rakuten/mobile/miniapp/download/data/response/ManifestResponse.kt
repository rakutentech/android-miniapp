package com.rakuten.mobile.miniapp.download.data.response

import com.google.gson.annotations.SerializedName

/**
 * Wrapper object of data class Manifest.
 */
object ManifestResponse {

  /**
   * Manifest data class which reprents response of /manifest endpoint.
   */
  data class Manifest(
    /**
     * Manifest which contains a list of URLs.
     */
    @SerializedName("manifest") val manifest: List<String>
  )
}
