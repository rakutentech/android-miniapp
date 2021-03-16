package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import com.rakuten.tech.mobile.miniapp.TEST_ATP_LIST
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

class DownloadedManifestCacheSpec {

    private lateinit var manifestCache: DownloadedManifestCache
    private val mockSharedPrefs: SharedPreferences = mock()
    private val mockEditor: SharedPreferences.Editor = mock()
    private val mockContext: Context = mock()
    private val demoManifest =
        MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")),
            listOf(),
            TEST_ATP_LIST,
            mapOf()
        )
    private val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, demoManifest)

    @Before
    fun setUp() {
        Mockito.`when`(mockSharedPrefs.edit()).thenReturn(mockEditor)
        Mockito.`when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPrefs)
        Mockito.`when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        Mockito.`when`(mockEditor.remove(anyString())).thenReturn(mockEditor)
        manifestCache = spy(DownloadedManifestCache(mockContext))

        doReturn(cachedManifest).whenever(manifestCache).readDownloadedManifest(TEST_MA_ID)
    }

    @Test
    fun `readDownloadedManifest should return null when it hasn't stored any data yet`() {
        val actual = DownloadedManifestCache(mockContext).readDownloadedManifest(TEST_MA_ID)
        val expected = null
        actual shouldEqual expected
    }

    @Test
    fun `storeDownloadedManifest will invoke putString while storing manifest`() {
        val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, demoManifest)
        manifestCache.storeDownloadedManifest(TEST_MA_ID, cachedManifest)
        verify(mockEditor).putString(anyString(), anyString())
    }

    /** region: isRequiredPermissionDenied */
    @Test
    fun `isRequiredPermissionDenied will be true when provided manifest has denied permission`() {
        val customPermission = createCustomPermission(false)
        doReturn(customPermission.pairValues).whenever(manifestCache)
            .getRequiredPermissions(customPermission)
        manifestCache.isRequiredPermissionDenied(customPermission) shouldEqual true
    }

    @Test
    fun `isRequiredPermissionDenied will be false when provided manifest has allowed permission`() {
        val customPermission = createCustomPermission(true)
        doReturn(customPermission.pairValues).whenever(manifestCache)
            .getRequiredPermissions(customPermission)
        manifestCache.isRequiredPermissionDenied(customPermission) shouldEqual false
    }
    /** end region */

    @Test
    fun `readDownloadedManifest will return expected values`() {
        val actual = manifestCache.readDownloadedManifest(TEST_MA_ID)
        actual shouldEqual cachedManifest
    }

    @Test
    fun `should get access token permissions correctly`() {
        manifestCache.getAccessTokenPermissions(TEST_MA_ID) shouldEqual TEST_ATP_LIST
    }

    @Test
    fun `should get empty list of AccessTokenPermission when no cache`() {
        DownloadedManifestCache(mockContext).getAccessTokenPermissions(TEST_MA_ID) shouldEqual emptyList()
    }

    private fun createCustomPermission(isAllowed: Boolean): MiniAppCustomPermission {
        val list = arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()
        if (isAllowed) list.add(
            Pair(
                MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.ALLOWED
            )
        )
        else list.add(
            Pair(
                MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED
            )
        )
        return MiniAppCustomPermission(TEST_MA_ID, list)
    }
}
