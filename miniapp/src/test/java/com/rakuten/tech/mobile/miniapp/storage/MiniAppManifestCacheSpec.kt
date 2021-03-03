package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import kotlin.test.assertEquals

class MiniAppManifestCacheSpec {

    private lateinit var miniAppCustomPermissionCache: MiniAppCustomPermissionCache
    private lateinit var manifestCache: MiniAppManifestCache
    private val mockSharedPrefs: SharedPreferences = mock()
    private val mockEditor: SharedPreferences.Editor = mock()
    private val mockContext: Context = mock()
    private val demoManifest = MiniAppManifest(
        listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")),
        listOf(),
        mapOf()
    )

    @Before
    fun setUp() {
        Mockito.`when`(mockSharedPrefs.edit()).thenReturn(mockEditor)
        Mockito.`when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPrefs)
        Mockito.`when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        Mockito.`when`(mockEditor.remove(anyString())).thenReturn(mockEditor)

        miniAppCustomPermissionCache = spy(MiniAppCustomPermissionCache(mockContext))
        manifestCache = spy(MiniAppManifestCache(mockContext, miniAppCustomPermissionCache))
    }

    /** region: readMiniAppManifest */
    @Test
    fun `readMiniAppManifest should return null when it hasn't stored any data yet`() {
        val actual = manifestCache.readMiniAppManifest(TEST_MA_ID)
        val expected = null
        actual shouldEqual expected
    }

    @Test
    fun `readMiniAppManifest should return expected value when it has stored any data`() {
        doReturn(demoManifest).whenever(manifestCache).readMiniAppManifest(TEST_MA_ID)
        val actual = manifestCache.readMiniAppManifest(TEST_MA_ID)
        actual shouldEqual demoManifest
    }
    /** end region */

    @Test
    fun `storeMiniAppManifest will invoke putString while storing manifest`() {
        manifestCache.storeMiniAppManifest(TEST_MA_ID, demoManifest)
        verify(mockEditor).putString(anyString(), anyString())
    }

    @Test
    fun `getCachedAllPermission will invoke cached required and optional permissions`() {
        val customPermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED))
        )
        doReturn(customPermission).whenever(miniAppCustomPermissionCache).readPermissions(TEST_MA_ID)
        doReturn(demoManifest).whenever(manifestCache).readMiniAppManifest(TEST_MA_ID)

        manifestCache.getCachedAllPermissions(TEST_MA_ID)

        verify(manifestCache).getCachedRequiredPermissions(TEST_MA_ID)
        verify(manifestCache).getCachedOptionalPermissions(TEST_MA_ID)
    }

    @Test
    fun `isRequiredPermissionDenied will invoke storeMiniAppManifest`() {
        val customPermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.ALLOWED))
        )
        doReturn(customPermission).whenever(miniAppCustomPermissionCache).readPermissions(TEST_MA_ID)
        doReturn(customPermission.pairValues).whenever(manifestCache).getCachedRequiredPermissions(TEST_MA_ID)

        manifestCache.isRequiredPermissionDenied(TEST_MA_ID, demoManifest)
        verify(manifestCache).storeMiniAppManifest(TEST_MA_ID, demoManifest)
        verify(manifestCache).getCachedRequiredPermissions(TEST_MA_ID)
    }

    @Test
    fun `getCachedRequiredPermissions will return expected values`() {
        val customPermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED))
        )
        doReturn(demoManifest).whenever(manifestCache).readMiniAppManifest(TEST_MA_ID)
        doReturn(customPermission).whenever(miniAppCustomPermissionCache).readPermissions(TEST_MA_ID)

        val actual = manifestCache.getCachedRequiredPermissions(TEST_MA_ID)
        verify(manifestCache).readMiniAppManifest(TEST_MA_ID)
        assertEquals(actual, customPermission.pairValues)
    }

    @Test
    fun `getCachedOptionalPermissions will return expected values`() {
        val customPermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(Pair(MiniAppCustomPermissionType.CONTACT_LIST, MiniAppCustomPermissionResult.DENIED))
        )

        val manifest = MiniAppManifest(
            listOf(),
            listOf(Pair(MiniAppCustomPermissionType.CONTACT_LIST, "reason")),
            mapOf()
        )
        doReturn(manifest).whenever(manifestCache).readMiniAppManifest(TEST_MA_ID)
        doReturn(customPermission).whenever(miniAppCustomPermissionCache).readPermissions(TEST_MA_ID)

        val actual = manifestCache.getCachedOptionalPermissions(TEST_MA_ID)
        verify(manifestCache).readMiniAppManifest(TEST_MA_ID)
        assertEquals(actual, customPermission.pairValues)
    }
}
