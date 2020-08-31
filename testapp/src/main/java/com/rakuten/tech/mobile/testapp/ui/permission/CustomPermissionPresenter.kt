package com.rakuten.tech.mobile.testapp.ui.permission

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ListCustomPermissionBinding
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class CustomPermissionPresenter(private val miniapp: MiniApp) {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    fun promptForCustomPermissions(
        context: Context,
        miniAppId: String,
        permissions: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (grantResult: String) -> Unit
    ) {
        if (miniAppId.isEmpty())
            return

        // prepare UI for adapter
        val layoutInflater = LayoutInflater.from(context)
        val permissionLayout = ListCustomPermissionBinding.inflate(layoutInflater, null, false)
        permissionLayout.listCustomPermission.layoutManager = LinearLayoutManager(context)
        val adapter = CustomPermissionAdapter()

        permissionLayout.listCustomPermission.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        // prepare adapter for showing items
        val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()
        val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()
        val descriptionForAdapter: ArrayList<String> = arrayListOf()

        permissions.forEach {
            namesForAdapter.add(it.first)
            descriptionForAdapter.add(it.second)
        }

        val cachedList = miniapp.getCustomPermissions(miniAppId)?.pairValues

        val filteredPair =
            mutableListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        permissions.forEach { (first) ->
            if (cachedList != null) {
                if (first == MiniAppCustomPermissionType.UNKNOWN)
                    filteredPair.add(
                        Pair(
                            first,
                            MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
                        )
                    )
                filteredPair.addAll(cachedList.filter {
                    first.type == it.first.type
                })
            } else {
                if (first == MiniAppCustomPermissionType.UNKNOWN)
                    filteredPair.add(
                        Pair(
                            first,
                            MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
                        )
                    )
                else
                    filteredPair.add(Pair(first, MiniAppCustomPermissionResult.DENIED))
            }
        }

        filteredPair.forEach {
            resultsForAdapter.add(it.second)
        }
        adapter.addPermissionList(namesForAdapter, resultsForAdapter, descriptionForAdapter)
        permissionLayout.listCustomPermission.adapter = adapter

        // prepare listener for adapter
        val permissionsToStore = MiniAppCustomPermission(miniAppId, adapter.permissionPairs)

        val listener = DialogInterface.OnClickListener { _, _ ->
            val response = miniapp.setCustomPermissions(
                permissionsToStore
            )

            // send json response to miniapp
            callback.invoke(response)
        }

        val permissionDialogBuilder =
            CustomPermissionDialog.Builder().build(context).apply {
                setView(permissionLayout.root)
                setListener(listener)
            }

        // show dialog
        permissionDialogBuilder.show()
    }
}
