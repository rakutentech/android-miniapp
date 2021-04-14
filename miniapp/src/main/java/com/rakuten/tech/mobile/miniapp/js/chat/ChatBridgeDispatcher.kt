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

    /**
     * Triggered when Mini App wants to send a message to a specific contact.
     * Should send a message to the specified contactId without any prompt to the User.
     * Should invoke [onSuccess] after message was successfully sent.
     * Should invoke [onError] when there was an error.
     */
    fun sendMessageToContactId(
        contactId: String,
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    )

    /**
     * Triggered when Mini App wants to send a message to multiple contacts.
     * Should open a contact chooser which allows the user to choose multiple contacts,
     * and should then send the message to all chosen contacts.
     * Should invoke [onSuccess] with a list of IDs of the contacts which were successfully sent the message.
     * If the user cancelled sending the message, should invoked [onSuccess] with null.
     * Should invoke [onError] when there was an error.
     */
    fun sendMessageToMultipleContacts(
        message: MessageToContact,
        onSuccess: (contactIds: List<String>?) -> Unit,
        onError: (message: String) -> Unit
    )
}
