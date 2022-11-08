package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.analytics.DemoAppAnalytics
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

/**
 * This is a custom Button to handle rat analytics.
 */
class RATButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var siteSection = ""
    private var pageName = ""
    private var action: ActionType = ActionType.DEFAULT

    override fun performClick(): Boolean {
        val returnClick = super.performClick()
        DemoAppAnalytics.init(AppSettings.instance.projectIdForAnalytics).sendAnalytics(
            RATEvent(
                event = EventType.CLICK,
                action = action,
                pageName = pageName,
                siteSection = siteSection,
                componentName = this.text.toString(),
                elementType = "Button"
            )
        )
        return returnClick
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.RatCustomAttributes, 0, 0)
            .let {
                pageName = it.getString(R.styleable.RatCustomAttributes_pageName) ?: ""
                siteSection = it.getString(R.styleable.RatCustomAttributes_siteSection) ?: ""
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
                elementType = "Button"
            )
        )
    }
}
