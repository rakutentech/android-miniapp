package com.rakuten.tech.mobile.testapp.ui.display

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class MiniAppDisplayViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private val _miniAppView = MutableLiveData<View>()
    private val _errorData = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()
    private lateinit var miniAppDisplay: MiniAppDisplay

    val miniAppView: LiveData<View>
        get() = _miniAppView
    val errorData: LiveData<String>
        get() = _errorData
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    suspend fun obtainMiniAppView(appId: String, versionId: String) {
        try {
            _isLoading.postValue(true)
            miniAppDisplay = miniapp.create(appId, versionId)
            _miniAppView.postValue(miniAppDisplay.getMiniAppView())
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
            _errorData.postValue(e.message)
        } finally {
            _isLoading.postValue(false)
        }
    }

    fun destroyMiniAppView() {
        if (::miniAppDisplay.isInitialized)
            miniAppDisplay.destroyView()
    }
}
