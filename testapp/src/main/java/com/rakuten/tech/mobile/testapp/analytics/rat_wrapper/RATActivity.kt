package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.rakuten.tech.mobile.testapp.analytics.DemoAppAnalytics
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

enum class MenuItemDefaults() {
    HOME,
    BACK,
}

open class RATActivity : AppCompatActivity(), IRatActivity {
    private var ratEvent: RATEvent? = null
    private var ratEventMenuItem: RATEvent? = null
    private var menu_item_label = ""
    private var screen_name = ""

    /** Will send Appear event when open a activity. */
    override fun onResume() {
        sendAnalyticsDefault()
        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            supportActionBar?.let {
                menu_item_label = if (it.displayOptions and ActionBar.DISPLAY_HOME_AS_UP !== 0) {
                    MenuItemDefaults.BACK.name
                } else {
                    MenuItemDefaults.HOME.name
                }
            }
        } else {
            menu_item_label = item.title.toString()
        }
        sendAnalyticsForMenuItem()
        return super.onOptionsItemSelected(item)
    }

    /** Will send click event when any of the menu item is being clicked. */
    private fun sendAnalyticsForMenuItem() {
        prepareMenuItemClickEventToSend()
        ratEventMenuItem?.let {
            DemoAppAnalytics.init(AppSettings.instance.projectId).sendAnalytics(it)
        }
    }

    /** Will send Appear event when open a activity. */
    private fun sendAnalyticsDefault() {
        prepareScreenAppearEventToSend()
        ratEvent?.let {
            DemoAppAnalytics.init(AppSettings.instance.projectId).sendAnalytics(it)
        }
    }

    /** Prepare default event if custom RAT event is not set. */
    override fun prepareMenuItemClickEventToSend() {
        if (ratEventMenuItem == null)
            ratEventMenuItem = RATEvent(
                event = EventType.CLICK,
                action = ActionType.OPEN,
                label = menu_item_label,
                screen_name = getScreenName()
            )
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
    override fun setCustomRatEventForMenuItem(ratEvent: RATEvent) {
        this.ratEventMenuItem = ratEvent
    }

    /** Can send custom RAT event if set. */
    override fun setCustomRatEventForDefault(ratEvent: RATEvent) {
        this.ratEvent = ratEvent
    }

    /** Can clear custom RAT event if set. */
    override fun clearCustomRatEvent() {
        this.ratEvent = null
    }

    /** Can clear custom RAT event if set. */
    override fun clearCustomRatEventforMenuItem() {
        this.ratEventMenuItem = null
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
