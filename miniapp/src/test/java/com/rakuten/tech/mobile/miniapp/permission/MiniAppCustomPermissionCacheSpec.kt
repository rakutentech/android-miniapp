package com.rakuten.tech.mobile.miniapp.permission

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
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
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)

        miniAppCustomPermissionCache = spy(MiniAppCustomPermissionCache(mockContext))
    }

    @Test
    fun `readPermissions should return default value when it hasn't stored any data yet`() {
        val actual = miniAppCustomPermissionCache.readPermissions(TEST_MA_ID)
        val expected = miniAppCustomPermissionCache.defaultDeniedList(TEST_MA_ID)

        verify(miniAppCustomPermissionCache).applyStoringPermissions(expected)
        actual shouldEqual expected
    }

    @Test
    fun `applyStoringPermissions will invoke putString while storing custom permissions`() {
        val miniAppCustomPermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(
                Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED)
            )
        )

        miniAppCustomPermissionCache.applyStoringPermissions(miniAppCustomPermission)

        verify(mockEditor).putString(anyString(), anyString())
    }

    /**
     * region: prepareAllPermissionsToStore
     */
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

        val actual = miniAppCustomPermissionCache.prepareAllPermissionsToStore(TEST_MA_ID, cached, supplied)

        actual.size shouldBe 5
        verify(miniAppCustomPermissionCache).getNewPermissions(TEST_MA_ID, supplied)
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

        val actual = miniAppCustomPermissionCache.prepareAllPermissionsToStore(TEST_MA_ID, cached, supplied)

        actual.size shouldBe 4
    }
    /** end region */

    @Test
    fun `getNewPermissions should return the correct values based on supplied list`() {
        val supplied = listOf(
            Pair(
                MiniAppCustomPermissionType.USER_NAME,
                MiniAppCustomPermissionResult.ALLOWED
            )
        )

        val actual = miniAppCustomPermissionCache.getNewPermissions(TEST_MA_ID, supplied)

        actual.size shouldBe 3
    }

    /**
     * region: defaultDeniedList.
     * Update the values in the following tests when adding or removing a custom permission.
     */
    @Test
    fun `check the size of the default denied list`() {
        val actual = miniAppCustomPermissionCache.defaultDeniedList(TEST_MA_ID).pairValues

        actual.size shouldBe 4
    }

    @Test
    fun `check MiniAppCustomPermissionType of the default denied list`() {
        val actual = miniAppCustomPermissionCache.defaultDeniedList(TEST_MA_ID)

        actual.pairValues[0].first shouldEqual MiniAppCustomPermissionType.USER_NAME
        actual.pairValues[1].first shouldEqual MiniAppCustomPermissionType.PROFILE_PHOTO
        actual.pairValues[2].first shouldEqual MiniAppCustomPermissionType.CONTACT_LIST
        actual.pairValues[3].first shouldEqual MiniAppCustomPermissionType.LOCATION
    }

    @Test
    fun `check MiniAppCustomPermissionResult of the default denied list`() {
        val actual = miniAppCustomPermissionCache.defaultDeniedList(TEST_MA_ID)

        actual.pairValues.forEach {
            it.second shouldEqual MiniAppCustomPermissionResult.DENIED
        }
    }
    /** end region */
}
