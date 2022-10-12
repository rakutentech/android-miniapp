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
    fun getMiniAppList() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val miniAppsList = miniapp.listMiniApp()
            _miniAppListData.postValue(miniAppsList.sortedBy { it.id })
        } catch (error: MiniAppSdkException) {
            _errorData.postValue(error.message)
        }
    }
}
