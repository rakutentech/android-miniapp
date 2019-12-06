package com.rakuten.mobile.miniapp.download.dagger

import com.rakuten.mobile.miniapp.core.dagger.ContextModule
import com.rakuten.mobile.miniapp.core.dagger.scopes.AppScope
import com.rakuten.mobile.miniapp.download.DownloadMiniAppImpl
import com.rakuten.mobile.miniapp.download.listener.FileDownloadListener
import com.rakuten.mobile.miniapp.download.utility.MiniAppFileWriter
import com.rakuten.mobile.miniapp.download.work.scheduler.DownloadScheduler
import com.rakuten.mobile.miniapp.download.work.worker.DownloadWorker
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
