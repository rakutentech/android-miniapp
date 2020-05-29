package com.rakuten.tech.mobile.testapp.ui.display

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class MiniAppDisplayViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private val _miniAppDisplay = MutableLiveData<MiniAppDisplay>()
    private val _errorData = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()

    val miniAppDisplay: LiveData<MiniAppDisplay>
        get() = _miniAppDisplay
    val errorData: LiveData<String>
        get() = _errorData
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    suspend fun obtainMiniAppDisplay(
        miniAppInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge
    ) {
        try {
            _isLoading.postValue(true)
            _miniAppDisplay.postValue(miniapp.create(miniAppInfo, miniAppMessageBridge))
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
            _errorData.postValue(e.message)
        } finally {
            _isLoading.postValue(false)
        }
    }

    suspend fun obtainMiniAppDisplay(appId: String, miniAppMessageBridge: MiniAppMessageBridge) {
        try {
            obtainMiniAppDisplay(miniapp.fetchInfo(appId), miniAppMessageBridge)
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
            _errorData.postValue(e.message)
        } finally {
            _isLoading.postValue(false)
        }
    }

}
