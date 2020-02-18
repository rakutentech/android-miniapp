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
class MiniAppSharedPreferencesTest {
    private lateinit var prefs: MiniAppSharedPreferences

    @Before
    fun setup() {
        prefs = MiniAppSharedPreferences(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `isAppExisted should be false if no app has been saved`() {
        prefs.isAppExisted(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION) shouldBe false
    }

    @Test
    fun `setAppExsited and isAppExisted should correspond with each other`() {
        prefs.setAppExisted(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION, true)
        prefs.isAppExisted(TEST_ID_MINIAPP, TEST_ID_MINIAPP_VERSION) shouldBe true
    }
}
