package com.rakuten.tech.mobile.testapp.ui.miniapplist

import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.Version
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class MiniAppListViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private val _miniAppListData =
        MutableLiveData<List<MiniAppInfo>>().apply { value = getDummyMiniAppList() }

    private val _errorData = MutableLiveData<String>()

    private val _miniAppView = MutableLiveData<WebView>()

    val miniAppListData: LiveData<List<MiniAppInfo>>
        get() = _miniAppListData

    val errorData: LiveData<String>
        get() = _errorData

    val miniAppView: LiveData<WebView>
        get() = _miniAppView

    //for brevity
    suspend fun getMiniAppList() {
        try {
            val miniAppsList = miniapp.listMiniApp()
            _miniAppListData.postValue(miniAppsList)
        } catch (error: MiniAppSdkException) {
            _errorData.postValue((error.message))
        }
    }

    fun getDummyMiniAppList() = listOf(
        MiniAppInfo(
            id = "c67f32b6-987d-405d-978f-6d8118b336ea",
            displayName = "Lookbook",
            icon = "",
            version = Version("", "9ab14a68-e8f9-4216-a6df-2c3b06f3c7f7")
        ),
        MiniAppInfo(
            id = "34d0e875-e3aa-410b-b625-c71bc18a19c7",
            displayName = "Panda Park",
            icon = "",
            version = Version("", "56dc97af-f278-465b-b985-ec383b6a4dac")
        ),
        MiniAppInfo(
            id = "0d207c56-6cbf-44ba-b550-64266869b83f",
            displayName = "Mixed Juice",
            icon = "",
            version = Version("", "b6dec279-9ad0-4da4-a46a-e4f4c3b18a01")
        )
    )
}
