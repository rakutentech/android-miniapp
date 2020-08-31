package com.rakuten.tech.mobile.miniapp.permission

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

@Suppress("LongMethod")
class MiniAppCustomPermissionCacheSpec {
    private lateinit var miniAppCustomPermissionCache: MiniAppCustomPermissionCache
    private val mockSharedPrefs: SharedPreferences = mock()
    private val mockEditor: SharedPreferences.Editor = mock()
    private val mockContext: Context = mock()

    @Before
    fun setUp() {
        `when`(mockSharedPrefs.edit()).thenReturn(mockEditor)
        `when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPrefs)
        miniAppCustomPermissionCache =
            MiniAppCustomPermissionCache(
                mockContext
            )
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
    }

    @Test
    fun `readPermissions should return null when preference does not contain miniAppId`() {
        val miniAppId = ""
        val actual = miniAppCustomPermissionCache.readPermissions(miniAppId)

        actual shouldBe null
    }

    @Test
    fun `storePermissions should put string as grantResult in preferences`() {
        val miniAppCustomPermission = createDummyPermissions()
        val json: String = Gson().toJson(miniAppCustomPermission)
        miniAppCustomPermissionCache.storePermissions(miniAppCustomPermission)

        verify(mockEditor).putString(miniAppCustomPermission.miniAppId, json)
    }

    @Test
    fun `storePermissions should return string`() {
        val miniAppCustomPermission = createDummyPermissions()
        val actual = miniAppCustomPermissionCache.storePermissions(miniAppCustomPermission)

        actual shouldBeInstanceOf String::class.java
    }

    @Test
    fun `combineAllPermissionsToStore should combine cached and supplied list properly with known permissions`() {
        val cached = listOf(
            Pair(
                MiniAppCustomPermissionType.USER_NAME,
                MiniAppCustomPermissionResult.DENIED
            )
        )

        val supplied = listOf(
            Pair(
                MiniAppCustomPermissionType.CONTACT_LIST,
                MiniAppCustomPermissionResult.DENIED
            )
        )

        val actual = miniAppCustomPermissionCache.combineAllPermissionsToStore(cached, supplied)

        actual.size shouldBe 2
    }

    @Test
    fun `combineAllPermissionsToStore should combine cached and supplied list properly with unknown permissions`() {
        val cached = listOf(
            Pair(
                MiniAppCustomPermissionType.UNKNOWN,
                MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
            )
        )

        val supplied = listOf(
            Pair(
                MiniAppCustomPermissionType.PROFILE_PHOTO,
                MiniAppCustomPermissionResult.ALLOWED
            )
        )

        val actual = miniAppCustomPermissionCache.combineAllPermissionsToStore(cached, supplied)

        actual.size shouldBe 2
    }

    @Test
    fun `combineAllPermissionsToStore should combine cached and supplied list properly with empty cached`() {
        val cached =
            emptyList<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        val supplied = listOf(
            Pair(
                MiniAppCustomPermissionType.CONTACT_LIST,
                MiniAppCustomPermissionResult.ALLOWED
            )
        )

        val actual = miniAppCustomPermissionCache.combineAllPermissionsToStore(cached, supplied)

        actual.size shouldBe 1
    }

    @Test
    fun `toJsonResponse should return expected JSON string with given inputs`() {
        val inputs = MiniAppCustomPermission(
            "miniAppId",
            listOf(
                Pair(
                    MiniAppCustomPermissionType.USER_NAME,
                    MiniAppCustomPermissionResult.DENIED
                ),
                Pair(
                    MiniAppCustomPermissionType.UNKNOWN,
                    MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
                )
            )
        )
        val actual =
            miniAppCustomPermissionCache.toJsonResponse(inputs.pairValues)
        val expected =
            "{\"permissions\":[{\"name\":\"rakuten.miniapp.user.USER_NAME\",\"isGranted\":\"DENIED\"}" +
                    ",{\"name\":\"UNKNOWN\",\"isGranted\":\"PERMISSION_NOT_AVAILABLE\"}]}"

        assertEquals(expected, actual)
    }

    private fun createDummyPermissions(): MiniAppCustomPermission {
        val miniAppId = "dummyMiniAppId"
        return MiniAppCustomPermission(
            miniAppId,
            listOf(
                Pair(
                    MiniAppCustomPermissionType.USER_NAME,
                    MiniAppCustomPermissionResult.DENIED
                )
            )
        )
    }
}
