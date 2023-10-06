package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.view.View
import androidx.lifecycle.*
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooserDefault
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.view.MiniAppConfig
import com.rakuten.tech.mobile.miniapp.view.MiniAppParameters
import com.rakuten.tech.mobile.miniapp.view.MiniAppView
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

private const val NO_PUBLISHED_VERSION_ERROR = "No published version for the provided Mini App ID."
private const val NO_MINI_APP_FOUND_ERROR = "No Mini App found for the provided Project ID."
private const val MINI_APP_CORRUPTED_ERROR = "MiniApp Bundle is corrupted or altered."
private const val NO_BUNDLE_FOUND_ERROR = "No Mini App bundle found for the provided App ID and version ID."
private const val INVALID_MINI_APP_ERROR = "No Mini App found for the provided App ID or Version ID."

class MiniAppDisplayViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {
    private lateinit var miniAppDisplay: MiniAppDisplay

    private val _miniAppView = MutableLiveData<View>()
    private val _errorData = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _containTooManyRequestsError = MutableLiveData<Boolean>()

    val miniAppView: LiveData<View>
        get() = _miniAppView
    val errorData: LiveData<String>
        get() = _errorData
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    val containTooManyRequestsError: LiveData<Boolean>
        get() = _containTooManyRequestsError

    @Suppress("LongParameterList")
    fun obtainMiniAppDisplay(
        context: Context,
        appInfo: MiniAppInfo?,
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator,
        miniAppFileChooser: MiniAppFileChooser,
        appParameters: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _isLoading.postValue(true)
            miniAppDisplay = createMiniAppDisplay(
                appInfo,
                appId,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser,
                appParameters
            )
            _miniAppView.postValue(miniAppDisplay.getMiniAppView(context))
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
            when (e) {
                is MiniAppHasNoPublishedVersionException ->
                    _errorData.postValue(NO_PUBLISHED_VERSION_ERROR)
                is MiniAppNotFoundException ->
                    _errorData.postValue(NO_MINI_APP_FOUND_ERROR)
                is MiniAppTooManyRequestsError ->
                    _containTooManyRequestsError.postValue(true)
                else -> {
                    //try to load MiniApp from cache
                    try {
                        miniAppDisplay = createMiniAppDisplay(
                            appInfo,
                            appId,
                            miniAppMessageBridge,
                            miniAppNavigator,
                            miniAppFileChooser,
                            appParameters,
                            true
                        )
                        _miniAppView.postValue(miniAppDisplay.getMiniAppView(context))
                    } catch (e: MiniAppSdkException) {
                        when (e) {
                            is MiniAppNotFoundException ->
                                _errorData.postValue(NO_MINI_APP_FOUND_ERROR)
                            else -> _errorData.postValue(e.message)
                        }
                    }
                }
            }
        } finally {
            _isLoading.postValue(false)
        }
    }

    @Suppress("LongParameterList")
    private suspend fun createMiniAppDisplay(
        appInfo: MiniAppInfo?,
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator,
        miniAppFileChooser: MiniAppFileChooser,
        appParameters: String,
        fromCache: Boolean = false
    ): MiniAppDisplay = if (appInfo != null)
        miniapp.create(
            appInfo,
            miniAppMessageBridge,
            miniAppNavigator,
            miniAppFileChooser,
            appParameters,
            fromCache
        )
    else
        miniapp.create(
            appId,
            miniAppMessageBridge,
            miniAppNavigator,
            miniAppFileChooser,
            appParameters,
            fromCache
        )

    @Suppress("LongParameterList")
    fun obtainMiniAppDisplayUrl(
        context: Context,
        appUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator,
        miniAppFileChooser: MiniAppFileChooser,
        appParameters: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _isLoading.postValue(true)
            miniAppDisplay =
                miniapp.createWithUrl(
                    appUrl,
                    miniAppMessageBridge,
                    miniAppNavigator,
                    miniAppFileChooser,
                    appParameters
                )
            _miniAppView.postValue(miniAppDisplay.getMiniAppView(context))
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
            _errorData.postValue(e.message)
        } finally {
            _isLoading.postValue(false)
        }
    }

    fun addLifeCycleObserver(lifecycle: Lifecycle) {
        lifecycle.addObserver(miniAppDisplay)
    }

    fun onBackPressed(onCloseAlertShowing: () -> Unit) {
        if (!::miniAppDisplay.isInitialized || !miniAppDisplay.navigateBackward()) {
            onCloseAlertShowing()
        }
    }

    @Suppress("LongParameterList")
    fun obtainNewMiniAppDisplayUrl(
        context: Context,
        appUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator,
        miniAppFileChooser: MiniAppFileChooserDefault,
        urlParameters: String
    ) {
        val miniAppView = MiniAppView.init(
            createMiniAppUrlParam(
                context,
                appUrl,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser
            )
        )
        miniAppView.load(queryParams = urlParameters) { display, miniAppSdkException ->
            display?.let {
                miniAppDisplay = it
                viewModelScope.launch(Dispatchers.IO) {
                    _miniAppView.postValue(miniAppDisplay.getMiniAppView(context))
                }
            } ?: kotlin.run {
                miniAppSdkException?.let { e ->
                    handleErrors(e)
                }
            }
        }
    }

    @Suppress("LongParameterList")
    fun obtainMiniAppDisplay(
        context: Context,
        miniAppInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator,
        miniAppFileChooser: MiniAppFileChooserDefault,
        urlParameters: String
    ) {
        val miniAppView = MiniAppView.init(
            createMiniAppInfoParam(
                context,
                miniAppInfo,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser
            )
        )
        miniAppView.load(queryParams = urlParameters) { display, miniAppSdkException ->
            display?.let {
                miniAppDisplay = it
                viewModelScope.launch(Dispatchers.IO) {
                    _miniAppView.postValue(miniAppDisplay.getMiniAppView(context))
                }
            } ?: kotlin.run {
                miniAppSdkException?.let { e ->
                    handleErrors(e)
                }
            }
        }
    }

    @Suppress("LongParameterList")
    fun obtainMiniAppDisplayFromBundle(
        context: Context,
        appId: String,
        versionId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator,
        miniAppFileChooser: MiniAppFileChooserDefault,
    ) {
        val miniAppView = MiniAppView.init(
            createMiniAppDefaultParam(
                context,
                appId,
                versionId,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser
            )
        )

        miniAppView.loadFromBundle(manifest = createMiniAppManifestFor(appId, versionId)) { display, miniAppSdkException ->
            display?.let {
                miniAppDisplay = it
                viewModelScope.launch(Dispatchers.IO) {
                    _miniAppView.postValue(miniAppDisplay.getMiniAppView(context))
                }
            } ?: kotlin.run {
                miniAppSdkException?.let { e ->
                    handleErrors(e)
                }
            }
        }
    }

    /**
     * Save the permissions.
     * */
    private fun storeManifestPermission(
        appId: String,
        permissions: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
    ) {
        // store values in SDK cache
        val permissionsWhenAccept = MiniAppCustomPermission(
            miniAppId = appId, pairValues = permissions
        )
        miniapp.setCustomPermissions(permissionsWhenAccept)
    }

    /**
     * Create a manifest file manually.
     * */
    private fun createMiniAppManifestFor(appId: String, versionId: String): MiniAppManifest {
        // Save the permissions manually
        storeManifestPermission(
            appId = appId,
            permissions = listOf(
                Pair(
                    MiniAppCustomPermissionType.USER_NAME,
                    MiniAppCustomPermissionResult.ALLOWED
                ),
                Pair(
                    MiniAppCustomPermissionType.PROFILE_PHOTO,
                    MiniAppCustomPermissionResult.ALLOWED
                ),
                Pair(
                    MiniAppCustomPermissionType.CONTACT_LIST,
                    MiniAppCustomPermissionResult.ALLOWED
                ),
                Pair(
                    MiniAppCustomPermissionType.ACCESS_TOKEN,
                    MiniAppCustomPermissionResult.ALLOWED
                ),
                Pair(
                    MiniAppCustomPermissionType.SEND_MESSAGE,
                    MiniAppCustomPermissionResult.ALLOWED
                ),
                Pair(
                    MiniAppCustomPermissionType.LOCATION,
                    MiniAppCustomPermissionResult.ALLOWED
                ),
                Pair(
                    MiniAppCustomPermissionType.POINTS,
                    MiniAppCustomPermissionResult.ALLOWED
                ),
                Pair(
                    MiniAppCustomPermissionType.FILE_DOWNLOAD,
                    MiniAppCustomPermissionResult.ALLOWED
                ),
            )
        )
        return MiniAppManifest(
            requiredPermissions = emptyList(),
            optionalPermissions = listOf(
                Pair(
                    MiniAppCustomPermissionType.USER_NAME,
                    MiniAppCustomPermissionResult.ALLOWED.name
                ),
                Pair(
                    MiniAppCustomPermissionType.PROFILE_PHOTO,
                    MiniAppCustomPermissionResult.ALLOWED.name
                ),
                Pair(
                    MiniAppCustomPermissionType.CONTACT_LIST,
                    MiniAppCustomPermissionResult.ALLOWED.name
                ),
                Pair(
                    MiniAppCustomPermissionType.ACCESS_TOKEN,
                    MiniAppCustomPermissionResult.ALLOWED.name
                ),
                Pair(
                    MiniAppCustomPermissionType.SEND_MESSAGE,
                    MiniAppCustomPermissionResult.ALLOWED.name
                ),
                Pair(
                    MiniAppCustomPermissionType.LOCATION,
                    MiniAppCustomPermissionResult.ALLOWED.name
                ),
                Pair(
                    MiniAppCustomPermissionType.POINTS,
                    MiniAppCustomPermissionResult.ALLOWED.name
                ),
            ),
            accessTokenPermissions = listOf(
                AccessTokenScope(
                    "rae",
                    listOf("idinfo_read_openid", "memberinfo_read_point")
                ),
                AccessTokenScope(
                    "api-c",
                    listOf("your_service_scope_here")
                ),
            ),
            customMetaData = emptyMap(),
            versionId = versionId
        )
    }

    private fun handleErrors(e: MiniAppSdkException) {
        e.printStackTrace()
        when (e) {
            is MiniAppHasNoPublishedVersionException ->
                _errorData.postValue(NO_PUBLISHED_VERSION_ERROR)
            is MiniAppNotFoundException ->
                _errorData.postValue(NO_MINI_APP_FOUND_ERROR)
            is MiniAppTooManyRequestsError ->
                _containTooManyRequestsError.postValue(true)
            is MiniAppBundleNotFoundException ->
                _errorData.postValue(NO_BUNDLE_FOUND_ERROR)
            is MiniAppHasCorruptedException ->
                _errorData.postValue(MINI_APP_CORRUPTED_ERROR)
            is InvalidMiniAppInfoException ->
                _errorData.postValue(INVALID_MINI_APP_ERROR)
            else -> {
                _errorData.postValue(e.message)
            }
        }
    }

    private fun createMiniAppInfoParam(
        context: Context,
        miniAppInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator,
        miniAppFileChooser: MiniAppFileChooser
    ): MiniAppParameters {
        return MiniAppParameters.InfoParams(
            context = context,
            config = MiniAppConfig(
                miniAppSdkConfig = AppSettings.instance.newMiniAppSdkConfig,
                miniAppMessageBridge = miniAppMessageBridge,
                miniAppNavigator = miniAppNavigator,
                miniAppFileChooser = miniAppFileChooser,
                queryParams = AppSettings.instance.urlParameters
            ),
            miniAppInfo = miniAppInfo,
            fromCache = false
        )
    }

    private fun createMiniAppDefaultParam(
        context: Context,
        appId: String,
        versionId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator,
        miniAppFileChooser: MiniAppFileChooser
    ): MiniAppParameters {
        return MiniAppParameters.DefaultParams(
            context = context,
            config = MiniAppConfig(
                miniAppSdkConfig = AppSettings.instance.newMiniAppSdkConfig,
                miniAppMessageBridge = miniAppMessageBridge,
                miniAppNavigator = miniAppNavigator,
                miniAppFileChooser = miniAppFileChooser,
                queryParams = AppSettings.instance.urlParameters
            ),
            miniAppId = appId,
            miniAppVersion = versionId,
            fromCache = false
        )
    }

    private fun createMiniAppUrlParam(
        context: Context,
        miniAppUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator,
        miniAppFileChooser: MiniAppFileChooser
    ): MiniAppParameters {
        return MiniAppParameters.UrlParams(
            context = context,
            config = MiniAppConfig(
                miniAppSdkConfig = AppSettings.instance.newMiniAppSdkConfig,
                miniAppMessageBridge = miniAppMessageBridge,
                miniAppNavigator = miniAppNavigator,
                miniAppFileChooser = miniAppFileChooser,
                queryParams = AppSettings.instance.urlParameters
            ),
            miniAppUrl = miniAppUrl
        )
    }
}
