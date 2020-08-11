package com.rakuten.tech.mobile.miniapp.permission

import android.app.Activity
import android.content.DialogInterface
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.permission.widget.PermissionDialog
import com.rakuten.tech.mobile.miniapp.permission.widget.PermissionLayout

/**
 * A class to request single or multiple permissions with displaying PermissionDialog.
 */
internal class MiniAppPermissionManager(val activity: Activity) {
    private val permissionChecker = MiniAppPermissionChecker(activity)

    fun startRequestingPermission(permission: String) {
        val permissionLayout = PermissionLayout(activity)
        permissionLayout.text = permission

        val listener = DialogInterface.OnClickListener { _, _ ->
            permissionChecker.storePermissionResult(
                permission,
                permissionLayout.isChecked
            )
            (activity as MiniApp.OnRequestPermissionResultCallback).onRequestPermissionResult(
                permission, permissionLayout.isChecked
            )
        }

        val permissionDialogBuilder = PermissionDialog.Builder().build(activity).apply {
            setView(permissionLayout)
            setListener(listener)
        }

        if (!permissionChecker.checkPermission(permission)) {
            permissionDialogBuilder.show()
        } else {
            (activity as MiniApp.OnRequestPermissionResultCallback).onRequestPermissionResult(
                permission, permissionLayout.isChecked
            )
        }
    }
}
