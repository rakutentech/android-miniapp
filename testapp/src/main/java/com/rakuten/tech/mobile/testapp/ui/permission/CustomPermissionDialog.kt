package com.rakuten.tech.mobile.testapp.ui.permission

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class CustomPermissionDialog {

    class Builder {
        var alert: AlertDialog.Builder? = null

        fun build(context: Context?): Builder {
            alert = AlertDialog.Builder(context)
            setTitle(context)
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

        private fun setTitle(context: Context?) {
            val title = TextView(context)
            title.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            title.setPadding(4, 36, 4, 36)
            title.setBackgroundColor(Color.parseColor("#f2f3f4"))
            title.setTextColor(Color.BLACK)
            title.text =
                "The miniapp wants to access the below permissions. You can also manage these permissions later in the Mini App settings."
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            title.gravity = Gravity.CENTER
            alert?.setCustomTitle(title)
        }
    }
}
