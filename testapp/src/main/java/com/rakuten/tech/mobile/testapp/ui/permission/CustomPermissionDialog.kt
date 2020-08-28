package com.rakuten.tech.mobile.testapp.ui.permission

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View

class CustomPermissionDialog {

    class Builder {
        var alert: AlertDialog.Builder? = null

        fun build(context: Context?, miniAppName: String): Builder {
            alert = AlertDialog.Builder(context)
            alert?.setMessage("Are you sure you don't want to allow any of the permissions for $miniAppName?")
            return this
        }

        fun setView(view: View): Builder {
            alert?.setView(view)
            return this
        }

        fun setListener(listener: DialogInterface.OnClickListener): Builder {
            alert?.setPositiveButton("Okay", listener)
            return this
        }

        fun show() {
            alert?.create()?.let {
                if (!it.isShowing)
                    it.show()
            }
        }
    }
}
