package com.rakuten.tech.mobile.testapp.ui.settings

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.rakuten.tech.mobile.miniapp.testapp.R

class SettingsProgressDialog(
    context: Context
) : Dialog(context) {

    init {
        setContentView(R.layout.settings_progress_dialog)
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}
