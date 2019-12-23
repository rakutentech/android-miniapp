package com.rakuten.tech.mobile.miniapp.legacy.download

import com.rakuten.tech.mobile.miniapp.legacy.download.dagger.DaggerDownloadComponent
import com.rakuten.tech.mobile.miniapp.legacy.download.dagger.DownloadComponent
import com.rakuten.tech.mobile.miniapp.legacy.download.work.scheduler.DownloadScheduler
import javax.inject.Inject

/**
 * Downloading mini app implementation.
 */
class DownloadMiniAppImpl : MiniAppDownloader {

    /**
     * Dagger injected object.
     */
    @Inject
    lateinit var downloadScheduler: DownloadScheduler

    init {
        daggerDownloadComponent.inject(this)
    }

    override fun downloadMiniApp(manifestEndpoint: String) {
        // TODO: Should validate endpoint and its format.
        downloadScheduler.scheduleDownload(manifestEndpoint)
    }

    companion object {
        val daggerDownloadComponent: DownloadComponent = DaggerDownloadComponent.create()
    }
}
