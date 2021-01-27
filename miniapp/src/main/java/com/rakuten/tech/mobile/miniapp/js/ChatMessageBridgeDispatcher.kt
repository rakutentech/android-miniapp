package com.rakuten.tech.mobile.miniapp.js

import com.google.gson.Gson

/**
 * Functionality related to Chat/Messaging. These methods should be implemented by the Host App.
 */
@Suppress("TooGenericExceptionCaught")
abstract class ChatMessageBridgeDispatcher {

    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var miniAppId: String

    /**
     * Triggered when Mini App wants to send a message to a contact.
     * Should open a contact chooser which allows the user to choose a single contact,
     * and should then send the message to the chosen contact.
     * Should invoke [onSuccess] with the ID of the contact which was sent the message.
     * If the user cancelled sending the message, should invoked [onSuccess] with [null].
     * Should invoke [onError] when there was an error.
     */
    abstract fun sendMessageToContact(
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    )

    internal fun init(
        bridgeExecutor: MiniAppBridgeExecutor,
        miniAppId: String
    ) {
        this.bridgeExecutor = bridgeExecutor
        this.miniAppId = miniAppId
    }

    internal fun onSendMessageToContact(callbackId: String, jsonStr: String) = try {
        {
            val callbackObj = Gson().fromJson(jsonStr, SendContactCallbackObj::class.java)
            val messageToContact = callbackObj.param.messageToContact
            val successCallback = { contactId: String? ->
                bridgeExecutor.postValue(callbackId, contactId.toString())
            }
            val errorCallback = { errMessage: String ->
                bridgeExecutor.postError(callbackId, "$ERR_SEND_MESSAGE $errMessage")
            }

            sendMessageToContact(messageToContact, successCallback, errorCallback)
        }
    } catch (e: Exception) {
        bridgeExecutor.postError(callbackId, "$ERR_SEND_MESSAGE ${e.message}")
    }

    internal companion object {
        const val ERR_SEND_MESSAGE = "Cannot send message:"
    }
}
