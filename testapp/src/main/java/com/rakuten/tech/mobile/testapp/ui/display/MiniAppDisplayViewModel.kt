package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MiniAppDisplayViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private lateinit var miniAppDisplay: MiniAppDisplay
    private var hostLifeCycle: Lifecycle? = null

    private val _miniAppView = MutableLiveData<View>()
    private val _errorData = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()

    val miniAppView: LiveData<View>
        get() = _miniAppView
    val errorData: LiveData<String>
        get() = _errorData
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun obtainMiniAppDisplay(
        context: Context,
        appInfo: MiniAppInfo?,
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _isLoading.postValue(true)
            miniAppDisplay = if (appInfo != null)
                miniapp.create(appInfo, miniAppMessageBridge, miniAppNavigator)
            else
                miniapp.create(appId, miniAppMessageBridge, miniAppNavigator)
            hostLifeCycle?.addObserver(miniAppDisplay)
            _miniAppView.postValue(miniAppDisplay.getMiniAppView(context))
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
            when (e) {
                is MiniAppHasNoPublishedVersionException ->
                    _errorData.postValue("No published versions for the provided Mini App ID.")
                is MiniAppNotFoundException ->
                    _errorData.postValue("No Mini App found for the provided Project ID.")
                else -> _errorData.postValue(e.message)
            }
        } finally {
            _isLoading.postValue(false)
        }
    }

    fun obtainMiniAppDisplayUrl(
        context: Context,
        appUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _isLoading.postValue(true)
            miniAppDisplay = miniapp.createWithUrl(appUrl, miniAppMessageBridge, miniAppNavigator)
            hostLifeCycle?.addObserver(miniAppDisplay)
            _miniAppView.postValue(miniAppDisplay.getMiniAppView(context))
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
             _errorData.postValue(e.message)
        } finally {
            _isLoading.postValue(false)
        }
    }

    fun setHostLifeCycle(lifecycle: Lifecycle) {
        this.hostLifeCycle = lifecycle
    }

    fun canGoBackwards(): Boolean =
        if (::miniAppDisplay.isInitialized)
            miniAppDisplay.navigateBackward()
        else
            false
}
