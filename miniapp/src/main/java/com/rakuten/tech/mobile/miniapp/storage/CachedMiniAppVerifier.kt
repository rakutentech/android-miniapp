package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.rakuten.tech.mobile.miniapp.MiniAppVerificationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

internal class CachedMiniAppVerifier
@VisibleForTesting constructor(
    private val prefs: SharedPreferences,
    private val coroutineDispatcher: CoroutineDispatcher
) {
    constructor(context: Context) : this(
        prefs = initEncryptedSharedPreference(context),
        coroutineDispatcher = Dispatchers.IO
    )

    /** Verifies that the cached files for the Mini App have not been modified. */
    fun verify(appId: String, directory: File): Boolean {
        val hash = calculateHash(directory)
        val storedHash = prefs.getString(appId, null) ?: ""

        return hash == storedHash
    }

    /** Stores hash in encrypted shared preferences. This runs asynchronously so it will return immediately. */
    suspend fun storeHashAsync(appId: String, directory: File) = withContext(coroutineDispatcher) {
        async {
            val hash = calculateHash(directory)
            prefs.edit().putString(appId, hash).apply()
        }
    }

    @SuppressWarnings("TooGenericExceptionCaught", "LongMethod", "NestedBlockDepth")
    private fun calculateHash(
        directory: File
    ): String = try {
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

@Suppress("TooGenericExceptionCaught", "SwallowedException")
private fun initEncryptedSharedPreference(context: Context) = try {
    EncryptedSharedPreferences.create(
        "com.rakuten.tech.mobile.miniapp.cache.hash",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
} catch (e: Exception) {
    throw MiniAppVerificationException(e.message)
}
