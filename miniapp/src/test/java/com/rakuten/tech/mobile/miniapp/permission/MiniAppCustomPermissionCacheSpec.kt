package com.rakuten.tech.mobile.miniapp.permission

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`

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
    fun `prepareAllPermissionsToStore should combine cached and supplied list properly with unknown permissions`() {
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

        val actual = miniAppCustomPermissionCache.prepareAllPermissionsToStore(cached, supplied)

        actual.size shouldBe 2
    }

    @Test
    fun `prepareAllPermissionsToStore should combine cached and supplied list properly with empty cached`() {
        val cached =
            emptyList<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        val supplied = listOf(
            Pair(
                MiniAppCustomPermissionType.CONTACT_LIST,
                MiniAppCustomPermissionResult.ALLOWED
            )
        )

        val actual = miniAppCustomPermissionCache.prepareAllPermissionsToStore(cached, supplied)

        actual.size shouldBe 1
    }
}
