package com.rakuten.tech.mobile.miniapp.storage

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.errors.MiniAppStorageError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets

private const val SUB_DIR_MINIAPP = "miniapp"
private const val SUB_DIR_SECURE_STORAGE = "secure-storage"

internal class MiniAppSecureStorage(private val activity: Activity) {
    private val hostAppBasePath = activity.filesDir
    private val miniAppBasePath
        get() = "$hostAppBasePath/$SUB_DIR_MINIAPP"
    private val secureStorageBasePath
        get() = "$miniAppBasePath/$SUB_DIR_SECURE_STORAGE/"
    private var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private fun makeDirectoryAvailable(){
        val storageDir = File(secureStorageBasePath)
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
    }

    private fun isStorageAvailable(miniAppId: String): Boolean{
        val securedStorageFile = File(secureStorageBasePath, "$miniAppId.txt")
        return securedStorageFile.exists()
    }

    fun secureStorageSize(
        miniAppId: String,
        onSuccess: (String) -> Unit,
        onFailed: (MiniAppStorageError) -> Unit
    ) {
        if (isStorageAvailable(miniAppId)) {
            val sizeInMb = File(secureStorageBasePath, "$miniAppId.txt").length() / (1024.0 * 1024)
            onSuccess("%.2f".format(sizeInMb))
        } else {
            onFailed(MiniAppStorageError.unavailableStorage)
        }
    }

    private fun writeToEncryptedFile(
        miniAppId: String,
        content: String,
        onSuccess: () -> Unit,
        onFailed: (MiniAppStorageError) -> Unit
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
        onSuccess()
    } catch (e: Exception) {
        onFailed(MiniAppStorageError.ioError)
    }

    @VisibleForTesting
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
                val byteArrayOutputStream = ByteArrayOutputStream()
                var nextByte: Int = inputStream.read()
                while (nextByte != -1) {
                    byteArrayOutputStream.write(nextByte)
                    nextByte = inputStream.read()
                }

                val plaintext: ByteArray = byteArrayOutputStream.toByteArray()
                val jsonToRead = plaintext.toString(Charsets.UTF_8)
                return deSerializeItems(jsonToRead)
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun deleteSecureStorage(
        miniAppId: String,
        onSuccess: () -> Unit,
        onFailed: (MiniAppStorageError) -> Unit
    ) {
        if (isStorageAvailable(miniAppId)) {
            scope.launch {
                val file = File(secureStorageBasePath, "$miniAppId.txt")
                file.delete()
                if (!file.exists()) {
                    onSuccess()
                }else{
                    onFailed(MiniAppStorageError.failedDeleteError)
                }
            }
        }
    }

    fun deleteSecureStorageItems(
        miniAppId: String,
        keySet: Set<String>,
        onSuccess: () -> Unit,
        onFailed: (MiniAppStorageError) -> Unit
    ) {
        scope.launch {
            if (isStorageAvailable(miniAppId)) {
                val storedItems = readFromEncryptedFile(miniAppId)
                storedItems?.let { items ->
                    val filterItems = items.filter { !keySet.contains(it.key) }
                    writeToEncryptedFile(
                        miniAppId,
                        serializeItems(filterItems),
                        onSuccess,
                        onFailed
                    )
                } ?: kotlin.run {
                    onFailed(MiniAppStorageError.emptyStorage)
                }
            } else {
                onFailed(MiniAppStorageError.unavailableStorage)
            }
        }
    }

    fun loadSecureStorage(
        miniAppId: String,
        onSuccess: (Map<String, String>) -> Unit,
        onFailed: (MiniAppStorageError) -> Unit
    ) {
        scope.launch {
            if (isStorageAvailable(miniAppId)) {
                val storedItems = readFromEncryptedFile(miniAppId)
                storedItems?.let {
                    onSuccess(storedItems)
                } ?: kotlin.run {
                    onFailed(MiniAppStorageError.emptyStorage)
                }
            } else {
                onFailed(MiniAppStorageError.unavailableStorage)
            }
        }
    }

    fun insertSecureStorageItem(
        miniAppId: String,
        items: Map<String, String>,
        onSuccess: () -> Unit,
        onFailed: (MiniAppStorageError) -> Unit
    ){
        scope.launch {
            if (isStorageAvailable(miniAppId)) {
                val storedItems = async {
                    readFromEncryptedFile(miniAppId)
                }.await()
                storedItems?.let {
                    val allItems = it.toMutableMap().apply { putAll(items) }
                    writeToEncryptedFile(miniAppId, serializeItems(allItems), onSuccess, onFailed)
                } ?: kotlin.run {
                    writeToEncryptedFile(miniAppId, serializeItems(items), onSuccess, onFailed)
                }
            } else {
                writeToEncryptedFile(miniAppId, serializeItems(items), onSuccess, onFailed)
            }
        }
    }

    fun getItem(
        miniAppId: String,
        key: String,
        onSuccess: (String) -> Unit,
        onFailed: (MiniAppStorageError) -> Unit
    ) {
        scope.launch {
            if (isStorageAvailable(miniAppId)) {
                val storedItems = readFromEncryptedFile(miniAppId)
                storedItems?.let {
                    if (storedItems.containsKey(key)) {
                        onSuccess(storedItems[key] ?: "")
                    } else {
                        onFailed(MiniAppStorageError.unavailableItem)
                    }
                } ?: kotlin.run {
                    onFailed(MiniAppStorageError.unavailableItem)
                }
            } else {
                onFailed(MiniAppStorageError.unavailableStorage)
            }
        }
    }

    private fun deSerializeItems(jsonToRead: String): Map<String, String> {
        return Gson().fromJson(
            jsonToRead,
            object : TypeToken<Map<String, String>>() {}.type
        )
    }

    private fun serializeItems(items: Map<String, String>): String {
        return Gson().toJson(items)
    }
}

