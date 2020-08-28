package com.rakuten.tech.mobile.miniapp.js

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

class MiniAppCustomPermissionCacheSpec {
    private lateinit var miniAppCustomPermissionCache: MiniAppCustomPermissionCache
    private val mockSharedPrefs: SharedPreferences = mock()
    private val mockEditor: SharedPreferences.Editor = mock()
    private val mockContext: Context = mock()

    @Before
    fun setUp() {
        Mockito.`when`(mockSharedPrefs.edit()).thenReturn(mockEditor)
        Mockito.`when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPrefs)
        miniAppCustomPermissionCache =
            MiniAppCustomPermissionCache(
                mockContext
            )
    }

    @Test
    fun `storePermissionResult should put string as grantResult in preferences`() {
        Mockito.`when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)

        val miniAppId = "dummyMiniAppId"
        val miniAppCustomPermission =
            MiniAppCustomPermission(
                miniAppId,
                listOf(
                    Pair(
                        MiniAppCustomPermissionType.USER_NAME,
                        MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
                    ),
                    Pair(
                        MiniAppCustomPermissionType.CONTACT_LIST,
                        MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
                    ),
                    Pair(
                        MiniAppCustomPermissionType.PROFILE_PHOTO,
                        MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
                    )
                )
            )
        val json: String = Gson().toJson(miniAppCustomPermission)

        miniAppCustomPermissionCache.storePermissions(miniAppCustomPermission)

        verify(mockEditor).putString(miniAppId, json)
    }
}
