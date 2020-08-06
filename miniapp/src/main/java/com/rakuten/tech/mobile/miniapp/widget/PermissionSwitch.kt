package com.rakuten.tech.mobile.miniapp.widget

import android.content.Context
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView

class PermissionSwitch(context: Context) : LinearLayout(context) {

    lateinit var switch: Switch
    lateinit var textView: TextView

    var isChecked: Boolean
        get() = switch.isChecked
        set(value) {
            switch.isChecked = value
        }

    init {
        orientation = HORIZONTAL
        setPadding(72, 24, 24, 24)
        addChildComponents()
    }

    private fun addChildComponents() {
        switch = Switch(context)
        textView = TextView(context)
        addView(switch)
        addView(textView)
    }
}
