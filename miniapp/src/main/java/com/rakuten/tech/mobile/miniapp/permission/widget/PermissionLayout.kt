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
        setPadding(P_LEFT, P_DEFAULT, P_DEFAULT, P_DEFAULT)
        addChildComponents()
    }

    private fun addChildComponents() {
        textView = TextView(context)
        textView.setPadding(0, 0, P_DEFAULT, 0)
        textView.textSize = TEXT_SIZE
        addView(textView)

        toggle = ToggleButton(context)
        toggle.textOff = "Deny"
        toggle.textOn = "Allow"
        addView(toggle)
        isChecked = true
    }

    private companion object {
        const val P_DEFAULT = 24
        const val P_LEFT = 72
        const val TEXT_SIZE = 18F
    }
}
