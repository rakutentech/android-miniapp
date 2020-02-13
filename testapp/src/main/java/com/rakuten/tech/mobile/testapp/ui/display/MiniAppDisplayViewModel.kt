package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException

class MiniAppDisplayViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance())

    private val _miniAppView = MutableLiveData<View>()
    private val _errorData = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()

    val miniAppView: LiveData<View>
        get() = _miniAppView
    val errorData: LiveData<String>
        get() = _errorData
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    suspend fun obtainMiniAppView(appId: String, versionId: String, context: Context) {
        try {
            _isLoading.postValue(true)
            val miniAppDisplay= miniapp.create(appId, versionId)
            _miniAppView.postValue(miniAppDisplay.getMiniAppView())
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
            _errorData.postValue(e.message)
        } finally {
            _isLoading.postValue(false)
        }
    }
}
