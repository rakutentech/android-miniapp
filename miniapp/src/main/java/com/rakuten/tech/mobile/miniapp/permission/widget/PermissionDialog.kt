package com.rakuten.tech.mobile.miniapp.permission.widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup

class PermissionDialog {

    class Builder {
        private var B: AlertDialog.Builder? = null

        fun build(context: Context?): Builder {
            B = AlertDialog.Builder(context)
            B?.setMessage("Allow MiniApp to access permission?")
            return this
        }

        fun setView(view: ViewGroup): Builder {
            B?.setView(view)
            return this
        }

        fun setListener(listener: DialogInterface.OnClickListener): Builder {
            B?.setPositiveButton("DONE", listener)
            return this
        }

        fun show() {
            B?.create()?.show()
        }
    }
}
