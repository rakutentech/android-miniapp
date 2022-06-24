//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.js.userinfo](../index.md)/[UserInfoBridgeDispatcher](index.md)/[getPoints](get-points.md)

# getPoints

[androidJvm]\
open fun [getPoints](get-points.md)(onSuccess: (points: [Points](../-points/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), onError: (pointsError: [MiniAppPointsError](../../com.rakuten.tech.mobile.miniapp.errors/-mini-app-points-error/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

Get points from host app. You can also throw an [Exception](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html) from this method to pass an error message to the mini app.
