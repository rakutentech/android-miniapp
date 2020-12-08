package com.rakuten.tech.mobile.miniapp.display

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.widget.EditText
import android.widget.FrameLayout
import com.rakuten.tech.mobile.miniapp.js.DialogType

@Suppress("LongParameterList", "LongMethod")
internal fun onShowDialog(
    context: Context,
    message: String?,
    defaultValue: String? = "",
    result: JsResult?,
    dialogType: DialogType,
    miniAppTitle: String
): Boolean {
    val dialogBuilder = AlertDialog.Builder(context)
        .setTitle(miniAppTitle)
        .setMessage(message)

    if (dialogType != DialogType.ALERT)
        onCancelBuild(dialogBuilder, result)
    if (dialogType == DialogType.PROMPT)
        onPromptBuild(context, defaultValue, dialogBuilder, result)
    else
        onOkBuild(dialogBuilder, result)

    dialogBuilder.create()
    dialogBuilder.show()

    return true
}

private fun onOkBuild(
    dialogBuilder: AlertDialog.Builder,
    result: JsResult?
) {
    dialogBuilder.setPositiveButton(android.R.string.ok) { dialog, which ->
        result?.confirm()
        dialog.dismiss()
    }
}

private fun onPromptBuild(
    context: Context,
    defaultValue: String?,
    dialogBuilder: AlertDialog.Builder,
    result: JsResult?
) {
    val inputText = EditText(context)
    inputText.setText(defaultValue ?: "")
    dialogBuilder.setEditText(inputText)

    dialogBuilder.setPositiveButton(android.R.string.ok) { dialog, which ->
        if (result != null && result is JsPromptResult)
            result.confirm(inputText.text.toString())
        dialog.dismiss()
    }
}

private fun onCancelBuild(dialogBuilder: AlertDialog.Builder, result: JsResult?) {
    dialogBuilder.setNegativeButton(android.R.string.cancel) { dialog, which ->
        result?.cancel()
        dialog.dismiss()
    }
}

private const val DEFAULT_MARGIN = 16

internal val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

internal fun AlertDialog.Builder.setEditText(editText: EditText): AlertDialog.Builder {
    val container = FrameLayout(context)
    container.addView(editText)
    val containerParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    )
    containerParams.marginStart = DEFAULT_MARGIN.px
    containerParams.marginEnd = DEFAULT_MARGIN.px
    container.layoutParams = containerParams

    val view = FrameLayout(context)
    view.addView(container)
    setView(view)

    return this
}
