package com.rakuten.tech.mobile.miniapp.storage.verifier

import android.content.Context
import androidx.annotation.VisibleForTesting
import java.io.File

internal class CachedMiniAppVerifier(context: Context) {
    @VisibleForTesting
    internal var storeHashVerifier =
        StoreHashVerifier(context, "com.rakuten.tech.mobile.miniapp.cache.hash")

    /** Verifies that the cached files for the Mini App have not been modified. */
    fun verify(appId: String, directory: File): Boolean =
        storeHashVerifier.verify(appId, directory)

    /** Stores hash in encrypted shared preferences. */
    suspend fun storeHashAsync(appId: String, directory: File) =
        storeHashVerifier.storeHashAsync(appId, directory)
}
