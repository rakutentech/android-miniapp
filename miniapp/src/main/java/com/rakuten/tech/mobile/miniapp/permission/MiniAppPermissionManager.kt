package com.rakuten.tech.mobile.miniapp.permission

import android.app.Activity
import android.content.DialogInterface
import com.rakuten.tech.mobile.miniapp.permission.widget.PermissionDialog
import com.rakuten.tech.mobile.miniapp.permission.widget.PermissionToggle

class MiniAppPermissionManager {

    internal fun startRequestingSinglePermission(
        activity: Activity,
        permission: String
    ) {
        val permissionSwitch = PermissionToggle(activity)
        permissionSwitch.textView.text = permission
        val permissionChecker =
            MiniAppPermissionChecker(
                activity
            )

        val listener = DialogInterface.OnClickListener { _, _ ->
            permissionChecker.setPermissionResult(
                permission,
                permissionSwitch.isChecked
            )
            (activity as OnRequestPermissionResultCallback).onRequestPermissionResult(
                permission, permissionSwitch.isChecked
            )
        }

        val permissionDialogBuilder = PermissionDialog.Builder().build(activity).apply {
            setView(permissionSwitch)
            setListener(listener)
        }

        if (!permissionChecker.checkPermission(permission)) {
            permissionDialogBuilder.show()
        } else {
            (activity as OnRequestPermissionResultCallback).onRequestPermissionResult(
                permission, permissionSwitch.isChecked
            )
        }
    }

    interface OnRequestPermissionResultCallback {
        fun onRequestPermissionResult(
            permission: String,
            isGranted: Boolean
        )
    }
}
