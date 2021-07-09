package com.rakuten.tech.mobile.miniapp.analytics

import android.util.Log

internal class RealRatTracker: RATTrackerDispatcher() {
    override fun sendEvent() {
        //TODO: Send Events to RAT
        Log.e("caling","from sdk internally")
    }
}
