package com.rakuten.tech.mobile.testapp.ui.display.preload

import androidx.lifecycle.*
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreloadMiniAppViewModel constructor(private val miniApp: MiniApp) : ViewModel() {
    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private val _miniAppManifest = MutableLiveData<MiniAppManifest>()
    private val _manifestErrorData = MutableLiveData<String>()

    val miniAppManifest: LiveData<MiniAppManifest>
        get() = _miniAppManifest
    val manifestErrorData: LiveData<String>
        get() = _manifestErrorData

    fun checkMiniAppManifest(miniAppId: String, versionId: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val miniAppManifest = miniApp.getMiniAppManifest(miniAppId, versionId)
            val downloadedManifest = miniApp.getDownloadedManifest(miniAppId)
            if (downloadedManifest != null && isManifestEqual(miniAppManifest, downloadedManifest) &&
                isAcceptedRequiredPermissions(miniAppId, miniAppManifest))
                _miniAppManifest.postValue(null)
            else
                _miniAppManifest.postValue(miniAppManifest)
        } catch (error: MiniAppSdkException) {
            _manifestErrorData.postValue(error.message)
        }
    }

    private fun isManifestEqual(apiManifest: MiniAppManifest, downloadedManifest: MiniAppManifest): Boolean {
        val changedRequiredPermissions =
            (apiManifest.requiredPermissions + downloadedManifest.requiredPermissions).groupBy { it.first.type }
                .filter { it.value.size == 1 }
                .flatMap { it.value }

        val changedOptionalPermissions =
            (apiManifest.optionalPermissions + downloadedManifest.optionalPermissions).groupBy { it.first.type }
                .filter { it.value.size == 1 }
                .flatMap { it.value }

        return changedRequiredPermissions.isEmpty() && changedOptionalPermissions.isEmpty() &&
                apiManifest.customMetaData == downloadedManifest.customMetaData
    }

    fun storeManifestPermission(
        miniAppId: String,
        permissions: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
    ) {
        // store values in SDK cache
        val permissionsWhenAccept = MiniAppCustomPermission(
            miniAppId = miniAppId,
            pairValues = permissions
        )
        miniApp.setCustomPermissions(permissionsWhenAccept)
    }

    private fun isAcceptedRequiredPermissions(miniAppId: String, manifest: MiniAppManifest): Boolean {
        // verify if user has been denied any required permission
        val cachedPermissions = miniApp.getCustomPermissions(miniAppId).pairValues
        val notGrantedPairs =
            mutableListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()
        manifest.requiredPermissions.forEach { (first) ->
            cachedPermissions.find {
                it.first == first && it.second == MiniAppCustomPermissionResult.DENIED
            }?.let { notGrantedPairs.add(it) }
        }

        return notGrantedPairs.isEmpty()
    }
}
