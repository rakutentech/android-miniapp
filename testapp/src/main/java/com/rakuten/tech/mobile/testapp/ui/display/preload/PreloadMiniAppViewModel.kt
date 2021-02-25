package com.rakuten.tech.mobile.testapp.ui.display.preload

import androidx.lifecycle.*
import com.google.gson.GsonBuilder
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
    private val _miniAppManifestMetadata = MutableLiveData<String>()
    private val _miniAppVersionId = MutableLiveData<String>()
    private val _manifestErrorData = MutableLiveData<String>()
    private val _versionIdErrorData = MutableLiveData<String>()

    val miniAppManifest: LiveData<MiniAppManifest>
        get() = _miniAppManifest
    val miniAppManifestMetadata: LiveData<String>
        get() = _miniAppManifestMetadata
    val miniAppVersionId: LiveData<String>
        get() = _miniAppVersionId
    val manifestErrorData: LiveData<String>
        get() = _manifestErrorData
    val versionIdErrorData: LiveData<String>
        get() = _versionIdErrorData

    fun getMiniAppManifest(miniAppId: String, versionId: String, metadataKey: String) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val miniAppManifest = miniApp.getMiniAppManifest(miniAppId, versionId)
                _miniAppManifest.postValue(miniAppManifest)
                val metadata = GsonBuilder().setPrettyPrinting().create().toJson(miniAppManifest.customMetaData)
                _miniAppManifestMetadata.postValue(metadata)
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
