package com.rakuten.tech.mobile.miniapp.permission

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

    @Test
    fun `should get unknown custom permission type when there is no permission type match`() {
        MiniAppCustomPermissionType.getValue("") shouldBe MiniAppCustomPermissionType.UNKNOWN
    }

    @Test
    fun `should keep the predefined type of custom permissions in enum`() {
        MiniAppCustomPermissionType.USER_NAME.type shouldBe "rakuten.miniapp.user.USER_NAME"
        MiniAppCustomPermissionType.PROFILE_PHOTO.type shouldBe "rakuten.miniapp.user.PROFILE_PHOTO"
        MiniAppCustomPermissionType.CONTACT_LIST.type shouldBe "rakuten.miniapp.user.CONTACT_LIST"
        MiniAppCustomPermissionType.UNKNOWN.type shouldBe "UNKNOWN"
    }

    @Test
    fun `should keep the predefined results of custom permissions in enum`() {
        MiniAppCustomPermissionResult.ALLOWED.name shouldBe "ALLOWED"
        MiniAppCustomPermissionResult.DENIED.name shouldBe "DENIED"
        MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE.name shouldBe
                "PERMISSION_NOT_AVAILABLE"
    }
}
