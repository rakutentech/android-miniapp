package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.annotation.NonNull
import androidx.appcompat.widget.SwitchCompat
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.analytics.DemoAppAnalytics
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

/**
 * This is a custom Switch to handle rat analytics.
 */
class RATSwitch : SwitchCompat {
    private var passedlistener: OnCheckedChangeListener? = null
    private var siteSection = ""
    private var pageName = ""
    private var action: ActionType = ActionType.DEFAULT

    constructor(@NonNull context: Context) : super(context)

    constructor(@NonNull context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialization(context = context, attrs = attrs)
    }

    constructor(@NonNull context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialization(context = context, attrs = attrs)
    }

    private fun initialization(context: Context, attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.RatCustomAttributes, 0, 0)
            .let {
                siteSection = it.getString(R.styleable.RatCustomAttributes_siteSection) ?: ""
                pageName = it.getString(R.styleable.RatCustomAttributes_pageName) ?: ""
                val index = it.getInt(R.styleable.RatCustomAttributes_actionType, 0)
                if (index > -1) action = ActionType.values()[index]
                it.recycle()
            }
        DemoAppAnalytics.init(AppSettings.instance.projectIdForAnalytics).sendAnalytics(
            RATEvent(
                event = EventType.APPEAR,
                action = action,
                pageName = pageName,
                siteSection = siteSection,
                componentName = this.text.toString(),
                elementType = "Switch"
            )
        )
    }

    /** This listener will send the RAT event first then send the values to child listener. */
    private var listener: OnCheckedChangeListener =
        OnCheckedChangeListener { buttonView, isChecked ->
             DemoAppAnalytics.init(AppSettings.instance.projectIdForAnalytics).sendAnalytics(
                 RATEvent(
                     event = EventType.CLICK,
                     action = action,
                     pageName = pageName,
                     siteSection = siteSection,
                     componentName = this.text.toString(),
                     elementType = "Switch"
                 )
             )
            passedlistener?.onCheckedChanged(buttonView, isChecked)
        }


    init {
        this.setOnCheckedChangeListener(listener)
    }

    override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        if (listener == this.listener) {
            super.setOnCheckedChangeListener(this.listener)
        } else {
            passedlistener = listener
        }
    }
}
