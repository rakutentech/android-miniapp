package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View

class ContactInputDialog {

    class Builder {
        var alert: AlertDialog.Builder? = null

        fun build(context: Context?): Builder {
            alert = AlertDialog.Builder(context)
            alert?.setTitle("Please enter the custom ID you would like to add in Contacts")
            setNegativeListener()
            return this
        }

        fun setView(view: View): Builder {
            alert?.setView(view)
            return this
        }

        fun setPositiveListener(listener: DialogInterface.OnClickListener): Builder {
            alert?.setPositiveButton("Add", listener)
            return this
        }

        private fun setNegativeListener(): Builder {
            alert?.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
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
