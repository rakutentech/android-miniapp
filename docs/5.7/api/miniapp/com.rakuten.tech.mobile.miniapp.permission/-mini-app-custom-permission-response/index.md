//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.permission](../index.md)/[MiniAppCustomPermissionResponse](index.md)

# MiniAppCustomPermissionResponse

[androidJvm]\
data class [MiniAppCustomPermissionResponse](index.md)(permissions: [ArrayList](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-array-list/index.html)&lt;[MiniAppCustomPermissionResponse.CustomPermissionResponseObj](-custom-permission-response-obj/index.md)&gt;)

A data class to prepare the json response of custom permissions to be sent from this SDK.

## Constructors

| | |
|---|---|
| [MiniAppCustomPermissionResponse](-mini-app-custom-permission-response.md) | [androidJvm]<br>fun [MiniAppCustomPermissionResponse](-mini-app-custom-permission-response.md)(permissions: [ArrayList](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-array-list/index.html)&lt;[MiniAppCustomPermissionResponse.CustomPermissionResponseObj](-custom-permission-response-obj/index.md)&gt;) |

## Types

| Name | Summary |
|---|---|
| [CustomPermissionResponseObj](-custom-permission-response-obj/index.md) | [androidJvm]<br>data class [CustomPermissionResponseObj](-custom-permission-response-obj/index.md)(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), status: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))<br>A data class to hold the json elements to be sent as inside the response. |

## Properties

| Name | Summary |
|---|---|
| [permissions](permissions.md) | [androidJvm]<br>val [permissions](permissions.md): [ArrayList](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-array-list/index.html)&lt;[MiniAppCustomPermissionResponse.CustomPermissionResponseObj](-custom-permission-response-obj/index.md)&gt; |
