package com.rakuten.tech.mobile.miniapp

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldNotBe
import org.junit.Test
import java.util.UUID

internal class MiniAppInfoSpec {

    @Test
    fun `forUrl should provide MiniAppInfo contains a generated UUID`() {
        MiniAppInfo.forUrl().apply {
            shouldBeInstanceOf(MiniAppInfo::class)
            id shouldNotBe ""
            id.length shouldBe UUID.randomUUID().toString().length
        }
    }
}
