package com.rakuten.tech.mobile.miniapp.storage.verifier

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.rakuten.tech.mobile.miniapp.MiniAppVerificationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

internal class StoreHashVerifier @VisibleForTesting constructor(
    private val prefs: SharedPreferences,
    private val coroutineDispatcher: CoroutineDispatcher
) {

    constructor(context: Context, fileName: String) : this(
        prefs = initEncryptedSharedPreference(context, fileName),
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

@SuppressWarnings("SwallowedException")
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
