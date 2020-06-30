package com.rakuten.tech.mobile.miniapp.js

import org.amshove.kluent.shouldBe
import org.junit.Test

class MiniAppPermissionSpec {

    @Test
    fun `the permission request should be empty array when cannot find the permission type`() {
        MiniAppPermission.getPermissionRequest("").size shouldBe 0
    }

    @Test
    fun `the request code should be 0 when cannot find the permission type`() {
        MiniAppPermission.getRequestCode("") shouldBe 0
    }
}
