package com.rakuten.tech.mobile.miniapp.js

import org.amshove.kluent.shouldBe
import org.junit.Test

class MiniAppPermissionSpec {

    @Test
    fun `the permission request should be blank when cannot find the permission type`() {
        MiniAppPermission.getPermissionRequest(0) shouldBe ""
    }
}
