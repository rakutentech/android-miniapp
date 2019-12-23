package com.rakuten.tech.mobile.miniapp.legacy.download

/**
 * Download MiniApp interface.
 */
interface MiniAppDownloader {

    /**
     * Download mini app based on manifest manifestEndpoint.
     */
    fun downloadMiniApp(manifestEndpoint: String)
}
