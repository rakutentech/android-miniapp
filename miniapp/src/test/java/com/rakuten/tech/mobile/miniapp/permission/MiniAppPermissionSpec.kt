package com.rakuten.tech.mobile.miniapp.permission

import org.amshove.kluent.shouldBe
import org.junit.Test

class MiniAppPermissionSpec {

    @Test
    fun `should get the correct value of permission result`() {
        MiniAppDevicePermissionResult.getValue(true) shouldBe MiniAppDevicePermissionResult.ALLOWED
        MiniAppDevicePermissionResult.getValue(false) shouldBe MiniAppDevicePermissionResult.DENIED
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
        MiniAppCustomPermissionType.ACCESS_TOKEN.type shouldBe "rakuten.miniapp.user.ACCESS_TOKEN"
        MiniAppCustomPermissionType.SEND_MESSAGE.type shouldBe "rakuten.miniapp.user.action.SEND_MESSAGE"
        MiniAppCustomPermissionType.LOCATION.type shouldBe "rakuten.miniapp.device.LOCATION"
        MiniAppCustomPermissionType.POINTS.type shouldBe "rakuten.miniapp.user.POINTS"
        MiniAppCustomPermissionType.FILE_DOWNLOAD.type shouldBe "rakuten.miniapp.device.FILE_DOWNLOAD"
        MiniAppCustomPermissionType.UNKNOWN.type shouldBe "UNKNOWN"
    }

    @Test
    fun `should keep the predefined type of device permissions in enum`() {
        MiniAppDevicePermissionType.LOCATION.type shouldBe "location"
        MiniAppDevicePermissionType.CAMERA.type shouldBe "camera"
        MiniAppDevicePermissionType.UNKNOWN.type shouldBe "unknown"
    }

    @Test
    fun `should get unknown device permission type when there is no permission type match`() {
        MiniAppDevicePermissionType.getValue("") shouldBe MiniAppDevicePermissionType.UNKNOWN
    }

    @Test
    fun `should get known device permission type when there is permission type match`() {
        MiniAppDevicePermissionType.getValue(MiniAppDevicePermissionType.LOCATION.type) shouldBe
                MiniAppDevicePermissionType.LOCATION
    }

    @Test
    fun `should keep the predefined results of custom permissions in enum`() {
        MiniAppCustomPermissionResult.ALLOWED.name shouldBe "ALLOWED"
        MiniAppCustomPermissionResult.DENIED.name shouldBe "DENIED"
        MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE.name shouldBe
                "PERMISSION_NOT_AVAILABLE"
    }
}
