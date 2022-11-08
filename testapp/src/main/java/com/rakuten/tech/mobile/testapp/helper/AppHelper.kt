package com.rakuten.tech.mobile.testapp.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.NativeEventType
import com.rakuten.tech.mobile.miniapp.testapp.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

private const val UUID_LENGTH = 36
fun String.isInvalidUuid(): Boolean = try {
    UUID.fromString(this)
    this.length != UUID_LENGTH
} catch (e: IllegalArgumentException) {
    true
}

fun isInputEmpty(input: AppCompatEditText): Boolean {
    return input.text.toString().isEmpty() || input.text.toString().isBlank()
}

fun parseDateToString(format: String, date: Date): String {
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    return dateFormat.format(date)
}

fun parseStringToDate(givenFormat: String, str: String): Date {
    val format = SimpleDateFormat(givenFormat, Locale.getDefault())
    try {
        return format.parse(str)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return Date()
}

fun showAlertDialog(
    activity: Activity,
    title: String = "Alert",
    content: String,
    negativeButton: String = "Close"
) {
    // prepare an EditText where the content can be copied by long press
    val editText = EditText(activity)
    editText.setText(content)
    editText.setTextIsSelectable(true)
    editText.inputType = InputType.TYPE_NULL
    editText.isSingleLine = false
    editText.background = null
    val container = FrameLayout(activity)
    val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    params.topMargin = activity.resources.getDimensionPixelSize(R.dimen.medium_16)
    params.bottomMargin = activity.resources.getDimensionPixelSize(R.dimen.medium_16)
    params.leftMargin = activity.resources.getDimensionPixelSize(R.dimen.medium_16)
    params.rightMargin = activity.resources.getDimensionPixelSize(R.dimen.medium_16)
    editText.layoutParams = params
    container.addView(editText)

    // show content using alert dialog
    val alertDialog = AlertDialog.Builder(activity)
    alertDialog.setTitle(title)
    alertDialog.setView(container)
    alertDialog.setNegativeButton(negativeButton) { dialog, _ ->
        dialog.dismiss()
    }
    alertDialog.create().show()
}

fun showErrorDialog(
    context: Context,
    description: String
) {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(description)
        .setNegativeButton("Close") { _, _ -> }
    val alert = builder.create()
    alert.show()
}

fun ImageView.load(context: Context, res: String, placeholder: Int = R.drawable.ic_default) =
    Glide.with(context)
        .load(res)
        .placeholder(placeholder)
        .into(this)

fun String.isEmailValid(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun hideSoftKeyboard(view: View) {
    val imm: InputMethodManager? =
        view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
}

fun setResizableSoftInputMode(activity: Activity) {
    activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE + WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
}

/**
 * Return true if this [Context] is available.
 * Availability is defined as the following:
 * + [Context] is not null
 * + [Context] is not destroyed/finishing (tested with [Activity.isDestroyed] && [Activity.isFinishing])
 */
val Context?.isAvailable: Boolean
    get() {
        if (this == null) {
            return false
        } else if (this !is Application) {
            return this is Activity && (!this.isDestroyed && !this.isFinishing)
        }
        return true
    }

fun getAdapterDataObserver(observeUIState: () -> Unit): RecyclerView.AdapterDataObserver =
    object : RecyclerView.AdapterDataObserver() {
        @SuppressLint("SyntheticAccessor")
        override fun onChanged() {
            super.onChanged()
            observeUIState()
        }

        @SuppressLint("SyntheticAccessor")
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            observeUIState()
        }

        @SuppressLint("SyntheticAccessor")
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            observeUIState()
        }
    }


fun Dialog.registerOnShowAndOnDismissEvent(onShow: () -> Unit, onDismiss: () -> Unit) {
    setOnShowListener { onShow() }
    setOnDismissListener { onDismiss() }
}

fun MiniAppMessageBridge.dispatchOnPauseEvent() {
    dispatchNativeEvent(NativeEventType.MINIAPP_ON_PAUSE, "MiniApp Paused")
}

fun MiniAppMessageBridge.dispatchOnResumeEvent() {
    dispatchNativeEvent(
        NativeEventType.MINIAPP_ON_RESUME, "MiniApp Resumed"
    )
}
