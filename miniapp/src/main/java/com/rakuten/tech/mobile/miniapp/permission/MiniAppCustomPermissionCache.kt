package com.rakuten.tech.mobile.miniapp.permission

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception

/**
 * A class to read and store the grant results of custom permissions per MiniApp.
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException", "LongMethod")
internal class MiniAppCustomPermissionCache(context: Context) {
    @VisibleForTesting
    val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.custom.permissions.cache", Context.MODE_PRIVATE
    )

    /**
     * Reads the grant results from SharedPreferences.
     * @param [miniAppId] the key provided to find the stored results per MiniApp
     * @return [MiniAppCustomPermission] an object to contain the results per MiniApp
     * if data has been stored in cache, otherwise null.
     */
    fun readPermissions(miniAppId: String): MiniAppCustomPermission? {
        if (prefs.contains(miniAppId)) {
            return try {
                Gson().fromJson(
                    prefs.getString(miniAppId, ""),
                    object : TypeToken<MiniAppCustomPermission>() {}.type
                )
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    /**
     * Stores the grant results to SharedPreferences.
     * @param [miniAppCustomPermission] an object to contain the results per MiniApp.
     * @return [String] JSON string response provides custom permission names and
     * the corresponding grant results to the HostApp.
     */
    fun storePermissions(
        miniAppCustomPermission: MiniAppCustomPermission
    ): String {
        val cached = readPermissions(miniAppCustomPermission.miniAppId)?.pairValues ?: emptyList()
        val supplied = miniAppCustomPermission.pairValues.toMutableList()
        // HostApp can send unknown permission parameter, but no need to cache it
        supplied.removeAll { (first) ->
            first.type == MiniAppCustomPermissionType.UNKNOWN.type
        }

        val result = combineAllPermissionsToStore(cached, supplied)
        if (result.isNotEmpty()) {
            return try {
                val json: String = Gson().toJson(
                    MiniAppCustomPermission(
                        miniAppCustomPermission.miniAppId,
                        result
                    )
                )
                prefs.edit().putString(miniAppCustomPermission.miniAppId, json).apply()

                // return JSON string response after stored data
                toJsonResponse(filterSuppliedPermissionsToSend(miniAppCustomPermission))
            } catch (e: Exception) {
                e.message.toString()
            }
        }
        return ""
    }

    /**
     * Combines all custom permissions per MiniApp by comparing the cached and supplied
     * custom permissions with replacing old grant results with new grant results.
     */
    @VisibleForTesting
    internal fun combineAllPermissionsToStore(
        cached: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>,
        supplied: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val combined = (cached + supplied).toMutableList()
        combined.removeAll { (first) ->
            first.type in supplied.groupBy { it.first.type }
        }
        return combined + supplied
    }

    /**
     * Filters supplied custom permissions from all cached custom permissions per MiniApp.
     */
    private fun filterSuppliedPermissionsToSend(
        suppliedPermission: MiniAppCustomPermission
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val cached = readPermissions(suppliedPermission.miniAppId)?.pairValues ?: emptyList()
        val supplied = suppliedPermission.pairValues
        val filteredPair =
            mutableListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        supplied.forEach { (first) ->
            if (cached.isNotEmpty()) {
                filteredPair.addAll(cached.filter {
                    first.type == it.first.type
                })
            }

            if (first.type == MiniAppCustomPermissionType.UNKNOWN.type)
                filteredPair.add(
                    Pair(
                        first,
                        MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
                    )
                )
        }

        return filteredPair
    }

    /**
     * Provides a JSON string by mapping with [MiniAppCustomPermissionResponse] class.
     * @param [permissions] list of custom permissions Pair.
     * @return [String].
     */
    @VisibleForTesting
    internal fun toJsonResponse(
        permissions: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
    ): String {
        val responseObj =
            MiniAppCustomPermissionResponse(
                arrayListOf()
            )
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
}
