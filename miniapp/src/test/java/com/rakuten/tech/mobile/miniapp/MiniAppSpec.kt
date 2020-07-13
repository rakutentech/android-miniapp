package com.rakuten.tech.mobile.miniapp

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.sdkutils.AppInfo
import org.amshove.kluent.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MiniAppSpec {

    @Test
    fun `should update configuration when get instance of MiniApp`() {
        val miniApp: MiniApp = mock()
        val miniAppSdkConfig: MiniAppSdkConfig = mock()
        val miniAppCompanion = MiniApp.Companion

        miniAppCompanion.instance = miniApp
        miniAppCompanion.instance(miniAppSdkConfig)

        verify(miniApp, times(1)).updateConfiguration(miniAppSdkConfig)
    }

    @Test
    fun `MiniApp should init without problem`() {
        AppInfo.instance = mock()
        MiniApp.init(
            context = getApplicationContext(),
            miniAppSdkConfig = MiniAppSdkConfig(
                baseUrl = TEST_URL_HTTPS_2,
                isTestMode = true,
                rasAppId = TEST_HA_ID_APP,
                subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
                hostAppVersionId = TEST_HA_ID_VERSION,
                hostAppInfo = TEST_HA_NAME
            )
        )
        MiniApp.instance shouldNotBe null
    }
}
