package com.rakuten.tech.mobile.testapp.rat_wrapper

interface IRatComponent {
    /**
     * Prepare the component's rat event before it is sent using eventTypes, actions,
     * screen name, miniapp info
     */
    fun prepareEventForSend()

    /**
     * Set Custom rat events
     */
    fun setCustomRatEvent(ratEvent: RATEvent)
}
