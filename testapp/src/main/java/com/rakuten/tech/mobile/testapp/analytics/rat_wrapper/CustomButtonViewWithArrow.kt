package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.analytics.DemoAppAnalytics
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.android.synthetic.main.custom_button_view_with_arrow.view.*

/**
 * This is custom View with a label and arrow.
 * It can also handle rat analytics.
 * label can be set by app:titleLabel.
 * right arrow can be hide and show by app:rightArrowEnable = true/false.
 */
class CustomButtonViewWithArrow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var btnLabel = ""
    private var btnDetails = ""
    private var siteSection = ""
    private var pageName = ""
    private var action: ActionType = ActionType.DEFAULT

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_button_view_with_arrow, this, true)

        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomButtonViewWithArrow, 0, 0)
            .let {
                siteSection = it.getString(R.styleable.RatCustomAttributes_siteSection) ?: ""
                pageName = it.getString(R.styleable.RatCustomAttributes_pageName) ?: ""
                val index = it.getInt(R.styleable.RatCustomAttributes_actionType, 0)
                if (index > -1) action = ActionType.values()[index]
                btnLabel = it.getString(R.styleable.CustomButtonViewWithArrow_titleLabel) ?: ""
                btnDetails = it.getString(R.styleable.CustomButtonViewWithArrow_titleDetails) ?: ""
                if(btnDetails == "") tv_details.visibility = View.GONE else tv_details.visibility = View.VISIBLE
                val isArrowEnable =
                    it.getBoolean(R.styleable.CustomButtonViewWithArrow_rightArrowEnable, true)
                tv_label.text = btnLabel
                tv_details.text = btnDetails
                if (isArrowEnable) img_arrow_right.visibility =
                    View.VISIBLE else img_arrow_right.visibility = View.INVISIBLE

                DemoAppAnalytics.init(AppSettings.instance.projectIdForAnalytics).sendAnalytics(
                    RATEvent(
                        event = EventType.APPEAR,
                        action = action,
                        pageName = pageName,
                        siteSection = siteSection,
                        componentName = btnLabel,
                        elementType = "ButtonWithTextArrow"
                    )
                )
                it.recycle()
            }
    }

    override fun performClick(): Boolean {
        val returnClick = super.performClick()
        DemoAppAnalytics.init(AppSettings.instance.projectIdForAnalytics).sendAnalytics(
            RATEvent(
                event = EventType.CLICK,
                action = action,
                pageName = pageName,
                siteSection = siteSection,
                componentName = btnLabel,
                elementType = "ButtonWithTextArrow"
            )
        )
        return returnClick
    }
}
