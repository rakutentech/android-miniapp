package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.view.View
import androidx.lifecycle.*
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooserDefault
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.view.MiniAppConfig
import com.rakuten.tech.mobile.miniapp.view.MiniAppParameters
import com.rakuten.tech.mobile.miniapp.view.MiniAppView
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MiniAppDisplayViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {
    private lateinit var miniAppDisplay: MiniAppDisplay

    private val _miniAppView = MutableLiveData<View>()
    private val _errorData = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _containTooManyRequestsError = MutableLiveData<Boolean>()

    private val NO_PUBLISHED_VERSION_ERROR = "No published version for the provided Mini App ID."
    private val NO_MINI_APP_FOUND_ERROR = "No Mini App found for the provided Project ID."

    val miniAppView: LiveData<View>
        get() = _miniAppView
    val errorData: LiveData<String>
        get() = _errorData
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    val containTooManyRequestsError: LiveData<Boolean>
        get() = _containTooManyRequestsError

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
            miniAppDisplay = createMiniAppDisplay(appInfo, appId, miniAppMessageBridge, miniAppNavigator, miniAppFileChooser, appParameters)
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
                else ->{
                    //try to load MiniApp from cache
                    try {
                        miniAppDisplay = createMiniAppDisplay(appInfo, appId, miniAppMessageBridge, miniAppNavigator, miniAppFileChooser, appParameters, true)
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

    private suspend fun createMiniAppDisplay(
        appInfo: MiniAppInfo?,
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator,
        miniAppFileChooser: MiniAppFileChooser,
        appParameters: String,
        fromCache: Boolean = false
    ): MiniAppDisplay = if (appInfo != null)
        miniapp.create(appInfo, miniAppMessageBridge, miniAppNavigator, miniAppFileChooser, appParameters, fromCache)
    else
        miniapp.create(appId, miniAppMessageBridge, miniAppNavigator, miniAppFileChooser, appParameters, fromCache)

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
                miniapp.createWithUrl(appUrl, miniAppMessageBridge, miniAppNavigator, miniAppFileChooser, appParameters)
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

    fun canGoBackwards(): Boolean =
        if (::miniAppDisplay.isInitialized)
            miniAppDisplay.navigateBackward()
        else
            false

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

    fun obtainMiniAppDisplay(
        context: Context,
        miniAppInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator,
        miniAppFileChooser: MiniAppFileChooserDefault,
        urlParameters: String
    ){
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

    private fun handleErrors(e: MiniAppSdkException){
        e.printStackTrace()
        when (e) {
            is MiniAppHasNoPublishedVersionException ->
                _errorData.postValue(NO_PUBLISHED_VERSION_ERROR)
            is MiniAppNotFoundException ->
                _errorData.postValue(NO_MINI_APP_FOUND_ERROR)
            is MiniAppTooManyRequestsError ->
                _containTooManyRequestsError.postValue(true)
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
