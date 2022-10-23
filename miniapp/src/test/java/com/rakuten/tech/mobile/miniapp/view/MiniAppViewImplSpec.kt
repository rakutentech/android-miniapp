package com.rakuten.tech.mobile.miniapp.view

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.any
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify

@Suppress("LargeClass", "MaximumLineLength")
@RunWith(AndroidJUnit4::class)
class MiniAppViewImplSpec {

    private val coroutineDispatcher = CoroutineScope(Dispatchers.Unconfined)
    private lateinit var miniAppDefaultParams: MiniAppParameters.DefaultParams
    private lateinit var miniAppInfoParams: MiniAppParameters.InfoParams
    private lateinit var urlParams: MiniAppParameters.UrlParams
    private lateinit var miniAppViewHandler: MiniAppViewHandler
    private val testQuery = "testquery"
    val miniAppConfig = MiniAppConfig(
        mock(), mock(), mock(), mock(), ""
    )

    private fun withRealActivity(onReady: (Activity) -> Unit) {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            onReady(activity)
        }
    }

    private fun getMiniAppSdkConfig() = MiniAppSdkConfig(
        baseUrl = TEST_URL_HTTPS_2,
        isPreviewMode = true,
        requireSignatureVerification = true,
        rasProjectId = TEST_HA_ID_PROJECT,
        subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
        hostAppUserAgentInfo = TEST_HA_NAME
    )

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

    @Test(expected = MiniAppVerificationException::class)
    fun `miniAppViewHandler should throw MiniAppVerificationException due to not having the AndroidKeystore`() {
        withRealActivity { activity ->
            val miniAppSdkConfig = getMiniAppSdkConfig()
            MiniAppViewHandler(activity, miniAppSdkConfig)
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
}
