package com.rakuten.tech.mobile.miniapp.permission

import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.miniapp.js.CustomPermissionObj
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class MiniAppCustomPermissionManagerSpec {
    private lateinit var miniAppCustomPermissionManager: MiniAppCustomPermissionManager
    private var miniAppCustomPermissionCache: MiniAppCustomPermissionCache = mock()
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
        doReturn(miniAppCustomPermission).whenever(miniAppCustomPermissionCache)
            .readPermissions(miniAppId)
        miniAppCustomPermissionManager = MiniAppCustomPermissionManager()
    }

    @Test
    fun `preparePermissionsWithDescription should return a list of pair with name and description`() {
        val description = "dummy description"
        val customPermissionObj = CustomPermissionObj(
            "rakuten.miniapp.user.USER_NAME",
            description
        )
        val actual = miniAppCustomPermissionManager.preparePermissionsWithDescription(
            arrayListOf(customPermissionObj)
        )
        val expected = listOf(Pair(MiniAppCustomPermissionType.USER_NAME, description))
        assertEquals(expected, actual)
    }

    @Test
    fun `createJsonResponse should return json string based on supplied custom permissions`() {
        val miniAppId = "miniAppId"
        val suppliedPermissions = listOf(
            Pair(MiniAppCustomPermissionType.USER_NAME, "dummy description")
        )
        val actual = miniAppCustomPermissionManager.createJsonResponse(
            miniAppCustomPermissionCache,
            miniAppId,
            suppliedPermissions
        )
        val expected =
            "{\"permissions\":[{\"name\":\"rakuten.miniapp.user.USER_NAME\",\"status\":\"DENIED\"}]}"

        assertEquals(expected, actual)
    }

    @Test
    fun `filterPermissionsToSend should return correct values based on known custom permissions`() {
        val miniAppId = "miniAppId"
        val suppliedPermissions = listOf(
            Pair(MiniAppCustomPermissionType.USER_NAME, "dummy description")
        )

        val actual = miniAppCustomPermissionManager.filterPermissionsToSend(
            miniAppCustomPermissionCache,
            miniAppId,
            suppliedPermissions
        )
        val expected = miniAppCustomPermission.pairValues

        assertEquals(expected, actual)
    }

    @Test
    fun `filterPermissionsToSend should return correct values based on unknown custom permissions`() {
        val miniAppId = "miniAppId"
        val suppliedPermissions = listOf(
            Pair(MiniAppCustomPermissionType.UNKNOWN, "dummy description")
        )

        val actual = miniAppCustomPermissionManager.filterPermissionsToSend(
            miniAppCustomPermissionCache,
            miniAppId,
            suppliedPermissions
        )
        val expected = listOf(
            Pair(
                MiniAppCustomPermissionType.UNKNOWN,
                MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
            )
        )

        assertEquals(expected, actual)
    }
}
