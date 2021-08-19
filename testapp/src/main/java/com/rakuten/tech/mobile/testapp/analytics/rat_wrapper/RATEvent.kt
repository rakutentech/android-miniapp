package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

import com.rakuten.tech.mobile.miniapp.MiniAppInfo


enum class EventType(val value: String) {
    APPEAR("appear"),
    CLICK("click"),
    PAGE_LOAD("pv")
}

enum class ActionType(val value: String) {
    OPEN("open"),
    CLOSE("close")
}

class RATEvent {
    private var event: EventType?
    private var action: ActionType? = null
    private var label: String? = null
    private var screenName: String? = null
    private var miniAppInfo: MiniAppInfo? = null

    fun getEvent(): EventType {
        return event ?: EventType.CLICK
    }

    fun getAction(): ActionType {
        return action ?: ActionType.OPEN
    }

    fun getLabel(): String {
        return label ?: ""
    }


    fun getMiniAppInfo(): MiniAppInfo? {
        return miniAppInfo
    }

    fun getScreenName():String{
        return screenName ?: ""
    }

    constructor(event: EventType, action: ActionType) {
        this.event = event
        this.action = action
    }

    constructor(event: EventType, action: ActionType, label: String) {
        this.event = event
        this.action = action
        this.label = label
    }

    constructor(event: EventType, action: ActionType, miniAppInfo: MiniAppInfo) {
        this.event = event
        this.action = action
        this.miniAppInfo = miniAppInfo
    }

    constructor(event: EventType, action: ActionType, label: String, screen_name: String){
        this.event = event
        this.action = action
        this.label = label
        this.screenName = screen_name
    }
}
