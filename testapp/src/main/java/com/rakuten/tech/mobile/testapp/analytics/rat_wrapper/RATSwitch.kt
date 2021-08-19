package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.annotation.NonNull
import androidx.appcompat.widget.SwitchCompat
import com.rakuten.tech.mobile.testapp.analytics.DemoAppAnalytics
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class RATSwitch : SwitchCompat, IRatComponent {
    private var ratEvent: RATEvent? = null
    private var screen_name = ""
    private var passedlistener: OnCheckedChangeListener? = null

    constructor(@NonNull context: Context) : super(context)

    constructor(@NonNull context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(@NonNull context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /** This listener will send the RAT event first then send the values to child listener. */
    private var listener: OnCheckedChangeListener =
        OnCheckedChangeListener { buttonView, isChecked ->
            prepareEventToSend()
            ratEvent?.let {
                DemoAppAnalytics.init(AppSettings.instance.projectId).sendAnalytics(it)
            }
            passedlistener?.onCheckedChanged(buttonView, isChecked)
        }

    init {
        this.setOnCheckedChangeListener(listener)
    }

    override fun prepareEventToSend() {
        if (ratEvent == null)
            ratEvent = RATEvent(
                event = EventType.CLICK,
                action = ActionType.OPEN,
                label = this.text.toString()
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

    override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        if (listener == this.listener) {
            super.setOnCheckedChangeListener(this.listener)
        } else {
            passedlistener = listener
        }
    }
}

