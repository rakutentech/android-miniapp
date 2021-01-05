package com.rakuten.tech.mobile.analytics

/** Event tracking dummy class. Please notice the proguard class path. */
class Event(name: String, parameters: Map<String, Any>) {
    fun track() {}
}

class RatTracker {
    companion object {
        fun event(name: String, parameters: Map<String, Any>) = Event(name, parameters)
    }
}
