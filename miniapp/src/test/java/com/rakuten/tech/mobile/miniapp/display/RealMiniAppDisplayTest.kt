package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.TEST_BASE_PATH
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class RealMiniAppDisplayTest {

    lateinit var context: Context

    @Before
    fun setup() {
        context = getApplicationContext()
    }

    @Test
    fun `for a given basePath, obtainView returns corresponding view to the caller`() =
        runBlockingTest {
            val display = Displayer(context).createMiniAppDisplay(basePath = TEST_BASE_PATH)
            val miniAppView = display.getMiniAppView()
            if (miniAppView is WebView) {
                miniAppView.url shouldContain TEST_BASE_PATH
            }
        }

    @Test
    fun `for a given basePath, obtainView should not return WebView to the caller`() =
        runBlockingTest {
            val display = Displayer(context).createMiniAppDisplay(basePath = TEST_BASE_PATH)
            val miniAppView = display.getMiniAppView()
            miniAppView shouldNotHaveTheSameClassAs WebView::class
        }

    @Test
    fun `when getLoadUrl is called then a formed path is returned from basePath for loading`() {
        val miniAppWindow = RealMiniAppDisplay(context, basePath = TEST_BASE_PATH)
        miniAppWindow.getLoadUrl() shouldBeEqualTo "file://dummy/index.html"
    }

    @Test
    fun `when MiniAppDisplay is created then LayoutParams use MATCH_PARENT for dimensions`() {
        val miniAppWindow = RealMiniAppDisplay(context, basePath = TEST_BASE_PATH)
        miniAppWindow.layoutParams.width shouldEqualTo ViewGroup.LayoutParams.MATCH_PARENT
        miniAppWindow.layoutParams.height shouldEqualTo ViewGroup.LayoutParams.MATCH_PARENT
    }

    @Test
    fun `when MiniAppDisplay is created then required websettings should be enabled`() {
        val miniAppWindow = RealMiniAppDisplay(context, basePath = TEST_BASE_PATH)
        assertTrue { miniAppWindow.settings.javaScriptEnabled }
        assertTrue { miniAppWindow.settings.allowUniversalAccessFromFileURLs }
    }

    @Test
    fun `when MiniAppDisplay is created then then WebViewClient is set to MiniAppWebViewClient`() {
        val miniAppWindow = RealMiniAppDisplay(context, basePath = TEST_BASE_PATH)
        (miniAppWindow as WebView).webViewClient shouldBeInstanceOf MiniAppWebViewClient::class
    }
}
