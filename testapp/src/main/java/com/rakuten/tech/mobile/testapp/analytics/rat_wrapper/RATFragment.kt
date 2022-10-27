package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import androidx.fragment.app.Fragment
import com.rakuten.tech.mobile.testapp.analytics.DemoAppAnalytics
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

/**
 * This is a custom Fragment to handle rat analytics.
 */
abstract class RATFragment : Fragment(), RatComponent {
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
}
