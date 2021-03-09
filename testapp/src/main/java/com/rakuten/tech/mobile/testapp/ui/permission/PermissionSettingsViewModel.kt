package com.rakuten.tech.mobile.testapp.ui.permission

import androidx.lifecycle.*
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PermissionSettingsViewModel constructor(
    internal val miniApp: MiniApp
) : ViewModel() {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private val _miniAppCustomPermission = MutableLiveData<MiniAppCustomPermission>()
    private val _errorData = MutableLiveData<String>()

    val miniAppCustomPermission: LiveData<MiniAppCustomPermission>
        get() = _miniAppCustomPermission
    val errorData: LiveData<String>
        get() = _errorData

    fun getCustomPermission(miniAppId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _miniAppCustomPermission.postValue(miniApp.getCustomPermissions(miniAppId))
            } catch (error: MiniAppSdkException) {
                _errorData.postValue(error.message)
            }
        }
}
