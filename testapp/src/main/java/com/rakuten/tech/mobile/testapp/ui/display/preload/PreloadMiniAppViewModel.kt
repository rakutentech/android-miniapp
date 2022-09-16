package com.rakuten.tech.mobile.testapp.ui.display.preload

import androidx.lifecycle.*
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class PreloadMiniAppViewModel(private val miniApp: MiniApp) : ViewModel() {
    private val _miniAppManifest = MutableLiveData<MiniAppManifest?>()
    private val _manifestErrorData = MutableLiveData<String>()
    private val _containTooManyRequestsError = MutableLiveData<Boolean>()

    val miniAppManifest: LiveData<MiniAppManifest?>
        get() = _miniAppManifest
    val manifestErrorData: LiveData<String>
        get() = _manifestErrorData
    val containTooManyRequestsError: LiveData<Boolean>
        get() = _containTooManyRequestsError

    fun checkMiniAppManifest(miniAppId: String, versionId: String) = viewModelScope.launch(Dispatchers.IO) {
        val downloadedManifest = miniApp.getDownloadedManifest(miniAppId)
        try {
            val miniAppManifest = miniApp.getMiniAppManifest(miniAppId, versionId, Locale.getDefault().language)
            if (downloadedManifest != null && isManifestEqual(miniAppManifest, downloadedManifest) &&
                isAcceptedRequiredPermissions(miniAppId, miniAppManifest))
                _miniAppManifest.postValue(null)
            else
                _miniAppManifest.postValue(miniAppManifest)
        } catch (error: MiniAppSdkException) {
            when {
                error is MiniAppNetException && downloadedManifest !== null -> {
                    if (isAcceptedRequiredPermissions(miniAppId, downloadedManifest))
                        _miniAppManifest.postValue(null)
                    else
                        _miniAppManifest.postValue(downloadedManifest)
                }
                error is MiniAppTooManyRequestsError ->
                    _containTooManyRequestsError.postValue(true)
                else -> {
                    _manifestErrorData.postValue(error.message)
                }
            }
        }
    }

    private fun isManifestEqual(apiManifest: MiniAppManifest, downloadedManifest: MiniAppManifest): Boolean {
        val changedRequiredPermissions =
            try {
                (apiManifest.requiredPermissions + downloadedManifest.requiredPermissions)
                    .groupBy { it.first.type }
                    .filter { it.value.size == 1 }
                    .flatMap { it.value }
            } catch (e: Exception) {
                emptyList()
            }

        val changedOptionalPermissions =
            try {
                (apiManifest.optionalPermissions + downloadedManifest.optionalPermissions).groupBy { it.first.type }
                .filter { it.value.size == 1 }
                .flatMap { it.value }
            } catch (e: Exception) {
                emptyList()
            }

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
