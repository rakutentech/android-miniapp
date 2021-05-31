package com.rakuten.tech.mobile.miniapp.storage.verifier

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

internal class StoreHashVerifier @VisibleForTesting constructor(
    private val prefs: SharedPreferences,
    private val coroutineDispatcher: CoroutineDispatcher
) {

    constructor(context: Context, fileName: String, initializer: EncryptedPrefInitializer) : this(
        prefs = initializer.initEncryptedSharedPreference(context, fileName),
        coroutineDispatcher = Dispatchers.IO
    )

    /** Verifies that the cached data has not been modified. */
    fun verify(appId: String, hash: String): Boolean {
        val storedHash = prefs.getString(appId, null) ?: ""
        return hash == storedHash
    }

    /** Stores hash in encrypted shared preferences. This runs asynchronously so it will return immediately. */
    suspend fun storeHashAsync(appId: String, hash: String) =
        withContext(coroutineDispatcher) {
            async {
                prefs.edit().putString(appId, hash).apply()
            }
        }
}
