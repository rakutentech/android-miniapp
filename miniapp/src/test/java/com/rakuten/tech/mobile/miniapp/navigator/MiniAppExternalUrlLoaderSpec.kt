package com.rakuten.tech.mobile.miniapp.navigator

import com.nhaarman.mockitokotlin2.mock
import com.rakuten.tech.mobile.miniapp.MiniAppScheme
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_URL_HTTPS_1
import org.amshove.kluent.shouldBe
import org.junit.Test

class MiniAppExternalUrlLoaderSpec {
    private val miniAppScheme = MiniAppScheme(TEST_MA_ID)
    private val externalUrlLoader = MiniAppExternalUrlLoader(TEST_MA_ID, mock())

    @Test
    fun `should only override url when it is supported by mini app view`() {
        externalUrlLoader.shouldOverrideUrlLoading(TEST_URL_HTTPS_1) shouldBe false
        externalUrlLoader.shouldOverrideUrlLoading(miniAppScheme.miniAppCustomScheme) shouldBe true
        externalUrlLoader.shouldOverrideUrlLoading(miniAppScheme.miniAppCustomDomain) shouldBe true
    }
}
