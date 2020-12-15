package com.rakuten.tech.mobile.miniapp.permission

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`

@Suppress("LongMethod", "LargeClass")
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
        `when`(mockEditor.remove(anyString())).thenReturn(mockEditor)

        miniAppCustomPermissionCache = spy(MiniAppCustomPermissionCache(mockContext))
    }

    @Test
    fun `isDataExist should return false when miniAppId is not stored`() {
        val actual = miniAppCustomPermissionCache.doesDataExist(TEST_MA_ID)

        actual shouldEqual false
    }

    @Test
    fun `isDataExist should return true when miniAppId is stored`() {
        doReturn(true).whenever(mockSharedPrefs).contains(TEST_MA_ID)
        val actual = miniAppCustomPermissionCache.doesDataExist(TEST_MA_ID)

        actual shouldEqual true
    }

    /**
     * region: readPermissions
     */
    @Test
    fun `readPermissions should return default value when it hasn't stored any data yet`() {
        val actual = miniAppCustomPermissionCache.readPermissions(TEST_MA_ID)
        val expected = miniAppCustomPermissionCache.defaultDeniedList(TEST_MA_ID)

        verify(miniAppCustomPermissionCache).applyStoringPermissions(expected)
        actual shouldEqual expected
    }

    @Test
    fun `readPermissions should return actual value when it has stored data`() {
        doReturn(true).whenever(miniAppCustomPermissionCache).doesDataExist(TEST_MA_ID)
        val actual = miniAppCustomPermissionCache.readPermissions(TEST_MA_ID)
        val expected = miniAppCustomPermissionCache.defaultDeniedList(TEST_MA_ID)

        actual shouldEqual expected
    }

    @Test
    fun `readPermissions will not apply data to be stored when exception`() {
        val default = MiniAppCustomPermission(
            TEST_MA_ID, listOf(
                Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED)
            )
        )

        doReturn(true).whenever(miniAppCustomPermissionCache).doesDataExist(TEST_MA_ID)
        doReturn(default).whenever(miniAppCustomPermissionCache).defaultDeniedList(TEST_MA_ID)

        val actual = miniAppCustomPermissionCache.readPermissions(TEST_MA_ID)

        verify(miniAppCustomPermissionCache, times(0)).applyStoringPermissions(default)
        actual shouldEqual default
    }

    /** end region */

    @Test
    fun `removeId will remove all permission data from the store`() {
        miniAppCustomPermissionCache.removePermission(TEST_MA_ID)
        verify(mockEditor, times(1)).remove(TEST_MA_ID)
    }

    @Test
    fun `storePermissions will invoke necessary functions to save value`() {
        val list = listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED))
        val miniAppCustomPermission = MiniAppCustomPermission(TEST_MA_ID, list)
        doReturn(miniAppCustomPermission).whenever(miniAppCustomPermissionCache).defaultDeniedList(TEST_MA_ID)

        miniAppCustomPermissionCache.storePermissions(miniAppCustomPermission)

        verify(miniAppCustomPermissionCache).prepareAllPermissionsToStore(TEST_MA_ID, list)
        verify(miniAppCustomPermissionCache, times(2)).applyStoringPermissions(miniAppCustomPermission)
    }

    @Test
    fun `applyStoringPermissions will invoke putString while storing custom permissions`() {
        val miniAppCustomPermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED))
        )

        miniAppCustomPermissionCache.applyStoringPermissions(miniAppCustomPermission)

        verify(mockEditor).putString(anyString(), anyString())
        verify(miniAppCustomPermissionCache).sortedByDefault(miniAppCustomPermission)
    }

    @Test
    fun `orderByDefaultList should return correct ordering by MiniAppCustomPermissionType`() {
        val unorderedPermission = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(
                Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED),
                Pair(MiniAppCustomPermissionType.CONTACT_LIST, MiniAppCustomPermissionResult.DENIED),
                Pair(MiniAppCustomPermissionType.LOCATION, MiniAppCustomPermissionResult.DENIED),
                Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, MiniAppCustomPermissionResult.DENIED)
            )
        )

        val actual = miniAppCustomPermissionCache.sortedByDefault(unorderedPermission)

        actual.pairValues[0].first shouldEqual MiniAppCustomPermissionType.USER_NAME
        actual.pairValues[1].first shouldEqual MiniAppCustomPermissionType.PROFILE_PHOTO
        actual.pairValues[2].first shouldEqual MiniAppCustomPermissionType.CONTACT_LIST
        actual.pairValues[3].first shouldEqual MiniAppCustomPermissionType.LOCATION
    }

    /**
     * region: prepareAllPermissionsToStore
     */
    @Test
    fun `prepareAllPermissionsToStore should combine cached and supplied list properly with unknown permissions`() {
        val cached = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(Pair(MiniAppCustomPermissionType.UNKNOWN, MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE))
        )
        val supplied = listOf(
            Pair(
                MiniAppCustomPermissionType.PROFILE_PHOTO,
                MiniAppCustomPermissionResult.ALLOWED
            )
        )
        doReturn(cached).whenever(miniAppCustomPermissionCache).readPermissions(TEST_MA_ID)

        val actual = miniAppCustomPermissionCache.prepareAllPermissionsToStore(TEST_MA_ID, supplied)

        actual.size shouldBe 2
    }

    @Test
    fun `prepareAllPermissionsToStore should combine cached and supplied list properly with empty cached`() {
        val cached = MiniAppCustomPermission(TEST_MA_ID, emptyList())
        val supplied = listOf(
            Pair(MiniAppCustomPermissionType.CONTACT_LIST, MiniAppCustomPermissionResult.ALLOWED)
        )
        doReturn(cached).whenever(miniAppCustomPermissionCache).readPermissions(TEST_MA_ID)

        val actual = miniAppCustomPermissionCache.prepareAllPermissionsToStore(TEST_MA_ID, supplied)

        actual.size shouldBe 1
    }
    /** end region */

    /**
     * region: hasPermission
     */
    @Test
    fun `hasPermission should return false by default when there is no allowed permission found`() {
        val actual = miniAppCustomPermissionCache.hasPermission(
            TEST_MA_ID,
            MiniAppCustomPermissionType.USER_NAME
        )

        actual shouldBe false
    }

    @Test
    fun `hasPermission should return true when there is allowed permission found`() {
        val allowedUserName = MiniAppCustomPermission(
            TEST_MA_ID,
            listOf(
                Pair(
                    MiniAppCustomPermissionType.USER_NAME,
                    MiniAppCustomPermissionResult.ALLOWED
                )
            )
        )

        doReturn(allowedUserName).whenever(miniAppCustomPermissionCache).readPermissions(TEST_MA_ID)

        val actual = miniAppCustomPermissionCache.hasPermission(
            TEST_MA_ID,
            MiniAppCustomPermissionType.USER_NAME
        )

        actual shouldBe true
    }
    /** end region */

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
