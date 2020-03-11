---
layout: userguide
---

# Mini App SDK for Android

Provides functionality to show a Mini App in Android Applications. The SDK offers features like downloading, caching, updating, and displaying of a Mini App.

## Requirements

### Supported Android Versions

This SDK supports Android API level 21 (Lollipop) and above.

## Getting Started

### #1 Add dependency to your app's `build.gradle`

```groovy
repositories {
    jcenter()
    maven { url 'http://oss.jfrog.org/artifactory/simple/libs-snapshot/' } // If you want to use snapshot releases
}

dependency {
    implementation 'com.rakuten.tech.mobile.miniapp:miniapp:0.1.0-SNAPSHOT'
}
```

### #2 Set your App Id, Subscription Key, & Base URL

We don't currently host a public API, so you will need to provide your own Base URL for API requests.

In your `AndroidManifest.xml`:

```xml
<manifest>
    <application>

        <!-- Base URL used for retrieving a Mini App -->
        <meta-data
            android:name="com.rakuten.tech.mobile.miniapp.BaseUrl"
            android:value="https://www.example.com" />

        <!-- Version of your app - used to determine feature compatibility for Mini App -->
        <meta-data
            android:name="com.rakuten.tech.mobile.miniapp.HostAppVersion"
            android:value="your_app_version" />

        <!-- App ID for the Platform API -->
        <meta-data
            android:name="com.rakuten.tech.mobile.ras.AppId"
            android:value="your_app_id" />

        <!-- Subscription Key for the Platform API -->
        <meta-data
            android:name="com.rakuten.tech.mobile.ras.ProjectSubscriptionKey"
            android:value="your_subscription_key" />

    </application>
</manifest>
```

**Note:**  All meta-data values must be string values, including the value for `com.rakuten.tech.mobile.miniapp.HostAppVersion`. For example it could be set to the string value `1.0.0`, but if you need to use a number value such as `1.0` or `1`, then you must declare the value in your string resources (`res/values/strings.xml`) and reference the string ID in the manifest, for example `@string/app_version`.

### #3 Create and display a Mini App

Calling `MiniApp.create` will download the Mini App if it has not yet been downloaded, or it will download the latest version of the Mini App if an old version is already downloaded. A view will then be returned which will display the Mini App.

```kotlin
class MiniAppActivity : Activity(), CoroutineScope {

    override val coroutineContext = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super(savedInstanceState)
        setContentView(R.layout.loading)

        val context = this
        launch {
            val miniAppDisplay = withContext(Dispatchers.Default) {
                MiniApp.instance().create("mini_app_id", "mini_app_version_id")
            }
            val miniAppView = miniAppDisplay.getMiniAppView()

            setContentView(miniAppView)
        }
    }
}
```

## Advanced

### Clearing up mini app display

For a mini app, it is required to destroy necessary view state and any services registered with, either automatically or manually. `MiniAppDisplay` complies to Android's `LifecycleObserver` contract. It is quite easy to setup for automatic clean up of resources.

```kotlin
class MiniAppActivity : Activity(), CoroutineScope {

    override fun onCreate(savedInstanceState: Bundle?) {
    //...
        launch {
            val miniAppDisplay = withContext(Dispatchers.Default) {
                MiniApp.instance().create("mini_app_id", "mini_app_version_id")
            }
            lifeCycle.addObserver(miniAppDisplay)
    //...
        }
    }
}
```

To read more about `Lifecycle` please see [link](https://developer.android.com/topic/libraries/architecture/lifecycle#lc). 

On the other hand, when the consuming app manages resources manually or where it has more control on the lifecycle of views `MiniAppDisplay.destroyView` should be called upon e.g. when removing a view from the view system, yet within the same state of parent's lifecycle.

## Changelog
