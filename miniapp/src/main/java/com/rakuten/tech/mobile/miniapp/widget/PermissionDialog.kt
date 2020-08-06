package com.rakuten.tech.mobile.miniapp.widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup

class PermissionDialog {

    class Builder {
        private var B: AlertDialog.Builder? = null

        fun build(context: Context?): Builder {
            B = AlertDialog.Builder(context)
            return this
        }

        fun setInfo(miniAppName: String): Builder {
            B?.setMessage("Allow $miniAppName to access permission?")
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
