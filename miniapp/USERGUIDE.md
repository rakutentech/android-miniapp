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
    maven { url 'http://oss.jfrog.org/artifactory/simple/libs-snapshot/' } // Needed only if you want to use snapshot releases
}

dependency {
    implementation 'com.rakuten.tech.mobile.miniapp:miniapp:${version}'
}
```

### #2 Configure SDK settings in AndroidManifest.xml

The SDK is configured via manifest meta-data, the configurable values are:

| Field                        | Datatype| Manifest Key                                         | Optional   | Default   |
|------------------------------|---------|------------------------------------------------------|------------|---------- |
| Base URL                     | String  | `com.rakuten.tech.mobile.miniapp.BaseUrl`            | ❌         | 🚫        |
| Testbox Config               | boolean | `com.rakuten.tech.mobile.miniapp.IsTestMode`         | ✅         | `false`   |
| Host App Version             | String  | `com.rakuten.tech.mobile.miniapp.HostAppVersion`     | ❌         | 🚫        |
| Host App Info                | String  | `com.rakuten.tech.mobile.miniapp.HostAppInfo`        | ✅         | 🚫        |
| App ID                       | String  | `com.rakuten.tech.mobile.ras.AppId`                  | ❌         | 🚫        |
| RAS Project Subscription Key | String  | `com.rakuten.tech.mobile.ras.ProjectSubscriptionKey` | ❌         | 🚫        |

**Note:**  
* We don't currently host a public API, so you will need to provide your own Base URL for API requests.
* All meta-data values must be string values, including the value for `com.rakuten.tech.mobile.miniapp.HostAppVersion`. For example it could be set to the string value `1.0.0`, but if you need to use a number value such as `1.0` or `1`, then you must declare the value in your string resources (`res/values/strings.xml`) and reference the string ID in the manifest, for example `@string/app_version`.
* The host app info is the string which is appended to user-agent of webview. It should be a meaningful keyword such as host app name to differentiate other host apps.

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

### #3 Fetch Mini App Info

Information about Mini Apps can be fetched in two different ways: by using `MiniApp.listMiniApp` to get a list of info for all Mini Apps, or by using `MiniApp.fetchInfo` to get info for a single Mini App. Either method will return `MiniAppInfo` objects with info about the Mini App such as name, icon URL, ID, version, etc.

Use `MiniApp.listMiniApp` if you want a list of all Mini Apps:

```kotlin
CoroutineScope(Dispatchers.Default).launch {
    try {
        val miniAppList = MiniApp.instance().listMiniApp()
    } catch(e: MiniAppSdkException) {
        Log.e("MiniApp", "There was an error retrieving the list", e)
    }
}
```

Or use `MiniApp.fetchInfo` if you want info for a single Mini App and already know the Mini App's ID:

```kotlin
CoroutineScope(Dispatchers.Default).launch {
    try {
        val miniAppInfo = MiniApp.instance().fetchInfo("MINI_APP_ID")
    } catch(e: MiniAppSdkException) {
        Log.e("MiniApp", "There was an error retrieving the Mini App info", e)
    }
}
```

**Note:** This SDK uses `suspend` functions, so you should use [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) when calling the functions. These examples use `Dispatchers.Default`, but you can use whichever `CoroutineContext` and `CouroutineScope` that is appropriate for your App.

### #4 Implement the MiniAppMessageBridge

The `MiniAppMessageBridge` is used for passing messages between the Mini App (JavaScript) and the Host App (your native Android App) and vice versa. Your App must provide the implementation for these functions and pass this implementation to the `MiniApp#create` function.

```kotlin
val miniAppMessageBridge = object: MiniAppMessageBridge() {
    override fun getUniqueId() = AppSettings.instance.uniqueId
}
```

### #5 Create and display a Mini App

Calling `MiniApp.create` with a `MiniAppInfo` object will download the Mini App if it has not yet been downloaded. A view will then be returned which will display the Mini App. The `MiniAppInfo` object also contains information about the latest Mini App version, so make sure to fetch the latest `MiniAppInfo` first.

```kotlin
class MiniAppActivity : Activity(), CoroutineScope {

    override val coroutineContext = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super(savedInstanceState)
        setContentView(R.layout.loading)

        val context = this
        launch {
            try {
                val miniAppDisplay = withContext(Dispatchers.Default) {
                    val miniAppInfo = MiniApp.instance().fetchInfo("MINI_APP_ID") // Or use `MiniApp.listMiniApp` if you want the whole list of Mini Apps
                    MiniApp.instance().create(miniAppInfo, miniAppMessageBridge)
                }
                val miniAppView = miniAppDisplay.getMiniAppView(this@MiniAppActivity)

                setContentView(miniAppView)
            } catch (e: MiniAppSdkException) {
                setContentView(R.layout.error_screen)
            }
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

## Troubleshooting

### AppCompat Version

`androidx.appcompat:appcompat`

The stable version of AndroidX AppCompat library `1.1.0` had issues on old Android OS when creating `Webview` with `ActivityContext`.  
We recommend using the updated versions of this library.

## Changelog

### 1.1.1 (2020-06-11)

**SDK**
- *Bugfix:* `select` and `date` input elements weren't working correctly.
- Deprecated `MiniAppDisplay#getMiniAppView()` and added `MiniAppDisplay#getMiniAppView(activityContext: Context)`. You now must provide an Activity Context when retrieving the `View` for the Mini App. This is related to the bugfix for `select` and `date` inputs - if you use the deprecated method, then these elements will not work correctly.

**Sample App**
- Display first time setup instructions on first launch of App.

### 1.1.0 (2020-06-02)

- Added JavaScript bridge for passing data between Mini App and Host App. Your App now must implement `MiniAppMessageBridge` and provide the implementation when calling `MiniApp#create`.
- Deprecated `MiniApp#create(info: MiniAppInfo)`. Your App should instead use `MiniApp#create(info: MiniAppInfo, miniAppMessageBridge: MiniAppMessageBridge)`.
- Added `getUniqueId` function to `MiniAppMessageBridge`. This function should provide a unique identifier (unique to the user and device) to Mini Apps.
- Added support for custom scheme URL redirect. The URL `mscheme.MINI_APP_ID://miniapp/index.html` can be used from within the Mini App view to redirect to the Mini App. This matches the URL used in the iOS Mini App SDK.

### 1.0.0 (2020-04-21)

- Initial release
