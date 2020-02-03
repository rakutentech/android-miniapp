package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppView

class MiniAppDwnlViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance())

    private val _miniAppView = MutableLiveData<WebView>()

    val miniAppView: LiveData<WebView>
        get() = _miniAppView

    suspend fun obtainMiniAppView(miniAppInfo: MiniAppInfo, context: Context) {
        val miniAppView: MiniAppView = miniapp.create(miniAppInfo)
        _miniAppView.postValue(miniAppView.obtainView(context))
    }
}
