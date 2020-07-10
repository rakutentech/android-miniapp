package com.rakuten.tech.mobile.miniapp

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.manifestconfig.annotations.ManifestConfig
import com.rakuten.tech.mobile.manifestconfig.annotations.MetaData

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
         * Base URL used for retrieving a Mini App.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.miniapp.BaseUrl")
        fun baseUrl(): String

        /**
         * Whether the sdk is running in Testing mode.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.miniapp.IsTestMode")
        fun isTestMode(): Boolean

        /**
         * Host app version for the mini app backend.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.miniapp.HostAppVersion")
        fun hostAppVersion(): String

        /**
         * This info will be appended to user-agent of webview.
         * It should be a meaningful keyword such as host app name to differentiate other host apps.
         **/
        @MetaData(key = "com.rakuten.tech.mobile.miniapp.HostAppInfo")
        fun hostAppInfo(): String

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
        val manifestConfig = createAppManifestConfig(context)

        MiniApp.init(
            context = context,
            miniAppSdkConfig = MiniAppSdkConfig(
                baseUrl = manifestConfig.baseUrl(),
                rasAppId = manifestConfig.rasAppId(),
                subscriptionKey = manifestConfig.subscriptionKey(),
                hostAppVersionId = manifestConfig.hostAppVersion(),
                hostAppInfo = provideHostAppInfo(context, manifestConfig),
                isTestMode = manifestConfig.isTestMode()
            )
        )

        return true
    }

    @VisibleForTesting
    internal fun provideHostAppInfo(context: Context, manifestConfig: AppManifestConfig): String =
        if (manifestConfig.hostAppInfo().isEmpty()) {
            val packageManager = context.packageManager
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(context.packageName, 0)).toString()
        } else
            manifestConfig.hostAppInfo()

    @VisibleForTesting
    internal fun createAppManifestConfig(context: Context) = AppManifestConfig(context)

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
