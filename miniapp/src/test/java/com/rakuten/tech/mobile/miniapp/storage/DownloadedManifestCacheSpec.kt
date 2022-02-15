package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.content.SharedPreferences
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_ATP_LIST
import com.rakuten.tech.mobile.miniapp.TEST_BASE_PATH
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import org.mockito.kotlin.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import java.io.File
import kotlin.test.assertEquals

@Suppress("LongMethod")
class DownloadedManifestCacheSpec {
    private lateinit var manifestCache: DownloadedManifestCache
    private val mockSharedPrefs: SharedPreferences = mock()
    private val mockEditor: SharedPreferences.Editor = mock()
    private val mockContext: Context = mock()
    private val demoManifest =
        MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")), listOf(),
            TEST_ATP_LIST, mapOf(), TEST_MA_VERSION_ID
        )
    private val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, demoManifest)

    @Before
    fun setUp() {
        Mockito.`when`(mockSharedPrefs.edit()).thenReturn(mockEditor)
        Mockito.`when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPrefs)
        Mockito.`when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        Mockito.`when`(mockEditor.remove(anyString())).thenReturn(mockEditor)
        Mockito.`when`(mockContext.filesDir).thenReturn(File(TEST_BASE_PATH))
        manifestCache = spy(DownloadedManifestCache(mockContext))
        doReturn(cachedManifest).whenever(manifestCache).readDownloadedManifest(TEST_MA_ID)
    }

    @Test
    fun `readDownloadedManifest should return null when it hasn't stored any data yet`() {
        val actual = DownloadedManifestCache(mockContext).readDownloadedManifest(TEST_MA_ID)
        actual shouldBeEqualTo null
    }

    @Test
    fun `readDownloadedManifest will return expected values`() {
        val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, demoManifest)
        doReturn(cachedManifest).whenever(manifestCache).readDownloadedManifest(TEST_MA_ID)
        val actual = manifestCache.readDownloadedManifest(TEST_MA_ID)
        actual shouldBeEqualTo cachedManifest
    }

    @Test
    fun `storeDownloadedManifest will store cached manifest in a new file`() {
        val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, demoManifest)
        doReturn(cachedManifest).whenever(manifestCache).readDownloadedManifest(TEST_MA_ID)
        doNothing().whenever(manifestCache).storeNewFile(TEST_MA_ID, cachedManifest)
        manifestCache.storeDownloadedManifest(TEST_MA_ID, cachedManifest)
        verify(manifestCache).storeNewFile(TEST_MA_ID, cachedManifest)
    }

    @Test
    fun `getAllPermissions will invoke required and optional permissions getters`() {
        val cachedPermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(
                Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.ALLOWED)
            )
        )
        manifestCache.getAllPermissions(cachedPermission)
        verify(manifestCache).getRequiredPermissions(cachedPermission)
        verify(manifestCache).getOptionalPermissions(cachedPermission)
    }

    @Test
    fun `isRequiredPermissionDenied will be true when provided manifest has denied permission`() {
        val customPermission = createCustomPermission(false)
        doReturn(customPermission.pairValues).whenever(manifestCache)
            .getRequiredPermissions(customPermission)
        manifestCache.isRequiredPermissionDenied(customPermission) shouldBeEqualTo true
    }

    @Test
    fun `isRequiredPermissionDenied will be false when provided manifest has allowed permission`() {
        val customPermission = createCustomPermission(true)
        doReturn(customPermission.pairValues).whenever(manifestCache)
            .getRequiredPermissions(customPermission)
        manifestCache.isRequiredPermissionDenied(customPermission) shouldBeEqualTo false
    }

    @Test
    fun `getRequiredPermissions will return empty while there is error`() {
        val cachedPermission = MiniAppCustomPermission(TEST_MA_ID, listOf())
        doReturn(null).whenever(manifestCache).readDownloadedManifest(TEST_MA_ID)

        val actual = manifestCache.getRequiredPermissions(cachedPermission)
        assertEquals(emptyList(), actual)
    }

    @Test
    fun `getRequiredPermissions will return expected values`() {
        val expected = listOf(
            Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.ALLOWED)
        )
        val cachedPermission = MiniAppCustomPermission(TEST_MA_ID, expected)
        val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, demoManifest)
        doReturn(cachedManifest).whenever(manifestCache).readDownloadedManifest(TEST_MA_ID)

        val actual = manifestCache.getRequiredPermissions(cachedPermission)
        assertEquals(expected, actual)
    }

    @Test
    fun `getOptionalPermissions will return expected values`() {
        val expected = listOf(
            Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, MiniAppCustomPermissionResult.ALLOWED)
        )
        val demoOptionalManifest =
            MiniAppManifest(
                listOf(), listOf(Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "reason")),
                TEST_ATP_LIST, mapOf(), TEST_MA_VERSION_ID
            )
        val cachedPermission = MiniAppCustomPermission(TEST_MA_ID, expected)
        val cachedManifest = CachedManifest(TEST_MA_VERSION_ID, demoOptionalManifest)
        doReturn(cachedManifest).whenever(manifestCache).readDownloadedManifest(TEST_MA_ID)

        val actual = manifestCache.getOptionalPermissions(cachedPermission)
        assertEquals(expected, actual)
    }

    @Test
    fun `getOptionalPermissions will return empty while there is error`() {
        val cachedPermission = MiniAppCustomPermission(TEST_MA_ID, listOf())
        doReturn(null).whenever(manifestCache).readDownloadedManifest(TEST_MA_ID)

        val actual = manifestCache.getOptionalPermissions(cachedPermission)
        assertEquals(emptyList(), actual)
    }

    @Test
    fun `should get access token permissions correctly`() {
        manifestCache.getAccessTokenPermissions(TEST_MA_ID) shouldBeEqualTo TEST_ATP_LIST
    }

    @Test
    fun `should get empty list of AccessTokenPermission when no cache`() {
        DownloadedManifestCache(mockContext).getAccessTokenPermissions(TEST_MA_ID) shouldBeEqualTo emptyList()
    }

    @Test
    fun `toCachedManifest should create correct CachedManifest`() {
        val jsonStr = "{“miniAppManifest”:{“accessTokenPermissions”:[{“audience”:“aud”,“scopes”:[“scope1\",“scope2”]}],“optionalPermissions”:[{“first”:“CONTACT_LIST”,“second”:“reason here (optional).“}],“versionId”:“aaaa”},“versionId”:“bbbb”}"
        manifestCache.toCachedManifest(jsonStr)?.apply {
            versionId shouldBeEqualTo "bbbb"
            miniAppManifest.accessTokenPermissions[0].audience shouldBeEqualTo "aud"
            miniAppManifest.optionalPermissions[0].first shouldBeEqualTo MiniAppCustomPermissionType.CONTACT_LIST
        }
    }

    @Test
    fun `getManifestFile should invoke with manifest file path`() {
        manifestCache.getManifestFile(TEST_MA_ID)
        verify(manifestCache).getManifestPath(TEST_MA_ID)
    }

    private fun createCustomPermission(isAllowed: Boolean): MiniAppCustomPermission {
        val list = arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()
        if (isAllowed) list.add(
            Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.ALLOWED)
        ) else list.add(
            Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED)
        )
        return MiniAppCustomPermission(TEST_MA_ID, list)
    }
}
