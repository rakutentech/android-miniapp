package com.rakuten.mobile.miniapp.download

import com.rakuten.mobile.miniapp.download.dagger.DaggerDownloadComponent
import com.rakuten.mobile.miniapp.download.dagger.DownloadComponent
import com.rakuten.mobile.miniapp.download.work.scheduler.DownloadScheduler
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
