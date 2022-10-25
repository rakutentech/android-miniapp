//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniApp](index.md)/[clearSecureStorages](clear-secure-storages.md)

# clearSecureStorages

[androidJvm]\
abstract fun [clearSecureStorages](clear-secure-storages.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))

Clears all secure storage items for all mini apps.

## Parameters

androidJvm

| | |
|---|---|
| context | will be used to find the storage to be deleted. Host App should call this when they want to clear all sensitive and session data such as when a user logs out. |
