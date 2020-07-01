package com.rakuten.tech.mobile.miniapp.js

import android.content.pm.PackageManager
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

    @Test
    fun `should get the correct value of permission result`() {
        MiniAppPermission.getPermissionResult(PackageManager.PERMISSION_GRANTED) shouldBe
                MiniAppPermission.PermissionResult.GRANTED
        MiniAppPermission.getPermissionResult(PackageManager.PERMISSION_DENIED) shouldBe
                MiniAppPermission.PermissionResult.DENIED
    }
}
