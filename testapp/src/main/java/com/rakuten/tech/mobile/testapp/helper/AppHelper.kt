package com.rakuten.tech.mobile.testapp.helper

import android.app.Activity
import android.app.AlertDialog
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import com.rakuten.tech.mobile.miniapp.testapp.R

fun isInputEmpty(input: AppCompatEditText): Boolean {
    return input.text.toString().isEmpty() || input.text.toString().isBlank()
}

fun showDialogToCopy(activity: Activity, content: String) {
    // prepare view with copy option
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
    alertDialog.setTitle("Alert")
    alertDialog.setView(container)
    alertDialog.setNegativeButton("Close") { dialog, _ ->
        dialog.dismiss()
    }
    alertDialog.create().show()
}
