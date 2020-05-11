package com.rakuten.tech.mobile.testapp.ui.miniapplist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class MiniAppListViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private val _miniAppListData = MutableLiveData<List<MiniAppInfo>>()

    private val _errorData = MutableLiveData<String>()

    val miniAppListData: LiveData<List<MiniAppInfo>>
        get() = _miniAppListData

    val errorData: LiveData<String>
        get() = _errorData

    //for brevity
    suspend fun getMiniAppList() {
        try {
            val miniAppsList = miniapp.listMiniApp()
            _miniAppListData.postValue(miniAppsList)
        } catch (error: MiniAppSdkException) {
            _errorData.postValue((error.message))
        }
    }
}
