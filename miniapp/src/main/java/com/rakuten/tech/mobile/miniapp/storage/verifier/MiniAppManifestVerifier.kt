package com.rakuten.tech.mobile.miniapp.storage.verifier

import android.content.Context
import android.util.Base64
import android.util.Log
import com.rakuten.tech.mobile.miniapp.storage.CachedManifest
import java.security.MessageDigest

internal class MiniAppManifestVerifier(context: Context) {
    private var storeHashVerifier =
        StoreHashVerifier(context, "com.rakuten.tech.mobile.miniapp.manifest.cache.hash")

    /** Verifies that the cached data for the Mini App manifest have not been modified. */
    fun verify(appId: String, cachedManifest: CachedManifest): Boolean =
        storeHashVerifier.verify(appId, calculateHash(cachedManifest))

    @SuppressWarnings("TooGenericExceptionCaught")
    private fun calculateHash(cachedManifest: CachedManifest): String = try {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = messageDigest.digest(cachedManifest.toString().toByteArray(Charsets.UTF_8))
        Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to calculate hash for the Mini App manifest.", e)
        ""
    }

    /** Stores hash in encrypted shared preferences. */
    suspend fun storeHashAsync(appId: String, cachedManifest: CachedManifest) =
        storeHashVerifier.storeHashAsync(appId, calculateHash(cachedManifest))

    companion object {
        private val TAG = this::class.simpleName
    }
}
