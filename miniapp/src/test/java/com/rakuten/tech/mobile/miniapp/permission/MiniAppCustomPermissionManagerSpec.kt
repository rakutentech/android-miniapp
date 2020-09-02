package com.rakuten.tech.mobile.miniapp.permission

import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.miniapp.MiniApp
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class MiniAppCustomPermissionManagerSpec {
    private lateinit var miniAppCustomPermissionManager: MiniAppCustomPermissionManager
    private var miniApp: MiniApp = mock()
    private val miniAppId: String = "miniAppId"
    private val miniAppCustomPermission = MiniAppCustomPermission(
        miniAppId,
        listOf(
            Pair(
                MiniAppCustomPermissionType.USER_NAME,
                MiniAppCustomPermissionResult.DENIED
            )
        )
    )

    @Before
    fun setUp() {
        doReturn(miniAppCustomPermission).whenever(miniApp).getCustomPermissions(miniAppId)
        miniAppCustomPermissionManager = MiniAppCustomPermissionManager(miniApp)
    }

    @Test
    fun `createJsonResponse should return json string based on supplied custom permissions`() {
        val miniAppId = "miniAppId"
        val suppliedPermissions = listOf(
            Pair(MiniAppCustomPermissionType.USER_NAME, "description")
        )
        val actual =
            miniAppCustomPermissionManager.createJsonResponse(miniAppId, suppliedPermissions)
        val expected =
            "{\"permissions\":[{\"name\":\"rakuten.miniapp.user.USER_NAME\",\"isGranted\":\"DENIED\"}]}"

        assertEquals(expected, actual)
    }

    @Test
    fun `filterPermissionsToSend should return correct values based on known custom permissions`() {
        val miniAppId = "miniAppId"
        val suppliedPermissions = listOf(
            Pair(MiniAppCustomPermissionType.USER_NAME, "description")
        )

        val actual =
            miniAppCustomPermissionManager.filterPermissionsToSend(miniAppId, suppliedPermissions)
        val expected = miniAppCustomPermission.pairValues

        assertEquals(expected, actual)
    }

    @Test
    fun `filterPermissionsToSend should return correct values based on unknown custom permissions`() {
        val miniAppId = "miniAppId"
        val suppliedPermissions = listOf(
            Pair(MiniAppCustomPermissionType.UNKNOWN, "description")
        )

        val actual =
            miniAppCustomPermissionManager.filterPermissionsToSend(miniAppId, suppliedPermissions)
        val expected = listOf(
            Pair(
                MiniAppCustomPermissionType.UNKNOWN,
                MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
            )
        )

        assertEquals(expected, actual)
    }
}
