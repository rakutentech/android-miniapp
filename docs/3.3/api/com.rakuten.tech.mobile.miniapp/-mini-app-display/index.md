[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniAppDisplay](./index.md)

# MiniAppDisplay

`interface MiniAppDisplay : LifecycleObserver`

This represents the contract by which the host app can interact with the
display unit of the mini app.
This contract complies to Android's [LifecycleObserver](#) contract, and when made to observe
the lifecycle, it automatically clears up the view state and any services registered with.

### Functions

| [destroyView](destroy-view.md) | Upon invocation, destroys necessary view state and any services registered with. When the consumer has finished consuming, it is advisable to release the resources. Usual scenarios are when the app components are amidst their destroy cycle.`abstract fun destroyView(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [getMiniAppView](get-mini-app-view.md) | Provides the view associated with the mini app to the caller for showing the mini app.`abstract suspend fun getMiniAppView(activityContext: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`): `[`View`](https://developer.android.com/reference/android/view/View.html)`?` |
| [navigateBackward](navigate-backward.md) | Navigates one level back, in the call stack, if possible.`abstract fun navigateBackward(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [navigateForward](navigate-forward.md) | Navigates one level forward from current position, in the call hierarchy, if possible.`abstract fun navigateForward(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

