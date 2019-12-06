package com.rakuten.mobile.miniapp.download

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.mobile.miniapp.core.CoreImpl
import com.rakuten.mobile.miniapp.download.work.scheduler.DownloadScheduler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations.initMocks

/**
 * Test class for DownloadMiniAppImpl class.
 */
@RunWith(AndroidJUnit4::class)
class DownloadMiniAppImplTest : DownloadBaseTest() {

  @Mock
  lateinit var mockDownloadScheduler: DownloadScheduler
  lateinit var downloadMiniApp: DownloadMiniAppImpl

  @Before
  fun setup() {
    initMocks(this)
    CoreImpl.context = getApplicationContext()
    downloadMiniApp = DownloadMiniAppImpl()
  }

  @Test
  fun shouldScheduleDownloadMiniApp() {
    downloadMiniApp.downloadScheduler = mockDownloadScheduler
    downloadMiniApp.downloadMiniApp("")
    Mockito.verify(downloadMiniApp.downloadScheduler).scheduleDownload("")
  }
}
