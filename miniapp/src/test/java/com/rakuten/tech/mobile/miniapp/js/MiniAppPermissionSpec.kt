package com.rakuten.tech.mobile.miniapp.js

import com.rakuten.tech.mobile.miniapp.permission.MiniAppPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppPermissionType
import org.amshove.kluent.shouldBe
import org.junit.Test

class MiniAppPermissionSpec {

    @Test
    fun `should get default permission type when there is no permission type match`() {
        MiniAppPermissionType.getValue("") shouldBe MiniAppPermissionType.UNKNOWN
    }

    @Test
    fun `should get the correct value of permission result`() {
        MiniAppPermissionResult.getValue(true) shouldBe MiniAppPermissionResult.ALLOWED
        MiniAppPermissionResult.getValue(false) shouldBe MiniAppPermissionResult.DENIED
    }
}
