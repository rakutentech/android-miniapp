package com.rakuten.tech.mobile.miniapp.permission.ui

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.R
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
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
@SuppressWarnings("LongMethod")
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

    @SuppressWarnings("MagicNumber")
    fun displayPermissions(
        miniAppId: String,
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>
    ) {
        if (miniAppId.isEmpty())
            return

        val deniedPermissions = getDeniedPermissions(miniAppId, permissionsWithDescription)

        launch {
            // show permission default UI if there is any denied permission
            if (deniedPermissions.isNotEmpty()) {
                // initialize permission view after ensuring if there is any denied permission
                initDefaultWindow()
                prepareDataForAdapter(deniedPermissions)

                // add action listeners
                addPermissionClickListeners(miniAppId)

                // preview dialog
                customPermissionAlertDialog.show()
            } else {
                val toast =
                    Toast.makeText(
                        activity,
                        "Requested permissions has been granted!",
                        Toast.LENGTH_LONG
                    )
                toast.setGravity(Gravity.BOTTOM, 0, 100)
                toast.show()
            }
        }
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
        miniAppId: String
    ) {
        customPermissionLayout.findViewById<TextView>(R.id.permissionSave).setOnClickListener {
            // store values in SDK cache
            val miniAppCustomPermission = MiniAppCustomPermission(
                miniAppId = miniAppId,
                pairValues = customPermissionAdapter.permissionPairs
            )
            customPermissionCache.storePermissions(miniAppCustomPermission)

            customPermissionAlertDialog.dismiss()
        }

        customPermissionLayout.findViewById<TextView>(R.id.permissionCloseWindow)
            .setOnClickListener {
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
