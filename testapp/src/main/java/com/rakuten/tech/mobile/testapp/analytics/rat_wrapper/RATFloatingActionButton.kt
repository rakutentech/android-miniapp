package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.analytics.DemoAppAnalytics
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

/**
 * This is a FloatingActionButton to handle rat analytics.
 */
class RATFloatingActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FloatingActionButton(context, attrs, defStyleAttr) {

    private var action: ActionType = ActionType.DEFAULT
    private var pageName = ""
    private var siteSection = ""

    override fun performClick(): Boolean {
        val returnClick = super.performClick()
        DemoAppAnalytics.init(AppSettings.instance.projectIdForAnalytics).sendAnalytics(
            RATEvent(
                event = EventType.CLICK,
                action = action,
                pageName = pageName,
                siteSection = siteSection,
                componentName = this.id.toString(),
                elementType = "FabButton"
            )
        )
        return returnClick
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.RatCustomAttributes, 0, 0)
            .let {
                val index = it.getInt(R.styleable.RatCustomAttributes_actionType, 0)
                if (index > -1) action = ActionType.values()[index]
                siteSection = it.getString(R.styleable.RatCustomAttributes_siteSection) ?: ""
                pageName = it.getString(R.styleable.RatCustomAttributes_pageName) ?: ""
                it.recycle()
            }

        DemoAppAnalytics.init(AppSettings.instance.projectIdForAnalytics).sendAnalytics(
            RATEvent(
                event = EventType.APPEAR,
                action = action,
                pageName = pageName,
                siteSection = siteSection,
                componentName = this.id.toString(),
                elementType = "FabButton"
            )
        )
    }
}
