package com.rakuten.tech.mobile.miniapp.storage.verifier

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.rakuten.tech.mobile.miniapp.MiniAppVerificationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

internal class StoreHashVerifier @VisibleForTesting constructor(
    private val prefs: SharedPreferences,
    private val coroutineDispatcher: CoroutineDispatcher
) {

    constructor(context: Context, fileName: String) : this(
        prefs = initEncryptedSharedPreference(context, fileName),
        coroutineDispatcher = Dispatchers.IO
    )

    /** Verifies that the cached data has not been modified. */
    fun verify(appId: String, directory: File): Boolean {
        val hash = calculateHash(directory)
        val storedHash = prefs.getString(appId, null) ?: ""
        return hash == storedHash
    }

    /** Stores hash in encrypted shared preferences. This runs asynchronously so it will return immediately. */
    suspend fun storeHashAsync(appId: String, directory: File) =
        withContext(coroutineDispatcher) {
            async {
                val hash = calculateHash(directory)
                prefs.edit().putString(appId, hash).apply()
            }
        }

    @SuppressWarnings("TooGenericExceptionCaught", "LongMethod", "NestedBlockDepth")
    private fun calculateHash(directory: File): String = try {
        val buffer = ByteArray(BUFFER_SIZE)
        var count: Int
        val digest = MessageDigest.getInstance("SHA-256")

        directory.walk()
                .filter { it.isFile }
                .sortedBy { it.name }
                .forEach { file ->
                    file.inputStream().use { stream ->
                        while (stream.read(buffer).also { count = it } > 0) {
                            digest.update(buffer, 0, count)
                        }
                    }
                }

        Base64.encodeToString(digest.digest(), Base64.NO_WRAP)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to calculate hash for the Mini App.", e)
        ""
    }

    companion object {
        private val TAG = this::class.simpleName
        private const val BUFFER_SIZE = 8192
    }
}

@SuppressWarnings("SwallowedException", "TooGenericExceptionCaught")
private fun initEncryptedSharedPreference(context: Context, fileName: String) = try {
    EncryptedSharedPreferences.create(
        context,
        fileName,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
} catch (e: Exception) {
    throw MiniAppVerificationException(e.message)
}
