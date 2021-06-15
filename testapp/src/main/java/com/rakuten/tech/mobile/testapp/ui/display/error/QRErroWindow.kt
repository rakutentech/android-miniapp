package com.rakuten.tech.mobile.testapp.ui.display.error

import android.app.AlertDialog
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.WindowQrCodeErrorBinding

/**
 * This QRErroWindow is the common class for qrcode/deeplink error.
 */
class QRErroWindow {

    companion object {
        private var mInstance: QRErroWindow? = null
        private var dialog: AlertDialog? = null
        private var binding: WindowQrCodeErrorBinding? = null
        private lateinit var context: Context

        @Synchronized
        fun getInstance(context: Context): QRErroWindow {
            if (mInstance == null) {
                mInstance = QRErroWindow()
            }
            dialog = dialog ?: initDialog(context)
            this.context = context
            return mInstance as QRErroWindow
        }

        private fun initDialog(context: Context): AlertDialog {
            // set ui components
            val layoutInflater = LayoutInflater.from(context)
            binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.window_qr_code_error, null, false
            )
            val alertDialog = AlertDialog.Builder(context, R.style.AppTheme_DefaultWindow).create()
            alertDialog?.setView(binding?.root)
            return alertDialog
        }
    }

    /** show error screen for miniApp no longer exist. */
    fun showMiniAppNoLongerExistError() {
        renderScreen(QRCodeErrorType.MiniAppNoLongerExist)
    }

    /** show error screen for does not have permission. */
    fun showMiniAppPermissionError() {
        renderScreen(QRCodeErrorType.MiniAppNoPermission)
    }

    /** show error screen for qrCode expired. */
    fun showQRCodeExpiredError() {
        renderScreen(QRCodeErrorType.QRCodeExpire)
    }

    /** show error screen for miniApp can not be previewed for specific version. */
    fun showMiniAppPreviewError(versionCode: String) {
        renderScreen(QRCodeErrorType.MiniAppNoPreview, versionCode)
    }

    /** show error screen for miniApp version do not exist. */
    fun showMiniAppVersionError(versionCode: String) {
        renderScreen(QRCodeErrorType.MiniAppVersionMisMatch, versionCode)
    }

    private fun dissmissDialog() {
        dialog?.cancel()
    }

    private enum class QRCodeErrorType {
        MiniAppNoLongerExist,
        MiniAppNoPermission,
        QRCodeExpire,
        MiniAppNoPreview,
        MiniAppVersionMisMatch
    }

    /** render view for specific error type. */
    private fun renderScreen(type: QRCodeErrorType, versionCode: String? = null) {
        if (dialog?.isShowing == true) dialog?.cancel()
        binding?.imgThumbnail?.setImageResource(getThumbImage(type))
        binding?.tvErrorTitle?.text = getTitleDescription(type, versionCode).first
        binding?.tvErrorDescription?.text = getTitleDescription(type, versionCode).second
        binding?.tvErrorDescription?.movementMethod = LinkMovementMethod.getInstance();
        binding?.btnClose?.setOnClickListener(listener)
        dialog?.show()
    }

    /** return the thumb image res id. */
    private fun getThumbImage(type: QRCodeErrorType): Int{
        return when(type){
            QRCodeErrorType.MiniAppNoLongerExist,QRCodeErrorType.MiniAppNoPermission -> R.drawable.ic_qr_error_invalid_miniapp
            else -> R.drawable.ic_qr_error_basic
        }
    }

    /** return the error title and error description for specific error type. */
    private fun getTitleDescription(type: QRCodeErrorType, versionCode: String? = null): Pair<String,SpannableString>{
        when (type) {
            QRCodeErrorType.MiniAppNoLongerExist -> {
                return Pair(
                    context.resources.getString(R.string.error_title_miniapp_no_longer_exist),
                    spanDescription(context.resources.getString(R.string.error_desc_miniapp_no_longer_exist))
                )
            }
            QRCodeErrorType.MiniAppNoPermission -> {
                return Pair(
                    context.resources.getString(R.string.error_title_miniapp_no_permission),
                    spanDescription(context.resources.getString(R.string.error_desc_miniapp_no_permission))
                )
            }
            QRCodeErrorType.QRCodeExpire -> {
                return Pair(
                    context.resources.getString(R.string.error_title_qr_code_expire),
                    spanDescription(context.resources.getString(R.string.error_desc_qr_code_expire))
                )
            }
            QRCodeErrorType.MiniAppNoPreview -> {
                return Pair(
                    context.resources.getString(R.string.error_title_miniapp_no_preview,versionCode ?: ""),
                    spanDescription(context.resources.getString(R.string.error_desc_miniapp_no_preview))
                )
            }
            QRCodeErrorType.MiniAppVersionMisMatch -> {
                return Pair(
                    context.resources.getString(R.string.error_title_miniapp_version_mismatch,versionCode ?: ""),
                    spanDescription(context.resources.getString(R.string.error_desc_miniapp_version_mismatch))
                )
            }
        }
    }

    /** return the description with added listener which can open support link */
    private fun spanDescription(des: String): SpannableString{
        val ss = SpannableString(des)
        ss.setSpan(spanListener, getSpanIndex(des).first, getSpanIndex(des).second, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ss
    }

    /** return the start and end index of support link to create span */
    private fun getSpanIndex(src: String): Pair<Int, Int> {
        val substringToFind = context.resources.getString(R.string.help_center_text)
        return Pair(
            src.indexOf(substringToFind),
            src.indexOf(substringToFind) + substringToFind.length
        )
    }

    private val spanListener = object: ClickableSpan(){
        override fun onClick(widget: View) {
            //TODO: Open the support URL
        }

        override fun updateDrawState(ds: TextPaint) {
            // set to false to remove underline.
            ds.isUnderlineText = false
            ds.color = context.getColor(R.color.link_blue)
        }
    }
    private val listener = View.OnClickListener { dissmissDialog() }
}
