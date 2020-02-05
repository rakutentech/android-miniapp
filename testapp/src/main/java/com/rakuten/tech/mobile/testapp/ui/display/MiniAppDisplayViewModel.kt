package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.MiniAppView

class MiniAppDisplayViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance())

    private val _miniAppView = MutableLiveData<WebView>()
    private val _errorData = MutableLiveData<String>()

    val miniAppView: LiveData<WebView>
        get() = _miniAppView
    val errorData: LiveData<String>
        get() = _errorData

    suspend fun obtainMiniAppView(appId: String, versionId: String, context: Context) {
        try {
            val mavInstance: MiniAppView = miniapp.create(appId, versionId)
            _miniAppView.postValue(mavInstance.obtainView(context))
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
            _errorData.postValue(e.message)
        }
    }
}
