package com.rakuten.tech.mobile.testapp.ui.display

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.WindowShareMiniappBinding
import com.rakuten.tech.mobile.testapp.helper.load
import com.rakuten.tech.mobile.testapp.helper.registerOnShowAndOnDismissEvent

class MiniAppShareWindow {
    companion object {
        private var instance: MiniAppShareWindow? = null
        private lateinit var dialog: AlertDialog
        private lateinit var binding: WindowShareMiniappBinding
        private lateinit var context: Context

        @Synchronized
        fun getInstance(
            context: Context,
            onShow: () -> Unit,
            onDismiss: () -> Unit
        ): MiniAppShareWindow {
            dialog = initDialog(context, onShow, onDismiss)
            this.context = context
            return instance ?: MiniAppShareWindow().also { instance = it }
        }

        private fun initDialog(
            context: Context,
            onShow: () -> Unit,
            onDismiss: () -> Unit
        ): AlertDialog {
            // set ui components
            val layoutInflater = LayoutInflater.from(context)
            binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.window_share_miniapp, null, false
            )
            val alertDialog = AlertDialog.Builder(context, R.style.AppThemeSettings).create()
            alertDialog?.setView(binding.root)
            alertDialog?.registerOnShowAndOnDismissEvent(onShow = onShow, onDismiss = onDismiss)
            return alertDialog
        }
    }

    fun show(miniAppInfo: MiniAppInfo) {
        if (instance != null) {
            renderScreen(miniAppInfo)
        }
    }

    private fun renderScreen(miniAppInfo: MiniAppInfo) {
        if (dialog.isShowing) dismissDialog()
        binding.imgPromotional.load(
            context,
            miniAppInfo.promotionalImageUrl ?: "",
            android.R.color.darker_gray
        )
        binding.tvPromotionalText.text = miniAppInfo.promotionalText
            ?: context.getText(R.string.error_desc_promotional_text_missing)
        binding.btnClose.setOnClickListener(listener)
        dialog.setCancelable(false)
        if (!(context as Activity).isFinishing) {
            dialog.show()
        }
    }

    private fun dismissDialog() {
        dialog.cancel()
    }

    private val listener = View.OnClickListener { dismissDialog() }

}
