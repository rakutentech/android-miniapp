package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.NonNull
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rakuten.tech.mobile.testapp.analytics.DemoAppAnalytics
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class RATFloatingActionButton: FloatingActionButton , IRatComponent {

    private var ratEvent: RATEvent? = null
    private var screen_name = ""

    constructor(@NonNull context: Context) : super(context)

    constructor(@NonNull context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(@NonNull context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setCustomRatEvent(ratEvent: RATEvent) {
        this.ratEvent = ratEvent
    }

    override fun getScreenName(): String {
        return screen_name
    }

    override fun prepareEventToSend() {
        if (ratEvent == null)
            ratEvent = RATEvent(
                event = EventType.CLICK,
                action = ActionType.OPEN,
                label = "Fab"
            )
    }

    override fun clearCustomRatEvent() {
        this.ratEvent = null
    }

    override fun setScreenName(screen_name: String) {
        this.screen_name = screen_name
    }

    override fun performClick(): Boolean {
        val returnClick = super.performClick()
        prepareEventToSend()
        ratEvent?.let {
            DemoAppAnalytics.init(AppSettings.instance.projectId).sendAnalytics(it)
        }
        return returnClick
    }
}

