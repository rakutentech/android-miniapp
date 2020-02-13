package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay

class MiniAppDisplayViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance())

    private val _miniAppView = MutableLiveData<View>()
    private val _errorData = MutableLiveData<String>()

    val miniAppView: LiveData<View>
        get() = _miniAppView
    val errorData: LiveData<String>
        get() = _errorData

    suspend fun obtainMiniAppView(appId: String, versionId: String, context: Context) {
        try {
            val miniAppDisplay: MiniAppDisplay = miniapp.create(appId, versionId)
            _miniAppView.postValue(miniAppDisplay.getMiniAppView())
        } catch (e: MiniAppSdkException) {
            e.printStackTrace()
            _errorData.postValue(e.message)
        }
    }
}
