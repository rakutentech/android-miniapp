package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.TEST_HA_NAME
import com.rakuten.tech.mobile.miniapp.TEST_MA
import com.rakuten.tech.mobile.miniapp.TEST_MA_URL
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisplayerSpec {

    private lateinit var context: Context
    private val miniAppMessageBridge: MiniAppMessageBridge = mock()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `for a given base path createMiniAppDisplay returns an implementer of MiniAppDisplay`() {
        val obtainedDisplay = getMiniAppDisplay()
        val obtainedDisplayUrl = getMiniAppDisplayUrl()
        obtainedDisplay shouldBeInstanceOf RealMiniAppDisplay::class
        obtainedDisplayUrl shouldBeInstanceOf RealMiniAppDisplay::class
    }

    @Test
    fun `for a given base path createMiniAppDisplay returns an implementer of LifecycleObserver`() {
        val obtainedDisplay = getMiniAppDisplay()
        val obtainedDisplayUrl = getMiniAppDisplayUrl()
        obtainedDisplay shouldBeInstanceOf LifecycleObserver::class
        obtainedDisplayUrl shouldBeInstanceOf LifecycleObserver::class
    }

    private fun getMiniAppDisplay(): MiniAppDisplay =
        Displayer(context, TEST_HA_NAME).createMiniAppDisplay(
            basePath = context.filesDir.path,
            miniAppInfo = TEST_MA,
            miniAppMessageBridge = miniAppMessageBridge,
            miniAppNavigator = mock(),
            miniAppCustomPermissionCache = mock()
        )

    private fun getMiniAppDisplayUrl(): MiniAppDisplay =
        Displayer(context, TEST_HA_NAME).createMiniAppDisplay(
            appUrl = TEST_MA_URL,
            miniAppMessageBridge = miniAppMessageBridge,
            miniAppNavigator = mock(),
            miniAppCustomPermissionCache = mock()
        )
}
