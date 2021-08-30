package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper



enum class EventType(val value: String) {
    APPEAR("appear"),
    CLICK("click"),
    PAGE_LOAD("pv")
}

enum class ActionType(val value: String) {
    OPEN("open"),
    CLOSE("close"),
    CHANGE_STATUS("changeStatus"),
    DEFAULT("default")
}

class RATEvent {
    private var event: EventType?
    private var action: ActionType? = null
    private var siteSection: String? = null
    private var pageName: String? = null
    private var targetElement: String? = null
    private var componentName: String? = null
    private var elementType: String? = null

    fun getEvent(): EventType {
        return event ?: EventType.CLICK
    }

    fun getAction(): ActionType {
        return action ?: ActionType.OPEN
    }

    fun getPgn():String{
        return pageName ?: ""
    }

    fun getTargetElement(): String {
        return targetElement ?: ""
    }

    fun getSiteSection(): String {
        return siteSection ?: ""
    }

    constructor(event: EventType, pageName: String, siteSection: String){
        this.event = event
        this.pageName = pageName
        this.siteSection = siteSection
    }

    constructor(event: EventType,action: ActionType, pageName: String, siteSection: String, componentName: String, elementType: String){
        this.event = event
        this.action= action
        this.pageName = pageName
        this.siteSection = siteSection
        this.componentName = componentName
        this.elementType = elementType
        //{component_name}-{element_type}.{action}
        this.targetElement = "$componentName-$elementType.${action.value}"
    }

    constructor(event: EventType, pageName: String, siteSection: String, componentName: String, elementType: String){
        this.event = event
        this.pageName = pageName
        this.siteSection = siteSection
        this.componentName = componentName
        this.elementType = elementType
        //{component_name}-{element_type}
        this.targetElement = "$componentName-$elementType"
    }

}
