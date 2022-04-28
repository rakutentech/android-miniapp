package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.errors.MiniAppStorageError
import com.rakuten.tech.mobile.miniapp.storage.MiniAppSecureStorage
import com.rakuten.tech.mobile.miniapp.storage.StorageState

@Suppress("TooManyFunctions", "LargeClass")
internal class MiniAppSecureStorageDispatcher(
    private val storageMaxSizeKB: Int
) {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var activity: Activity
    private lateinit var miniAppId: String
    private lateinit var secureStorage: MiniAppSecureStorage
    private var cachedItems: Map<String, String>? = null
    private var storageState: StorageState = StorageState.DEFAULT
    private val stateObserver = Observer<StorageState> { state ->
        storageState = state
    }

    fun setBridgeExecutor(activity: Activity, bridgeExecutor: MiniAppBridgeExecutor) {
        this.activity = activity
        this.bridgeExecutor = bridgeExecutor
    }

    fun setMiniAppComponents(miniAppId: String) {
        this.miniAppId = miniAppId
        this.secureStorage = MiniAppSecureStorage(activity)
        secureStorage.storageState.observeForever(stateObserver)
        onLoad()
    }

    @Suppress("ComplexCondition")
    private fun <T> whenReady(callback: () -> T) {
        if (this::bridgeExecutor.isInitialized &&
            this::activity.isInitialized &&
            this::miniAppId.isInitialized &&
            this::secureStorage.isInitialized
        ) {
            callback.invoke()
        }
    }

    private fun onLoad() = whenReady {
        val onSuccess = { items: Map<String, String> ->
            cachedItems = items
            bridgeExecutor.dispatchEvent(eventType = NativeEventType.MINIAPP_SECURE_STORAGE_READY.value)
        }
        val onFailed = { error: MiniAppStorageError ->
            bridgeExecutor.dispatchEvent(
                eventType = NativeEventType.MINIAPP_SECURE_STORAGE_LOAD_ERROR.value,
                value = Gson().toJson(error)
            )
        }
        secureStorage.load(miniAppId, onSuccess, onFailed)
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException", "ComplexMethod", "LongMethod")
    fun onSetItems(callbackId: String, jsonStr: String) = whenReady {
        try {
            val callbackObj: SecureStorageCallbackObj? =
                Gson().fromJson(jsonStr, SecureStorageCallbackObj::class.java)
            if (callbackObj != null) {
                val onSuccess = { items: Map<String, String> ->
                    cachedItems = items
                    bridgeExecutor.postValue(callbackId, SAVE_SUCCESS_SECURE_STORAGE)
                }
                val onFailed = { error: MiniAppStorageError ->
                    bridgeExecutor.postError(callbackId, Gson().toJson(error))
                }
                if (storageState != StorageState.LOCK) {
                    cachedItems?.let {
                        val mergedItems = it.toMutableMap().apply { putAll(callbackObj.param.secureStorageItems) }
                        secureStorage.insertItems(
                            miniAppId,
                            mergedItems,
                            onSuccess,
                            onFailed
                        )
                    } ?: kotlin.run {
                        secureStorage.insertItems(
                            miniAppId,
                            callbackObj.param.secureStorageItems,
                            onSuccess,
                            onFailed
                        )
                    }
                } else {
                    bridgeExecutor.postError(
                        callbackId,
                        Gson().toJson(MiniAppStorageError.storageOccupiedError)
                    )
                }
            } else {
                bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
            }
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun onGetItem(callbackId: String, jsonStr: String) = whenReady {
        try {
            val callbackObj: GetItemCallbackObj? =
                Gson().fromJson(jsonStr, GetItemCallbackObj::class.java)
            if (callbackObj != null) {
                cachedItems?.let {
                    if (it.containsKey(callbackObj.param.secureStorageKey)) {
                        bridgeExecutor.postValue(
                            callbackId,
                            it[callbackObj.param.secureStorageKey] ?: ""
                        )
                    } else {
                        bridgeExecutor.postValue(
                            callbackId,
                            "null"
                        )
                    }
                } ?: kotlin.run {
                    val onSuccess = { itemValue: String ->
                        bridgeExecutor.postValue(callbackId, itemValue)
                    }
                    val onFailed = { error: MiniAppStorageError ->
                        bridgeExecutor.postError(callbackId, Gson().toJson(error))
                    }
                    secureStorage.getItem(
                        miniAppId,
                        callbackObj.param.secureStorageKey,
                        onSuccess,
                        onFailed
                    )
                }
            } else {
                bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
            }
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun onRemoveItems(callbackId: String, jsonStr: String) = whenReady {
        try {
            val callbackObj: DeleteItemsCallbackObj? =
                Gson().fromJson(jsonStr, DeleteItemsCallbackObj::class.java)
            if (callbackObj != null) {
                val onSuccess = { items: Map<String, String> ->
                    cachedItems = items
                    bridgeExecutor.postValue(callbackId, REMOVE_ITEMS_SUCCESS_SECURE_STORAGE)
                }
                val onFailed = { error: MiniAppStorageError ->
                    bridgeExecutor.postError(callbackId, Gson().toJson(error))
                }
                if (storageState != StorageState.LOCK)
                    secureStorage.deleteItems(
                        miniAppId,
                        callbackObj.param.secureStorageKeyList,
                        onSuccess,
                        onFailed
                    )
                else
                    bridgeExecutor.postError(
                        callbackId,
                        Gson().toJson(MiniAppStorageError.storageOccupiedError)
                    )
            } else {
                bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
            }
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
        }
    }

    fun onClearAll(callbackId: String) = whenReady {
        val onSuccess = {
            bridgeExecutor.postValue(callbackId, REMOVE_SUCCESS_SECURE_STORAGE)
        }
        val onFailed = { error: MiniAppStorageError ->
            bridgeExecutor.postError(callbackId, Gson().toJson(error))
        }
        secureStorage.delete(miniAppId, onSuccess, onFailed)
    }

    fun onSize(callbackId: String) = whenReady {
        val onSuccess = { fileSize: Double ->
            val storageSize = Gson().toJson(MiniAppSecureStorageSize(fileSize, storageMaxSizeKB.toDouble()))
            bridgeExecutor.postValue(callbackId, storageSize)
        }
        val onFailed = { error: MiniAppStorageError ->
            bridgeExecutor.postError(callbackId, Gson().toJson(error))
        }
        secureStorage.secureStorageSize(miniAppId, onSuccess, onFailed)
    }

    fun cleanupSecureStorage() {
        cachedItems = null
        secureStorage.storageState.removeObserver(stateObserver)
    }

    /**
     * Will be invoked by MiniApp.clearSecureStorage(miniAppId: String).
     * @param miniAppId will be used to find the file to be deleted.
     */
    fun clearSecureStorage(miniAppId: String) =
        whenReady { secureStorage.clearSecureStorage(miniAppId) }

    /**
     * Will be invoked by MiniApp.clearSecureStorage.
     */
    fun clearSecureStorage() = whenReady { secureStorage.clearSecureStorage() }

    internal companion object {
        const val ERR_WRONG_JSON_FORMAT = "Can not parse secure storage json object"
        const val SAVE_SUCCESS_SECURE_STORAGE = "Items saved successfully."
        const val REMOVE_ITEMS_SUCCESS_SECURE_STORAGE = "Items removed successfully."
        const val REMOVE_SUCCESS_SECURE_STORAGE = "Storage removed successfully."
    }
}
