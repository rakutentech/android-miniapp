package com.rakuten.tech.mobile.testapp.rat_wrapper

import com.rakuten.tech.mobile.miniapp.MiniAppInfo


enum class EventType {
    APPEAR,
    CLICK,
}

enum class Actiontype {
    HOST_LAUNCH,
    OPEN,
    CLOSE
}

class RATEvent {
    private var event: EventType? = null
    private var action: Actiontype? = null
    private var screenName: String? = null
    private var miniAppInfo: MiniAppInfo? = null

    constructor(event: EventType, action: Actiontype) {
        this.event = event
        this.action = action
    }

    constructor(event: EventType, action: Actiontype, screenName: String) {
        this.event = event
        this.action = action
        this.screenName = screenName
    }

    constructor(event: EventType, action: Actiontype, miniAppInfo: MiniAppInfo) {
        this.event = event
        this.action = action
        this.miniAppInfo = miniAppInfo
    }
}
