package com.rakuten.tech.mobile.miniapp.permission.widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting

/**
 * A class to contain a Builder class for creating AlertDialog which is used
 * as a default dialog while requesting permission per MiniApp.
 *
 * <pre>
 * PermissionDialog.Builder().build(context).apply {
 *     setView(view)
 *     setListener(listener)
 * }
 * </pre>
 *
 * The Builder class provides the benefits to create an AlertDialog that can
 * display a ViewGroup as MiniApp permission layout. There are some other
 * functions for setting up a positive button listener as well as
 * showing the permission dialog.
 */
internal class PermissionDialog {

    class Builder {
        @VisibleForTesting
        var alert: AlertDialog.Builder? = null

        fun build(context: Context?): Builder {
            alert = AlertDialog.Builder(context)
            alert?.setMessage("Allow MiniApp to access permission?")
            return this
        }

        fun setView(view: ViewGroup): Builder {
            alert?.setView(view)
            return this
        }

        fun setListener(listener: DialogInterface.OnClickListener): Builder {
            alert?.setPositiveButton("DONE", listener)
            return this
        }

        fun show() {
            alert?.create()?.show()
        }
    }
}
