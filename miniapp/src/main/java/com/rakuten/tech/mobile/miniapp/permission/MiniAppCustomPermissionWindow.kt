package com.rakuten.tech.mobile.miniapp.permission

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.R
import kotlinx.android.synthetic.main.window_custom_permission.view.*

class MiniAppCustomPermissionWindow {

//    private lateinit var activity: Activity
//    private lateinit var customPermissionCache: MiniAppCustomPermissionCache
//    private lateinit var miniAppId: String
//    private lateinit var permissionAlertDialog: AlertDialog.Builder
//    private lateinit var customPermissionLayout: View
//    private lateinit var customPermissionAdapter: MiniAppCustomPermissionAdapter

//    fun init(
//        activity: Activity,
//        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
//        miniAppId: String
//    ) {
//        this.activity = activity
//        this.customPermissionCache = miniAppCustomPermissionCache
//        this.miniAppId = miniAppId
//
//        preparePermissionDefaultUI()
//    }

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
            val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()
            val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()
            val descriptionForAdapter: ArrayList<String> = arrayListOf()
            permissionsForAdapter.forEach {
                namesForAdapter.add(it.first)
                descriptionForAdapter.add(it.second)
                resultsForAdapter.add(MiniAppCustomPermissionResult.ALLOWED)
            }

            val customPermissionAdapter = MiniAppCustomPermissionAdapter()
            customPermissionAdapter.addPermissionList(namesForAdapter, resultsForAdapter, descriptionForAdapter)
            val permissionLayout = getPermissionLayout(activity)
            permissionLayout.listCustomPermission.adapter = customPermissionAdapter

            // show dialog with listener which will invoke the callback
            val alert = AlertDialog.Builder(activity, R.style.AppTheme_CustomPermissionDialog)
                .setView(permissionLayout)
                .create()

            permissionLayout.permissionAllow.setOnClickListener {
                callback.invoke(customPermissionAdapter.permissionPairs)
                alert.dismiss()
            }

            permissionLayout.permissionDontAllow.setOnClickListener {
                callback.invoke(cachedList)
                alert.dismiss()
            }

            alert.show()
        } else {
            callback.invoke(cachedList)
        }
    }

//    private fun preparePermissionDefaultUI() {
//        customPermissionLayout = getPermissionLayout(activity)
//        customPermissionAdapter = MiniAppCustomPermissionAdapter()
//        permissionAlertDialog = AlertDialog.Builder(activity, R.style.AppTheme_CustomPermissionDialog)
//                .setView(customPermissionLayout)
//    }

    private fun setPositiveButton() {

    }

    private fun getPermissionLayout(activity: Activity): View {
        val layoutInflater = LayoutInflater.from(activity)
        val permissionLayout = layoutInflater.inflate(R.layout.window_custom_permission, null)
        permissionLayout.listCustomPermission.layoutManager = LinearLayoutManager(activity)
        permissionLayout.listCustomPermission.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )

        return permissionLayout
    }
}
