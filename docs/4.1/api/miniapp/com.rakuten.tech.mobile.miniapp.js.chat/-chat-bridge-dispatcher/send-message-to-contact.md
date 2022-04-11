//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.js.chat](../index.md)/[ChatBridgeDispatcher](index.md)/[sendMessageToContact](send-message-to-contact.md)

# sendMessageToContact

[androidJvm]\
abstract fun [sendMessageToContact](send-message-to-contact.md)(message: [MessageToContact](../../com.rakuten.tech.mobile.miniapp.js/-message-to-contact/index.md), onSuccess: (contactId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), onError: (message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

Triggered when Mini App wants to send a message to a single contact. Should invoke [onSuccess](send-message-to-contact.md) with the contact ID to send the message. If the user wants to cancel sending the message, should invoked [onSuccess](send-message-to-contact.md) with null. Should invoke [onError](send-message-to-contact.md) when there was an error.
