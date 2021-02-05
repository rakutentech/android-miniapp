package com.rakuten.tech.mobile.miniapp.js.chat

import com.rakuten.tech.mobile.miniapp.js.MessageToContact

/**
 * Functionality related to Chat/Messaging. These methods should be implemented by the Host App.
 */
interface ChatBridgeDispatcher {

    /**
     * Triggered when Mini App wants to send a message to a contact.
     * Should open a contact chooser which allows the user to choose a single contact,
     * and should then send the message to the chosen contact.
     * Should invoke [onSuccess] with the ID of the contact which was sent the message.
     * If the user cancelled sending the message, should invoked [onSuccess] with [null].
     * Should invoke [onError] when there was an error.
     */
    fun sendMessageToContact(
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    )
}
