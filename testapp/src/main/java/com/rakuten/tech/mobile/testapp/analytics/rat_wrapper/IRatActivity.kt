package com.rakuten.tech.mobile.testapp.analytics.rat_wrapper

interface IRatActivity {
    /**
     * Prepare the component's rat event before it is sent using eventTypes, actions,
     * screen name, miniapp info
     */
    fun prepareMenuItemClickEventToSend()

    /**
     * Prepare the component's rat event before it is sent using eventTypes, actions,
     * screen name, miniapp info
     */
    fun prepareScreenAppearEventToSend()

    /**
     * Set Custom rat events for menu items
     */
    fun setCustomRatEventForMenuItem(ratEvent: RATEvent)

    /**
     * Set Custom rat events for whole activity
     */
    fun setCustomRatEventForDefault(ratEvent: RATEvent)

    /**
     * Clear Custom rat events
     */
    fun clearCustomRatEvent()

    /**
     * Clear Custom rat events for menu item click
     */
    fun clearCustomRatEventforMenuItem()

    /**
     * Get the screen name of the activity
     * @return the name of the screen to be reported to RAT for a screen view event
     * @throws UnsetScreenException if screen view is not set by overriding this method
     */
    @Throws(UnsetScreenException::class)
    fun getScreenName(): String

    /**
     * Set Custom screen name
     */
    fun setScreenName(screenName: String)

}
