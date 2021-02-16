package com.rakuten.tech.mobile.testapp.ui.display.preload

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreloadMiniAppViewModel constructor(
    private val miniApp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private val _miniAppManifest = MutableLiveData<MiniAppManifest>()

    private val _errorData = MutableLiveData<String>()

    val miniAppManifest: LiveData<MiniAppManifest>
        get() = _miniAppManifest

    val errorData: LiveData<String>
        get() = _errorData

    fun getMiniAppManifest(miniAppId: String, versionId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _miniAppManifest.postValue(miniApp.getMiniAppManifest(miniAppId, versionId))
            } catch (error: MiniAppSdkException) {
                _errorData.postValue(error.message)
            }
        }
}
