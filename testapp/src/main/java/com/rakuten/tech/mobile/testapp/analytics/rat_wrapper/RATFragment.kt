package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import androidx.fragment.app.Fragment
import com.rakuten.tech.mobile.testapp.analytics.DemoAppAnalytics
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

open class RATFragment: Fragment(), IRATFragment {
    private var ratEvent: RATEvent? = null
    private var ratEventMenuItem: RATEvent? = null
    private var screen_name = ""

    /** Will send Appear event when open a fragment. */
    override fun onResume() {
        sendAnalyticsDefault()
        super.onResume()
    }

    /** Will send Appear event when open a fragment. */
    private fun sendAnalyticsDefault() {
        prepareScreenAppearEventToSend()
        ratEvent?.let {
            DemoAppAnalytics.init(AppSettings.instance.projectId).sendAnalytics(it)
        }
    }

    /** Prepare default event if custom RAT event is not set. */
    override fun prepareScreenAppearEventToSend() {
        if (ratEvent == null)
            ratEvent = RATEvent(
                event = EventType.APPEAR,
                action = ActionType.OPEN,
                label = "",
                screen_name = getScreenName()
            )
    }

    /** Can send custom RAT event if set. */
    override fun setCustomRatEventForDefault(ratEvent: RATEvent) {
        this.ratEvent = ratEvent
    }

    /** Can clear custom RAT event if set. */
    override fun clearCustomRatEvent() {
        this.ratEvent = null
    }

    /**
     * Get the screen name of the activity
     * @return the name of the screen to be reported to RAT for a screen view event
     * @throws UnsetScreenException if screen view is not set by overriding this method
     */
    @Throws(UnsetScreenException::class)
    override fun getScreenName(): String {
        return if (screen_name == "") {
            this.javaClass.simpleName
        } else {
            screen_name
        }
    }

    /** User can set the screen name by this function, Default is activity name. */
    override fun setScreenName(screen_name: String) {
        this.screen_name = screen_name
    }
}


