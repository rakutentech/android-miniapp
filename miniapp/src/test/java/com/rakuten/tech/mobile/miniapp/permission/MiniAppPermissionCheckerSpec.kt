package com.rakuten.tech.mobile.miniapp.permission

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito

class MiniAppPermissionCheckerSpec {
    private lateinit var miniAppPermissionChecker: MiniAppPermissionChecker
    private val mockSharedPrefs: SharedPreferences = mock()
    private val mockEditor: SharedPreferences.Editor = mock()
    private val mockContext: Context = mock()

    @Before
    fun setUp() {
        Mockito.`when`(mockSharedPrefs.edit()).thenReturn(mockEditor)
        Mockito.`when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPrefs)
        miniAppPermissionChecker = MiniAppPermissionChecker(mockContext)
    }

    @Test
    fun `storePermissionResult should put boolean as grantResult in preferences`() {
        Mockito.`when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)

        val dummyPermission = "dummy_permission"
        val grantResult = true
        miniAppPermissionChecker.storePermissionResult(dummyPermission, grantResult)

        verify(mockEditor).putBoolean(dummyPermission, grantResult)
    }
}
