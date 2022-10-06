package com.rakuten.tech.mobile.miniapp.view

import android.content.Context
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.mock
import org.junit.Test
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class MiniAppViewImplSpec {
    private val miniAppViewHandler: MiniAppViewHandler = mock()
    private val context: Context = mock()
    private val miniAppConfig: MiniAppConfig = mock()
    private val miniAppInfo: MiniAppInfo = mock()
    private val defaultParameters = MiniAppParameters.DefaultParams(
        context,
        miniAppConfig,
        TEST_MA_ID,
        TEST_MA_VERSION_ID,
        false
    )
    private val infoParameters = MiniAppParameters.InfoParams(
        context,
        miniAppConfig,
        miniAppInfo,
        false
    )
    private val urlParameters = MiniAppParameters.UrlParams(
        context,
        miniAppConfig,
        ""
    )

    @Test
    fun `load will call the correct createMiniAppView for defaultParameters`() = runBlocking {
        val miniAppViewImpl = spy(MiniAppViewImpl(defaultParameters) { miniAppViewHandler })
        When calling miniAppViewImpl.scope itReturns this
        val onComplete: (MiniAppDisplay) -> Unit = mock()
        miniAppViewImpl.load("", onComplete)
        delay(100)
        verify(onComplete).invoke(
            miniAppViewHandler.createMiniAppView(
                TEST_MA_ID,
                miniAppConfig,
                false
            )
        )
    }

    @Test
    fun `load will call the correct createMiniAppView for urlParameters`() = runBlockingTest {
        val miniAppViewImpl = spy(MiniAppViewImpl(urlParameters) { miniAppViewHandler })
        When calling miniAppViewImpl.scope itReturns this
        val onComplete: (MiniAppDisplay) -> Unit = mock()
        miniAppViewImpl.load("", onComplete)
        verify(onComplete).invoke(
            miniAppViewHandler.createMiniAppViewWithUrl(
                "",
                miniAppConfig
            )
        )
    }
}
