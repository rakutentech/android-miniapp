package com.rakuten.tech.mobile.miniapp.analytics


abstract class RATTrackerDispatcher internal constructor() {
    abstract fun sendEvent()

    companion object {
        internal lateinit var instance: RATTrackerDispatcher
        fun instance(): RATTrackerDispatcher {
            instance = RealRatTracker()
            return instance
        }
    }
}
