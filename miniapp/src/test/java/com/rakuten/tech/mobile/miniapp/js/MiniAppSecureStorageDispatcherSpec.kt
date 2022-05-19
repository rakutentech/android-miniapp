package com.rakuten.tech.mobile.miniapp.js

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TestActivity
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.errors.MiniAppSecureStorageError
import com.rakuten.tech.mobile.miniapp.storage.MiniAppSecureStorage
import com.rakuten.tech.mobile.miniapp.storage.StorageState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Suppress("LargeClass")
class MiniAppSecureStorageDispatcherSpec {
    private val TEST_KEY = "key"
    private val TEST_VALUE = "value"
    private val TEST_ITEMS: Map<String, String> = mapOf(TEST_KEY to TEST_VALUE)
    private val secureStorageCallbackObj = SecureStorageCallbackObj(
        action = ActionType.SECURE_STORAGE_SET_ITEMS.action,
        param = SecureStorageItems(TEST_ITEMS),
        id = TEST_CALLBACK_ID
    )
    private val getItemCallbackObj = GetItemCallbackObj(
        action = ActionType.SECURE_STORAGE_SET_ITEMS.action,
        param = SecureStorageKey(TEST_KEY),
        id = TEST_CALLBACK_ID
    )
    private val deleteItemsCallbackObj = DeleteItemsCallbackObj(
        action = ActionType.SECURE_STORAGE_SET_ITEMS.action,
        param = SecureStorageKeyList(setOf(TEST_KEY)),
        id = TEST_CALLBACK_ID
    )
    private val setItemsJsonStr = Gson().toJson(secureStorageCallbackObj)
    private val getItemsJsonStr = Gson().toJson(getItemCallbackObj)
    private val deleteItemsJsonStr = Gson().toJson(deleteItemsCallbackObj)

    private val webViewListener: WebViewListener = mock()
    private val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))
    private lateinit var miniAppSecureStorageDispatcher: MiniAppSecureStorageDispatcher
    private val MAX_STORAGE_SIZE_KB = 1000
    private val miniAppSecureStorage: MiniAppSecureStorage = mock()
    private val JSON_ERROR = "Can not parse secure storage json object"

    @Before
    fun setUp() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            miniAppSecureStorageDispatcher = MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB)
            miniAppSecureStorageDispatcher.setBridgeExecutor(activity, bridgeExecutor)
            miniAppSecureStorageDispatcher.setMiniAppComponents(TEST_MA_ID)
        }
    }

    @After
    fun tearDown() {
        Mockito.reset(bridgeExecutor)
    }

    @Test
    fun `onLoad should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher =
            Mockito.spy(MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB))
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onLoad()
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).load(
            TEST_MA_ID, {}, {})
    }

    /** region: set item to secure storage */
    @Test
    fun `onSetItems should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher =
            Mockito.spy(MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB))
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onSetItems(TEST_CALLBACK_ID, setItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).isSecureStorageAvailable(
            TEST_MA_ID, MAX_STORAGE_SIZE_KB
        )
    }

    @Test
    fun `postError should be called if storage is not available`() {
        val errMsg = Gson().toJson(MiniAppSecureStorageError.secureStorageFullError)
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        When calling miniAppSecureStorageDispatcher.secureStorage.isSecureStorageAvailable(
            TEST_MA_ID, MAX_STORAGE_SIZE_KB
        ) itReturns false
        miniAppSecureStorageDispatcher.onSetItems(TEST_CALLBACK_ID, setItemsJsonStr)
        verify(bridgeExecutor, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called if can not parse the jsonStr`() {
        val errMsg = JSON_ERROR
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        When calling miniAppSecureStorageDispatcher.secureStorage.isSecureStorageAvailable(
            TEST_MA_ID, MAX_STORAGE_SIZE_KB
        ) itReturns true
        miniAppSecureStorageDispatcher.onSetItems(TEST_CALLBACK_ID, "")
        verify(bridgeExecutor, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called if storage is currently busy writing`() {
        val errMsg = Gson().toJson(MiniAppSecureStorageError.secureStorageBusyError)
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        When calling miniAppSecureStorageDispatcher.secureStorage.isSecureStorageAvailable(
            TEST_MA_ID, MAX_STORAGE_SIZE_KB
        ) itReturns true
        miniAppSecureStorageDispatcher.storageState = StorageState.LOCK
        miniAppSecureStorageDispatcher.onSetItems(TEST_CALLBACK_ID, setItemsJsonStr)
        verify(bridgeExecutor, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `insertItems should be called if can successfully parse the jsonStr`() {
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        When calling miniAppSecureStorageDispatcher.secureStorage.isSecureStorageAvailable(
            TEST_MA_ID, MAX_STORAGE_SIZE_KB
        ) itReturns true

        miniAppSecureStorageDispatcher.onSetItems(TEST_CALLBACK_ID, setItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(1)).insertItems(
            TEST_MA_ID,
            TEST_ITEMS,
            miniAppSecureStorageDispatcher.onSuccess,
            miniAppSecureStorageDispatcher.onFailed
        )
    }

    /** end region */

    /** region: get item to secure storage */
    @Test
    fun `onGetItem should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher =
            Mockito.spy(MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB))
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onGetItem(TEST_CALLBACK_ID, getItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).getItem(
            TEST_MA_ID, getItemCallbackObj.param.secureStorageKey
        ) {}
    }

    @Test
    fun `postError should be called if can not parse the jsonStr for getItem`() {
        val errMsg = JSON_ERROR
        miniAppSecureStorageDispatcher.onGetItem(TEST_CALLBACK_ID, "")
        verify(bridgeExecutor, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `getItem should be called if can successfully parse the jsonStr and cached item is null`() {
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onGetItem(TEST_CALLBACK_ID, getItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(1)).getItem(
            TEST_MA_ID,
            TEST_KEY,
            miniAppSecureStorageDispatcher.onSuccessGetItem
        )
    }

    @Test
    fun `getItem should not be called if can successfully parse the jsonStr and cached item is not null`() {
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.cachedItems = mapOf(TEST_KEY to TEST_VALUE)
        miniAppSecureStorageDispatcher.onSuccessGetItem = {}
        miniAppSecureStorageDispatcher.onGetItem(TEST_CALLBACK_ID, getItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).getItem(
            TEST_MA_ID,
            TEST_KEY,
            miniAppSecureStorageDispatcher.onSuccessGetItem
        )
    }

    @Test
    fun `postValue should return value from cached item if it is available`() {
        miniAppSecureStorageDispatcher.cachedItems = mapOf(TEST_KEY to TEST_VALUE)
        miniAppSecureStorageDispatcher.onSuccessGetItem = {}
        miniAppSecureStorageDispatcher.onGetItem(TEST_CALLBACK_ID, getItemsJsonStr)
        verify(bridgeExecutor, times(1)).postValue(TEST_CALLBACK_ID, TEST_VALUE)
    }

    @Test
    fun `postValue should return null from cached item if it is not available`() {
        miniAppSecureStorageDispatcher.cachedItems = mapOf("key2" to TEST_VALUE)
        miniAppSecureStorageDispatcher.onSuccessGetItem = {}
        miniAppSecureStorageDispatcher.onGetItem(TEST_CALLBACK_ID, getItemsJsonStr)
        verify(bridgeExecutor, times(1)).postValue(TEST_CALLBACK_ID, "null")
    }

    /** end region */

    /** region: remove items from secure storage */
    @Test
    fun `onRemoveItems should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher =
            Mockito.spy(MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB))
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onRemoveItems(TEST_CALLBACK_ID, deleteItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).deleteItems(
            TEST_MA_ID, deleteItemsCallbackObj.param.secureStorageKeyList, {}, {})
    }

    @Test
    fun `postError should be called if can not parse the jsonStr for removeItems`() {
        val errMsg = JSON_ERROR
        miniAppSecureStorageDispatcher.onRemoveItems(TEST_CALLBACK_ID, "")
        verify(bridgeExecutor, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `postError should be called if storage is currently busy removing`() {
        val errMsg = Gson().toJson(MiniAppSecureStorageError.secureStorageBusyError)
        miniAppSecureStorageDispatcher.storageState = StorageState.LOCK
        miniAppSecureStorageDispatcher.onRemoveItems(TEST_CALLBACK_ID, deleteItemsJsonStr)
        verify(bridgeExecutor, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `deleteItems should be called if can successfully parse the jsonStr`() {
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onRemoveItems(TEST_CALLBACK_ID, deleteItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(1)).deleteItems(
            TEST_MA_ID,
            setOf(TEST_KEY),
            miniAppSecureStorageDispatcher.onSuccess,
            miniAppSecureStorageDispatcher.onFailed
        )
    }

    @Test
    fun `onClearAll should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher =
            Mockito.spy(MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB))
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onClearAll(TEST_CALLBACK_ID)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).delete(
            TEST_MA_ID, {}, {})
    }

    @Test
    fun `delete should be called for onClearAll`() {
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onClearAll(TEST_CALLBACK_ID)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(1)).delete(
            TEST_MA_ID,
            miniAppSecureStorageDispatcher.onSuccessClearSecureStorage,
            miniAppSecureStorageDispatcher.onFailed
        )
    }

    @Test
    fun `clearSecureStorage should be called with the same mini app id`() {
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.clearSecureStorage(TEST_MA_ID)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(1)).clearSecureStorage(
            TEST_MA_ID
        )
    }

    @Test
    fun `clearSecureStorage should be called without the mini app id`() {
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.clearSecureStorage()
        verify(miniAppSecureStorageDispatcher.secureStorage, times(1)).clearSecureStorage()
    }

    @Test
    fun `cleanupSecureStorage should clear the cache`() {
        miniAppSecureStorageDispatcher.cachedItems = TEST_ITEMS
        miniAppSecureStorageDispatcher.cleanupSecureStorage()
        miniAppSecureStorageDispatcher.cachedItems shouldBe null
    }

    @Test
    fun `postValue should return on onClearAll if the file is not available`() {
        miniAppSecureStorageDispatcher.onClearAll(TEST_CALLBACK_ID)
        verify(bridgeExecutor, times(1)).postValue(
            TEST_CALLBACK_ID,
            "Storage removed successfully."
        )
    }

    /** end region */

    /** region: size of secure storage */
    @Test
    fun `onSize should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher =
            Mockito.spy(MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB))
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onSize(TEST_CALLBACK_ID)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).secureStorageSize(
            TEST_MA_ID
        ) {}
    }

    @Test
    fun `secureStorageSize should be called for onSize`() {
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onSize(TEST_CALLBACK_ID)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(1)).secureStorageSize(
            TEST_MA_ID,
            miniAppSecureStorageDispatcher.onSuccessFileSize
        )
    }

    @Test
    fun `postValue should return 0 from if the file is not available`() {
        miniAppSecureStorageDispatcher.onSize(TEST_CALLBACK_ID)
        verify(bridgeExecutor, times(1)).postValue(
            TEST_CALLBACK_ID,
            Gson().toJson(MiniAppSecureStorageSize(0, (MAX_STORAGE_SIZE_KB * 1024).toLong()))
        )
    }
    /** end region */
}
