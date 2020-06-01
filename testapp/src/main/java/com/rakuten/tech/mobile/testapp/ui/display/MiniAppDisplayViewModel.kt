package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class MiniAppDisplayViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private lateinit var miniAppDisplay: MiniAppDisplay
    private lateinit var hostLifeCycle: Lifecycle

    private val _miniAppView = MutableLiveData<View>()
    private val _errorData = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()

    val miniAppView: LiveData<View>
        get() = _miniAppView
    val errorData: LiveData<String>
        get() = _errorData
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    suspend fun obtainMiniAppView(
        context: Context,
        miniAppInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge) {
        try {
            _isLoading.postValue(true)
            miniAppDisplay = miniapp.create(context, miniAppInfo, miniAppMessageBridge)
            hostLifeCycle?.addObserver(miniAppDisplay)
            _miniAppView.postValue(miniAppDisplay.getMiniAppView())
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
            _errorData.postValue(e.message)
        } finally {
            _isLoading.postValue(false)
        }
    }

    suspend fun obtainMiniAppView(
        context: Context,
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge) {
        try {
            obtainMiniAppView(context, miniapp.fetchInfo(appId), miniAppMessageBridge)
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
}
