package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.TEST_BASE_PATH
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MiniAppWindowTest {

    lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `when getLoadUrl is called then a formed path is returned from basePath for loading`() {
        val miniAppWindow = MiniAppWindow(context, basePath = TEST_BASE_PATH)
        miniAppWindow.getLoadUrl() shouldBeEqualTo "file://dummy/index.html"
    }

    @Test
    fun `when MiniAppWindow is created then LayoutParams should use MATCH_PARENT for dimensions`() {
        val miniAppWindow = MiniAppWindow(context, basePath = TEST_BASE_PATH)
        miniAppWindow.layoutParams.width shouldEqualTo ViewGroup.LayoutParams.MATCH_PARENT
        miniAppWindow.layoutParams.height shouldEqualTo ViewGroup.LayoutParams.MATCH_PARENT
    }

    @Test
    fun `when MiniAppWindow is created then required websettings should be enabled`() {
        val miniAppWindow = MiniAppWindow(context, basePath = TEST_BASE_PATH)
        assertTrue { miniAppWindow.settings.javaScriptEnabled }
        assertTrue { miniAppWindow.settings.allowUniversalAccessFromFileURLs }
    }

    @Test
    fun `when MiniAppWindow is created then then WebViewClient is set to MiniAppWebViewClient`() {
        val miniAppWindow = MiniAppWindow(context, basePath = TEST_BASE_PATH)
        (miniAppWindow as WebView).webViewClient shouldBeInstanceOf MiniAppWebViewClient::class
    }
}
