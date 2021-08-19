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
    context:
    Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), IRatComponent {

    private var ratEvent: RATEvent? = null
    private var btnLabel = ""
    private var screen_name = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_button_view_with_arrow, this, true)

        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomButtonViewWithArrow, 0, 0)
            .let {
                btnLabel = it.getString(R.styleable.CustomButtonViewWithArrow_titleLabel) ?: ""
                val isArrowEnable =
                    it.getBoolean(R.styleable.CustomButtonViewWithArrow_rightArrowEnable, true)
                tv_label.text = btnLabel
                if (isArrowEnable) img_arrow_right.visibility =
                    View.VISIBLE else img_arrow_right.visibility = View.INVISIBLE
                it.recycle()
            }
    }

    override fun performClick(): Boolean {
        val returnClick = super.performClick()
        prepareEventToSend()
        ratEvent?.let {
            DemoAppAnalytics.init(AppSettings.instance.projectId).sendAnalytics(it)
        }
        return returnClick
    }

    override fun prepareEventToSend() {
        if (ratEvent == null)
            ratEvent = RATEvent(
                event = EventType.CLICK,
                action = ActionType.OPEN,
                label = btnLabel
            )
    }

    override fun setCustomRatEvent(ratEvent: RATEvent) {
        this.ratEvent = ratEvent
    }

    override fun clearCustomRatEvent() {
        this.ratEvent = null
    }

    override fun getScreenName(): String {
        return screen_name
    }

    override fun setScreenName(screen_name: String) {
        this.screen_name = screen_name
    }
}
