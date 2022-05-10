package com.rakuten.tech.mobile.miniapp.storage

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.errors.MiniAppSecureStorageError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.charset.StandardCharsets

private const val SUB_DIR_MINIAPP = "miniapp"
private const val SUB_DIR_SECURE_STORAGE = "secure-storage"

internal enum class StorageState {
    DEFAULT,
    LOCK,
    UNLOCK
}

@Suppress("TooManyFunctions", "LargeClass")
internal class MiniAppSecureStorage(private val activity: Activity) {
    private val hostAppBasePath = activity.filesDir
    private val miniAppBasePath
        get() = "$hostAppBasePath/$SUB_DIR_MINIAPP"
    private val secureStorageBasePath
        get() = "$miniAppBasePath/$SUB_DIR_SECURE_STORAGE/"
    private var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    var storageState: MutableLiveData<StorageState> = MutableLiveData<StorageState>()

    private fun makeDirectoryAvailable() {
        val storageDir = File(secureStorageBasePath)
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
    }

    @Suppress("StringLiteralDuplication")
    private fun isStorageAvailable(miniAppId: String): Boolean {
        val securedStorageFile = File(secureStorageBasePath, "$miniAppId.txt")
        return securedStorageFile.exists()
    }

    @Suppress("MagicNumber")
    fun secureStorageSize(
        miniAppId: String,
        onSuccess: (Long) -> Unit,
    ) {
        if (isStorageAvailable(miniAppId)) {
            val sizeInBytes = File(secureStorageBasePath, "$miniAppId.txt").length()
            onSuccess(sizeInBytes)
        } else {
            onSuccess(0)
        }
    }

    /**
     * Check the usage of the secure storage.
     */
    @Suppress("MagicNumber")
    fun isSecureStorageAvailable(miniAppId: String, storageMaxSizeKB: Int): Boolean {
        return if (isStorageAvailable(miniAppId)) {
            val maxSizeInBytes = storageMaxSizeKB * 1024
            val usedSizeInBytes = File(secureStorageBasePath, "$miniAppId.txt").length()
            !(usedSizeInBytes == maxSizeInBytes.toLong() || usedSizeInBytes > maxSizeInBytes)
        } else {
            true
        }
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    private fun writeToEncryptedFile(
        miniAppId: String,
        content: String,
        onSuccess: (Map<String, String>) -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) = try {
        makeDirectoryAvailable()
        val fileToWrite = File(secureStorageBasePath, "$miniAppId.txt")
        if (fileToWrite.exists()) {
            fileToWrite.delete()
        }
        val encryptedFile = EncryptedFile.Builder(
            activity,
            fileToWrite,
            MasterKey.Builder(activity).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val fileContent = content.toByteArray(StandardCharsets.UTF_8)
        encryptedFile.openFileOutput().apply {
            write(fileContent)
            flush()
            close()
        }
        onSuccess(deserializeItems(content))
    } catch (e: Exception) {
        onFailed(MiniAppSecureStorageError.secureStorageIOError)
    }

    @VisibleForTesting
    @Suppress("SwallowedException", "TooGenericExceptionCaught", "ReturnCount")
    private fun readFromEncryptedFile(miniAppId: String): Map<String, String>? {
        try {
            if (isStorageAvailable(miniAppId)) {
                val encryptedFile = EncryptedFile.Builder(
                    activity,
                    File(secureStorageBasePath, "$miniAppId.txt"),
                    MasterKey.Builder(activity).setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build(),
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).build()

                val inputStream = encryptedFile.openFileInput()
                val plaintext: ByteArray = inputStream.readBytes()
                val jsonToRead = plaintext.toString(Charsets.UTF_8)
                return deserializeItems(jsonToRead)
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun delete(
        miniAppId: String,
        onSuccess: () -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        if (isStorageAvailable(miniAppId)) {
            scope.launch {
                val file = File(secureStorageBasePath, "$miniAppId.txt")
                file.delete()
                if (!file.exists()) {
                    onSuccess()
                } else {
                    onFailed(MiniAppSecureStorageError.secureStorageIOError)
                }
            }
        } else {
            onSuccess()
        }
    }

    fun deleteItems(
        miniAppId: String,
        keySet: Set<String>,
        onSuccess: (Map<String, String>) -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            storageState.postValue(StorageState.LOCK)
            if (isStorageAvailable(miniAppId)) {
                val storedItems = readFromEncryptedFile(miniAppId)
                storedItems?.let { items ->
                    val filterItems = items.filter { !keySet.contains(it.key) }
                    if (storedItems.size == filterItems.size) {
                        onFailed(MiniAppSecureStorageError.secureStorageIOError)
                    } else {
                        writeToEncryptedFile(
                            miniAppId,
                            serializedItems(filterItems),
                            onSuccess,
                            onFailed
                        )
                    }
                } ?: kotlin.run {
                    onFailed(MiniAppSecureStorageError.secureStorageIOError)
                }
            } else {
                onFailed(MiniAppSecureStorageError.secureStorageUnavailableError)
            }
            storageState.postValue(StorageState.UNLOCK)
        }
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun load(
        miniAppId: String,
        onSuccess: (Map<String, String>) -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            storageState.postValue(StorageState.LOCK)
            try {
                if (isStorageAvailable(miniAppId)) {
                    val storedItems = readFromEncryptedFile(miniAppId)
                    storedItems?.let {
                        onSuccess(storedItems)
                    } ?: kotlin.run {
                        onSuccess(emptyMap())
                    }
                } else {
                    onSuccess(emptyMap())
                }
            } catch (e: Exception) {
                onFailed(MiniAppSecureStorageError.secureStorageUnavailableError)
            }
            storageState.postValue(StorageState.UNLOCK)
        }
    }

    fun insertItems(
        miniAppId: String,
        items: Map<String, String>,
        onSuccess: (Map<String, String>) -> Unit,
        onFailed: (MiniAppSecureStorageError) -> Unit
    ) {
        scope.launch {
            storageState.postValue(StorageState.LOCK)
            writeToEncryptedFile(miniAppId, serializedItems(items), onSuccess, onFailed)
            storageState.postValue(StorageState.UNLOCK)
        }
    }

    fun getItem(
        miniAppId: String,
        key: String,
        onSuccess: (String) -> Unit,
    ) {
        scope.launch {
            if (isStorageAvailable(miniAppId)) {
                val storedItems = readFromEncryptedFile(miniAppId)
                storedItems?.let {
                    if (storedItems.containsKey(key)) {
                        onSuccess(storedItems[key] ?: "")
                    } else {
                        onSuccess("null")
                    }
                } ?: kotlin.run {
                    onSuccess("null")
                }
            } else {
                onSuccess("null")
            }
        }
    }

    private fun deserializeItems(jsonToRead: String): Map<String, String> {
        return Gson().fromJson(
            jsonToRead,
            object : TypeToken<Map<String, String>>() {}.type
        )
    }

    private fun serializedItems(items: Map<String, String>): String = Gson().toJson(items)

    /**
     * Will be invoked by MiniApp.clearSecureStorage(miniAppId: String).
     * @param miniAppId will be used to find the file to be deleted.
     */
    fun clearSecureStorage(miniAppId: String) {
        if (isStorageAvailable(miniAppId)) {
            scope.launch {
                val file = File(secureStorageBasePath, "$miniAppId.txt")
                file.delete()
            }
        }
    }

    /**
     * Will be invoked by MiniApp.clearSecureStorage.
     */
    fun clearSecureStorage() {
        val storageDir = File(secureStorageBasePath)
        if (storageDir.exists()) {
            scope.launch {
                storageDir.deleteRecursively()
            }
        }
    }
}
