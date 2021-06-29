[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.ads](../index.md) / [AdMobDisplayer](./index.md)

# AdMobDisplayer

`class AdMobDisplayer : `[`MiniAppAdDisplayer`](../-mini-app-ad-displayer/index.md)`, CoroutineScope`

The ad displayer.

### Parameters

`context` - should use the same activity context for #MiniAppDisplay.getMiniAppView.
Support Interstitial, Reward ads.

### Constructors

| [&lt;init&gt;](-init-.md) | The ad displayer.`AdMobDisplayer(context: `[`Activity`](https://developer.android.com/reference/android/app/Activity.html)`)` |

### Properties

| [coroutineContext](coroutine-context.md) | `val coroutineContext: `[`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines/-coroutine-context/index.html) |

### Functions

| [loadInterstitialAd](load-interstitial-ad.md) | Load the interstitial ad when it is ready.`fun loadInterstitialAd(adUnitId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, onLoaded: () -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onFailed: (`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [loadRewardedAd](load-rewarded-ad.md) | Load the rewarded ad when it is ready.`fun loadRewardedAd(adUnitId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, onLoaded: () -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onFailed: (`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [showInterstitialAd](show-interstitial-ad.md) | Show the interstitial ad when it is already loaded.`fun showInterstitialAd(adUnitId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, onClosed: () -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onFailed: (`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [showRewardedAd](show-rewarded-ad.md) | Show the rewarded ad when it is already loaded.`fun showRewardedAd(adUnitId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, onClosed: (reward: `[`Reward`](../-reward/index.md)`?) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onFailed: (`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

