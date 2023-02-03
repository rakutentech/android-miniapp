package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_ATP_LIST
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import com.rakuten.tech.mobile.miniapp.permission.*
import com.rakuten.tech.mobile.miniapp.permission.ui.MiniAppCustomPermissionWindow
import com.rakuten.tech.mobile.miniapp.storage.CachedManifest
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class CustomPermissionBridgeSpec : BridgeCommon() {

    private lateinit var customPermissionBridgeDispatcher: CustomPermissionBridgeDispatcher
    private val miniappMessageBridge: MiniAppMessageBridge = Mockito.spy(
        createMiniAppMessageBridge(true)
    )

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val prefs = context.getSharedPreferences("test-cache", Context.MODE_PRIVATE)

    private val customPermissionManifest: List<Pair<MiniAppCustomPermissionType, String>> =
        listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "description"))

    private val customPermissionCallbackObj = CustomPermissionCallbackObj(
        action = ActionType.REQUEST_CUSTOM_PERMISSIONS.action,
        param = CustomPermission(
            listOf(CustomPermissionObj(MiniAppCustomPermissionType.USER_NAME.type, "descriptiono"))
        ),
        id = TEST_CALLBACK_ID
    )

    private fun getCallbackJson(): CallbackObj {
        return CallbackObj(
            customPermissionCallbackObj.action,
            customPermissionCallbackObj.param,
            customPermissionCallbackObj.id
        )
    }

    private lateinit var customPermissionCache: MiniAppCustomPermissionCache
    private lateinit var downloadedManifestCache: DownloadedManifestCache
    private val cachedManifest = CachedManifest(
        TEST_MA_VERSION_ID,
        MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "")),
            listOf(Pair(MiniAppCustomPermissionType.LOCATION, "")),
            TEST_ATP_LIST, emptyMap(), TEST_MA_VERSION_ID
        )
    )
    private lateinit var customPermissionWindow: MiniAppCustomPermissionWindow
    lateinit var activity: Activity

    @Before
    fun setUp() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            this.activity = activity
            downloadedManifestCache = spy(DownloadedManifestCache(activity))
            When calling downloadedManifestCache.readFromCachedFile(TEST_MA_ID) itReturns cachedManifest
            customPermissionCache = spy(MiniAppCustomPermissionCache(prefs, prefs))
            customPermissionBridgeDispatcher =
                CustomPermissionBridgeDispatcher(
                    bridgeExecutor = bridgeExecutor,
                    customPermissionCache = customPermissionCache,
                    downloadedManifestCache = downloadedManifestCache,
                    miniAppId = TEST_MA_ID,
                    jsonStr = Gson().toJson(getCallbackJson())

                )

            customPermissionWindow =
                spy(MiniAppCustomPermissionWindow(activity, customPermissionBridgeDispatcher))
            customPermissionBridgeDispatcher.permissionsAsManifest = customPermissionManifest
            miniappMessageBridge.setComponentsIAPDispatcher(mock())
            miniappMessageBridge.init(
                activity = activity,
                webViewListener = webViewListener,
                customPermissionCache = customPermissionCache,
                downloadedManifestCache = downloadedManifestCache,
                miniAppId = TEST_MA_ID,
                ratDispatcher = mock(),
                secureStorageDispatcher = mock()
            )

            When calling miniappMessageBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor

            miniappMessageBridge.setMiniAppFileDownloader(mock())
            miniappMessageBridge.setChatBridgeDispatcher(mock())
        }
    }

    @Test
    fun `should call getCustomPermissionBridgeDispatcher when action type is REQUEST_CUSTOM_PERMISSIONS `() {
        val callbackJson = getCallbackObjToJsonStr(getCallbackJson())
        miniappMessageBridge.postMessage(callbackJson)
        verify(miniappMessageBridge).getCustomPermissionBridgeDispatcher(callbackJson)
    }

    @Test
    fun `should call requestCustomPermissions when action type is REQUEST_CUSTOM_PERMISSIONS`() {
        val callbackJson = getCallbackObjToJsonStr(getCallbackJson())
        miniappMessageBridge.postMessage(callbackJson)
        verify(miniappMessageBridge).onRequestCustomPermissions(callbackJson)
    }
}
