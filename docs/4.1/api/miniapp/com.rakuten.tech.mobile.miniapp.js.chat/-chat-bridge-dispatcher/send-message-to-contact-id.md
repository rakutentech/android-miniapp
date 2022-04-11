//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.js.chat](../index.md)/[ChatBridgeDispatcher](index.md)/[sendMessageToContactId](send-message-to-contact-id.md)

# sendMessageToContactId

[androidJvm]\
abstract fun [sendMessageToContactId](send-message-to-contact-id.md)(contactId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), message: [MessageToContact](../../com.rakuten.tech.mobile.miniapp.js/-message-to-contact/index.md), onSuccess: (contactId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), onError: (message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

Triggered when Mini App wants to send a message to a specific contact. Should send a message to the specified contactId without any prompt to the User. Should invoke [onSuccess](send-message-to-contact-id.md) after message was successfully sent. Should invoke [onError](send-message-to-contact-id.md) when there was an error.
