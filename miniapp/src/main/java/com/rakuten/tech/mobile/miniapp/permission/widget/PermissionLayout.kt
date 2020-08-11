package com.rakuten.tech.mobile.miniapp.permission.widget

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton

/**
 * A layout that combines child views to prepare a row of permission layout.
 * PermissionLayout class includes a ToggleButton to show toggles with 'Allow' and 'Deny' texts,
 * where the ToggleButton is checked as true by default; isChecked variable
 * has been provided to set/get the value of ToggleButton.
 */
internal class PermissionLayout(context: Context) : LinearLayout(context) {

    private lateinit var toggle: ToggleButton
    private lateinit var textView: TextView

    var isChecked: Boolean
        get() = toggle.isChecked
        set(value) {
            toggle.isChecked = value
        }

    var text: CharSequence
        get() = textView.text
        set(value) {
            textView.text = value
        }

    init {
        orientation = HORIZONTAL
        setPadding(72, 24, 24, 24)
        addChildComponents()
    }

    private fun addChildComponents() {
        textView = TextView(context)
        textView.setPadding(0, 0, 24, 0)
        textView.textSize = 18F
        addView(textView)

        toggle = ToggleButton(context)
        toggle.textOff = "Deny"
        toggle.textOn = "Allow"
        addView(toggle)
        isChecked = true
    }
}
