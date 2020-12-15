package com.rakuten.tech.mobile.miniapp.permission

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.js.CustomPermissionCallbackObj
import com.rakuten.tech.mobile.miniapp.js.CustomPermissionObj
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor

/**
 * A class to dispatch the bridge operations involved with custom permissions in this SDK.
 */
@Suppress("TooGenericExceptionCaught")
internal class CustomPermissionBridgeDispatcher(
    private val bridgeExecutor: MiniAppBridgeExecutor,
    private val customPermissionCache: MiniAppCustomPermissionCache,
    private val miniAppId: String
) {

    private var callbackObj: CustomPermissionCallbackObj? = null
    private var permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>> = emptyList()

    /**
     * assign values to callbackObj and permissionsWithDescription properties using jsonStr data.
     * @param [jsonStr] json string for custom permissions dispatched from MiniAppMessageBridge.
     */
    fun initCallBackObject(jsonStr: String) {
        try {
            callbackObj = Gson().fromJson(jsonStr, CustomPermissionCallbackObj::class.java)
            val permissionObjList = arrayListOf<CustomPermissionObj>()
            callbackObj?.param?.permissions?.forEach {
                permissionObjList.add(CustomPermissionObj(it.name, it.description))
            }
            permissionsWithDescription = preparePermissionsWithDescription(permissionObjList)
        } catch (e: Exception) {
            e.message?.let { postCustomPermissionError(it) }
        }
    }

    /**
     * Prepares a list of custom permissions Pair with names and description.
     * @param [permissionObjList] list of CustomPermissionObj.
     * @return [List<Pair<MiniAppCustomPermissionType, String>>].
     */
    @Suppress("FunctionMaxLength")
    @VisibleForTesting
    fun preparePermissionsWithDescription(
        permissionObjList: ArrayList<CustomPermissionObj>
    ): List<Pair<MiniAppCustomPermissionType, String>> {
        val permissionsWithDescription =
            arrayListOf<Pair<MiniAppCustomPermissionType, String>>()
        permissionObjList.forEach {
            MiniAppCustomPermissionType.getValue(it.name).let { type ->
                permissionsWithDescription.add(Pair(type, it.description))
            }
        }
        return permissionsWithDescription
    }

    /**
     * Prepares a list of custom permissions which are denied.
     * @return [List<Pair<MiniAppCustomPermissionType, String>>].
     */
    fun filterDeniedPermissions(): List<Pair<MiniAppCustomPermissionType, String>> {
        if (permissionsWithDescription.isEmpty()) return emptyList()

        return permissionsWithDescription.filter { (first) ->
            !customPermissionCache.hasPermission(miniAppId, first)
        }
    }

    /**
     * Creates a JSON string by mapping with [MiniAppCustomPermissionResponse] class.
     * @return [String] as json response with permission grant results.
     */
    @VisibleForTesting
    fun createJsonResponse(): String {
        val responseObj = MiniAppCustomPermissionResponse(arrayListOf())
        val permissions = retrievePermissionsForJson()

        if (permissions.isEmpty()) return "${ErrorBridgeMessage.ERR_REQ_CUSTOM_PERMISSION} $EMPTY_RESPONSE"

        permissions.forEach {
            responseObj.permissions.add(
                MiniAppCustomPermissionResponse.CustomPermissionResponseObj(
                    it.first.type,
                    it.second.name
                )
            )
        }

        return Gson().toJson(responseObj).toString()
    }

    /**
     * Retrieves custom permissions from the cache per MiniApp to create a JSON response
     * By default, PERMISSION_NOT_AVAILABLE will be added if there is
     * any unknown permission.
     */
    @VisibleForTesting
    fun retrievePermissionsForJson(): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        if (permissionsWithDescription.isEmpty()) return emptyList()

        val cachedPermissions = customPermissionCache.readPermissions(miniAppId).pairValues
        val filteredPair =
            mutableListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        permissionsWithDescription.forEach { (first) ->
            cachedPermissions.find {
                it.first == first
            }?.let { filteredPair.add(it) }

            // Add PERMISSION_NOT_AVAILABLE if there is any unknown permission parameter
            // sent from HostApp.
            if (first.type == MiniAppCustomPermissionType.UNKNOWN.type)
                filteredPair.add(defaultUnknownPermissionPair)
        }

        return filteredPair
    }

    fun sendHostAppCustomPermissions(
        permissionsWithResult: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
    ) {
        // store values in SDK cache
        val miniAppCustomPermission = MiniAppCustomPermission(
            miniAppId,
            pairValues = permissionsWithResult
        )
        customPermissionCache.storePermissions(miniAppCustomPermission)
        postCustomPermissionsValue(createJsonResponse())
    }

    fun sendCachedCustomPermissions() {
        postCustomPermissionsValue(createJsonResponse())
    }

    @VisibleForTesting
    fun postCustomPermissionsValue(jsonResult: String) {
        // send JSON response to miniapp
        callbackObj?.id?.let { bridgeExecutor.postValue(it, jsonResult) }
    }

    internal fun postCustomPermissionError(errMessage: String) {
        callbackObj?.id?.let {
            bridgeExecutor.postError(
                it,
                "${ErrorBridgeMessage.ERR_REQ_CUSTOM_PERMISSION} $errMessage"
            )
        }
    }

    private val defaultUnknownPermissionPair = Pair(
        MiniAppCustomPermissionType.UNKNOWN,
        MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
    )

    companion object {
        private const val EMPTY_RESPONSE = "{}"
    }
}
