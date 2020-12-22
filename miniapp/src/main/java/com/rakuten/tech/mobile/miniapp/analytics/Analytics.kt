package com.rakuten.tech.mobile.miniapp.analytics

/**
 * Check whether hostapp provides Analytics dependency.
 * The class name can be replaced with analytics package for real tracking.
 */
@Suppress("EmptyCatchBlock", "SwallowedException")
private inline fun <T> whenHasAnalytics(callback: () -> T) {
    try {
        // ToDo replace with analytics package.
        Class.forName("com.rakuten.tech.mobile.miniapp.analytics.Event")
        callback.invoke()
    } catch (e: ClassNotFoundException) {}
}

/** Send analytics when dependency is provided. */
internal fun sendAnalytics(name: String, params: Map<String, Any>) = whenHasAnalytics {
    // ToDo replace the dummy Event.
    Event("rat.$name", params).track()
}
