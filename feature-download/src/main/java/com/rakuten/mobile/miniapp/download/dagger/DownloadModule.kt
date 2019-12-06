package com.rakuten.mobile.miniapp.download.dagger

import com.rakuten.mobile.miniapp.core.dagger.scopes.AppScope
import com.rakuten.mobile.miniapp.core.utils.LocalUrlParser
import com.rakuten.mobile.miniapp.download.DownloadMiniAppImpl
import com.rakuten.mobile.miniapp.download.MiniAppDownloader
import com.rakuten.mobile.miniapp.download.network.client.RetrofitClient
import com.rakuten.mobile.miniapp.download.utility.MiniAppFileWriter
import com.rakuten.mobile.miniapp.download.work.scheduler.DownloadScheduler
import dagger.Module
import dagger.Provides

/**
 * Dagger module.
 */
@Module
class DownloadModule {

  /**
   * Provides DownloadMiniApp to dagger.
   */
  @Provides
  @AppScope
  fun provideDownloadMiniApp(): MiniAppDownloader {
    return DownloadMiniAppImpl()
  }

  /**
   * Provides MiniAppFileWriter to dagger.
   */
  @Provides
  @AppScope
  fun provideFileWriter(): MiniAppFileWriter {
    return MiniAppFileWriter()
  }

  /**
   * Provides DownloadScheduler to dagger.
   */
  @Provides
  @AppScope
  fun provideDownloadScheduler(): DownloadScheduler {
    return DownloadScheduler()
  }

  /**
   * Provides LocalUrlParser to dagger.
   */
  @Provides
  @AppScope
  fun provideUrlParser(): LocalUrlParser {
    return LocalUrlParser()
  }

  /**
   * Provides RetrofitClient to dagger.
   */
  @Provides
  @AppScope
  fun providerRetrofitClient(): RetrofitClient {
    return RetrofitClient()
  }
}
