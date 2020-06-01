package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class MiniAppDisplayViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private var hostLifeCycle: Lifecycle? = null

    private val _miniAppDisplay = MutableLiveData<View>()
    private val _errorData = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()

    val miniAppDisplay: LiveData<View>
        get() = _miniAppDisplay
    val errorData: LiveData<String>
        get() = _errorData
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    suspend fun obtainMiniAppDisplay(
        context: Context,
        miniAppInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge
    ) {
        try {
            _isLoading.postValue(true)
            val miniAppDisplay = miniapp.create(miniAppInfo, miniAppMessageBridge)
            hostLifeCycle?.addObserver(miniAppDisplay)
            _miniAppDisplay.postValue(miniAppDisplay.getMiniAppView(context))
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
            _errorData.postValue(e.message)
        } finally {
            _isLoading.postValue(false)
        }
    }

    suspend fun obtainMiniAppDisplay(
        context: Context,
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge) {
            try {
                obtainMiniAppDisplay(context, miniapp.fetchInfo(appId), miniAppMessageBridge)
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
