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

// TODO
class MiniAppCustomPermissionWindow(
    private val miniApp: MiniApp,
    private val miniAppId: String,
    private val activity: Activity
) {

    private lateinit var customPermissionAlertDialog: AlertDialog
    private lateinit var customPermissionAdapter: MiniAppCustomPermissionAdapter
    private lateinit var customPermissionLayout: View

    private fun getCachedList() = miniApp.getCustomPermissions(miniAppId).pairValues

    private fun getDeniedPermissions(permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>) =
        permissionsWithDescription.filter { (first) ->

            getCachedList().find {
                it.first == first && it.second == MiniAppCustomPermissionResult.DENIED
            } != null
        }

    fun show(
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        if (miniAppId.isEmpty())
            return

        val deniedPermissions = getDeniedPermissions(permissionsWithDescription)

        // show permission default UI if there is any denied permission
        if (deniedPermissions.isNotEmpty()) {

            // initialize permission view after ensuring if there is any denied permission
            initPermissionView(activity)

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
            customPermissionLayout.listCustomPermission.adapter = customPermissionAdapter

            // add action listeners
            addPermissionClickListeners(callback)

            // preview dialog
            customPermissionAlertDialog.show()
        } else {
            callback.invoke(getCachedList())
        }
    }

    private fun initPermissionView(activity: Activity) {
        customPermissionLayout = getPermissionLayout(activity)
        customPermissionAdapter = MiniAppCustomPermissionAdapter()
        customPermissionAlertDialog =
            AlertDialog.Builder(activity, R.style.AppTheme_CustomPermissionDialog).apply {
                setView(customPermissionLayout)
            }.create()
    }

    private fun addPermissionClickListeners(
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        customPermissionLayout.permissionAllow.setOnClickListener {
            callback.invoke(customPermissionAdapter.permissionPairs)
            customPermissionAlertDialog.dismiss()
        }

        customPermissionLayout.permissionDontAllow.setOnClickListener {
            callback.invoke(getCachedList())
            customPermissionAlertDialog.dismiss()
        }
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
