[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.ads](../index.md) / [MiniAppAdDisplayer](./index.md)

# MiniAppAdDisplayer

`interface MiniAppAdDisplayer`

Control ads load &amp; display when want to use AdMob.

### Functions

| [loadInterstitialAd](load-interstitial-ad.md) | Load the interstitial ad when it is ready.`abstract fun loadInterstitialAd(adUnitId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, onLoaded: () -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onFailed: (`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [loadRewardedAd](load-rewarded-ad.md) | Load the rewarded ad when it is ready.`abstract fun loadRewardedAd(adUnitId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, onLoaded: () -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onFailed: (`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [showInterstitialAd](show-interstitial-ad.md) | Show the interstitial ad when it is already loaded.`abstract fun showInterstitialAd(adUnitId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, onClosed: () -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onFailed: (`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [showRewardedAd](show-rewarded-ad.md) | Show the rewarded ad when it is already loaded.`abstract fun showRewardedAd(adUnitId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, onClosed: (reward: `[`Reward`](../-reward/index.md)`?) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onFailed: (`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Inheritors

| [AdMobDisplayer19](../-ad-mob-displayer19/index.md) | The ad displayer.`class AdMobDisplayer19 : `[`MiniAppAdDisplayer`](./index.md)`, CoroutineScope` |

