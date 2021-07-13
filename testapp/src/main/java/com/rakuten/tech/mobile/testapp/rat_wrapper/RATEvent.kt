package com.rakuten.tech.mobile.testapp.rat_wrapper

import com.rakuten.tech.mobile.miniapp.MiniAppInfo

enum class EventType {
    APPEAR,
    CLICK,
}

enum class Actiontype {
    OPEN,
    CLOSE
}

class RATEvent {
    private var event: EventType?
    private var action: Actiontype? = null
    private var label: String? = null
    private var miniAppInfo: MiniAppInfo? = null

    constructor(event: EventType, action: Actiontype) {
        this.event = event
        this.action = action
    }

    constructor(event: EventType, action: Actiontype, label: String) {
        this.event = event
        this.action = action
        this.label = label
    }

    constructor(event: EventType, action: Actiontype, miniAppInfo: MiniAppInfo) {
        this.event = event
        this.action = action
        this.miniAppInfo = miniAppInfo
    }
}
