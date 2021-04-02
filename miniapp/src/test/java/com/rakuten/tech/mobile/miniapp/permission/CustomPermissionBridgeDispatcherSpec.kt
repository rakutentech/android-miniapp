package com.rakuten.tech.mobile.miniapp.permission

import com.google.gson.Gson
import org.mockito.kotlin.*
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_ATP_LIST
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_VERSION_ID
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.*
import com.rakuten.tech.mobile.miniapp.storage.CachedManifest
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import kotlin.test.assertEquals

@SuppressWarnings("LongMethod", "LargeClass")
class CustomPermissionBridgeDispatcherSpec {

    private lateinit var customPermissionBridgeDispatcher: CustomPermissionBridgeDispatcher
    private val webViewListener: WebViewListener = mock()
    private val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))
    private var miniAppCustomPermissionCache: MiniAppCustomPermissionCache = mock()
    private var downloadedManifestCache: DownloadedManifestCache = mock()
    private val miniAppId: String = TEST_MA_ID
    private val miniAppCustomPermission = MiniAppCustomPermission(
        miniAppId,
        listOf(
            Pair(
                MiniAppCustomPermissionType.USER_NAME,
                MiniAppCustomPermissionResult.DENIED
            )
        )
    )
    private val customPermissionCallbackObj = CustomPermissionCallbackObj(
        action = ActionType.REQUEST_CUSTOM_PERMISSIONS.action,
        param = CustomPermission(
            listOf(CustomPermissionObj(MiniAppCustomPermissionType.USER_NAME.type, ""))
        ),
        id = TEST_CALLBACK_ID
    )
    private val permissions: List<Pair<MiniAppCustomPermissionType, String>> =
        listOf(Pair(MiniAppCustomPermissionType.USER_NAME, ""))
    private val cachedManifest = CachedManifest(
        TEST_MA_VERSION_ID,
        MiniAppManifest(
            listOf(Pair(MiniAppCustomPermissionType.USER_NAME, "")),
            listOf(Pair(MiniAppCustomPermissionType.LOCATION, "")),
            TEST_ATP_LIST,
            emptyMap()
        )
    )

    @Before
    fun setUp() {
        doReturn(miniAppCustomPermission).whenever(miniAppCustomPermissionCache)
            .readPermissions(miniAppId)
        customPermissionBridgeDispatcher = spy(createCustomPermissionBridgeDispatcher())
        customPermissionBridgeDispatcher.permissionsAsManifest = permissions
    }

    @Test
    fun `preparePermissionsWithDescription should return a list of pair with name and description`() {
        val description = "dummy description"
        val customPermissionObj = CustomPermissionObj(
            "rakuten.miniapp.user.USER_NAME",
            description
        )
        When calling downloadedManifestCache.readDownloadedManifest(miniAppId) itReturns cachedManifest
        val actual = customPermissionBridgeDispatcher.preparePermissionsWithDescription(
            arrayListOf(customPermissionObj)
        )
        val expected = listOf(Pair(MiniAppCustomPermissionType.USER_NAME, description))
        assertEquals(expected, actual)
        verify(customPermissionBridgeDispatcher).getRequiredPermissions(
            arrayListOf(Pair(MiniAppCustomPermissionType.USER_NAME, description)), cachedManifest
        )
        verify(customPermissionBridgeDispatcher).getOptionalPermissions(
            arrayListOf(Pair(MiniAppCustomPermissionType.USER_NAME, description)), cachedManifest
        )
    }

    @Test
    fun `getRequiredPermissions should return the correct values`() {
        val description = "dummy description"
        val actual = customPermissionBridgeDispatcher.getRequiredPermissions(
            arrayListOf(Pair(MiniAppCustomPermissionType.USER_NAME, description)), cachedManifest
        )
        val expected = listOf(Pair(MiniAppCustomPermissionType.USER_NAME, description))
        assertEquals(expected, actual)
    }

    @Test
    fun `getOptionalPermissions should return the correct values`() {
        val description = "dummy description"
        val actual = customPermissionBridgeDispatcher.getOptionalPermissions(
            arrayListOf(Pair(MiniAppCustomPermissionType.LOCATION, description)), cachedManifest
        )
        val expected = listOf(Pair(MiniAppCustomPermissionType.LOCATION, description))
        assertEquals(expected, actual)
    }

    @Test
    fun `filterDeniedPermissions should return empty list permissionsWithDescription is empty`() {
        val emptyPermissionCallbackObj = CustomPermissionCallbackObj(
            action = ActionType.REQUEST_CUSTOM_PERMISSIONS.action,
            param = CustomPermission(emptyList()),
            id = TEST_CALLBACK_ID
        )
        val dispatcher = createCustomPermissionBridgeDispatcher(jsonStr = Gson().toJson(emptyPermissionCallbackObj))

        assertEquals(emptyList(), dispatcher.filterDeniedPermissions())
    }

    @Test
    fun `filterDeniedPermissions should return correct list permissionsWithDescription is not empty`() {
        val expected = listOf(Pair(MiniAppCustomPermissionType.USER_NAME, ""))
        assertEquals(expected, customPermissionBridgeDispatcher.filterDeniedPermissions())
    }

    @Test
    fun `filterDeniedPermissions should use hasPermission from MiniAppCustomPermissionCache`() {
        customPermissionBridgeDispatcher.permissionsAsManifest = permissions
        customPermissionBridgeDispatcher.filterDeniedPermissions()
        verify(miniAppCustomPermissionCache).hasPermission(TEST_MA_ID, MiniAppCustomPermissionType.USER_NAME)
    }

    @Test
    fun `createJsonResponse should return json string based on supplied custom permissions`() {
        val actual = customPermissionBridgeDispatcher.createJsonResponse()
        val expected =
            "{\"permissions\":[{\"name\":\"rakuten.miniapp.user.USER_NAME\",\"status\":\"DENIED\"}]}"

        assertEquals(expected, actual)
    }

    @Test
    fun `createJsonResponse should return error string based on retrieved custom permissions`() {
        val emptyPermissionCallbackObj = CustomPermissionCallbackObj(
            action = ActionType.REQUEST_CUSTOM_PERMISSIONS.action,
            param = CustomPermission(emptyList()),
            id = TEST_CALLBACK_ID
        )
        val dispatcher = createCustomPermissionBridgeDispatcher(jsonStr = Gson().toJson(emptyPermissionCallbackObj))

        val actual = dispatcher.createJsonResponse()
        val expected = "Cannot request custom permissions: {}"

        assertEquals(expected, actual)
    }

    @Test
    fun `retrievePermissionsForJson should return correct values based on known custom permissions`() {
        val actual = customPermissionBridgeDispatcher.retrievePermissionsForJson()
        val expected = miniAppCustomPermission.pairValues

        assertEquals(expected, actual)
    }

    @Test
    fun `retrievePermissionsForJson should return correct values based on unknown custom permissions`() {
        val unknownPermissionCallbackObj = CustomPermissionCallbackObj(
            action = ActionType.REQUEST_CUSTOM_PERMISSIONS.action,
            param = CustomPermission(
                listOf(CustomPermissionObj(MiniAppCustomPermissionType.UNKNOWN.type, ""))
            ),
            id = TEST_CALLBACK_ID
        )
        val manifest = CachedManifest(
            TEST_MA_VERSION_ID,
            MiniAppManifest(
                listOf(Pair(MiniAppCustomPermissionType.UNKNOWN, "")), emptyList(), TEST_ATP_LIST, emptyMap()
            )
        )
        When calling downloadedManifestCache.readDownloadedManifest(miniAppId) itReturns manifest
        val dispatcher = createCustomPermissionBridgeDispatcher(jsonStr = Gson().toJson(unknownPermissionCallbackObj))

        val actual = dispatcher.retrievePermissionsForJson()
        val expected = listOf(
            Pair(
                MiniAppCustomPermissionType.UNKNOWN,
                MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
            )
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `should store permissions in cache while sending result by HostApp`() {
        val permissionsWithResult = listOf(
            Pair(
                MiniAppCustomPermissionType.USER_NAME,
                MiniAppCustomPermissionResult.ALLOWED
            )
        )
        val permissionsToStore = MiniAppCustomPermission(TEST_MA_ID, permissionsWithResult)

        customPermissionBridgeDispatcher.sendHostAppCustomPermissions(permissionsWithResult)

        verify(miniAppCustomPermissionCache).storePermissions(permissionsToStore)
    }

    @Test
    fun `should create json response while sending result by HostApp`() {
        val dispatcher = spy(createCustomPermissionBridgeDispatcher())
        val permissionsWithResult = listOf(
            Pair(
                MiniAppCustomPermissionType.USER_NAME,
                MiniAppCustomPermissionResult.ALLOWED
            )
        )

        dispatcher.sendHostAppCustomPermissions(permissionsWithResult)
        verify(dispatcher).createJsonResponse()
    }

    @Test
    fun `should post correct value by postCustomPermissionsValue`() {
        val jsonResult =
            "{\"permissions\":[{\"name\":\"rakuten.miniapp.user.USER_NAME\",\"status\":\"DENIED\"}]}"
        customPermissionBridgeDispatcher.postCustomPermissionsValue(jsonResult)

        verify(bridgeExecutor, times(1)).postValue(TEST_CALLBACK_ID, jsonResult)
    }

    @Test
    fun `should post correct error message by postCustomPermissionError`() {
        val errMessage = "dummy error"
        customPermissionBridgeDispatcher.postCustomPermissionError(errMessage)

        verify(bridgeExecutor, times(1)).postError(
            TEST_CALLBACK_ID,
            "${ErrorBridgeMessage.ERR_REQ_CUSTOM_PERMISSION} $errMessage"
        )
    }

    private fun createCustomPermissionBridgeDispatcher(
        miniAppId: String = this.miniAppId,
        jsonStr: String = Gson().toJson(customPermissionCallbackObj)
    ) = CustomPermissionBridgeDispatcher(
        bridgeExecutor = bridgeExecutor,
        customPermissionCache = miniAppCustomPermissionCache,
        downloadedManifestCache = downloadedManifestCache,
        miniAppId = miniAppId,
        jsonStr = jsonStr
    )
}
