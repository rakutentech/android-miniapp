[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.js.userinfo](../index.md) / [UserInfoBridgeDispatcher](index.md) / [getContacts](./get-contacts.md)

# getContacts

`open fun getContacts(onSuccess: (contacts: `[`ArrayList`](https://docs.oracle.com/javase/6/docs/api/java/util/ArrayList.html)`<`[`Contact`](../-contact/index.md)`>) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onError: (message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Get contacts from host app.
You can also throw an [Exception](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html) from this method to pass an error message to the mini app.

