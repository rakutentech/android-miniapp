package com.rakuten.tech.mobile.miniapp.storage.verifier

import android.content.Context
import java.io.File

internal class MiniAppManifestVerifier(context: Context) {
    private var storeHashVerifier =
        StoreHashVerifier(context, "com.rakuten.tech.mobile.miniapp.manifest.cache.hash")

    /** Verifies that the cached data for the Mini App manifest have not been modified. */
    fun verify(appId: String, directory: File): Boolean =
            storeHashVerifier.verify(appId, directory)

    /** Stores hash in encrypted shared preferences. */
    suspend fun storeHashAsync(appId: String, directory: File) =
            storeHashVerifier.storeHashAsync(appId, directory)
}
