package com.rakuten.tech.mobile.miniapp

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.rakuten.tech.mobile.miniapp.storage.CachedManifest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.security.MessageDigest

internal class MiniAppManifestVerifier
@VisibleForTesting constructor(
    private val prefs: SharedPreferences,
    private val coroutineDispatcher: CoroutineDispatcher
) {
    constructor(context: Context) : this(
        prefs = initEncryptedSharedPreference(context),
        coroutineDispatcher = Dispatchers.IO
    )

    /** Verifies that the cached files for the Mini App manifest have not been modified. */
    fun verify(appId: String, cachedManifest: CachedManifest): Boolean {
        val hash = calculateHash(cachedManifest)
        val storedHash = prefs.getString(appId, null) ?: ""

        return hash == storedHash
    }

    /** Stores hash in encrypted shared preferences. This runs asynchronously so it will return immediately. */
    suspend fun storeHashAsync(appId: String, cachedManifest: CachedManifest) =
        withContext(coroutineDispatcher) {
            async {
                val hash = calculateHash(cachedManifest)
                prefs.edit().putString(appId, hash).apply()
            }
        }

    @SuppressWarnings("TooGenericExceptionCaught", "LongMethod", "NestedBlockDepth")
    private fun calculateHash(
        cachedManifest: CachedManifest
    ): String = try {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = messageDigest.digest(cachedManifest.toString().toByteArray(Charsets.UTF_8))
        Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to calculate hash for the Mini App manifest.", e)
        ""
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}

@Suppress("TooGenericExceptionCaught", "SwallowedException")
private fun initEncryptedSharedPreference(context: Context) = try {
    EncryptedSharedPreferences.create(
        "com.rakuten.tech.mobile.miniapp.manifest.cache.hash",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
} catch (e: Exception) {
    throw MiniAppVerificationException(e.message)
}
