package com.rakuten.mobile.miniapp.download

/**
 * Download MiniApp interface.
 */
interface MiniAppDownloader {

    /**
     * Download mini app based on manifest manifestEndpoint.
     */
    fun downloadMiniApp(manifestEndpoint: String)
}
