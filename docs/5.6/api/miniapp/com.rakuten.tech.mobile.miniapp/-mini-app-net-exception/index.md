//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniAppNetException](index.md)

# MiniAppNetException

[androidJvm]\
class [MiniAppNetException](index.md)(message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), cause: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)?) : [MiniAppSdkException](../-mini-app-sdk-exception/index.md)

Exception indicating that there was an issue with network connectivity.

This usually means the device doesn't have internet access currently, so you should display the cached mini app version.

## Constructors

| | |
|---|---|
| [MiniAppNetException](-mini-app-net-exception.md) | [androidJvm]<br>fun [MiniAppNetException](-mini-app-net-exception.md)(e: [Exception](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html)) |
| [MiniAppNetException](-mini-app-net-exception.md) | [androidJvm]<br>fun [MiniAppNetException](-mini-app-net-exception.md)(message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |
| [MiniAppNetException](-mini-app-net-exception.md) | [androidJvm]<br>fun [MiniAppNetException](-mini-app-net-exception.md)(message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), cause: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)?) |

## Functions

| Name | Summary |
|---|---|
| [addSuppressed](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#282858770%2FFunctions%2F1451286739) | [androidJvm]<br>fun [addSuppressed](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#282858770%2FFunctions%2F1451286739)(p0: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)) |
| [fillInStackTrace](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#-1102069925%2FFunctions%2F1451286739) | [androidJvm]<br>open fun [fillInStackTrace](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#-1102069925%2FFunctions%2F1451286739)(): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html) |
| [getLocalizedMessage](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#1043865560%2FFunctions%2F1451286739) | [androidJvm]<br>open fun [getLocalizedMessage](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#1043865560%2FFunctions%2F1451286739)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [getStackTrace](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#2050903719%2FFunctions%2F1451286739) | [androidJvm]<br>open fun [getStackTrace](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#2050903719%2FFunctions%2F1451286739)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[StackTraceElement](https://developer.android.com/reference/kotlin/java/lang/StackTraceElement.html)&gt; |
| [getSuppressed](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#672492560%2FFunctions%2F1451286739) | [androidJvm]<br>fun [getSuppressed](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#672492560%2FFunctions%2F1451286739)(): [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)&gt; |
| [initCause](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#-418225042%2FFunctions%2F1451286739) | [androidJvm]<br>open fun [initCause](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#-418225042%2FFunctions%2F1451286739)(p0: [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html) |
| [printStackTrace](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#-1769529168%2FFunctions%2F1451286739) | [androidJvm]<br>open fun [printStackTrace](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#-1769529168%2FFunctions%2F1451286739)()<br>open fun [printStackTrace](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#1841853697%2FFunctions%2F1451286739)(p0: [PrintStream](https://developer.android.com/reference/kotlin/java/io/PrintStream.html))<br>open fun [printStackTrace](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#1175535278%2FFunctions%2F1451286739)(p0: [PrintWriter](https://developer.android.com/reference/kotlin/java/io/PrintWriter.html)) |
| [setStackTrace](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#2135801318%2FFunctions%2F1451286739) | [androidJvm]<br>open fun [setStackTrace](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#2135801318%2FFunctions%2F1451286739)(p0: [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[StackTraceElement](https://developer.android.com/reference/kotlin/java/lang/StackTraceElement.html)&gt;) |

## Properties

| Name | Summary |
|---|---|
| [cause](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#-654012527%2FProperties%2F1451286739) | [androidJvm]<br>open val [cause](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#-654012527%2FProperties%2F1451286739): [Throwable](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)? |
| [message](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#1824300659%2FProperties%2F1451286739) | [androidJvm]<br>open val [message](../../com.rakuten.tech.mobile.miniapp.signatureverifier.api/-invalid-signature-verifier-subscription-exception/index.md#1824300659%2FProperties%2F1451286739): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? |
