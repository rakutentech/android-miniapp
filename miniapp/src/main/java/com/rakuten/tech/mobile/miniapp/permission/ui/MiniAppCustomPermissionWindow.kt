package com.rakuten.tech.mobile.miniapp.permission.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.R
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * A class to show default custom permissions UI to manage permissions in this SDK.
 */
internal class MiniAppCustomPermissionWindow(
    private val activity: Activity,
    private val customPermissionCache: MiniAppCustomPermissionCache
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    @VisibleForTesting
    lateinit var customPermissionAlertDialog: AlertDialog

    private lateinit var customPermissionAdapter: MiniAppCustomPermissionAdapter
    private lateinit var customPermissionLayout: View

    fun displayPermissions(
        miniAppId: String,
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        if (miniAppId.isEmpty())
            return

        val deniedPermissions = getDeniedPermissions(miniAppId, permissionsWithDescription)

        // show permission default UI if there is any denied permission
        if (deniedPermissions.isNotEmpty()) {
            launch {
                // initialize permission view after ensuring if there is any denied permission
                initDefaultWindow()
                prepareDataForAdapter(deniedPermissions)

                // add action listeners
                addPermissionClickListeners(miniAppId, callback)

                // preview dialog
                customPermissionAlertDialog.show()
            }
        } else {
            invokeCachedPermissions(miniAppId, callback)
        }
    }

    private fun invokeCachedPermissions(
        miniAppId: String,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        callback.invoke(getCachedList(miniAppId))
    }

    @VisibleForTesting
    fun initDefaultWindow() {
        val layoutInflater = LayoutInflater.from(activity)
        customPermissionLayout = layoutInflater.inflate(R.layout.window_custom_permission, null)
        val permissionRecyclerView =
            customPermissionLayout.findViewById<RecyclerView>(R.id.listCustomPermission)
        permissionRecyclerView.layoutManager = LinearLayoutManager(activity)
        permissionRecyclerView.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )

        customPermissionAdapter = MiniAppCustomPermissionAdapter()
        permissionRecyclerView.adapter = customPermissionAdapter

        customPermissionAlertDialog =
            AlertDialog.Builder(activity, R.style.AppTheme_CustomPermissionDialog).create()
        customPermissionAlertDialog.setView(customPermissionLayout)
    }

    @VisibleForTesting
    fun prepareDataForAdapter(deniedPermissions: List<Pair<MiniAppCustomPermissionType, String>>) {
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
    }

    @VisibleForTesting
    fun addPermissionClickListeners(
        miniAppId: String,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        customPermissionLayout.findViewById<TextView>(R.id.permissionSave).setOnClickListener {
            callback.invoke(customPermissionAdapter.permissionPairs)
            customPermissionAlertDialog.dismiss()
        }

        customPermissionLayout.findViewById<TextView>(R.id.permissionCloseWindow)
            .setOnClickListener {
                invokeCachedPermissions(miniAppId, callback)
                customPermissionAlertDialog.dismiss()
            }
    }

    @VisibleForTesting
    fun getCachedList(miniAppId: String) =
        customPermissionCache.readPermissions(miniAppId).pairValues

    @VisibleForTesting
    fun getDeniedPermissions(
        miniAppId: String,
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>
    ) = permissionsWithDescription.filter { (first) ->
        getCachedList(miniAppId).find {
            it.first == first && it.second == MiniAppCustomPermissionResult.DENIED
        } != null
    }
}
