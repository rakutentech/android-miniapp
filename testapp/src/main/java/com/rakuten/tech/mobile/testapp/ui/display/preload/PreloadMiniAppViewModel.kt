package com.rakuten.tech.mobile.testapp.ui.display.preload

import androidx.lifecycle.*
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
    private val _miniAppVersionId = MutableLiveData<String>()
    private val _manifestErrorData = MutableLiveData<String>()
    private val _versionIdErrorData = MutableLiveData<String>()

    val miniAppManifest: LiveData<MiniAppManifest>
        get() = _miniAppManifest
    val miniAppVersionId: LiveData<String>
        get() = _miniAppVersionId
    val manifestErrorData: LiveData<String>
        get() = _manifestErrorData
    val versionIdErrorData: LiveData<String>
        get() = _versionIdErrorData

    fun getMiniAppManifest(miniAppId: String, versionId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _miniAppManifest.postValue(miniApp.getMiniAppManifest(miniAppId, versionId))
            } catch (error: MiniAppSdkException) {
                _manifestErrorData.postValue(error.message)
            }
        }

    fun getMiniAppVersionId(miniAppId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val miniAppInfo = miniApp.fetchInfo(miniAppId)
                _miniAppVersionId.postValue(miniAppInfo.version.versionId)
            } catch (error: MiniAppSdkException) {
                _versionIdErrorData.postValue(error.message)
            }
        }
}