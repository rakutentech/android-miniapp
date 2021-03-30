package com.rakuten.tech.mobile.miniapp.js.chat

import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import com.rakuten.tech.mobile.miniapp.js.SendContactCallbackObj

@Suppress("TooGenericExceptionCaught", "LongMethod")
internal class ChatBridge {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var miniAppId: String
    private var isMiniAppComponentReady = false
    private lateinit var chatBridgeDispatcher: ChatBridgeDispatcher

    fun setMiniAppComponents(
        bridgeExecutor: MiniAppBridgeExecutor,
        miniAppId: String
    ) {
        this.bridgeExecutor = bridgeExecutor
        this.miniAppId = miniAppId
        isMiniAppComponentReady = true
    }

    fun setChatBridgeDispatcher(chatBridgeDispatcher: ChatBridgeDispatcher) {
        this.chatBridgeDispatcher = chatBridgeDispatcher
    }

    private fun <T> whenReady(callbackId: String, callback: () -> T) {
        if (isMiniAppComponentReady) {
            if (this::chatBridgeDispatcher.isInitialized)
                callback.invoke()
            else
                bridgeExecutor.postError(callbackId, "The `ChatBridgeDispatcher` ${ErrorBridgeMessage.NO_IMPL}")
        }
    }

    internal fun onSendMessageToContact(callbackId: String, jsonStr: String) =
        whenReady(callbackId) {
            try {
                val callbackObj = Gson().fromJson(jsonStr, SendContactCallbackObj::class.java)
                val messageToContact = callbackObj.param.messageToContact
                val successCallback = { contactId: String? ->
                    bridgeExecutor.postValue(callbackId, contactId.toString())
                }
                val errorCallback = { errMessage: String ->
                    bridgeExecutor.postError(callbackId, "$ERR_SEND_MESSAGE $errMessage")
                }

                chatBridgeDispatcher.sendMessageToContact(
                    messageToContact,
                    successCallback,
                    errorCallback
                )
            } catch (e: Exception) {
                bridgeExecutor.postError(callbackId, "$ERR_SEND_MESSAGE ${e.message}")
            }
        }

    internal companion object {
        const val ERR_SEND_MESSAGE = "Cannot send message:"
    }
}
