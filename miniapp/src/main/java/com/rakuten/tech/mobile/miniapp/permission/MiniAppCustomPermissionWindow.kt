package com.rakuten.tech.mobile.miniapp.permission

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.R
import kotlinx.android.synthetic.main.window_custom_permission.view.listCustomPermission
import kotlinx.android.synthetic.main.window_custom_permission.view.permissionSave
import kotlinx.android.synthetic.main.window_custom_permission.view.permissionCloseWindow

/**
 * A class to show default custom permissions UI to manage permissions in this SDK.
 */
internal class MiniAppCustomPermissionWindow(
    private val customPermissionCache: MiniAppCustomPermissionCache
) {
    private lateinit var customPermissionAlertDialog: AlertDialog
    private lateinit var customPermissionAdapter: MiniAppCustomPermissionAdapter
    private lateinit var customPermissionLayout: View

    fun show(
        activity: Activity,
        miniAppId: String,
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        if (miniAppId.isEmpty())
            return

        val deniedPermissions = getDeniedPermissions(miniAppId, permissionsWithDescription)

        // show permission default UI if there is any denied permission
        if (deniedPermissions.isNotEmpty()) {
            // initialize permission view after ensuring if there is any denied permission
            initDefaultWindow(activity)

            // prepare data for adapter
            val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()
            val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()
            val descriptionForAdapter: ArrayList<String> = arrayListOf()
            deniedPermissions.forEach {
                namesForAdapter.add(it.first)
                descriptionForAdapter.add(it.second)
                resultsForAdapter.add(MiniAppCustomPermissionResult.ALLOWED)
            }
            customPermissionAdapter.addPermissionList(
                namesForAdapter,
                resultsForAdapter,
                descriptionForAdapter
            )

            // add action listeners
            addPermissionClickListeners(miniAppId, callback)

            // preview dialog
            customPermissionAlertDialog.show()
        } else {
            callback.invoke(getCachedList(miniAppId))
        }
    }

    private fun initDefaultWindow(activity: Activity) {
        val layoutInflater = LayoutInflater.from(activity)
        customPermissionLayout = layoutInflater.inflate(R.layout.window_custom_permission, null)
        customPermissionLayout.listCustomPermission.layoutManager = LinearLayoutManager(activity)
        customPermissionLayout.listCustomPermission.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )

        customPermissionAdapter = MiniAppCustomPermissionAdapter()
        customPermissionLayout.listCustomPermission.adapter = customPermissionAdapter

        customPermissionAlertDialog =
            AlertDialog.Builder(activity, R.style.AppTheme_CustomPermissionDialog).create()
        customPermissionAlertDialog.setView(customPermissionLayout)
    }

    private fun addPermissionClickListeners(
        miniAppId: String,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        customPermissionLayout.permissionSave.setOnClickListener {
            callback.invoke(customPermissionAdapter.permissionPairs)
            customPermissionAlertDialog.dismiss()
        }

        customPermissionLayout.permissionCloseWindow.setOnClickListener {
            callback.invoke(getCachedList(miniAppId))
            customPermissionAlertDialog.dismiss()
        }
    }

    private fun getCachedList(miniAppId: String) =
        customPermissionCache.readPermissions(miniAppId).pairValues

    private fun getDeniedPermissions(
        miniAppId: String,
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>
    ) = permissionsWithDescription.filter { (first) ->
        getCachedList(miniAppId).find {
            it.first == first && it.second == MiniAppCustomPermissionResult.DENIED
        } != null
    }
}
