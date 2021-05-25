package com.rakuten.tech.mobile.miniapp.permission

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.MiniAppVerificationException
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("LongMethod", "LargeClass")
@RunWith(AndroidJUnit4::class)
class MiniAppCustomPermissionCacheSpec {
    private lateinit var miniAppCustomPermissionCache: MiniAppCustomPermissionCache
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val prefs = context.getSharedPreferences("test-cache", Context.MODE_PRIVATE)
    private val deniedPermissions =
        listOf(
            Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED)
        )
    private val miniAppCustomPermission = MiniAppCustomPermission(TEST_MA_ID, deniedPermissions)

    @Before
    fun setUp() {
        miniAppCustomPermissionCache = spy(MiniAppCustomPermissionCache(prefs, prefs))
    }

    @Test(expected = MiniAppVerificationException::class)
    fun `initiating encrypted preferences with invalid context will throw error`() {
        MiniAppCustomPermissionCache(context)
    }

    /**
     * region: readPermissions
     */
    @Test
    fun `readPermissions should return empty value when it hasn't stored any data yet`() {
        val actual = miniAppCustomPermissionCache.readPermissions(TEST_MA_ID)
        val expected = MiniAppCustomPermission(TEST_MA_ID, emptyList())

        actual shouldEqual expected
    }
    /** end region */

    @Test
    fun `removePermissionsNotMatching will invoke necessary function to save value`() {
        doReturn(miniAppCustomPermission).whenever(miniAppCustomPermissionCache).readPermissions(TEST_MA_ID)
        miniAppCustomPermissionCache.removePermissionsNotMatching(TEST_MA_ID, deniedPermissions)
        verify(miniAppCustomPermissionCache).applyStoringPermissions(miniAppCustomPermission)
    }

    @Test
    fun `removeId will remove all permission data from the store`() {
        miniAppCustomPermissionCache.removePermission(TEST_MA_ID)
        assertFalse(prefs.all.contains(TEST_MA_ID))
    }

    @Test
    fun `storePermissions will invoke necessary functions to save value`() {
        miniAppCustomPermissionCache.storePermissions(miniAppCustomPermission)

        verify(miniAppCustomPermissionCache).prepareAllPermissionsToStore(TEST_MA_ID, deniedPermissions)
        verify(miniAppCustomPermissionCache).applyStoringPermissions(miniAppCustomPermission)
    }

    @Test
    fun `applyStoringPermissions will invoke sortedByDefault to save value`() {
        miniAppCustomPermissionCache.applyStoringPermissions(miniAppCustomPermission)
        assertTrue(prefs.all.contains(miniAppCustomPermission.miniAppId))
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
                Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.ALLOWED)
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
}
