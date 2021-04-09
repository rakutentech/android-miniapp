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
    private val multipleCallbackObj = CallbackObj(
        action = ActionType.SEND_MESSAGE_TO_MULTIPLE_CONTACTS.action,
        param = null,
        id = TEST_CALLBACK_ID
    )
    private val specificCallbackObj = CallbackObj(
        action = ActionType.SEND_MESSAGE_TO_CONTACT_ID.action,
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

        override fun sendMessageToContactId(
            contactId: String,
            message: MessageToContact,
            onSuccess: (contactId: String?) -> Unit,
            onError: (message: String) -> Unit
        ) {
            if (canSendMessage)
                onSuccess.invoke(TEST_CONTACT.id)
            else
                onError.invoke(TEST_ERROR_MSG)
        }

        override fun sendMessageToMultipleContacts(
            message: MessageToContact,
            onSuccess: (contactIds: List<String>) -> Unit,
            onError: (message: String) -> Unit
        ) {
            if (canSendMessage)
                onSuccess.invoke(listOf(TEST_CONTACT.id))
            else
                onError.invoke(TEST_ERROR_MSG)
        }
    }

    private val messageToContact = MessageToContact("", "", "", "")

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

    private val multipleMessageJsonStr = Gson().toJson(
        CallbackObj(
            action = ActionType.SEND_MESSAGE_TO_MULTIPLE_CONTACTS.action,
            param = SendContactCallbackObj.MessageParam(messageToContact),
            id = TEST_CALLBACK_ID
        )
    )

    private val specificMessageJsonStr = Gson().toJson(
        CallbackObj(
            action = ActionType.SEND_MESSAGE_TO_CONTACT_ID.action,
            param = SendContactIdCallbackObj.MessageParamId("contactId", messageToContact),
            id = TEST_CALLBACK_ID
        )
    )

    private val chatBridgeOnSuccess =
        Mockito.spy(
            createChatBridge(
                Mockito.spy(
                    createChatMessageBridgeDispatcher(true)
                ), true
            )
        )

    @Test
    fun `postError should be called when hostapp doesn't implement the chat dispatcher for single chat`() {
        val chatMessageBridgeDispatcher =
            Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, false))
        val errMsg = "The `ChatBridgeDispatcher` has not been implemented by the Host App."

        chatBridge.onSendMessageToContact(sendMessageCallbackObj.id, sendingMessageJsonStr)
        verify(bridgeExecutor).postError(sendMessageCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when hostapp doesn't implement the chat dispatcher for multiple chat`() {
        val chatMessageBridgeDispatcher =
            Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, false))
        val errMsg = "The `ChatBridgeDispatcher` has not been implemented by the Host App."

        chatBridge.onSendMessageToMultipleContacts(multipleCallbackObj.id, multipleMessageJsonStr)
        verify(bridgeExecutor).postError(multipleCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when hostapp doesn't implement the chat dispatcher for a contact id`() {
        val chatMessageBridgeDispatcher =
            Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, false))
        val errMsg = "The `ChatBridgeDispatcher` has not been implemented by the Host App."

        chatBridge.onSendMessageToContactId(specificCallbackObj.id, specificMessageJsonStr)
        verify(bridgeExecutor).postError(specificCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when hostapp can't send message to single contact`() {
        val chatMessageBridgeDispatcher =
            Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, true))
        val errMsg = "$ERR_SEND_MESSAGE $TEST_ERROR_MSG"

        chatBridge.onSendMessageToContact(sendMessageCallbackObj.id, sendingMessageJsonStr)
        verify(bridgeExecutor).postError(sendMessageCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when hostapp can't send message to multiple contacts`() {
        val chatMessageBridgeDispatcher =
            Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, true))
        val errMsg = "$ERR_SEND_MESSAGE $TEST_ERROR_MSG"

        chatBridge.onSendMessageToContact(multipleCallbackObj.id, multipleMessageJsonStr)
        verify(bridgeExecutor).postError(multipleCallbackObj.id, errMsg)
    }

    @Test
    fun `postError should be called when hostapp can't send message to a contact id`() {
        val chatMessageBridgeDispatcher =
            Mockito.spy(createChatMessageBridgeDispatcher(false))
        val chatBridge = Mockito.spy(createChatBridge(chatMessageBridgeDispatcher, true))
        val errMsg = "$ERR_SEND_MESSAGE $TEST_ERROR_MSG"

        chatBridge.onSendMessageToContact(specificCallbackObj.id, specificMessageJsonStr)
        verify(bridgeExecutor).postError(specificCallbackObj.id, errMsg)
    }

    @Test
    fun `postValue should be called when hostapp can send message to single contact`() {
        chatBridgeOnSuccess.onSendMessageToContact(sendMessageCallbackObj.id, sendingMessageJsonStr)
        verify(bridgeExecutor).postValue(sendMessageCallbackObj.id, TEST_CONTACT.id)
    }

    @Test
    fun `postValue should be called when hostapp can send message to specific contact id`() {
        chatBridgeOnSuccess.onSendMessageToContact(specificCallbackObj.id, specificMessageJsonStr)
        verify(bridgeExecutor).postValue(specificCallbackObj.id, TEST_CONTACT.id)
    }

    @Test
    fun `postValue should be called when hostapp can send message to multiple contacts`() {
        chatBridgeOnSuccess.onSendMessageToMultipleContacts(
            multipleCallbackObj.id, multipleMessageJsonStr
        )
        verify(bridgeExecutor).postValue(
            multipleCallbackObj.id,
            Gson().toJson(listOf(TEST_CONTACT.id)).toString()
        )
    }
}
