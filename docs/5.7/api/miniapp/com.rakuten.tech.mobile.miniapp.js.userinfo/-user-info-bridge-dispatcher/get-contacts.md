//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.js.userinfo](../index.md)/[UserInfoBridgeDispatcher](index.md)/[getContacts](get-contacts.md)

# getContacts

[androidJvm]\
open fun [getContacts](get-contacts.md)(onSuccess: (contacts: [ArrayList](https://developer.android.com/reference/kotlin/java/util/ArrayList.html)&lt;[Contact](../-contact/index.md)&gt;) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), onError: (message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

Get contacts from host app. You can also throw an [Exception](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html) from this method to pass an error message to the mini app.
