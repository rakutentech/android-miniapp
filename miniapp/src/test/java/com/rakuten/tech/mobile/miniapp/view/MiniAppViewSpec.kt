package com.rakuten.tech.mobile.miniapp.view

import android.content.Context
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import com.rakuten.tech.mobile.miniapp.TEST_URL_PARAMS
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import org.amshove.kluent.*
import org.junit.Test
import org.mockito.kotlin.spy

open class MiniAppViewSpec {
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
    fun `MiniAppConfig should be initialized`() {
        val miniAppConfig = MiniAppConfig(
            mock(),
            mock(),
            mock(),
            mock(),
            ""
        )

        miniAppConfig.shouldBeInstanceOf<MiniAppConfig>()
        miniAppConfig.shouldNotBeNull()

        miniAppConfig.miniAppSdkConfig.shouldBeInstanceOf<MiniAppSdkConfig>()
        miniAppConfig.miniAppSdkConfig.shouldNotBeNull()

        miniAppConfig.miniAppMessageBridge.shouldBeInstanceOf<MiniAppMessageBridge>()
        miniAppConfig.miniAppMessageBridge.shouldNotBeNull()

        miniAppConfig.miniAppNavigator.shouldBeInstanceOf<MiniAppNavigator>()
        miniAppConfig.miniAppNavigator.shouldNotBeNull()

        miniAppConfig.miniAppFileChooser.shouldBeInstanceOf<MiniAppFileChooser>()
        miniAppConfig.miniAppFileChooser.shouldNotBeNull()

        miniAppConfig.queryParams.shouldBeInstanceOf<String>()
        miniAppConfig.queryParams.shouldNotBeNull()
        miniAppConfig.queryParams.shouldBeEmpty()

        miniAppConfig.queryParams = TEST_URL_PARAMS
        miniAppConfig.queryParams.shouldBeEqualTo(TEST_URL_PARAMS)
    }

    @Test
    fun `MiniAppConfig queryParams defaultValue should be empty`() {
        val miniAppConfig = MiniAppConfig(
            mock(),
            mock(),
            mock(),
            mock(),
        )

        miniAppConfig.queryParams.shouldBeEmpty()
    }

    @Test
    fun `MiniAppConfig miniAppNavigator and miniAppFileChooser should be null`() {
        val miniAppConfig = MiniAppConfig(
            mock(),
            mock(),
            null,
            null,
            "",
        )

        miniAppConfig.shouldBeInstanceOf<MiniAppConfig>()
        miniAppConfig.shouldNotBeNull()

        miniAppConfig.miniAppSdkConfig.shouldBeInstanceOf<MiniAppSdkConfig>()
        miniAppConfig.miniAppSdkConfig.shouldNotBeNull()

        miniAppConfig.miniAppMessageBridge.shouldBeInstanceOf<MiniAppMessageBridge>()
        miniAppConfig.miniAppMessageBridge.shouldNotBeNull()

        miniAppConfig.miniAppNavigator.shouldBeInstanceOf<MiniAppNavigator?>()
        miniAppConfig.miniAppNavigator.shouldBeNull()

        miniAppConfig.miniAppFileChooser.shouldBeInstanceOf<MiniAppFileChooser?>()
        miniAppConfig.miniAppFileChooser.shouldBeNull()

        miniAppConfig.queryParams.shouldBeInstanceOf<String>()
        miniAppConfig.queryParams.shouldNotBeNull()
        miniAppConfig.queryParams.shouldBeEmpty()

        miniAppConfig.queryParams = TEST_URL_PARAMS
        miniAppConfig.queryParams.shouldBeEqualTo(TEST_URL_PARAMS)
    }

    @Test
    fun `init with defaultParameters should return miniAppView`() {
        val instance = getMockMiniAppView()
        instance.init(defaultParameters) shouldBe miniAppView
    }

    @Test
    fun `init with infoParameters should return miniAppView`() {
        val instance = getMockMiniAppView()
        instance.init(infoParameters) shouldBe miniAppView
    }

    @Test
    fun `init with urlParameters should return miniAppView`() {
        val instance = getMockMiniAppView()
        instance.init(urlParameters) shouldBe miniAppView
    }

    private fun getMockMiniAppView(): MiniAppView.Companion {
        val instance = spy<MiniAppView.Companion>()
        When calling miniAppConfig.miniAppSdkConfig itReturns miniAppSdkConfig
        When calling instance.createMiniAppView(
            context,
            defaultParameters,
            miniAppSdkConfig,
            false
        ) itReturns miniAppView
        When calling instance.createMiniAppView(
            context,
            infoParameters,
            miniAppSdkConfig,
            false
        ) itReturns miniAppView
        When calling instance.createMiniAppView(
            context,
            urlParameters,
            miniAppSdkConfig,
            false
        ) itReturns miniAppView
        return instance
    }
}
