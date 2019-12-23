package com.rakuten.tech.mobile.miniapp.legacy.core.dagger

import android.content.Context
import com.rakuten.tech.mobile.miniapp.legacy.core.CoreImpl
import com.rakuten.tech.mobile.miniapp.legacy.core.dagger.scopes.AppScope
import dagger.Module
import dagger.Provides

/**
 * Dagger module.
 */
@Module
class ContextModule {

    /**
     * Dagger provides host app's application context.
     */
    @AppScope
    @Provides
    fun provideContext(): Context = CoreImpl.context!!
}
