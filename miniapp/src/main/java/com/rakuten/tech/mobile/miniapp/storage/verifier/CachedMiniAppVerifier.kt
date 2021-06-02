package com.rakuten.tech.mobile.miniapp.storage.verifier

import android.content.Context
import android.util.Base64
import android.util.Log
import java.io.File
import java.security.MessageDigest

internal class CachedMiniAppVerifier(context: Context) {
    private var storeHashVerifier =
        StoreHashVerifier(context, "com.rakuten.tech.mobile.miniapp.cache.hash")

    /** Verifies that the cached files for the Mini App have not been modified. */
    fun verify(appId: String, directory: File): Boolean =
        storeHashVerifier.verify(appId, calculateHash(directory))

    /** Stores hash in encrypted shared preferences. */
    suspend fun storeHashAsync(appId: String, directory: File) =
        storeHashVerifier.storeHashAsync(appId, calculateHash(directory))

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
