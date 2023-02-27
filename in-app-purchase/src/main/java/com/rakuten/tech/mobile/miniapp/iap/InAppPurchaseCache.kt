package com.rakuten.tech.mobile.miniapp.iap

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.android.billingclient.api.Purchase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

internal class InAppPurchaseCache @VisibleForTesting constructor(
    private val prefs: SharedPreferences,
    private val coroutineDispatcher: CoroutineDispatcher
) {

    constructor(context: Context, fileName: String) : this(
        prefs = initEncryptedSharedPreference(context, fileName),
        coroutineDispatcher = Dispatchers.IO
    )

    /** Verifies that the the product id is in cached data. */
    fun getPurchaseByTransactionId(transactionId: String): Purchase? {
        val purhcasetStr = prefs.getString(transactionId, null)
        purhcasetStr?.let {
            return Gson().fromJson(it, Purchase::class.java)
        }
        return null
    }

    suspend fun storePurchaseItemsAsync(appId: String, item: Purchase) =
        withContext(coroutineDispatcher) {
            async {
                val itemListStr = Gson().toJson(item)
                prefs.edit().putString(appId, itemListStr).apply()
            }
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
    throw e
}
