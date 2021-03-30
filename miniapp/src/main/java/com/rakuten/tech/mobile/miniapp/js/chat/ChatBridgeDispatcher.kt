package com.rakuten.tech.mobile.miniapp.js.chat

import com.rakuten.tech.mobile.miniapp.js.MessageToContact

/**
 * Functionality related to Chatting with the contacts.
 */
interface ChatBridgeDispatcher {

    /**
     * Triggered when Mini App wants to send a message to a single contact.
     * Should invoke [onSuccess] with the contact ID to send the message.
     * If the user wants to cancel sending the message, should invoked [onSuccess] with null.
     * Should invoke [onError] when there was an error.
     */
    fun sendMessageToContact(
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    )
}
