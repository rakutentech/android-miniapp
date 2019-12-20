package com.rakuten.mobile.miniapp.legacy.download.dagger

import com.rakuten.mobile.miniapp.legacy.core.dagger.scopes.AppScope
import com.rakuten.mobile.miniapp.legacy.core.utils.LocalUrlParser
import com.rakuten.mobile.miniapp.legacy.download.DownloadMiniAppImpl
import com.rakuten.mobile.miniapp.legacy.download.MiniAppDownloader
import com.rakuten.mobile.miniapp.legacy.download.network.client.RetrofitClient
import com.rakuten.mobile.miniapp.legacy.download.utility.MiniAppFileWriter
import com.rakuten.mobile.miniapp.legacy.download.work.scheduler.DownloadScheduler
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
    fun provideDownloadMiniApp(): MiniAppDownloader = DownloadMiniAppImpl()

    /**
     * Provides MiniAppFileWriter to dagger.
     */
    @Provides
    @AppScope
    fun provideFileWriter(): MiniAppFileWriter = MiniAppFileWriter()

    /**
     * Provides DownloadScheduler to dagger.
     */
    @Provides
    @AppScope
    fun provideDownloadScheduler(): DownloadScheduler = DownloadScheduler()

    /**
     * Provides LocalUrlParser to dagger.
     */
    @Provides
    @AppScope
    fun provideUrlParser(): LocalUrlParser = LocalUrlParser()

    /**
     * Provides RetrofitClient to dagger.
     */
    @Provides
    @AppScope
    fun providerRetrofitClient(): RetrofitClient = RetrofitClient()
}
