package com.rakuten.tech.mobile.miniapp.permission.ui

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import com.rakuten.tech.mobile.miniapp.TEST_BASE_PATH
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import org.mockito.kotlin.*
import com.rakuten.tech.mobile.miniapp.permission.CustomPermissionBridgeDispatcher
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import java.io.File

@Suppress("LongMethod")
@ExperimentalCoroutinesApi
class MiniAppCustomPermissionWindowSpec {
    private lateinit var permissionCache: MiniAppCustomPermissionCache
    private lateinit var downloadedManifestCache: DownloadedManifestCache
    private val dispatcher: CustomPermissionBridgeDispatcher = mock()
    private val context: Context = mock()
    private val editor: SharedPreferences.Editor = mock()
    private val prefs: SharedPreferences = mock()
    private val activity: Activity = mock()
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
        Mockito.`when`(prefs.edit()).thenReturn(editor)
        Mockito.`when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs)
        Mockito.`when`(context.filesDir).thenReturn(File(TEST_BASE_PATH))
        permissionCache = MiniAppCustomPermissionCache(prefs, prefs)
        downloadedManifestCache = spy(DownloadedManifestCache(context))
        cachedCustomPermission = permissionCache.readPermissions(miniAppId)
        permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))
    }

    @Test
    fun `should not init anything while miniAppId is empty`() {
        val mockDialog: AlertDialog = mock()
        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog

        permissionWindow.displayPermissions("", permissionWithDescriptions)

        verify(permissionWindow, times(0)).initDefaultWindow()
        verify(permissionWindow, times(0)).prepareDataForAdapter(permissionWithDescriptions)
        verify(permissionWindow, times(0)).addPermissionClickListeners()
        verify(mockDialog, times(0)).show()
    }

    @Test
    fun `should not init anything while permissions are empty`() {
        val mockDialog: AlertDialog = mock()
        val emptyPermissions: List<Pair<MiniAppCustomPermissionType, String>> = emptyList()
        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog

        permissionWindow.displayPermissions(miniAppId, emptyPermissions)

        verify(permissionWindow, times(0)).initDefaultWindow()
        verify(permissionWindow, times(0)).prepareDataForAdapter(emptyPermissions)
        verify(permissionWindow, times(0)).addPermissionClickListeners()
        verify(mockDialog, times(0)).show()
    }
}
