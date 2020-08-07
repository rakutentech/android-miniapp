package com.rakuten.tech.mobile.miniapp.display

import android.app.Activity
import android.content.Context
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.TEST_HA_NAME
import com.rakuten.tech.mobile.miniapp.TEST_MA
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RealMiniAppDisplayTest {
    private lateinit var context: Context
    private lateinit var basePath: String
    private lateinit var realDisplay: RealMiniAppDisplay
    private val miniAppMessageBridge: MiniAppMessageBridge = mock()

    @Before
    fun setup() {
        context = getApplicationContext()
        basePath = context.filesDir.path
        realDisplay = RealMiniAppDisplay(
            context,
            basePath = basePath,
            miniAppInfo = TEST_MA,
            miniAppMessageBridge = miniAppMessageBridge,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }

    @Test
    fun `when destroyView be called then the miniAppWebView should be disposed`() = runBlockingTest {
        val miniAppWebView: MiniAppWebView = mock()
        realDisplay.miniAppWebView = miniAppWebView
        realDisplay.destroyView()

        verify(miniAppWebView, times(1)).destroyView()
        realDisplay.miniAppWebView shouldBe null
    }

    @Test
    fun `should provide the exact context to MiniAppWebView`() = runBlockingTest {
        val displayer = Mockito.spy(realDisplay)
        val testContext = displayer.context
        When calling displayer.isContextValid(testContext) itReturns true
        val miniAppWebView = displayer.getMiniAppView(testContext) as MiniAppWebView

        miniAppWebView.context shouldBe testContext
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when the context provider is not activity context`() =
        runBlockingTest { realDisplay.getMiniAppView(context) }

    @Test
    fun `should create MiniAppWebView when provide activity context`() = runBlockingTest {
        val miniAppWebView: MiniAppWebView = mock()
        realDisplay.miniAppWebView = miniAppWebView
        realDisplay.getMiniAppView(Activity()) shouldBe miniAppWebView
    }

    @Test
    fun `for a given basePath, getMiniAppView should not return WebView to the caller`() =
        runBlockingTest {
            val displayer = Mockito.spy(realDisplay)
            When calling displayer.isContextValid(context) itReturns true
            displayer.getMiniAppView(context) shouldNotHaveTheSameClassAs WebView::class
        }

    @Test
    fun `should not navigate when MiniAppWebView is null`() {
        realDisplay.navigateBackward() shouldBe false
        realDisplay.navigateForward() shouldBe false
    }

    @Test
    fun `should not navigate when MiniAppWebView cannot do navigation`() = runBlockingTest {
        val displayer = Mockito.spy(realDisplay)
        When calling displayer.isContextValid(context) itReturns true
        displayer.getMiniAppView(context)

        displayer.navigateBackward() shouldBe false
        displayer.navigateForward() shouldBe false
    }

    @Test
    fun `should be able to do navigation when possible`() = runBlockingTest {
        val displayer = Mockito.spy(realDisplay)
        When calling displayer.isContextValid(context) itReturns true
        val miniAppWebView: MiniAppWebView = mock()
        When calling (miniAppWebView as WebView).canGoBack() itReturns true
        When calling miniAppWebView.canGoForward() itReturns true
        displayer.miniAppWebView = miniAppWebView

        displayer.navigateBackward() shouldBe true
        displayer.navigateForward() shouldBe true
    }
}
