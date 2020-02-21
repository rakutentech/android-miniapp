package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class RealMiniAppDisplayTest {
    lateinit var context: Context
    lateinit var basePath: String

    @Before
    fun setup() {
        context = getApplicationContext()
        basePath = context.filesDir.path
    }

    @Test
    fun `for a given app id, RealMiniAppDisplay creates corresponding view for the caller`() =
        runBlockingTest {
            val realDisplay = getRealMiniAppDisplay()
            realDisplay.url shouldContain TEST_MA_ID
            realDisplay.url shouldEndWith "index.html"
        }

    @Test
    fun `for a given basePath, getMiniAppView should not return WebView to the caller`() =
        runBlockingTest {
            val realDisplay = getRealMiniAppDisplay()
            realDisplay.getMiniAppView() shouldNotHaveTheSameClassAs WebView::class
        }

    @Test
    fun `when getLoadUrl is called then a formed path contains app id`() {
        val realDisplay = getRealMiniAppDisplay()
        realDisplay.getLoadUrl() shouldEqual
                "https://$TEST_MA_ID.miniapps.androidplatform.net/miniapp/index.html"
    }

    @Test
    fun `when MiniAppDisplay is created then LayoutParams use MATCH_PARENT for dimensions`() {
        val realDisplay = getRealMiniAppDisplay()
        realDisplay.layoutParams.width shouldEqualTo ViewGroup.LayoutParams.MATCH_PARENT
        realDisplay.layoutParams.height shouldEqualTo ViewGroup.LayoutParams.MATCH_PARENT
    }

    @Test
    fun `when MiniAppDisplay is created then javascript should be enabled`() {
        val miniAppWindow = getRealMiniAppDisplay()
        assertTrue { miniAppWindow.settings.javaScriptEnabled }
    }

    @Test
    fun `when MiniAppDisplay is created then allowUniversalAccessFromFileURLs should be enabled`() {
        val miniAppWindow = getRealMiniAppDisplay()
        assertTrue { miniAppWindow.settings.allowUniversalAccessFromFileURLs }
    }

    @Test
    fun `each mini app should have different domain`() {
        val realDisplayForMiniapp1 = RealMiniAppDisplay(context, basePath, "app-id-1")
        val realDisplayForMiniapp2 = RealMiniAppDisplay(context, basePath, "app-id-2")
        realDisplayForMiniapp1.url shouldNotBeEqualTo realDisplayForMiniapp2.url
    }

    private fun getRealMiniAppDisplay() = RealMiniAppDisplay(
        context,
        basePath = basePath,
        appId = TEST_MA_ID
    )
}
