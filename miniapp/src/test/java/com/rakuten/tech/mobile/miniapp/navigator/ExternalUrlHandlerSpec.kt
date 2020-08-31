package com.rakuten.tech.mobile.miniapp.navigator

import org.amshove.kluent.shouldNotBe
import org.junit.Test

class ExternalUrlHandlerSpec {
    private val externalResultHandler = MiniAppExternalUrlLoader()

    @Test
    fun `should not get the reference of miniAppUrlSchemes`() {
        externalResultHandler.getMiniAppUrlSchemes() shouldNotBe externalResultHandler.miniAppUrlSchemes
    }
}
