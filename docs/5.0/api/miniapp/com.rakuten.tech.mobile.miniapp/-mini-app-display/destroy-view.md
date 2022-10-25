//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniAppDisplay](index.md)/[destroyView](destroy-view.md)

# destroyView

[androidJvm]\
abstract fun [destroyView](destroy-view.md)()

Upon invocation, destroys necessary view state and any services registered with. When the consumer has finished consuming, it is advisable to release the resources. Usual scenarios are when the app components are amidst their destroy cycle.

## See also

androidJvm

| | |
|---|---|
| link | [https://developer.android.com/topic/libraries/architecture/lifecycle#lc] on how to setup automatic clearing based on [LifecycleObserver](https://developer.android.com/reference/kotlin/androidx/lifecycle/LifecycleObserver.html) for usual scenarios. To be called when resources are managed manually or where consumer app has more control on the lifecycle of views e.g. removal of the view from the view system, yet within the same state of parent's lifecycle. |
