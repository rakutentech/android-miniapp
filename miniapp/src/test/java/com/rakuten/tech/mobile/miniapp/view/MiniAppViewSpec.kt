package com.rakuten.tech.mobile.miniapp.view

import android.content.Context
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import org.amshove.kluent.*
import org.junit.Test
import org.mockito.kotlin.spy

class MiniAppViewSpec {
    private val context: Context = mock()
    private val miniAppConfig: MiniAppConfig = mock()
    private val miniAppSdkConfig: MiniAppSdkConfig = mock()
    private val miniAppInfo: MiniAppInfo = mock()
    private val miniAppView: MiniAppView = mock()
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
    fun `init will initialize the mini app view correctly`() {
        val instance = spy<MiniAppView.Companion>()
        When calling miniAppConfig.miniAppSdkConfig itReturns miniAppSdkConfig
        When calling instance.createMiniAppView(context, defaultParameters, miniAppSdkConfig) itReturns miniAppView
        When calling instance.createMiniAppView(context, infoParameters, miniAppSdkConfig) itReturns miniAppView
        When calling instance.createMiniAppView(context, urlParameters, miniAppSdkConfig) itReturns miniAppView
        instance.init(defaultParameters) shouldBe miniAppView
        instance.init(infoParameters) shouldBe miniAppView
        instance.init(urlParameters) shouldBe miniAppView
    }
}
