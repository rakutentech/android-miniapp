package com.rakuten.mobile.miniapp.platform

import android.app.Activity
import android.content.Context
import com.rakuten.mobile.miniapp.core.CoreImpl
import com.rakuten.mobile.miniapp.core.exceptions.MiniAppPlatformException
import com.rakuten.mobile.miniapp.download.MiniAppDownloader
import com.rakuten.mobile.miniapp.platform.dagger.DaggerMiniAppComponent
import com.rakuten.tech.mobile.display.MiniAppDisplayer
import javax.inject.Inject
import timber.log.Timber

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
