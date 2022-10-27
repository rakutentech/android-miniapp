package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.rakuten.tech.mobile.testapp.analytics.DemoAppAnalytics
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

enum class MenuItemDefaults {
    HOME,
    BACK,
}

/**
 * This is a custom Activity to handle rat analytics.
 */
abstract class RATActivity : AppCompatActivity(), RatComponent {
    private var menuItemLabel = ""

    override fun onResume() {
        DemoAppAnalytics.init(AppSettings.instance.projectIdForAnalytics).sendAnalytics(
            RATEvent(
                event = EventType.PAGE_LOAD,
                pageName = pageName,
                siteSection = siteSection
            )
        )
        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            supportActionBar?.let {
                menuItemLabel = if (it.displayOptions and ActionBar.DISPLAY_HOME_AS_UP !== 0) {
                    MenuItemDefaults.BACK.name
                } else {
                    MenuItemDefaults.HOME.name
                }
            }
        } else {
            menuItemLabel = item.title.toString()
        }
        DemoAppAnalytics.init(AppSettings.instance.projectIdForAnalytics).sendAnalytics(
            RATEvent(
                event = EventType.CLICK,
                action = ActionType.OPEN,
                pageName = pageName,
                siteSection = siteSection,
                componentName = menuItemLabel,
                elementType = "ActionBar"
            )
        )
        return super.onOptionsItemSelected(item)
    }
}
