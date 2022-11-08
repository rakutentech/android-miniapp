package com.rakuten.tech.mobile.miniapp.api

import android.content.Context
import android.content.SharedPreferences
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_ATP_LIST
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_LANGUAGE_CODE
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import org.mockito.kotlin.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import kotlin.test.assertEquals

internal class ManifestApiCacheSpec {
    private lateinit var manifestCache: ManifestApiCache
    private val mockSharedPrefs: SharedPreferences = mock()
    private val mockEditor: SharedPreferences.Editor = mock()
    private val mockContext: Context = mock()

    @Before
    fun setUp() {
        Mockito.`when`(mockSharedPrefs.edit()).thenReturn(mockEditor)
        Mockito.`when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPrefs)
        Mockito.`when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        manifestCache = spy(ManifestApiCache(mockContext))
    }

    @Test
    fun `readManifest should return null when it hasn't stored any data yet`() {
        val actual = manifestCache.readManifest(TEST_MA_ID, TEST_MA_VERSION_ID, TEST_MA_LANGUAGE_CODE)
        val expected = null
        actual shouldBeEqualTo expected
    }

    @Test
    fun `readManifest will return expected values`() {
        val cachedManifest = MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")), listOf(),
            TEST_ATP_LIST, mapOf(), TEST_MA_VERSION_ID
        )
        doReturn(cachedManifest).whenever(manifestCache)
            .readManifest(TEST_MA_ID, TEST_MA_VERSION_ID, TEST_MA_LANGUAGE_CODE)
        val actual = manifestCache.readManifest(TEST_MA_ID, TEST_MA_VERSION_ID, TEST_MA_LANGUAGE_CODE)
        actual shouldBeEqualTo cachedManifest
    }

    @Test
    fun `storeManifest will invoke putString while storing the latest manifest`() {
        val newManifest = MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "reason")), listOf(),
            TEST_ATP_LIST, mapOf(), TEST_MA_VERSION_ID
        )
        Mockito.`when`(mockEditor.clear()).thenReturn(mockEditor)
        manifestCache.storeManifest(TEST_MA_ID, TEST_MA_VERSION_ID, TEST_MA_LANGUAGE_CODE, newManifest)
        verify(mockEditor).putString(anyString(), anyString())
    }

    @Test
    fun `primaryKey should match the correct rule`() {
        val actual = manifestCache.primaryKey(TEST_MA_ID, TEST_MA_VERSION_ID, TEST_MA_LANGUAGE_CODE)
        assertEquals("$TEST_MA_ID-$TEST_MA_VERSION_ID-$TEST_MA_LANGUAGE_CODE", actual)
    }
}
