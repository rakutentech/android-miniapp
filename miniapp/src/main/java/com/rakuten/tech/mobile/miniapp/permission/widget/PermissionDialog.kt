package com.rakuten.tech.mobile.miniapp.permission.widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting

/**
 * A class to contain a Builder class for creating AlertDialog which is used
 * as a default dialog while requesting permission per MiniApp.
 * <pre>
 * PermissionDialog.Builder().build(context).apply {
 *     setView(view)
 *     setListener(listener)
 * }
 * </pre>
 */
internal class PermissionDialog {

    /**
     * The Builder class provides the functions to create an AlertDialog that can
     * display a ViewGroup as MiniApp permission layout. There are some other
     * functions for setting up a listener as well as showing the permission dialog.
     */
    class Builder {
        @VisibleForTesting
        var alert: AlertDialog.Builder? = null

        /**
         * A function to provide AlertDialog.Builder class with setting message.
         */
        fun build(context: Context?): Builder {
            alert = AlertDialog.Builder(context)
            alert?.setMessage("Allow MiniApp to access permission?")
            return this
        }

        /**
         * A function to set ViewGroup to AlertDialog.Builder.
         */
        fun setView(view: ViewGroup): Builder {
            alert?.setView(view)
            return this
        }

        /**
         * A function to set listener to AlertDialog.Builder.
         */
        fun setListener(listener: DialogInterface.OnClickListener): Builder {
            alert?.setPositiveButton("DONE", listener)
            return this
        }

        /**
         * A function to show the alert dialog.
         */
        fun show() {
            alert?.create()?.show()
        }
    }
}
