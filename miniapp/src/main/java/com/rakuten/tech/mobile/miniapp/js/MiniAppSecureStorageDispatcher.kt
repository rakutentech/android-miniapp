package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.errors.MiniAppSecureStorageError
import com.rakuten.tech.mobile.miniapp.storage.MiniAppSecureStorage
import com.rakuten.tech.mobile.miniapp.storage.StorageState

@Suppress("TooManyFunctions", "LargeClass")
internal class MiniAppSecureStorageDispatcher(
    private val storageMaxSizeKB: Int
) {
    @VisibleForTesting
    internal lateinit var bridgeExecutor: MiniAppBridgeExecutor
    @VisibleForTesting
    internal lateinit var activity: Activity
    @VisibleForTesting
    internal lateinit var miniAppId: String
    @VisibleForTesting
    internal lateinit var secureStorage: MiniAppSecureStorage
    @VisibleForTesting
    internal var cachedItems: Map<String, String>? = null
    @VisibleForTesting
    internal var storageState: StorageState = StorageState.DEFAULT
    @VisibleForTesting
    internal lateinit var onSuccess: (Map<String, String>) -> Unit
    @VisibleForTesting
    internal lateinit var onFailed: (MiniAppSecureStorageError) -> Unit
    @VisibleForTesting
    internal lateinit var onSuccessGetItem: (String) -> Unit
    @VisibleForTesting
    internal lateinit var onSuccessClearSecureStorage: () -> Unit
    @VisibleForTesting
    internal lateinit var onSuccessFileSize: (Long) -> Unit
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

    fun onLoad() = whenReady {
        onSuccess = { items: Map<String, String> ->
            cachedItems = items
            bridgeExecutor.dispatchEvent(eventType = NativeEventType.MINIAPP_SECURE_STORAGE_READY.value)
        }
        onFailed = { errorSecure: MiniAppSecureStorageError ->
            bridgeExecutor.dispatchEvent(
                eventType = NativeEventType.MINIAPP_SECURE_STORAGE_LOAD_ERROR.value,
                value = Gson().toJson(errorSecure)
            )
        }
        secureStorage.load(miniAppId, onSuccess, onFailed)
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException", "ComplexMethod", "LongMethod")
    fun onSetItems(callbackId: String, jsonStr: String) = whenReady {
        if (secureStorage.isSecureStorageAvailable(miniAppId, storageMaxSizeKB)) {
            try {
                val callbackObj: SecureStorageCallbackObj? =
                    Gson().fromJson(jsonStr, SecureStorageCallbackObj::class.java)
                if (callbackObj != null) {
                    onSuccess = { items: Map<String, String> ->
                        cachedItems = items
                        bridgeExecutor.postValue(callbackId, SAVE_SUCCESS_SECURE_STORAGE)
                    }
                    onFailed = { errorSecure: MiniAppSecureStorageError ->
                        bridgeExecutor.postError(callbackId, Gson().toJson(errorSecure))
                    }
                    if (storageState != StorageState.LOCK) {
                        cachedItems?.let {
                            val mergedItems = it.toMutableMap()
                                .apply { putAll(callbackObj.param.secureStorageItems) }
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
                            Gson().toJson(MiniAppSecureStorageError.secureStorageBusyError)
                        )
                    }
                } else {
                    bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
                }
            } catch (e: Exception) {
                bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
            }
        } else {
            bridgeExecutor.postError(
                callbackId,
                Gson().toJson(MiniAppSecureStorageError.secureStorageFullError)
            )
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
                    onSuccessGetItem = { itemValue: String ->
                        bridgeExecutor.postValue(callbackId, itemValue)
                    }
                    secureStorage.getItem(
                        miniAppId,
                        callbackObj.param.secureStorageKey,
                        onSuccessGetItem
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
                onSuccess = { items: Map<String, String> ->
                    cachedItems = items
                    bridgeExecutor.postValue(callbackId, REMOVE_ITEMS_SUCCESS_SECURE_STORAGE)
                }
                onFailed = { errorSecure: MiniAppSecureStorageError ->
                    bridgeExecutor.postError(callbackId, Gson().toJson(errorSecure))
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
                        Gson().toJson(MiniAppSecureStorageError.secureStorageBusyError)
                    )
            } else {
                bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
            }
        } catch (e: Exception) {
            bridgeExecutor.postError(callbackId, ERR_WRONG_JSON_FORMAT)
        }
    }

    fun onClearAll(callbackId: String) = whenReady {
        onSuccessClearSecureStorage = {
            cachedItems = null
            bridgeExecutor.postValue(callbackId, REMOVE_SUCCESS_SECURE_STORAGE)
        }
        onFailed = { errorSecure: MiniAppSecureStorageError ->
            bridgeExecutor.postError(callbackId, Gson().toJson(errorSecure))
        }
        secureStorage.delete(miniAppId, onSuccessClearSecureStorage, onFailed)
    }

    @Suppress("MagicNumber")
    fun onSize(callbackId: String) = whenReady {
        onSuccessFileSize = { fileSize: Long ->
            val maxSizeInBytes = storageMaxSizeKB * 1024
            val storageSize =
                Gson().toJson(MiniAppSecureStorageSize(fileSize, maxSizeInBytes.toLong()))
            bridgeExecutor.postValue(callbackId, storageSize)
        }
        secureStorage.secureStorageSize(miniAppId, onSuccessFileSize)
    }

    fun cleanupSecureStorage() {
        cachedItems = null
        secureStorage.storageState.removeObserver(stateObserver)
    }

    /**
     * Will be invoked by MiniApp.clearSecureStorage(miniAppId: String).
     * @param miniAppId will be used to find the storage to be deleted.
     */
    fun clearSecureStorage(miniAppId: String) = secureStorage.clearSecureStorage(miniAppId)

    /**
     * Will be invoked by MiniApp.clearSecureStorage.
     */
    fun clearSecureStorage() = secureStorage.clearSecureStorage()

    internal companion object {
        const val ERR_WRONG_JSON_FORMAT = "Can not parse secure storage json object"
        const val SAVE_SUCCESS_SECURE_STORAGE = "Items saved successfully."
        const val REMOVE_ITEMS_SUCCESS_SECURE_STORAGE = "Items removed successfully."
        const val REMOVE_SUCCESS_SECURE_STORAGE = "Storage removed successfully."
    }
}
