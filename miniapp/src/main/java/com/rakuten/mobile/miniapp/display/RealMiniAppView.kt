package com.rakuten.mobile.miniapp.display

import com.rakuten.mobile.miniapp.MiniAppView
import com.rakuten.mobile.miniapp.analytics.Broadcaster

internal class RealMiniAppView(
    val miniAppWebView: MiniAppWebView,
    val analyticsBroadcaster: Broadcaster
) : MiniAppView
