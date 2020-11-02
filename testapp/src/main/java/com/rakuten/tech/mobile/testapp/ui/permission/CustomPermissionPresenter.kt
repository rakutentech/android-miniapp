package com.rakuten.tech.mobile.testapp.ui.permission

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ListCustomPermissionBinding

class CustomPermissionPresenter {

    fun executeCustomPermissionsCallback(
        context: Context,
        miniAppId: String,
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        if (miniAppId.isEmpty())
            return

        // show dialog if permissions are not empty sent by the SDK
        if (permissionsWithDescription.isNotEmpty()) {
            val adapter = MiniAppPermissionSettingsAdapter()
            val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()
            val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()
            val descriptionForAdapter: ArrayList<String> = arrayListOf()
            permissionsWithDescription.forEach {
                namesForAdapter.add(it.first)
                descriptionForAdapter.add(it.second)
                resultsForAdapter.add(MiniAppCustomPermissionResult.ALLOWED)
            }

            adapter.addPermissionList(namesForAdapter, resultsForAdapter, descriptionForAdapter)
            val permissionLayout = getPermissionLayout(context)
            permissionLayout.listCustomPermission.adapter = adapter

            // show dialog with listener which will invoke the callback
            CustomPermissionDialog.Builder().build(context).apply {
                setView(permissionLayout.root)
                setListener(DialogInterface.OnClickListener { _, _ ->
                    callback.invoke(adapter.permissionPairs)
                })
            }.show()
        }
    }

    private fun getPermissionLayout(context: Context): ListCustomPermissionBinding {
        val layoutInflater = LayoutInflater.from(context)
        val permissionLayout = ListCustomPermissionBinding.inflate(layoutInflater, null, false)
        permissionLayout.listCustomPermission.layoutManager = LinearLayoutManager(context)
        permissionLayout.listCustomPermission.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )

        return permissionLayout
    }
}
