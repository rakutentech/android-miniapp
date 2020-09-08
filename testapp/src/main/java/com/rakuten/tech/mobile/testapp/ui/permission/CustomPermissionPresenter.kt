package com.rakuten.tech.mobile.testapp.ui.permission

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionManager
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ListCustomPermissionBinding
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class CustomPermissionPresenter(private val miniapp: MiniApp) {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    fun promptForCustomPermissions(
        context: Context,
        miniAppId: String,
        permissionWithDescriptions: List<Pair<MiniAppCustomPermissionType, String>>,
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
        val cachedList = miniapp.getCustomPermissions(miniAppId).pairValues
        val filteredPair = permissionWithDescriptions.filter { (first) ->
            cachedList.find {
                it.first == first && it.second == MiniAppCustomPermissionResult.DENIED
            } != null
        }

        val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()
        val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()
        val descriptionForAdapter: ArrayList<String> = arrayListOf()

        filteredPair.forEach {
            namesForAdapter.add(it.first)
            descriptionForAdapter.add(it.second)
            resultsForAdapter.add(MiniAppCustomPermissionResult.ALLOWED)
        }

        adapter.addPermissionList(namesForAdapter, resultsForAdapter, descriptionForAdapter)
        permissionLayout.listCustomPermission.adapter = adapter

        // prepare listener for adapter
        val permissionsToStore = MiniAppCustomPermission(miniAppId, adapter.permissionPairs)

        val listener = DialogInterface.OnClickListener { _, _ ->
            miniapp.setCustomPermissions(permissionsToStore)

            // send the callback with grant results
            sendGrantResultCallback(miniAppId, permissionWithDescriptions, callback)
        }

        val permissionDialogBuilder =
            CustomPermissionDialog.Builder().build(context).apply {
                setView(permissionLayout.root)
                setListener(listener)
            }

        // show dialog if there is any denied permission,
        // otherwise send the callback with grant results
        if (filteredPair.isNotEmpty())
            permissionDialogBuilder.show()
        else
            sendGrantResultCallback(miniAppId, permissionWithDescriptions, callback)
    }

    private fun sendGrantResultCallback(
        miniAppId: String,
        permissionWithDescriptions: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (grantResult: String) -> Unit
    ) {
        // send json response to miniapp
        val result = MiniAppCustomPermissionManager(miniapp).createJsonResponse(
            miniAppId,
            permissionWithDescriptions
        )
        callback.invoke(result)
    }
}
