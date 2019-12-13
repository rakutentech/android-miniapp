package com.rakuten.mobile.miniapp.core.dagger

import android.content.Context
import com.rakuten.mobile.miniapp.core.CoreImpl
import com.rakuten.mobile.miniapp.core.dagger.scopes.AppScope
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
