package com.rakuten.tech.mobile.testapp.ui.display.preload

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PreloadMiniAppViewModel(private val miniApp: MiniApp, private val shouldShowDialog: Boolean) :
    ViewModel() {
    private val _miniAppManifest = MutableLiveData<MiniAppManifest?>()
    private val _manifestErrorData = MutableLiveData<String>()
    private val _containTooManyRequestsError = MutableLiveData<Boolean>()

    val shouldshowDialog: Boolean
        get() = shouldShowDialog
    val miniAppManifest: LiveData<MiniAppManifest?>
        get() = _miniAppManifest
    val manifestErrorData: LiveData<String>
        get() = _manifestErrorData
    val containTooManyRequestsError: LiveData<Boolean>
        get() = _containTooManyRequestsError

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun checkMiniAppManifest(miniAppId: String, versionId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val downloadedManifest = miniApp.getDownloadedManifest(miniAppId)
            try {
                val miniAppManifest =
                    miniApp.getMiniAppManifest(miniAppId, versionId, Locale.getDefault().language)
                if (isAlreadyAccepted(
                        downloadedManifest = downloadedManifest,
                        miniAppManifest = miniAppManifest,
                        miniAppId = miniAppId
                    )
                ) {
                    onAlreadyAccepted(miniAppManifest)
                    return@launch
                }
                _miniAppManifest.postValue(miniAppManifest)
            } catch (error: MiniAppNetException) {
                handleMiniAppNetException(
                    downloadedManifest = downloadedManifest,
                    miniAppId = miniAppId
                )
            } catch (error: MiniAppTooManyRequestsError) {
                _containTooManyRequestsError.postValue(
                    true
                )
            } catch (error: Exception) {
                _manifestErrorData.postValue(error.message)
            }
        }

    private fun onAlreadyAccepted(miniAppManifest: MiniAppManifest) {
        if (shouldShowDialog) {
            _miniAppManifest.postValue(miniAppManifest)
        } else {
            _miniAppManifest.postValue(null)
        }
    }

    private fun handleMiniAppNetException(downloadedManifest: MiniAppManifest?, miniAppId: String) {
        downloadedManifest?.let {
            if (isAcceptedRequiredPermissions(
                    miniAppId,
                    downloadedManifest
                )
            ) {
                _miniAppManifest.postValue(null)
                return@let
            }
        }
        _miniAppManifest.postValue(downloadedManifest)
    }

    private fun isAlreadyAccepted(
        downloadedManifest: MiniAppManifest?,
        miniAppManifest: MiniAppManifest,
        miniAppId: String
    ): Boolean =
        downloadedManifest != null && isManifestEqual(
            miniAppManifest, downloadedManifest
        ) && isAcceptedRequiredPermissions(miniAppId, miniAppManifest)


    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    private fun isManifestEqual(
        apiManifest: MiniAppManifest, downloadedManifest: MiniAppManifest
    ): Boolean {
        val changedRequiredPermissions = try {
            (apiManifest.requiredPermissions + downloadedManifest.requiredPermissions).groupBy { it.first.type }
                .filter { it.value.size == 1 }.flatMap { it.value }
        } catch (e: Exception) {
            emptyList()
        }

        val changedOptionalPermissions = try {
            (apiManifest.optionalPermissions + downloadedManifest.optionalPermissions).groupBy { it.first.type }
                .filter { it.value.size == 1 }.flatMap { it.value }
        } catch (e: Exception) {
            emptyList()
        }

        return changedRequiredPermissions.isEmpty()
                && changedOptionalPermissions.isEmpty()
                && apiManifest.customMetaData == downloadedManifest.customMetaData
    }

    fun storeManifestPermission(
        miniAppId: String,
        permissions: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
    ) {
        // store values in SDK cache
        val permissionsWhenAccept = MiniAppCustomPermission(
            miniAppId = miniAppId, pairValues = permissions
        )
        miniApp.setCustomPermissions(permissionsWhenAccept)
    }

    private fun isAcceptedRequiredPermissions(
        miniAppId: String, manifest: MiniAppManifest
    ): Boolean {
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
