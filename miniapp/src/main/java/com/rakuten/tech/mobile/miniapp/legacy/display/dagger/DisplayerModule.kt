package com.rakuten.tech.mobile.miniapp.legacy.display.dagger

import com.rakuten.tech.mobile.miniapp.legacy.core.dagger.scopes.AppScope
import com.rakuten.tech.mobile.miniapp.legacy.display.MiniAppDisplayImpl
import com.rakuten.tech.mobile.miniapp.legacy.display.MiniAppDisplayer
import dagger.Module
import dagger.Provides

/**
 * Displayer module for Dagger.
 */
@Module
class DisplayerModule {

    /**
     * Providing MiniAppDisplayImpl object.
     */
    @AppScope
    @Provides
    fun provideMiniAppDisplayer(): MiniAppDisplayer = MiniAppDisplayImpl()
}
