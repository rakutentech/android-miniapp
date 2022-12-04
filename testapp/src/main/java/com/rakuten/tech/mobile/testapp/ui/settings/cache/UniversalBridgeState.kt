package com.rakuten.tech.mobile.testapp.ui.settings.cache

class UniversalBridgeState(
    private var shouldSendMessage: Boolean = false,
    private var message: String = ""
){
    fun update(shouldSendMessage: Boolean, message: String){
        this.shouldSendMessage = shouldSendMessage
        this.message = message
    }

    fun handleOnMiniAppLoaded(onMessageReady: (String) -> Unit){
        if(shouldSendMessage){
            onMessageReady(message)
        }
    }
}
