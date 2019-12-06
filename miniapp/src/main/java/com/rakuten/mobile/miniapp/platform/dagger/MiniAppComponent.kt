package com.rakuten.mobile.miniapp.platform.dagger

import com.rakuten.mobile.miniapp.core.dagger.scopes.AppScope
import com.rakuten.mobile.miniapp.download.dagger.DownloadModule
import com.rakuten.mobile.miniapp.platform.MiniAppPlatformImpl
import com.rakuten.tech.mobile.display.dagger.DisplayerModule
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
