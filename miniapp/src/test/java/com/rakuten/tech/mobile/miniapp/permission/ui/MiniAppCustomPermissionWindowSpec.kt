package com.rakuten.tech.mobile.miniapp.permission.ui

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TestActivity
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class MiniAppCustomPermissionWindowSpec {
    private lateinit var permissionCache: MiniAppCustomPermissionCache
    private val mockSharedPrefs: SharedPreferences = mock()
    private val mockEditor: SharedPreferences.Editor = mock()
    private val mockContext: Context = mock()
    private lateinit var activity: Activity
    private lateinit var permissionWindow: MiniAppCustomPermissionWindow

    private val miniAppId = TEST_CALLBACK_ID
    private val permissionWithDescriptions =
        listOf(
            Pair(MiniAppCustomPermissionType.USER_NAME, "dummy description"),
            Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "dummy description"),
            Pair(MiniAppCustomPermissionType.CONTACT_LIST, "dummy description")
        )
    private lateinit var cachedCustomPermission: MiniAppCustomPermission

    @Before
    fun setup() {
        `when`(mockSharedPrefs.edit()).thenReturn(mockEditor)
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPrefs)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)

        permissionCache = MiniAppCustomPermissionCache(mockContext)
        cachedCustomPermission = permissionCache.readPermissions(miniAppId)

        ActivityScenario.launch(TestActivity::class.java).onActivity {
            activity = it
            permissionWindow = spy(MiniAppCustomPermissionWindow(activity, permissionCache))
        }
    }

    @Test
    fun `should init default view with preparing data when trying to display permissions`() {
        doReturn(permissionWithDescriptions).whenever(permissionWindow)
            .getDeniedPermissions(miniAppId, permissionWithDescriptions)

        permissionWindow.displayPermissions(miniAppId, permissionWithDescriptions)

        verify(permissionWindow).initDefaultWindow()
        verify(permissionWindow).prepareDataForAdapter(permissionWithDescriptions)
    }

    @Test
    fun `should add click listeners when trying to display permissions`() {
        doReturn(permissionWithDescriptions).whenever(permissionWindow)
            .getDeniedPermissions(miniAppId, permissionWithDescriptions)

        permissionWindow.displayPermissions(miniAppId, permissionWithDescriptions)

        verify(permissionWindow).addPermissionClickListeners(miniAppId)
    }

    @Test
    fun `should show dialog when trying to display permissions`() {
        val mockDialog: AlertDialog = mock()
        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog
        doReturn(permissionWithDescriptions).whenever(permissionWindow)
            .getDeniedPermissions(miniAppId, permissionWithDescriptions)

        permissionWindow.displayPermissions(miniAppId, permissionWithDescriptions)

        verify(mockDialog).show()
    }

    @Test
    fun `should not init anything while miniAppId is empty`() {
        val mockDialog: AlertDialog = mock()
        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog
        doReturn(permissionWithDescriptions).whenever(permissionWindow)
            .getDeniedPermissions(miniAppId, permissionWithDescriptions)

        permissionWindow.displayPermissions("", permissionWithDescriptions)

        verify(permissionWindow, times(0)).initDefaultWindow()
        verify(permissionWindow, times(0)).prepareDataForAdapter(permissionWithDescriptions)
        verify(permissionWindow, times(0)).addPermissionClickListeners(miniAppId)
        verify(mockDialog, times(0)).show()
    }

    @Test
    fun `getCachedList should be the equal to the original pairs stored in cache`() {
        val actual = permissionWindow.getCachedList(miniAppId)
        val expected = permissionCache.readPermissions(miniAppId).pairValues

        assertEquals(expected, actual)
    }

    @Test
    fun `getDeniedPermissions should be the equal to the original pairs stored in cache`() {
        val expected =
            listOf(
                Pair(MiniAppCustomPermissionType.USER_NAME, "dummy description"),
                Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "dummy description"),
                Pair(MiniAppCustomPermissionType.CONTACT_LIST, "dummy description")
            )
        val actual = permissionWindow.getDeniedPermissions(miniAppId, permissionWithDescriptions)

        assertEquals(expected, actual)
    }
}
