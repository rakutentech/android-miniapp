package com.rakuten.mobile.miniapp

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.rakuten.tech.mobile.manifestconfig.annotations.ManifestConfig
import com.rakuten.tech.mobile.manifestconfig.annotations.MetaData

/**
 * This initializes the SDK module automatically as the Content Providers are initialized
 * at first before other initializations happen in the application's ecosystem.
 */
@Suppress("TodoComment", "UndocumentedPublicClass")
class MiniappSdkInitializer : ContentProvider() {

    @ManifestConfig
    interface App {

        /**
         * Base Url for the mini app backend.
         **/
        @MetaData(key = "com.rakuten.mobile.miniapp.BaseUrl")
        fun baseUrl(): String

        /**
         * App Id assigned to host App.
         **/
        @MetaData(key = "com.rakuten.mobile.ras.AppId")
        fun appId(): String

        /**
         * Subscription Key for the registered host app.
         **/
        @MetaData(key = "com.rakuten.mobile.ras.ProjectSubscriptionKey")
        fun subscriptionKey(): String
    }

    override fun onCreate(): Boolean {
        TODO("not implemented") // Initialize sdk components
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun getType(uri: Uri): String? = null
}
