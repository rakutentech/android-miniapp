package com.rakuten.tech.mobile.testapp.rat_wrapper

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.rakuten.tech.mobile.miniapp.testapp.R
import kotlinx.android.synthetic.main.settings_option_menu.view.*

class SettingMenuOptionView @JvmOverloads constructor(
    context:
    Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), IRatComponent {

    private lateinit var ratEvent: RATEvent

    init {
        LayoutInflater.from(context).inflate(R.layout.settings_option_menu, this, true)

        context.theme.obtainStyledAttributes(attrs, R.styleable.SettingMenuOptionView, 0, 0).let {
            val title = it.getString(R.styleable.SettingMenuOptionView_titleLabel)
            tv_label.text = title
            it.recycle()
        }
    }

    override fun performClick(): Boolean {
        val returnClick = super.performClick()
        prepareEventForSend()
        //TODO: Send The event
        return returnClick
    }

    override fun prepareEventForSend() {
        ratEvent = RATEvent(
            event = EventType.CLICK,
            action = Actiontype.OPEN
        )
    }

    override fun setCustomRatEvent(ratEvent: RATEvent) {
        this.ratEvent = ratEvent
    }
}
