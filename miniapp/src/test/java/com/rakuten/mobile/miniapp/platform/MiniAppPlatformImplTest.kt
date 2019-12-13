package com.rakuten.mobile.miniapp.platform

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import com.rakuten.mobile.miniapp.core.CoreImpl
import com.rakuten.mobile.miniapp.core.exceptions.MiniAppPlatformException
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
class MiniAppPlatformImplTest : MiniAppBaseTest() {

  lateinit var miniAppPlatformImpl: MiniAppPlatformImpl
  lateinit var applicationContext: Context

  @Before
  fun setup() {
    miniAppPlatformImpl = MiniAppPlatformImpl()
    applicationContext = getApplicationContext()
  }

  @After
  fun tearDown() {
    CoreImpl.context = null
    miniAppPlatformImpl.debugLogging(false)
  }

  @Test
  fun shouldIsInitializedReturnCorrectValue() {
    assertThat(miniAppPlatformImpl.isInitialized()).isFalse()
    miniAppPlatformImpl.init(applicationContext)
    assertThat(miniAppPlatformImpl.isInitialized())
  }

  // TODO: Verify the implementation: this test looks flaky, when run individually works as intended
  //  but when executed in batch, produces mixed results
  /*@Test
  fun shouldInitSdkAssignContext() {
    assertThat(CoreImpl.context).isNull()
    miniAppPlatformImpl.init(applicationContext)
    assertThat(CoreImpl.context).isNotNull()
  }*/

  @Test
  fun shouldTurnOnDebugLogging() {
    assertThat(Timber.forest().isEmpty())
    miniAppPlatformImpl.debugLogging(true)
    assertThat(Timber.forest().size == 1)
  }

  @Test
  fun shouldTurnOffDebugLogging() {
    miniAppPlatformImpl.debugLogging(true)
    assertThat(Timber.forest().size == 1)
    miniAppPlatformImpl.debugLogging(false)
    assertThat(Timber.forest().isEmpty())
  }

  @Test(expected = MiniAppPlatformException::class)
  fun shouldStartDownloadWithoutInit() {
    miniAppPlatformImpl.download(DOWNLOAD_ENDPOINT)
  }

  @Test(expected = UninitializedPropertyAccessException::class)
  fun shouldDownloadMiniAppNotInjectedAtStart() {
    miniAppPlatformImpl.downloadMiniApp
  }

  @Test
  fun shouldLazyInjectDownloadComponent() {
    WorkManagerTestInitHelper.initializeTestWorkManager(applicationContext)
    miniAppPlatformImpl.init(applicationContext).download(DOWNLOAD_ENDPOINT)
    assertThat(miniAppPlatformImpl.downloadMiniApp).isNotNull()
  }
}
