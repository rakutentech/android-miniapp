package com.rakuten.tech.mobile.miniapp.api

/**
 * The interface for MiniApp components which require api interaction.
 */
internal interface MiniAppApiImpl {
    /** Is executed when there is a change of SDK configuration at runtime **/
    fun updateApiClient(apiClient: ApiClient)
}
