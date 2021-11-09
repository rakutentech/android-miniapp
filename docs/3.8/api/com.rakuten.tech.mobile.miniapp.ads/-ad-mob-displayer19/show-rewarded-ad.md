[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.ads](../index.md) / [AdMobDisplayer19](index.md) / [showRewardedAd](./show-rewarded-ad.md)

# showRewardedAd

`fun showRewardedAd(adUnitId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, onClosed: (reward: `[`Reward`](../-reward/index.md)`?) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onFailed: (`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Show the rewarded ad when it is already loaded.

### Parameters

`onClosed` - When the ad is closed, forward the reward earned by the user.
Reward will be null if the user did not earn the reward.