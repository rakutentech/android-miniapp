//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniApp](index.md)/[clearSecureStorage](clear-secure-storage.md)

# clearSecureStorage

[androidJvm]\
abstract fun [clearSecureStorage](clear-secure-storage.md)()

Clears all secure storage items for all mini apps. Host App should call this when they want to clear all sensitive and session data such as when a user logs out.

[androidJvm]\
abstract fun [clearSecureStorage](clear-secure-storage.md)(miniAppId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))

Clears the secure storage for a particular Mini App ID.

## Parameters

androidJvm

| | |
|---|---|
| miniAppId | will be used to find the storage to be deleted. |
