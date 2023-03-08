package com.rakuten.tech.mobile.miniapp.iap

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniAppVerificationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

internal class MiniAppIAPCache @VisibleForTesting constructor(
    private val prefs: SharedPreferences,
    private val coroutineDispatcher: CoroutineDispatcher
) {

    constructor(context: Context, fileName: String) : this(
        prefs = initEncryptedSharedPreference(context, fileName),
        coroutineDispatcher = Dispatchers.IO
    )

    /** Verifies that the the product id is in cached data. */
    fun verify(appId: String, productId: String): Boolean {
        val itemListStr = prefs.getString(appId, null)
        itemListStr?.let {
            val purchaseItemList = Gson().fromJson(it, Array<ProductInfo>::class.java).asList()
            purchaseItemList.forEach { item ->
                if (item.id == productId) return true
            }
        }
        return false
    }

    suspend fun storePurchaseItemsAsync(appId: String, items: List<ProductInfo>) =
        withContext(coroutineDispatcher) {
            async {
                val itemListStr = Gson().toJson(items)
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
    throw MiniAppVerificationException(e.message)
}
