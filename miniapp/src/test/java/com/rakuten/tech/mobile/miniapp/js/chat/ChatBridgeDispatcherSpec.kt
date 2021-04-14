package com.rakuten.tech.mobile.miniapp.js.chat

import com.google.gson.Gson
import org.mockito.kotlin.*
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_VALUE
import com.rakuten.tech.mobile.miniapp.TEST_ERROR_MSG
import com.rakuten.tech.mobile.miniapp.TEST_MA
import com.rakuten.tech.mobile.miniapp.display.WebViewListener
import com.rakuten.tech.mobile.miniapp.js.*
import com.rakuten.tech.mobile.miniapp.js.chat.ChatBridge.Companion.ERR_SEND_MESSAGE
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class ChatBridgeDispatcherSpec {
    private lateinit var miniAppBridge: MiniAppMessageBridge
    private val sendMessageCallbackObj = CallbackObj(
        action = ActionType.SEND_MESSAGE_TO_CONTACT.action,
        param = null,
        id = TEST_CALLBACK_ID
    )
    private val customPermissionCache: MiniAppCustomPermissionCache = mock()
    private val downloadedManifestCache: DownloadedManifestCache = mock()
    private val webViewListener: WebViewListener = mock()
    private val bridgeExecutor = Mockito.spy(MiniAppBridgeExecutor(webViewListener))

    @Before
    fun setup() {
        miniAppBridge = Mockito.spy(createMessageBridge())
        When calling miniAppBridge.createBridgeExecutor(webViewListener) itReturns bridgeExecutor
        miniAppBridge.init(
            activity = TestActivity(),
            webViewListener = webViewListener,
            customPermissionCache = customPermissionCache,
            downloadedManifestCache = downloadedManifestCache,
            miniAppId = TEST_MA.id
        )
    }

    private fun createChatMessageBridgeDispatcher(
        canSendMessage: Boolean
    ): ChatBridgeDispatcher = object : ChatBridgeDispatcher {

        override fun sendMessageToContact(
            message: MessageToContact,
            onSuccess: (contactId: String?) -> Unit,
            onError: (message: String) -> Unit
        ) {
            if (canSendMessage)
                onSuccess.invoke(TEST_CONTACT.id)
            else
                onError.invoke(TEST_ERROR_MSG)
        }
    }

    private val messageToContact = MessageToContact("", "", "", "", "")

    private fun createChatBridge(
        chatBridgeDispatcher: ChatBridgeDispatcher,
        isImplement: Boolean
    ): ChatBridge {
        val chatBridge = ChatBridge()
        chatBridge.setMiniAppComponents(bridgeExecutor, TEST_MA.id)
        if (isImplement)
            chatBridge.setChatBridgeDispatcher(chatBridgeDispatcher)

        return chatBridge
    }

    private fun createMessageBridge(): MiniAppMessageBridge =
        object : MiniAppMessageBridge() {
            override fun getUniqueId() = TEST_CALLBACK_VALUE
        }

    private val sendingMessageJsonStr = Gson().toJson(
        CallbackObj(
            action = ActionType.SEND_MESSAGE_TO_CONTACT.action,
            param = SendContactCallbackObj.MessageParam(messageToContact),
            id = TEST_CALLBACK_ID
        )
    )

    @Test
    fun `postError should be called when hostapp doesn't implement the chat dispatcher`() {
        val chatMessageBridgeDispatcher = Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, false))

        chatBridge.onSendMessageToContact(sendMessageCallbackObj.id, sendingMessageJsonStr)
        verify(bridgeExecutor).postError(sendMessageCallbackObj.id, ErrorBridgeMessage.NO_IMPL)
    }

    @Test
    fun `postError should be called when hostapp can't send message`() {
        val chatMessageBridgeDispatcher = Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, true))
        val errMsg = "$ERR_SEND_MESSAGE $TEST_ERROR_MSG"

        chatBridge.onSendMessageToContact(sendMessageCallbackObj.id, sendingMessageJsonStr)
        verify(bridgeExecutor).postError(sendMessageCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when hostapp can send message`() {
        val chatMessageBridgeDispatcher = Mockito.spy(createChatMessageBridgeDispatcher(true))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, true))

        chatBridge.onSendMessageToContact(sendMessageCallbackObj.id, sendingMessageJsonStr)
        verify(bridgeExecutor).postValue(sendMessageCallbackObj.id, TEST_CONTACT.id)
    }
}
