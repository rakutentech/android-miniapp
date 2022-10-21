package com.rakuten.tech.mobile.miniapp.js

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_MAX_STORAGE_SIZE_IN_BYTES
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.errors.MiniAppSecureStorageError
import com.rakuten.tech.mobile.miniapp.storage.MiniAppSecureStorage
import com.rakuten.tech.mobile.miniapp.storage.database.MiniAppSecureDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.mockito.kotlin.mock
import kotlin.test.expect

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Suppress("LargeClass")
class MiniAppSecureStorageDispatcherSpec {
    private val testKey = "key"
    private val testValue = "value"
    private val testItems: Map<String, String> = mapOf(testKey to testValue)
    private val secureStorageCallbackObj = SecureStorageCallbackObj(
        action = ActionType.SECURE_STORAGE_SET_ITEMS.action,
        param = SecureStorageItems(testItems),
        id = TEST_CALLBACK_ID
    )
    private val getItemCallbackObj = GetItemCallbackObj(
        action = ActionType.SECURE_STORAGE_SET_ITEMS.action,
        param = SecureStorageKey(testKey),
        id = TEST_CALLBACK_ID
    )
    private val deleteItemsCallbackObj = DeleteItemsCallbackObj(
        action = ActionType.SECURE_STORAGE_SET_ITEMS.action,
        param = SecureStorageKeyList(setOf(testKey)),
        id = TEST_CALLBACK_ID
    )
    private val setItemsJsonStr = Gson().toJson(secureStorageCallbackObj)
    private val getItemsJsonStr = Gson().toJson(getItemCallbackObj)
    private val deleteItemsJsonStr = Gson().toJson(deleteItemsCallbackObj)

    private val webViewListener: WebViewListener = mock()
    private val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))
    private lateinit var miniAppSecureStorageDispatcher: MiniAppSecureStorageDispatcher
    private val testMaxStorageSizeInKB = 1000L
    private val miniAppSecureStorage: MiniAppSecureStorage = mock()
    private val testJsonError = "Can not parse secure storage json object."

    @Before
    fun setUp() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            miniAppSecureStorageDispatcher =
                MiniAppSecureStorageDispatcher(activity, testMaxStorageSizeInKB)
            miniAppSecureStorageDispatcher.setBridgeExecutor(bridgeExecutor)
            miniAppSecureStorageDispatcher.setMiniAppComponents(TEST_MA_ID)
            miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        }
    }

    @After
    fun tearDown() {
        Mockito.reset(bridgeExecutor)
    }

    @Test
    fun `onLoad should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher =
            Mockito.spy(MiniAppSecureStorageDispatcher(mock(), testMaxStorageSizeInKB))
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onLoad()
        verify(miniAppSecureStorageDispatcher.miniAppSecureStorage, times(0)).load(
            TEST_MA_ID, {}, {})
    }

    /**
     * Starting of insert items test cases
     */
    @Test
    fun `onSetItems should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher =
            Mockito.spy(MiniAppSecureStorageDispatcher(mock(), testMaxStorageSizeInKB))
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onSetItems(TEST_CALLBACK_ID, setItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.miniAppSecureStorage, times(0)).insertItems(
            testItems, {}, {}
        )
    }

    @Test
    fun `postError should be called if can not parse the jsonStr`() {
        val errMsg = testJsonError
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onSetItems(TEST_CALLBACK_ID, "")
        verify(bridgeExecutor, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `insertItems should be called if successfully parsed the jsonStr`() {
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onSetItems(TEST_CALLBACK_ID, setItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.miniAppSecureStorage, times(1)).insertItems(
            testItems,
            miniAppSecureStorageDispatcher.onSuccess,
            miniAppSecureStorageDispatcher.onFailed
        )
    }

    @Test
    fun `onSuccess should be called if successfully insertItems`() {
        setupMiniAppSetItemsStorageDispatcher()
        val job = runBlockingTest {
            miniAppSecureStorageDispatcher.onSuccess.invoke()
        }

        miniAppSecureStorageDispatcher.setBridgeExecutor(bridgeExecutor)

        miniAppSecureStorageDispatcher.onSetItems(TEST_CALLBACK_ID, setItemsJsonStr)
        // penyebab 2
        miniAppSecureStorageDispatcher.onSuccess.invoke().shouldBe(job)

        expect(job, miniAppSecureStorageDispatcher.onSuccess)

        verify(bridgeExecutor, times(2)).postValue(
            TEST_CALLBACK_ID,
            MiniAppSecureStorageDispatcher.SAVE_SUCCESS_SECURE_STORAGE
        )
        verify(bridgeExecutor, times(0)).postError(
            TEST_CALLBACK_ID,
            Gson().toJson(testJsonError)
        )
        verify(bridgeExecutor, times(0)).postError(
            TEST_CALLBACK_ID,
            MiniAppSecureStorageDispatcher.ERR_WRONG_JSON_FORMAT
        )
        verify(webViewListener, times(2)).runSuccessCallback(
            TEST_CALLBACK_ID,
            MiniAppSecureStorageDispatcher.SAVE_SUCCESS_SECURE_STORAGE
        )
    }

    @Test
    fun `bridgeExecutor should call postValue if successfully insertItems`() {
        setupMiniAppSetItemsStorageDispatcher()
        val job = runBlockingTest {
            miniAppSecureStorageDispatcher.onSuccess.invoke()
        }
        miniAppSecureStorageDispatcher.onSetItems(TEST_CALLBACK_ID, setItemsJsonStr)
        miniAppSecureStorageDispatcher.onSuccess.invoke().shouldBe(job)
        verify(bridgeExecutor).postValue(
            TEST_CALLBACK_ID,
            MiniAppSecureStorageDispatcher.SAVE_SUCCESS_SECURE_STORAGE
        )
    }

    private fun setupMiniAppSetItemsStorageDispatcher() {
        val miniAppSecureStorage: MiniAppSecureStorage = spy(
            MiniAppSecureStorage(
                mock(),
                1,
                TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()
            )
        )

        miniAppSecureStorage.databaseName = DB_NAME_PREFIX
        miniAppSecureStorage.miniAppSecureDatabase = spy(
            MiniAppSecureDatabase(
                mock(),
                DB_NAME_PREFIX,
                1,
                TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()
            )
        )

        val scope = CoroutineScope(Dispatchers.IO)

        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.miniAppSecureStorage.scope = scope
        miniAppSecureStorageDispatcher.onSuccess = spy()
        miniAppSecureStorageDispatcher.onFailed = spy()
    }

    @Test
    fun `webViewListener should call runSuccessCallback if onSuccess is called`() {
        setupMiniAppSetItemsStorageDispatcher()
        val job = runBlockingTest {
            miniAppSecureStorageDispatcher.onSuccess.invoke()
        }
        miniAppSecureStorageDispatcher.onSetItems(TEST_CALLBACK_ID, setItemsJsonStr)
        miniAppSecureStorageDispatcher.onSuccess.invoke().shouldBe(job)
        verify(webViewListener, times(1)).runSuccessCallback(
            TEST_CALLBACK_ID,
            MiniAppSecureStorageDispatcher.SAVE_SUCCESS_SECURE_STORAGE
        )
    }

    /** end region */

    @Test
    fun `onGetItem should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher =
            Mockito.spy(MiniAppSecureStorageDispatcher(mock(), testMaxStorageSizeInKB))
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onGetItem(TEST_CALLBACK_ID, getItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.miniAppSecureStorage, times(0)).getItem(
            getItemCallbackObj.param.secureStorageKey, {}, {}
        )
    }

    @Test
    fun `postError should be called if can not parse the jsonStr for getItem`() {
        val errMsg = testJsonError
        miniAppSecureStorageDispatcher.onGetItem(TEST_CALLBACK_ID, "")
        verify(bridgeExecutor, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `getItem should be called if can successfully parse the jsonStr and cached item is null`() {
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onGetItem(TEST_CALLBACK_ID, getItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.miniAppSecureStorage, times(1)).getItem(
            testKey,
            miniAppSecureStorageDispatcher.onSuccessGetItem,
            miniAppSecureStorageDispatcher.onFailed
        )
    }

    @Test
    fun `bridgeExecutor should call postError if an exception is thrown`() {
        setupMiniAppSetItemsStorageDispatcher()
        val job = runBlockingTest {
            miniAppSecureStorageDispatcher.onFailed.invoke(mock())
        }
        miniAppSecureStorageDispatcher.onGetItem(TEST_CALLBACK_ID, getItemsJsonStr)
        //miniAppSecureDatabaseHasNotInitialized
        miniAppSecureStorageDispatcher.onFailed.invoke(mock()).shouldBe(job)
        verify(bridgeExecutor).postError(
            TEST_CALLBACK_ID,
            "{}"
        )
    }

    /** end region */

    /** region: remove items from secure storage */
    @Test
    fun `onRemoveItems should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher =
            Mockito.spy(MiniAppSecureStorageDispatcher(mock(), testMaxStorageSizeInKB))
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onRemoveItems(TEST_CALLBACK_ID, deleteItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.miniAppSecureStorage, times(0)).deleteItems(
            deleteItemsCallbackObj.param.secureStorageKeyList, {}, {})
    }

    @Test
    fun `postError should be called if can not parse the jsonStr for removeItems`() {
        val errMsg = testJsonError
        miniAppSecureStorageDispatcher.onRemoveItems(TEST_CALLBACK_ID, "")
        verify(bridgeExecutor, times(1)).postError(TEST_CALLBACK_ID, errMsg)
    }

    @Test
    fun `deleteItems should be called if can successfully parse the jsonStr`() {
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onRemoveItems(TEST_CALLBACK_ID, deleteItemsJsonStr)
        verify(miniAppSecureStorageDispatcher.miniAppSecureStorage, times(1)).deleteItems(
            setOf(testKey),
            miniAppSecureStorageDispatcher.onSuccess,
            miniAppSecureStorageDispatcher.onFailed
        )
    }

    @Test
    fun `onClearAll should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher =
            Mockito.spy(MiniAppSecureStorageDispatcher(mock(), testMaxStorageSizeInKB))
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onClearAll(TEST_CALLBACK_ID)
        verify(miniAppSecureStorageDispatcher.miniAppSecureStorage, times(0)).delete(
            {}, {})
    }

    @Test
    fun `delete should be called for onClearAll`() {
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onClearAll(TEST_CALLBACK_ID)
        verify(miniAppSecureStorageDispatcher.miniAppSecureStorage, times(1)).delete(
            miniAppSecureStorageDispatcher.onSuccess,
            miniAppSecureStorageDispatcher.onFailed
        )
    }

    @Test
    fun `clearSecureStorage should be called with the same mini app id`() {
        val dbName = "database"
        miniAppSecureStorageDispatcher.context = mock()
        miniAppSecureStorageDispatcher.clearSecureStorage(dbName)
        verify(miniAppSecureStorageDispatcher.context, times(1)).deleteDatabase(
            DB_NAME_PREFIX + dbName
        )
    }

    @Test
    fun `clearSecureStorage should be called without the mini app id`() {
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.clearSecureStorages()
        verify(
            miniAppSecureStorageDispatcher.miniAppSecureStorage,
            times(1)
        ).closeDatabase()
    }

    /** end region */

    /** region: size of secure storage */
    @Test
    fun `onSize should not be working if initialization is not completed`() {
        val miniAppSecureStorageDispatcher =
            Mockito.spy(MiniAppSecureStorageDispatcher(mock(), testMaxStorageSizeInKB))
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onSize(TEST_CALLBACK_ID)
        verify(
            miniAppSecureStorageDispatcher.miniAppSecureStorage,
            times(0)
        ).getDatabaseUsedSize() {}
    }

    @Suppress("UnusedPrivateMember", "Deprecation")
    @Test
    fun `secureStorageSize should be called for onSize`() {
        miniAppSecureStorageDispatcher.miniAppSecureStorage = miniAppSecureStorage
        miniAppSecureStorageDispatcher.onSize(TEST_CALLBACK_ID)
        verify(
            miniAppSecureStorageDispatcher.miniAppSecureStorage,
            times(1)
        ).getDatabaseUsedSize(
            miniAppSecureStorageDispatcher.onSuccessDBSize
        )
    }
    /** end region */
}
