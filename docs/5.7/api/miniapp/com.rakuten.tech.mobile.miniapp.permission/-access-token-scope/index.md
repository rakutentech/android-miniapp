//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.permission](../index.md)/[AccessTokenScope](index.md)

# AccessTokenScope

[androidJvm]\
data class [AccessTokenScope](index.md)(audience: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), scopes: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;)

Contains the components which need to be validated when access token is granted.

## Constructors

| | |
|---|---|
| [AccessTokenScope](-access-token-scope.md) | [androidJvm]<br>fun [AccessTokenScope](-access-token-scope.md)() |
| [AccessTokenScope](-access-token-scope.md) | [androidJvm]<br>fun [AccessTokenScope](-access-token-scope.md)(audience: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), scopes: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;) |

## Properties

| Name | Summary |
|---|---|
| [audience](audience.md) | [androidJvm]<br>val [audience](audience.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The service of access token. |
| [scopes](scopes.md) | [androidJvm]<br>val [scopes](scopes.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;<br>List of areas that token can access. |
