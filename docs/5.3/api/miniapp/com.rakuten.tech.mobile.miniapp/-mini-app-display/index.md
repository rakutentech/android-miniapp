//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniAppDisplay](index.md)

# MiniAppDisplay

[androidJvm]\
interface [MiniAppDisplay](index.md) : [LifecycleObserver](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleObserver.html)

This represents the contract by which the host app can interact with the display unit of the mini app. This contract complies to Android's [LifecycleObserver](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleObserver.html) contract, and when made to observe the lifecycle, it automatically clears up the view state and any services registered with.

## Functions

| Name | Summary |
|---|---|
| [destroyView](destroy-view.md) | [androidJvm]<br>abstract fun [destroyView](destroy-view.md)()<br>Upon invocation, destroys necessary view state and any services registered with. When the consumer has finished consuming, it is advisable to release the resources. Usual scenarios are when the app components are amidst their destroy cycle. |
| [getMiniAppView](get-mini-app-view.md) | [androidJvm]<br>abstract suspend fun [getMiniAppView](get-mini-app-view.md)(activityContext: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [View](https://developer.android.com/reference/kotlin/android/view/View.html)?<br>Provides the view associated with the mini app to the caller for showing the mini app. |
| [navigateBackward](navigate-backward.md) | [androidJvm]<br>abstract fun [navigateBackward](navigate-backward.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Navigates one level back, in the call stack, if possible. |
| [navigateForward](navigate-forward.md) | [androidJvm]<br>abstract fun [navigateForward](navigate-forward.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Navigates one level forward from current position, in the call hierarchy, if possible. |
| [sendJsonToMiniApp](send-json-to-mini-app.md) | [androidJvm]<br>abstract fun [sendJsonToMiniApp](send-json-to-mini-app.md)(message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))<br>Send a generic message to MiniApp using [com.rakuten.tech.mobile.miniapp.js.NativeEventType.MINIAPP_RECEIVE_JSON_INFO](../../com.rakuten.tech.mobile.miniapp.js/-native-event-type/-m-i-n-i-a-p-p_-r-e-c-e-i-v-e_-j-s-o-n_-i-n-f-o/index.md). |
