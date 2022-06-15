package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
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
                    _errorData.postValue("No published version for the provided Mini App ID.")
                is MiniAppNotFoundException ->
                    _errorData.postValue("No Mini App found for the provided Project ID.")
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
                                _errorData.postValue("No Mini App found for the provided Project ID.")
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
}
