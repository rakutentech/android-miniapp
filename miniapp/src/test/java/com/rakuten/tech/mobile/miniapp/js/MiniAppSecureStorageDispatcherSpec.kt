package com.rakuten.tech.mobile.miniapp.js

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TestActivity
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.storage.MiniAppSecureStorage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class MiniAppSecureStorageDispatcherSpec {
    private val TEST_ITEMS: Map<String, String> = mapOf("key" to "value")
    private val secureStorageCallbackObj = SecureStorageCallbackObj(
        action = ActionType.SECURE_STORAGE_SET_ITEMS.action,
        param = SecureStorageItems(TEST_ITEMS),
        id = TEST_CALLBACK_ID
    )
    private val getItemCallbackObj = GetItemCallbackObj(
        action = ActionType.SECURE_STORAGE_SET_ITEMS.action,
        param = SecureStorageKey("key"),
        id = TEST_CALLBACK_ID
    )
    private val deleteItemsCallbackObj = DeleteItemsCallbackObj(
        action = ActionType.SECURE_STORAGE_SET_ITEMS.action,
        param = SecureStorageKeyList(setOf("key")),
        id = TEST_CALLBACK_ID
    )
    private val setItemsJsonStr = Gson().toJson(secureStorageCallbackObj)
    private val getItemsJsonStr = Gson().toJson(secureStorageCallbackObj)
    private val deleteItemsJsonStr = Gson().toJson(secureStorageCallbackObj)

    private val webViewListener: WebViewListener = mock()
    private val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))
    private lateinit var miniAppSecureStorageDispatcher: MiniAppSecureStorageDispatcher
    private val activity: TestActivity = mock()
    private val MAX_STORAGE_SIZE_KB = 1000
    private val miniAppSecureStorage: MiniAppSecureStorage = mock()

    @Before
    fun setUp() {
        miniAppSecureStorageDispatcher = MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB)
        miniAppSecureStorageDispatcher.setBridgeExecutor(activity, bridgeExecutor)
        miniAppSecureStorageDispatcher.setMiniAppComponents(TEST_MA_ID)
    }

    @Test
    fun `onLoad should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher = Mockito.spy(MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB))
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onLoad()
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).load(
            TEST_MA_ID, {}, {})
    }

    @Test
    fun `onSetItems should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher = Mockito.spy(MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB))
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onSetItems(TEST_CALLBACK_ID, setItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).isSecureStorageAvailable(
            TEST_MA_ID, MAX_STORAGE_SIZE_KB)
    }

    @Test
    fun `onGetItem should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher = Mockito.spy(MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB))
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onGetItem(TEST_CALLBACK_ID, getItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).getItem(
            TEST_MA_ID, getItemCallbackObj.param.secureStorageKey
        ) {}
    }

    @Test
    fun `onRemoveItems should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher = Mockito.spy(MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB))
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onRemoveItems(TEST_CALLBACK_ID, deleteItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).deleteItems(
            TEST_MA_ID, deleteItemsCallbackObj.param.secureStorageKeyList, {}, {})
    }

    @Test
    fun `onClearAll should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher = Mockito.spy(MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB))
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onClearAll(TEST_CALLBACK_ID)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).delete(
            TEST_MA_ID, {}, {})
    }

    @Test
    fun `onSize should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher = Mockito.spy(MiniAppSecureStorageDispatcher(MAX_STORAGE_SIZE_KB))
        miniAppSecureStorageDispatcher.secureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onSize(TEST_CALLBACK_ID)
        verify(miniAppSecureStorageDispatcher.secureStorage, times(0)).secureStorageSize(
            TEST_MA_ID
        ) {}
    }
}
