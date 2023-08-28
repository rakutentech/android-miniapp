package com.rakuten.tech.mobile.testapp.ui.miniapptabs.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MiniAppListViewModel constructor(
    private val miniapp: MiniApp
) : ViewModel() {

    private val _miniAppListData = MutableLiveData<List<MiniAppInfo>>()

    private val _errorData = MutableLiveData<String>()

    val miniAppListData: LiveData<List<MiniAppInfo>>
        get() = _miniAppListData

    val errorData: LiveData<String>
        get() = _errorData

    //for brevity
    fun getMiniAppList(cachedMiniAppInfoList: List<MiniAppInfo>) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cachedMiniAppInfoList.let {
                    if (it.isNotEmpty()) {
                        _miniAppListData.postValue(it)
                        return@launch
                    }
                }
                val miniAppsList = miniapp.listMiniApp()
                _miniAppListData.postValue(miniAppsList.sortedBy { it.id })
            } catch (error: MiniAppSdkException) {
                _errorData.postValue(error.message)
            }
        }

    fun downloadMiniApp(
        appId: String,
        versionId: String,
        completionHandler: (message: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            miniapp.downloadMiniApp(appId, versionId) { success, miniAppSdkException ->
                if (success) {
                    completionHandler.invoke("MiniApp is downloaded successfully")
                } else {
                    miniAppSdkException?.let {
                        completionHandler.invoke("MiniApp is failed to download + {${it.message}}")
                    }
                }
            }
        }
    }

    fun checkMiniApp(
        appId: String, versionId: String, completionHandler: (message: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val isMiniAppAvailable = miniapp.isMiniAppCacheAvailable(appId, versionId)
            if (isMiniAppAvailable)
                completionHandler.invoke("MiniApp is Available")
            else
                completionHandler.invoke("MiniApp is not Available")
        }
    }
}
