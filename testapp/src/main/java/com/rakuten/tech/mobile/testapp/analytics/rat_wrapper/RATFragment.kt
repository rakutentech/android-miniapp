package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import androidx.fragment.app.Fragment
import com.rakuten.tech.mobile.testapp.analytics.DemoAppAnalytics
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

abstract class RATFragment: Fragment(), RatComponent {
    override fun onResume() {
        DemoAppAnalytics.init(AppSettings.instance.projectId).sendAnalytics(
            RATEvent(
                event = EventType.PAGE_LOAD,
                pageName = pageName,
                siteSection = siteSection
            )
        )
        super.onResume()
    }
}


