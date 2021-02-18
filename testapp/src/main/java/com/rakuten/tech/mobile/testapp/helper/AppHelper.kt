package com.rakuten.tech.mobile.testapp.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rakuten.tech.mobile.miniapp.testapp.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun isInputEmpty(input: AppCompatEditText): Boolean {
    return input.text.toString().isEmpty() || input.text.toString().isBlank()
}

fun parseDateToString(format: String, date: Date): String {
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    return dateFormat.format(date)
}

fun parseStringToDate(format: String, str: String): Date {
    val format = SimpleDateFormat(format, Locale.getDefault())
    try {
        val date = format.parse(str)
        return date
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return Date()
}

fun showAlertDialog(activity: Activity, title: String = "Alert", content: String) {
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
    alertDialog.setNegativeButton("Close") { dialog, _ ->
        dialog.dismiss()
    }
    alertDialog.create().show()
}

fun setIcon(context: Context, uri: Uri, view: ImageView) {
    Glide.with(context)
        .load(uri).apply(RequestOptions().circleCrop())
        .placeholder(R.drawable.ic_default)
        .into(view)
}
