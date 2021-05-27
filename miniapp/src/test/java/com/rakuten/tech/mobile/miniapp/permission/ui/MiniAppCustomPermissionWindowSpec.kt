package com.rakuten.tech.mobile.miniapp.permission.ui

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.TEST_BASE_PATH
import org.mockito.kotlin.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TestActivity
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import com.rakuten.tech.mobile.miniapp.permission.CustomPermissionBridgeDispatcher
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import org.amshove.kluent.When
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.io.File
import java.io.FileNotFoundException

@Suppress("LongMethod")
@RunWith(AndroidJUnit4::class)
class MiniAppCustomPermissionWindowSpec {
    private lateinit var permissionCache: MiniAppCustomPermissionCache
    private lateinit var downloadedManifestCache: DownloadedManifestCache
    private lateinit var dispatcher: CustomPermissionBridgeDispatcher
    private val bridgeExecutor: MiniAppBridgeExecutor = mock()
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val prefs = context.getSharedPreferences("test-cache", Context.MODE_PRIVATE)
    private lateinit var activity: Activity
    private lateinit var permissionWindow: MiniAppCustomPermissionWindow
    private var activityScenario = ActivityScenario.launch(TestActivity::class.java)
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
        permissionCache = MiniAppCustomPermissionCache(prefs, prefs)
        downloadedManifestCache = DownloadedManifestCache(context)

        cachedCustomPermission = permissionCache.readPermissions(miniAppId)

        activityScenario.onActivity {
            activity = it
            dispatcher =
                CustomPermissionBridgeDispatcher(
                    bridgeExecutor,
                    permissionCache,
                    downloadedManifestCache,
                    miniAppId,
                    ""
                )
            permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))
        }
    }

    @After
    fun finish() {
        activityScenario.close()
    }

    @Test(expected = FileNotFoundException::class)
    fun `should init default view with preparing data when trying to display permissions`() {
        permissionWindow.displayPermissions(miniAppId, permissionWithDescriptions)

        verify(permissionWindow).initDefaultWindow()
        verify(permissionWindow).prepareDataForAdapter(permissionWithDescriptions)
    }

    @Test(expected = FileNotFoundException::class)
    fun `should add click listeners when trying to display permissions`() {
        permissionWindow.displayPermissions(miniAppId, permissionWithDescriptions)

        verify(permissionWindow).addPermissionClickListeners()
    }

    @Test(expected = FileNotFoundException::class)
    fun `should show dialog when trying to display permissions`() {
        val mockDialog: AlertDialog = mock()
        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog

        permissionWindow.displayPermissions(miniAppId, permissionWithDescriptions)

        verify(mockDialog).show()
    }

    @Test(expected = FileNotFoundException::class)
    fun `should not init anything while miniAppId is empty`() {
        val mockDialog: AlertDialog = mock()
        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog

        permissionWindow.displayPermissions("", permissionWithDescriptions)

        verify(permissionWindow, times(0)).initDefaultWindow()
        verify(permissionWindow, times(0)).prepareDataForAdapter(permissionWithDescriptions)
        verify(permissionWindow, times(0)).addPermissionClickListeners()
        verify(mockDialog, times(0)).show()
    }

    @Test(expected = FileNotFoundException::class)
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
