package com.rakuten.tech.mobile.miniapp.view

import android.content.Context
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.mock

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
}
