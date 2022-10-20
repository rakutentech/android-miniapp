package com.rakuten.tech.mobile.miniapp.permission.ui

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rakuten.tech.mobile.miniapp.R
import com.rakuten.tech.mobile.miniapp.TEST_BASE_PATH
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.permission.CustomPermissionBridgeDispatcher
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.io.File

@Suppress("LongMethod")
@ExperimentalCoroutinesApi
class MiniAppCustomPermissionWindowSpec {
    private lateinit var permissionCache: MiniAppCustomPermissionCache
    private lateinit var downloadedManifestCache: DownloadedManifestCache
    private val dispatcher: CustomPermissionBridgeDispatcher = mock()
    private var context: Context = mock()
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
        permissionWindow = mock()
    }

    @Test
    fun `the coroutine context should be Dispatchers Main`() {
        val permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))

        permissionWindow.coroutineContext.shouldBe(Dispatchers.Main)
    }

    @Test
    fun `should not init anything while miniAppId is empty`() {
        val mockDialog: AlertDialog = mock()
        val permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))

        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog

        permissionWindow.displayPermissions("", permissionWithDescriptions)

        verify(permissionWindow, times(0)).prepareDataForAdapter(permissionWithDescriptions)
        verify(permissionWindow, times(0)).addPermissionClickListeners()
        verify(mockDialog, times(0)).show()
    }

    @Test
    fun `should not init anything while permissions are empty`() {
        val mockDialog: AlertDialog = mock()
        val emptyPermissions: List<Pair<MiniAppCustomPermissionType, String>> = emptyList()

        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog

        val permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))

        permissionWindow.displayPermissions(miniAppId, emptyPermissions)

        verify(permissionWindow, times(0)).getRecyclerView()
        verify(permissionWindow, times(0)).prepareDataForAdapter(emptyPermissions)
        verify(permissionWindow, times(0)).addPermissionClickListeners()
        verify(mockDialog, times(0)).show()
    }

    @Suppress("MaxLineLength")
    @Test(expected = NullPointerException::class) // due to a require real context
    fun `getRecyclerView should return a RecyclerView`() {
        val permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))
        val customPermissionLayout: View = mock()
        val layoutManager: LayoutManager = mock()
        val permissionRecyclerView: RecyclerView = mock()

        doReturn(permissionRecyclerView).whenever(customPermissionLayout)
            .findViewById<RecyclerView>(R.id.listCustomPermission)
        doReturn(customPermissionLayout).whenever(permissionWindow).customPermissionLayout

        val recyclerView = permissionWindow.getRecyclerView()

        permissionRecyclerView.layoutManager shouldBeEqualTo layoutManager
        verify(permissionRecyclerView).layoutManager
        verify(permissionRecyclerView).adapter
        recyclerView.shouldBeInstanceOf(RecyclerView::class.java)
        recyclerView.shouldNotBeNull()
    }

    @Suppress("SwallowedException")
    @Test(expected = NullPointerException::class) // due to require a real context
    fun `initAdapter should initialize the layout, adapter and dialog`() {
        val permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))

        val customPermissionLayout: View = mock()
        val layoutManager: LayoutManager = mock()
        val customPermissionAdapter: MiniAppCustomPermissionAdapter = mock()
        val permissionAlertDialog: AlertDialog = mock()
        val permissionRecyclerView: RecyclerView = mock()

        doReturn(customPermissionAdapter).whenever(permissionWindow).customPermissionAdapter
        doReturn(customPermissionAdapter).whenever(permissionRecyclerView).adapter
        doReturn(permissionAlertDialog).whenever(permissionWindow).customPermissionAlertDialog
        doReturn(customPermissionLayout).whenever(permissionWindow).customPermissionLayout

        permissionWindow.initAdapterAndDialog(permissionRecyclerView)
        permissionRecyclerView.layoutManager shouldBeEqualTo layoutManager

        verify(permissionRecyclerView).layoutManager
        verify(permissionRecyclerView).adapter
        verify(permissionAlertDialog).setView(customPermissionLayout)
    }

    @Test
    fun `prepareDataForAdapter should add the deniedPermissions to adapter`() {
        val customPermissionAdapter: MiniAppCustomPermissionAdapter = mock()
        val permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))

        doReturn(customPermissionAdapter).whenever(permissionWindow).customPermissionAdapter

        permissionWindow.prepareDataForAdapter(permissionWithDescriptions)

        verify(customPermissionAdapter).addPermissionList(
            any(),
            any(),
            any(),
        )
    }

    @Test
    fun `addPermissionClickListeners should register view onClick listener or onKey listener`() {
        val permissionAlertDialog: AlertDialog = mock()
        val customPermissionLayout: View = mock()
        val textPermissionSave: TextView = mock()
        val textPermissionCloseWindow: TextView = mock()
        val customPermissionAdapter: MiniAppCustomPermissionAdapter = mock()
        permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))
        permissionWindow.customPermissionLayout = mock()

        doReturn(permissionAlertDialog).whenever(permissionWindow).customPermissionAlertDialog
        doReturn(textPermissionSave).whenever(customPermissionLayout)
            .findViewById<TextView>(R.id.permissionSave)
        doReturn(textPermissionCloseWindow).whenever(customPermissionLayout)
            .findViewById<TextView>(R.id.permissionCloseWindow)
        doReturn(customPermissionLayout).whenever(permissionWindow).customPermissionLayout
        doReturn(customPermissionAdapter).whenever(permissionWindow).customPermissionAdapter

        permissionWindow.addPermissionClickListeners()

        verify(customPermissionLayout).findViewById<TextView>(R.id.permissionSave)
        verify(customPermissionLayout).findViewById<TextView>(R.id.permissionCloseWindow)
        verify(textPermissionSave).setOnClickListener(any())
        verify(textPermissionCloseWindow).setOnClickListener(any())
        verify(permissionAlertDialog).setOnKeyListener(any())
    }

    @Test
    fun `onNoPermissionsSaved should send cached custom permissions`() {
        val mockDialog: AlertDialog = mock()
        permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))

        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog

        permissionWindow.onNoPermissionsSaved()

        verify(dispatcher).sendCachedCustomPermissions()
        verify(mockDialog).dismiss()
    }
}
