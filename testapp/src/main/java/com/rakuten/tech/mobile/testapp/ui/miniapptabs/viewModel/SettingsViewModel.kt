package com.rakuten.tech.mobile.testapp.ui.miniapptabs.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import kotlinx.coroutines.*

class SettingsViewModel : ViewModel() {

    private val _errorData = MutableLiveData<String>()

    val errorData: LiveData<String>
        get() = _errorData

    private fun getMiniAppList(
        miniapp: MiniApp,
        onSuccess: (List<MiniAppInfo>) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            onSuccess(miniapp.listMiniApp().sortedBy { it.id })
        } catch (error: MiniAppSdkException) {
            _errorData.value.let {
                if (it.isNullOrBlank()) {
                    _errorData.postValue(error.message)
                }
            }
        }
    }

    fun getEachTabMiniAppList(
        miniAppSdkConfigList: ArrayList<Pair<MiniAppSdkConfig, (List<MiniAppInfo>) -> Unit>>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val jobs = arrayListOf<Job>()
            miniAppSdkConfigList.forEach {
                jobs.add(getMiniAppList(MiniApp.instance(it.first), it.second))
            }

            jobs.joinAll()
            if (!_errorData.value.isNullOrBlank()) {
                _errorData.value = ""
                return@launch
            }

            if (jobs.all { it.isCompleted }) {
                onSuccess()
            }
        }
    }
}
