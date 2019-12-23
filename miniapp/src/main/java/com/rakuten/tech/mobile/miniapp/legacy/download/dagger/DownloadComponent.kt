package com.rakuten.tech.mobile.miniapp.legacy.download.dagger

import com.rakuten.tech.mobile.miniapp.legacy.core.dagger.ContextModule
import com.rakuten.tech.mobile.miniapp.legacy.core.dagger.scopes.AppScope
import com.rakuten.tech.mobile.miniapp.legacy.download.DownloadMiniAppImpl
import com.rakuten.tech.mobile.miniapp.legacy.download.listener.FileDownloadListener
import com.rakuten.tech.mobile.miniapp.legacy.download.utility.MiniAppFileWriter
import com.rakuten.tech.mobile.miniapp.legacy.download.work.scheduler.DownloadScheduler
import com.rakuten.tech.mobile.miniapp.legacy.download.work.worker.DownloadWorker
import dagger.Component

/**
 * Dagger use only.
 */
@AppScope
@Component(modules = [ContextModule::class, DownloadModule::class])
interface DownloadComponent {
    /**
     * Dagger injecting target object.
     */
    fun inject(target: DownloadScheduler)

    /**
     * Dagger injecting target object.
     */
    fun inject(target: FileDownloadListener)

    /**
     * Dagger injecting target object.
     */
    fun inject(target: DownloadMiniAppImpl)

    /**
     * Dagger injecting target object.
     */
    fun inject(target: MiniAppFileWriter)

    /**
     * Dagger injecting target object.
     */
    fun inject(target: DownloadWorker)
}
