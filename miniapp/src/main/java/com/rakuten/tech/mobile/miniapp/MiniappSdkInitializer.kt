package com.rakuten.tech.mobile.miniapp

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.rakuten.tech.mobile.manifestconfig.annotations.ManifestConfig
import com.rakuten.tech.mobile.manifestconfig.annotations.MetaData
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.storage.FileWriter
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage

/**
 * This initializes the SDK module automatically as the Content Providers are initialized
 * at first before other initializations happen in the application's ecosystem.
 *
 * @suppress
 */
@Suppress("UndocumentedPublicClass")
class MiniappSdkInitializer : ContentProvider() {

    @ManifestConfig
    interface App {

        /**
         * Base Url for the mini app backend.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.miniapp.BaseUrl")
        fun baseUrl(): String

        /**
         * Host app version for the mini app backend.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.miniapp.HostAppVersion")
        fun hostAppVersion(): String

        /**
         * App Id assigned to host App.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.ras.AppId")
        fun rasAppId(): String

        /**
         * Subscription Key for the registered host app.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.ras.ProjectSubscriptionKey")
        fun subscriptionKey(): String
    }

    override fun onCreate(): Boolean {
        val context = context ?: return false
        val manifestConfig = AppManifestConfig(context)
        val storage = MiniAppStorage(FileWriter(), context.filesDir)

        val apiClient = ApiClient(
            baseUrl = manifestConfig.baseUrl(),
            rasAppId = manifestConfig.rasAppId(),
            subscriptionKey = manifestConfig.subscriptionKey(),
            hostAppVersion = manifestConfig.hostAppVersion()
        )

        MiniApp.init(
            miniAppDownloader = MiniAppDownloader(storage, apiClient),
            displayer = Displayer(),
            miniAppLister = MiniAppLister(apiClient)
        )

        return true
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
