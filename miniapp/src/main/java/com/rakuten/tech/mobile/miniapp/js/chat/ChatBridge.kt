package com.rakuten.tech.mobile.miniapp.js.chat

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.js.MessageToContact
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import com.rakuten.tech.mobile.miniapp.js.SendContactCallbackObj
import com.rakuten.tech.mobile.miniapp.js.SendContactIdCallbackObj
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType

@Suppress("TooGenericExceptionCaught", "LongMethod")
internal class ChatBridge {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var miniAppId: String
    private var isMiniAppComponentReady = false
    private lateinit var chatBridgeDispatcher: ChatBridgeDispatcher
    private lateinit var customPermissionCache: MiniAppCustomPermissionCache

    fun setMiniAppComponents(
        bridgeExecutor: MiniAppBridgeExecutor,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        miniAppId: String
    ) {
        this.bridgeExecutor = bridgeExecutor
        this.customPermissionCache = miniAppCustomPermissionCache
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
                bridgeExecutor.postError(callbackId, ErrorBridgeMessage.NO_IMPL)
        }
    }

    internal fun onSendMessageToContact(callbackId: String, jsonStr: String) =
        whenReady(callbackId) {
            try {
                val successCallback = { contactId: String? ->
                    if (contactId.isNullOrEmpty())
                        bridgeExecutor.postValue(callbackId, "null")
                    else
                        bridgeExecutor.postValue(callbackId, contactId.toString())
                }
                chatBridgeDispatcher.sendMessageToContact(
                    createMessage(jsonStr),
                    successCallback,
                    createErrorCallback(callbackId)
                )
            } catch (e: Exception) {
                bridgeExecutor.postError(callbackId, "$ERR_SEND_MESSAGE ${e.message}")
            }
        }

    internal fun onSendMessageToContactId(callbackId: String, jsonStr: String) = whenReady(callbackId) {
        try {
            if (customPermissionCache.hasPermission(miniAppId, MiniAppCustomPermissionType.SEND_MESSAGE)) {
                val callbackObj = Gson().fromJson(jsonStr, SendContactIdCallbackObj::class.java)
                val specificContactId = callbackObj.param.contactId

                chatBridgeDispatcher.sendMessageToContactId(
                    specificContactId,
                    callbackObj.param.messageToContact,
                    createSuccessSendMsgContactId(specificContactId, callbackId),
                    createErrorCallback(callbackId)
                )
            } else
                bridgeExecutor.postError(
                    callbackId,
                    "$ERR_SEND_MESSAGE $ERR_NO_PERMISSION_CONTACT_ID"
                )
        } catch (e: Exception) {
            createErrorCallback(callbackId).invoke(e.message.orEmpty())
        }
    }

    @Suppress("FunctionMaxLength")
    internal fun onSendMessageToMultipleContacts(callbackId: String, jsonStr: String) =
        whenReady(callbackId) {
            try {
                val successCallback = { contactIds: List<String>? ->
                    if (contactIds.isNullOrEmpty())
                        bridgeExecutor.postValue(callbackId, "null")
                    else
                        bridgeExecutor.postValue(callbackId, Gson().toJson(contactIds).toString())
                }

                chatBridgeDispatcher.sendMessageToMultipleContacts(
                    createMessage(jsonStr),
                    successCallback,
                    createErrorCallback(callbackId)
                )
            } catch (e: Exception) {
                createErrorCallback(callbackId).invoke(e.message.orEmpty())
            }
        }

    @VisibleForTesting
    internal fun createMessage(jsonStr: String): MessageToContact {
        val callbackObj = Gson().fromJson(jsonStr, SendContactCallbackObj::class.java)
        return callbackObj.param.messageToContact
    }

    private fun createSuccessSendMsgContactId(
        specificContactId: String,
        callbackId: String
    ): (String?) -> Unit = { contactId: String? ->
        if (contactId.isNullOrEmpty() || contactId != specificContactId)
            bridgeExecutor.postValue(callbackId, "null")
        else
            bridgeExecutor.postValue(callbackId, specificContactId)
    }

    private fun createErrorCallback(callbackId: String) = { errMessage: String ->
        bridgeExecutor.postError(callbackId, "$ERR_SEND_MESSAGE $errMessage")
    }

    internal companion object {
        const val ERR_SEND_MESSAGE = "Cannot send message:"
        const val ERR_NO_PERMISSION_CONTACT_ID =
            "Permission has not been accepted yet for sending message to contact Id."
    }
}
