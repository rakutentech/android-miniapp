package com.rakuten.tech.mobile.miniapp.legacy.platform.dagger

import com.rakuten.tech.mobile.miniapp.legacy.core.dagger.scopes.AppScope
import com.rakuten.tech.mobile.miniapp.legacy.display.dagger.DisplayerModule
import com.rakuten.tech.mobile.miniapp.legacy.download.dagger.DownloadModule
import com.rakuten.tech.mobile.miniapp.legacy.platform.MiniAppPlatformImpl
import dagger.Component

/**
 * Dagger use only.
 */
@AppScope
@Component(modules = [DownloadModule::class, DisplayerModule::class])
interface MiniAppComponent {
    /**
     * Injecting target class.
     */
    fun inject(target: MiniAppPlatformImpl)
}
