package com.rakuten.tech.mobile.testapp.ui.permission

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ListCustomPermissionBinding
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class CustomPermissionPresenter(private val miniapp: MiniApp) {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    fun executeCustomPermissionsCallback(
        context: Context,
        miniAppId: String,
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        if (miniAppId.isEmpty())
            return

        // get cached data from SDK
        val cachedList = miniapp.getCustomPermissions(miniAppId).pairValues

        // prepare data for adapter to check if there is any denied permission
        val permissionsForAdapter = permissionsWithDescription.filter { (first) ->
            cachedList.find {
                it.first == first && it.second == MiniAppCustomPermissionResult.DENIED
            } != null
        }

        // show dialog if there is any denied permission
        if (permissionsForAdapter.isNotEmpty()) {
            val adapter = CustomPermissionAdapter()
            val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()
            val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()
            val descriptionForAdapter: ArrayList<String> = arrayListOf()
            permissionsForAdapter.forEach {
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
        } else {
            callback.invoke(cachedList)
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
