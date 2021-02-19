package com.rakuten.tech.mobile.miniapp.display

import android.app.Activity
import android.content.Context
import android.webkit.WebView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_HA_ID_PROJECT
import com.rakuten.tech.mobile.miniapp.TEST_HA_NAME
import com.rakuten.tech.mobile.miniapp.TEST_MA
import com.rakuten.tech.mobile.miniapp.analytics.Actype
import com.rakuten.tech.mobile.miniapp.analytics.Etype
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.util.UUID

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Suppress("LongMethod")
class RealMiniAppDisplaySpec {
    private lateinit var context: Context
    private lateinit var basePath: String
    private lateinit var realDisplay: RealMiniAppDisplay
    private lateinit var miniAppAnalytics: MiniAppAnalytics
    private val miniAppMessageBridge: MiniAppMessageBridge = mock()

    @Before
    fun setup() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            context = activity
            basePath = context.filesDir.path
            realDisplay = RealMiniAppDisplay(
                basePath = basePath,
                miniAppInfo = TEST_MA,
                miniAppMessageBridge = miniAppMessageBridge,
                miniAppNavigator = mock(),
                hostAppUserAgentInfo = TEST_HA_NAME,
                miniAppCustomPermissionCache = mock(),
                queryParams = TEST_URL_PARAMS
            )

            MiniAppAnalytics.init(TEST_HA_ID_PROJECT)
            miniAppAnalytics = Mockito.spy(MiniAppAnalytics.instance!!)
        }
    }

    @Test
    fun `should pass MiniAppInfo forUrl through the constructor`() {
        val realDisplay = RealMiniAppDisplay(
            appUrl = "",
            miniAppMessageBridge = miniAppMessageBridge,
            miniAppNavigator = mock(),
            hostAppUserAgentInfo = TEST_HA_NAME,
            miniAppCustomPermissionCache = mock(),
            queryParams = TEST_URL_PARAMS
        )

        realDisplay.miniAppInfo.apply {
            id shouldNotBe ""
            id.length shouldBe UUID.randomUUID().toString().length
            displayName shouldBe ""
            icon shouldBe ""
            version.versionId shouldBe ""
            version.versionTag shouldBe ""
        }
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
        val miniAppWebView = displayer.getMiniAppView(context) as MiniAppWebView

        miniAppWebView.context shouldBe context
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when the context provider is not activity context`() =
        runBlockingTest { realDisplay.getMiniAppView(getApplicationContext()) }

    @Test
    fun `should create MiniAppWebView when provide activity context`() = runBlockingTest {
        val miniAppWebView: MiniAppWebView = mock()
        realDisplay.miniAppWebView = miniAppWebView
        realDisplay.getMiniAppView(Activity()) shouldBe miniAppWebView
    }

    @Test
    fun `should send analytics when open miniapp view`() = runBlockingTest {
        val displayer = Mockito.spy(realDisplay)
        When calling displayer.getMiniAppAnalytics() itReturns miniAppAnalytics
        displayer.getMiniAppView(context)

        verify(miniAppAnalytics, times(0)).sendAnalytics(
            eType = Etype.APPEAR,
            actype = Actype.HOST_LAUNCH,
            miniAppInfo = null
        )
        verify(miniAppAnalytics).sendAnalytics(
            eType = Etype.CLICK,
            actype = Actype.OPEN,
            miniAppInfo = TEST_MA
        )
    }

    @Test
    fun `should send analytics when close miniapp view`() = runBlockingTest {
        val displayer = Mockito.spy(realDisplay)
        When calling displayer.getMiniAppAnalytics() itReturns miniAppAnalytics
        displayer.destroyView()

        verify(miniAppAnalytics).sendAnalytics(eType = Etype.CLICK, actype = Actype.CLOSE, miniAppInfo = TEST_MA)
    }

    @Test
    fun `for a given basePath, getMiniAppView should not return WebView to the caller`() =
        runBlockingTest {
            val displayer = Mockito.spy(realDisplay)
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
        displayer.getMiniAppView(context)

        displayer.navigateBackward() shouldBe false
        displayer.navigateForward() shouldBe false
    }

    @Test
    fun `should be able to do navigation when possible`() = runBlockingTest {
        val displayer = Mockito.spy(realDisplay)
        val miniAppWebView: MiniAppWebView = mock()
        When calling (miniAppWebView as WebView).canGoBack() itReturns true
        When calling miniAppWebView.canGoForward() itReturns true
        displayer.miniAppWebView = miniAppWebView

        displayer.navigateBackward() shouldBe true
        displayer.navigateForward() shouldBe true
    }
}
