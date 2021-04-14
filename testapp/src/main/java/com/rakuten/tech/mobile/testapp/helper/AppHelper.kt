package com.rakuten.tech.mobile.testapp.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.util.Patterns
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import com.bumptech.glide.Glide
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
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

fun ImageView.load(context: Context, res: String, placeholder: Int = R.drawable.ic_default) = Glide.with(context)
    .load(res)
    .placeholder(placeholder)
    .into(this)

fun defaultContact(id: String) = Contact(
    id = id,
    name = "default_name",
    email = "default@email.com"
)

fun String.isEmailValid(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()
