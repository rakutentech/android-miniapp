package com.rakuten.tech.mobile.miniapp.storage

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.TEST_ID_MINIAPP
import com.rakuten.tech.mobile.miniapp.TEST_ID_MINIAPP_VERSION
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MiniAppStatusTest {
    private lateinit var miniAppStatus: MiniAppStatus

    @Before
    fun setup() {
        miniAppStatus = MiniAppStatus(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `isVersionDownloaded should be false if no app has been saved`() {
        miniAppStatus.isVersionDownloaded(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION) shouldBe false
    }

    @Test
    fun `setVersionDownloaded and isVersionDownloaded should correspond with each other`() {
        miniAppStatus.setVersionDownloaded(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION, true)
        miniAppStatus.isVersionDownloaded(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION) shouldBe true
    }
}
