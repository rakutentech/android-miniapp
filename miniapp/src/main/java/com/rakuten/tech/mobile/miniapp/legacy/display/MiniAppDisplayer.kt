package com.rakuten.tech.mobile.miniapp.legacy.display

import android.app.Activity

/**
 * Display Mini App interface.
 */
interface MiniAppDisplayer {

    /**
     * Display mini app based on mini app ID and version ID to the host activity.
     */
    fun displayMiniApp(miniAppId: String, versionId: String, hostActivity: Activity)
}
