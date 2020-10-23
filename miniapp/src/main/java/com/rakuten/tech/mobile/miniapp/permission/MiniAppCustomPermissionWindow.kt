package com.rakuten.tech.mobile.miniapp.permission

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.R
import kotlinx.android.synthetic.main.window_custom_permission.view.*

class MiniAppCustomPermissionWindow {

    fun show(
        miniApp: MiniApp,
        activity: Activity,
        miniAppId: String,
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        if (miniAppId.isEmpty())
            return

        // get cached data from SDK
        val cachedList = miniApp.getCustomPermissions(miniAppId).pairValues

        // prepare data for adapter to check if there is any denied permission
        val permissionsForAdapter = permissionsWithDescription.filter { (first) ->
            cachedList.find {
                it.first == first && it.second == MiniAppCustomPermissionResult.DENIED
            } != null
        }

        // show dialog if there is any denied permission
        if (permissionsForAdapter.isNotEmpty()) {
            val adapter = MiniAppPermissionCustomAdapter()
            val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()
            val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()
            val descriptionForAdapter: ArrayList<String> = arrayListOf()
            permissionsForAdapter.forEach {
                namesForAdapter.add(it.first)
                descriptionForAdapter.add(it.second)
                resultsForAdapter.add(MiniAppCustomPermissionResult.ALLOWED)
            }

            adapter.addPermissionList(namesForAdapter, resultsForAdapter, descriptionForAdapter)
            val permissionLayout = getPermissionLayout(activity)
            permissionLayout.listCustomPermission.adapter = adapter

            // show dialog with listener which will invoke the callback
            AlertDialog.Builder(activity, android.R.style.Theme_NoTitleBar_Fullscreen)
                .setMessage("The miniapp wants to access the below permissions. You can also manage these permissions later in the Mini App settings.")
                .setView(permissionLayout)
                .setPositiveButton("Done") { _, _ ->
                    callback.invoke(adapter.permissionPairs)
                }
                .create().show()
        } else {
            callback.invoke(cachedList)
        }
    }

    private fun getPermissionLayout(context: Context): View {
        val layoutInflater = LayoutInflater.from(context)
        val permissionLayout = layoutInflater.inflate(R.layout.window_custom_permission, null)
        permissionLayout.listCustomPermission.layoutManager = LinearLayoutManager(context)
        permissionLayout.listCustomPermission.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )

        return permissionLayout
    }
}
