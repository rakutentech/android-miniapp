package com.rakuten.tech.mobile.miniapp.legacy.platform

import android.app.Activity
import android.content.Context
import com.rakuten.tech.mobile.miniapp.legacy.core.CoreImpl
import com.rakuten.tech.mobile.miniapp.legacy.core.exceptions.MiniAppPlatformException
import com.rakuten.tech.mobile.miniapp.legacy.display.MiniAppDisplayer
import com.rakuten.tech.mobile.miniapp.legacy.download.MiniAppDownloader
import com.rakuten.tech.mobile.miniapp.legacy.platform.dagger.DaggerMiniAppComponent
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation class of MiniAppPlatform interface.
 */
class MiniAppPlatformImpl : MiniAppPlatform {

    /**
     * Dagger will inject these variables.
     */
    @Inject
    lateinit var downloadMiniApp: MiniAppDownloader
    @Inject
    lateinit var miniAppDisplayer: MiniAppDisplayer

    /**
     * Object only used for locking methods of this class.
     */
    private val lockObject: Any = Any()

    override fun init(applicationContext: Context): MiniAppPlatform {
        synchronized(lockObject) {
            CoreImpl.context = applicationContext.applicationContext
            return this
        }
    }

    override fun debugLogging(shouldLog: Boolean): MiniAppPlatform {
        if (shouldLog) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.uprootAll()
        }
        return this
    }

    override fun isInitialized(): Boolean {
        synchronized(lockObject) {
            return CoreImpl.context != null
        }
    }

    @Throws(MiniAppPlatformException::class)
    override fun download(manifestEndpoint: String) {
        if (!isInitialized()) {
            throw MiniAppPlatformException("Mini App Platform has not been initialized yet.")
        }

        // Lazy Dagger injection.
        DaggerMiniAppComponent.create().inject(this)
        downloadMiniApp.downloadMiniApp(manifestEndpoint)
    }

    override fun displayMiniApp(miniAppId: String, versionId: String, hostActivity: Activity) {
        if (!isInitialized()) {
            throw MiniAppPlatformException("Mini App Platform has not been initialized yet.")
        }

        // Lazy Dagger injection.
        DaggerMiniAppComponent.create().inject(this)
        miniAppDisplayer.displayMiniApp(miniAppId, versionId, hostActivity)
    }
}
