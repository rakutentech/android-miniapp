package com.rakuten.tech.mobile.miniapp

import android.app.Activity
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.js.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.storage.FileWriter
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage

/**
 * This represents the contract between the consuming application and the SDK
 * by which operations in the mini app ecosystem are exposed.
 * Should be accessed via [MiniApp.instance].
 */
@Suppress("UnnecessaryAbstractClass")
abstract class MiniApp internal constructor() {

    /**
     * Fetches and lists out the mini applications available in the MiniApp Ecosystem.
     * @return [List] of type [MiniAppInfo] when obtained successfully
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any reason.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun listMiniApp(): List<MiniAppInfo>

    /**
     * Creates a mini app.
     * @param appId mini app id.
     * The mini app is downloaded, saved and provides a [MiniAppDisplay] when successful
     * @param miniAppMessageBridge the interface for communicating between host app & mini app
     * @throws MiniAppSdkException when there is some issue during fetching,
     * downloading or creating the view.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun create(
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge
    ): MiniAppDisplay

    /**
     * Fetches meta data information of a mini app.
     * @return [MiniAppInfo] for the provided appId of a mini app
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any reason.
     */
    @Throws(MiniAppSdkException::class)
    abstract suspend fun fetchInfo(appId: String): MiniAppInfo

    /**
     * Update SDK interaction interface based on [MiniAppSdkConfig] configuration.
     */
    internal abstract fun updateConfiguration(newConfig: MiniAppSdkConfig)

    companion object {
        @VisibleForTesting
        internal lateinit var instance: MiniApp
        private lateinit var defaultConfig: MiniAppSdkConfig

        /**
         * Instance of [MiniApp] which uses the default config settings,
         * as defined in AndroidManifest.xml. For usual scenarios the default config suffices.
         * However, should it be required to change the config at runtime for QA purpose or similar,
         * another [MiniAppSdkConfig] can be provided for customization.
         * @return [MiniApp] instance
         */
        @JvmStatic
        fun instance(settings: MiniAppSdkConfig = defaultConfig): MiniApp =
            instance.apply { updateConfiguration(settings) }

        @Suppress("LongMethod")
        internal fun init(context: Context, miniAppSdkConfig: MiniAppSdkConfig) {
            defaultConfig = miniAppSdkConfig
            val apiClient = ApiClient(
                baseUrl = miniAppSdkConfig.baseUrl,
                rasAppId = miniAppSdkConfig.rasAppId,
                subscriptionKey = miniAppSdkConfig.subscriptionKey,
                hostAppVersionId = miniAppSdkConfig.hostAppVersionId,
                isTestMode = miniAppSdkConfig.isTestMode
            )
            val apiClientRepository = ApiClientRepository().apply {
                registerApiClient(defaultConfig.key, apiClient)
            }

            val miniAppStatus = MiniAppStatus(context)
            val storage = MiniAppStorage(FileWriter(), context.filesDir)

            instance = RealMiniApp(
                apiClientRepository = apiClientRepository,
                displayer = Displayer(context, defaultConfig.hostAppUserAgentInfo),
                miniAppDownloader = MiniAppDownloader(storage, apiClient, miniAppStatus),
                miniAppInfoFetcher = MiniAppInfoFetcher(apiClient)
            )
        }

        /**
         * A self-check of custom permissions whether it's granted or rejected inside this SDK.
         * @param [activity] the Activity as a context of SharedPreferences
         * @return [Boolean] the grant results of custom permissions requested.
         */
        @JvmStatic
        fun selfReadCustomPermissions(
            activity: Activity,
            permissions: List<String>
        ): List<String> = MiniAppCustomPermissionCache(activity).readPermissions(permissions)

        /**
         * Get custom permissions with grant result from this SDK.
         * After the user has accepted or rejected the requested permissions will
         * receive a callback reporting whether the permissions were granted or not.
         * @see [OnRequestCustomPermissionResultCallback.onRequestCustomPermissionResult]
         */
        @JvmStatic
        fun getCustomPermissions(
            activity: Activity,
            permissions: List<String>
        ) {
            if (activity is OnRequestCustomPermissionResultCallback) {
                val grantResults =
                    MiniAppCustomPermissionCache(activity).readPermissions(permissions)

                (activity as OnRequestCustomPermissionResultCallback)
                    .onRequestCustomPermissionResult(
                        permissions, grantResults
                    )
            }
        }

        /**
         * Store custom permissions with grant results inside this SDK.
         * After data has stored, SDK will read custom permissions
         * @see [getCustomPermissions]
         */
        @JvmStatic
        fun setCustomPermissions(
            activity: Activity,
            permissions: List<String>,
            grantResults: List<String>
        ) {
            if (activity is OnRequestCustomPermissionResultCallback) {
                MiniAppCustomPermissionCache(activity).storePermissionResults(
                    permissions,
                    grantResults
                )

                (activity as OnRequestCustomPermissionResultCallback)
                    .onRequestCustomPermissionResult(
                        permissions, grantResults
                    )
            }
        }

        /**
         * Custom permission check result: if the custom permission has not been granted.
         */
        const val CUSTOM_PERMISSION_DENIED = "DENIED"

        /**
         * Custom permission check result: if the custom permission has been granted.
         */
        const val CUSTOM_PERMISSION_ALLOWED = "ALLOWED"
    }

    /**
     * This interface is the contract for receiving the result for custom permissions request.
     */
    interface OnRequestCustomPermissionResultCallback {

        /**
         * Callback for the result of requesting custom permissions to access user data
         * inside this SDK. This method is invoked for every call on [getCustomPermissions]
         */
        fun onRequestCustomPermissionResult(
            permissions: List<String>,
            grantResults: List<String>
        )
    }
}
