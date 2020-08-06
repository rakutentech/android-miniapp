package com.rakuten.tech.mobile.miniapp.permission.widget

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton

class PermissionToggle(context: Context) : LinearLayout(context) {

    lateinit var toggle: ToggleButton
    lateinit var textView: TextView

    var isChecked: Boolean
        get() = toggle.isChecked
        set(value) {
            toggle.isChecked = value
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
        toggle.textOff = "DENY"
        toggle.textOn = "ALLOW"
        addView(toggle)
        toggle.isChecked = true
    }
}
