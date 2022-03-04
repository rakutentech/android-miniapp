//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.js](../index.md)/[MiniAppMessageBridge](index.md)/[requestCustomPermissions](request-custom-permissions.md)

# requestCustomPermissions

[androidJvm]\
open fun [requestCustomPermissions](request-custom-permissions.md)(permissionsWithDescription: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)&lt;[MiniAppCustomPermissionType](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-type/index.md), [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;&gt;, callback: ([List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)&lt;[MiniAppCustomPermissionType](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-type/index.md), [MiniAppCustomPermissionResult](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-result/index.md)&gt;&gt;) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

Post custom permissions request.

## Parameters

androidJvm

| | |
|---|---|
| permissionsWithDescription | list of name and descriptions of custom permissions sent from external. |
| callback | to invoke a list of name and grant results of custom permissions sent from hostapp. |
