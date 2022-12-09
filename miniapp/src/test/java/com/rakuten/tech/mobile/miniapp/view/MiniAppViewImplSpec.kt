package com.rakuten.tech.mobile.miniapp.view

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.BridgeCommon
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import com.rakuten.tech.mobile.miniapp.js.NativeEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.any
import org.mockito.Mockito.spy
import org.mockito.kotlin.*
import org.mockito.kotlin.mock

@Suppress("LargeClass", "MaximumLineLength")
@RunWith(AndroidJUnit4::class)
class MiniAppViewImplSpec {
    internal val webViewListener: WebViewListener = mock()

    private val coroutineDispatcher = CoroutineScope(Dispatchers.Unconfined)
    private lateinit var miniAppDefaultParams: MiniAppParameters.DefaultParams
    private lateinit var miniAppInfoParams: MiniAppParameters.InfoParams
    private lateinit var urlParams: MiniAppParameters.UrlParams
    private lateinit var miniAppViewHandler: MiniAppViewHandler
    private val testQuery = "testquery"
    private val miniAppBridge = spy(BridgeCommon().createDefaultMiniAppMessageBridge())
    internal val bridgeExecutor = spy(MiniAppBridgeExecutor(webViewListener))

    private lateinit var miniAppConfig: MiniAppConfig

    private fun withRealActivity(onReady: (Activity) -> Unit) {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            onReady(activity)
        }
    }

    @Before
    fun setUp() {
        withRealActivity { activity ->
            When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
            miniAppBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = mock(),
                downloadedManifestCache = mock(),
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )
        }
        miniAppConfig = MiniAppConfig(mock(), miniAppBridge, mock(), mock(), "")
    }

    private fun withMiniAppDefaultParams(onReady: (MiniAppViewImpl) -> Unit) {
        withRealActivity { activity ->
            miniAppDefaultParams = MiniAppParameters.DefaultParams(
                config = miniAppConfig,
                context = activity,
                miniAppId = TEST_MA_ID,
                miniAppVersion = "1,0"
            )
            miniAppViewHandler = mock()
            val miniAppViewImpl = spy(MiniAppViewImpl(miniAppDefaultParams) {
                miniAppViewHandler
            }) as MiniAppViewImpl
            miniAppViewImpl.scope = coroutineDispatcher
            onReady(miniAppViewImpl)
        }
    }

    private fun withMiniAppInfoParams(onReady: (MiniAppViewImpl) -> Unit) {
        withRealActivity { activity ->
            miniAppInfoParams = MiniAppParameters.InfoParams(
                config = miniAppConfig,
                context = activity,
                miniAppInfo = mock(),
            )
            miniAppViewHandler = mock()
            val miniAppViewImpl = spy(MiniAppViewImpl(miniAppInfoParams) {
                miniAppViewHandler
            }) as MiniAppViewImpl
            miniAppViewImpl.scope = coroutineDispatcher
            onReady(miniAppViewImpl)
        }
    }

    private fun withMiniAppUrlParams(onReady: (MiniAppViewImpl) -> Unit) {
        withRealActivity { activity ->
            urlParams = MiniAppParameters.UrlParams(
                config = miniAppConfig, context = activity, miniAppUrl = TEST_BASE_URL
            )
            miniAppViewHandler = mock()
            val miniAppViewImpl = spy(MiniAppViewImpl(urlParams) {
                miniAppViewHandler
            }) as MiniAppViewImpl
            miniAppViewImpl.scope = coroutineDispatcher
            onReady(miniAppViewImpl)
        }
    }

    @Test
    fun `should call onComplete when it is finished`() = runBlocking {
        withMiniAppDefaultParams { miniAppViewImpl ->
            val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit = spy { _, _ -> }
            miniAppViewImpl.load(queryParams = "", fromCache = false, onComplete)
            verify(onComplete).invoke(any(), any())
        }
    }

    @Test
    fun `miniAppParameters fromCache should be true whe the load fromCache parameter is true`() =
        runBlocking {
            withMiniAppDefaultParams { miniAppViewImpl ->
                val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit = spy { _, _ -> }
                miniAppViewImpl.load(queryParams = "", fromCache = true, onComplete)
                miniAppDefaultParams.fromCache shouldBeEqualTo true
            }
        }

    @Test
    fun `defaultParams fromCache should be false whe the load fromCache parameter is false`() =
        runBlocking {
            withMiniAppDefaultParams { miniAppViewImpl ->
                val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit = spy { _, _ -> }
                miniAppViewImpl.load(queryParams = "", fromCache = false, onComplete)
                miniAppDefaultParams.fromCache shouldBeEqualTo false
            }
        }

    @Test
    fun `defaultParams queryParams should be the same with load queryParams if it is not empty`() =
        runBlocking {
            withMiniAppDefaultParams { miniAppViewImpl ->
                val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit = spy { _, _ -> }
                miniAppViewImpl.load(queryParams = testQuery, fromCache = false, onComplete)
                miniAppDefaultParams.config.queryParams shouldBeEqualTo testQuery
            }
        }

    @Test
    fun `infoParams fromCache should be true whe the load fromCache parameter is true`() =
        runBlocking {
            withMiniAppInfoParams { miniAppViewImpl ->
                val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit = spy { _, _ -> }
                miniAppViewImpl.load(queryParams = "", fromCache = true, onComplete)
                miniAppInfoParams.fromCache shouldBeEqualTo true
            }
        }

    @Test
    fun `infoParams fromCache should be false whe the load fromCache parameter is false`() =
        runBlocking {
            withMiniAppInfoParams { miniAppViewImpl ->
                val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit = spy { _, _ -> }
                miniAppViewImpl.load(queryParams = "", fromCache = false, onComplete)
                miniAppInfoParams.fromCache shouldBeEqualTo false
            }
        }

    @Test
    fun `infoParams queryParams should be the same with load queryParams if it is not empty`() =
        runBlocking {
            withMiniAppInfoParams { miniAppViewImpl ->
                val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit = spy { _, _ -> }
                miniAppViewImpl.load(queryParams = testQuery, fromCache = false, onComplete)
                miniAppInfoParams.config.queryParams shouldBeEqualTo testQuery
            }
        }

    @Test
    fun `urlParams queryParams should be the same with load queryParams if it is not empty`() =
        runBlocking {
            withMiniAppUrlParams { miniAppViewImpl ->
                val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit = spy { _, _ -> }
                miniAppViewImpl.load(queryParams = testQuery, fromCache = false, onComplete)
                urlParams.config.queryParams shouldBeEqualTo testQuery
            }
        }

    @Test
    fun `urlParams queryParams should not be the same with load queryParams if it's empty`() =
        runBlocking {
            withMiniAppUrlParams { miniAppViewImpl ->
                val onComplete: (MiniAppDisplay?, MiniAppSdkException?) -> Unit = spy { _, _ -> }
                miniAppViewImpl.load(queryParams = "", fromCache = false, onComplete)
                urlParams.config.queryParams shouldBeEqualTo ""
            }
        }

    @Test
    fun `should do nothing when universalBridgeMessage is blank`() {
        withMiniAppDefaultParams { miniAppViewImpl ->
            val onFailed: () -> Unit = mock()
            miniAppViewImpl.sendJsonToMiniApp("", onFailed)
            verifyZeroInteractions(
                bridgeExecutor
            )
            verify(onFailed).invoke()
        }
    }

    @Test
    fun `should call miniAppMessageBridge dispatchNativeEvent when universalBridgeMessage is not blank`() {
        withMiniAppDefaultParams { miniAppViewImpl ->
            miniAppViewImpl.sendJsonToMiniApp(TEST_BODY_CONTENT) {
                // empty intended
            }
            verify(miniAppBridge).dispatchNativeEvent(
                NativeEventType.MINIAPP_RECEIVE_JSON_INFO,
                TEST_BODY_CONTENT
            )
        }
    }

    @Test
    fun `should call miniAppMessageBridge  using urLParams`() {
        withMiniAppUrlParams { miniAppViewImpl ->
            miniAppViewImpl.sendJsonToMiniApp(TEST_BODY_CONTENT) {
                // empty intended
            }
            verify(miniAppBridge).dispatchNativeEvent(
                NativeEventType.MINIAPP_RECEIVE_JSON_INFO,
                TEST_BODY_CONTENT
            )
        }
    }

    @Test
    fun `should call miniAppMessageBridge  using infoParams`() {
        withMiniAppInfoParams { miniAppViewImpl ->
            miniAppViewImpl.sendJsonToMiniApp(TEST_BODY_CONTENT) {
                // empty intended
            }
            verify(miniAppBridge).dispatchNativeEvent(
                NativeEventType.MINIAPP_RECEIVE_JSON_INFO,
                TEST_BODY_CONTENT
            )
        }
    }
}
