package com.rakuten.tech.mobile.miniapp.js

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
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
    fun `storePermissionResult should put boolean as grantResult in preferences`() {
        Mockito.`when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)

        val permissions = listOf("CUSTOM_PERMISSION_NAME")
        val grantResults = listOf("DENIED")
        miniAppCustomPermissionCache.storePermissionResults(permissions, grantResults)

        val permissionCount = permissions.size
        for (i in 0 until permissionCount) {
            verify(mockEditor).putString(permissions[i], grantResults[i])
        }
    }
}
