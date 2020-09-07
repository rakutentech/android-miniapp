---
layout: userguide
---

# Mini App SDK for Android

Provides functionality to show a Mini App in Android Applications. The SDK offers features like downloading, caching, updating, and displaying of a Mini App.
Mini App SDK also facilitates communication between a mini app and the host app via a message bridge.

## Requirements

### Supported Android Versions

This SDK supports Android API level 23 and above.

## Getting Started

### #1 Add dependency to your app's `build.gradle`

```groovy
repositories {
    jcenter()

    // The following repo is needed only if you want to use snapshot releases
    maven { url 'http://oss.jfrog.org/artifactory/simple/libs-snapshot/' }
}

dependency {
    implementation 'com.rakuten.tech.mobile.miniapp:miniapp:${version}'
}
```

### #2 Configure SDK settings in AndroidManifest.xml

The SDK is configured via manifest meta-data, the configurable values are:

| Field                        | Datatype| Manifest Key                                           | Optional   | Default  |
|------------------------------|---------|--------------------------------------------------------|----------- |--------- |
| Base URL                     | String  | `com.rakuten.tech.mobile.miniapp.BaseUrl`              | ❌         | 🚫        |
| App ID                       | String  | `com.rakuten.tech.mobile.ras.AppId`                    | ❌         | 🚫        |
| RAS Project Subscription Key | String  | `com.rakuten.tech.mobile.ras.ProjectSubscriptionKey`   | ❌         | 🚫        |
| Host App Version             | String  | `com.rakuten.tech.mobile.miniapp.HostAppVersion`       | ❌         | 🚫        |
| Host App User Agent Info     | String  | `com.rakuten.tech.mobile.miniapp.HostAppUserAgentInfo` | ✅         | 🚫        |

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

        <!-- App ID for the Platform API -->
        <meta-data
            android:name="com.rakuten.tech.mobile.ras.AppId"
            android:value="your_app_id" />

        <!-- Subscription Key for the Platform API -->
        <meta-data
            android:name="com.rakuten.tech.mobile.ras.ProjectSubscriptionKey"
            android:value="your_subscription_key" />

        <!-- Version of your app - used to determine feature compatibility for Mini App -->
        <meta-data
            android:name="com.rakuten.tech.mobile.miniapp.HostAppVersion"
            android:value="your_app_version" />

        <!-- Optional User Agent Information relating to the host app -->
        <meta-data
            android:name="com.rakuten.tech.mobile.miniapp.HostAppUserAgentInfo"
            android:value="app_name/version_info" />

    </application>
</manifest>
```

### #3 Fetch Mini App Info

Information about Mini Apps can be fetched in two different ways: by using `MiniApp.listMiniApp` to get a list of info for all Mini Apps, or by using `MiniApp.fetchInfo` to get info for a single Mini App. Either method will return `MiniAppInfo` objects with info about the Mini App such as name, icon URL, ID, version, etc.

Use `MiniApp.listMiniApp` if you want a list of all Mini Apps:

```kotlin
CoroutineScope(Dispatchers.IO).launch {
    try {
        val miniAppList = MiniApp.instance().listMiniApp()
    } catch(e: MiniAppSdkException) {
        Log.e("MiniApp", "There was an error retrieving the list", e)
    }
}
```

Or use `MiniApp.fetchInfo` if you want info for a single Mini App and already know the Mini App's ID:

```kotlin
CoroutineScope(Dispatchers.IO).launch {
    try {
        val miniAppInfo = MiniApp.instance().fetchInfo("MINI_APP_ID")
    } catch(e: MiniAppSdkException) {
        Log.e("MiniApp", "There was an error retrieving the Mini App info", e)
    }
}
```

**Note:** This SDK uses `suspend` functions, so you should use [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) when calling the functions. These examples use `Dispatchers.IO`, but you can use whichever `CoroutineContext` and `CouroutineScope` that is appropriate for your App. However, you MUST NOT use `Dispatchers.Main` because network requests cannot be performed on the main thread.

### #4 Implement the MiniAppMessageBridge

The `MiniAppMessageBridge` is used for passing messages between the Mini App (JavaScript) and the Host App (your native Android App) and vice versa. Your App must provide the implementation for these functions and pass this implementation to the `MiniApp#create` function.

```kotlin
val miniAppMessageBridge = object: MiniAppMessageBridge() {
    override fun getUniqueId() {
        val id: String = ""
        // Implementation details to generate a Unique ID
        // .. .. ..

        return id
    }

    override fun requestPermission(
        miniAppPermissionType: MiniAppPermissionType,
        callback: (isGranted: Boolean) -> Unit
    ) {
        // Implementation details to request device permission for location
        // .. .. ..

        callback.invoke(true)
    }
}
```

### #5 Create and display a Mini App

Calling `MiniApp.create` with a Mini App ID object will download the latest version of the Mini App if it has not yet been downloaded. A view will then be returned which will display the Mini App.

```kotlin
class MiniAppActivity : Activity(), CoroutineScope {

    override val coroutineContext = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super(savedInstanceState)
        setContentView(R.layout.loading)

        val context = this
        launch {
            try {
                val miniAppDisplay = withContext(Dispatchers.IO) {
                    MiniApp.instance().create("MINI_APP_ID", miniAppMessageBridge)
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

`MiniAppDisplay.navigateBackward` and `MiniAppDisplay.navigateForward` facilitates the navigation inside a mini app if the history stack is available in it. A common usage pattern could be to link it up to the Android Back Key navigation.

## Advanced

### #1 Clearing up mini app display

For a mini app, it is required to destroy necessary view state and any services registered with, either automatically or manually. `MiniAppDisplay` complies to Android's `LifecycleObserver` contract. It is quite easy to setup for automatic clean up of resources.

```kotlin
class MiniAppActivity : Activity(), CoroutineScope {

    override fun onCreate(savedInstanceState: Bundle?) {
    //...
        launch {
            val miniAppDisplay = withContext(Dispatchers.IO) {
                MiniApp.instance().create("mini_app_id", miniAppMessageBridge)
            }
            lifeCycle.addObserver(miniAppDisplay)
    //...
        }
    }
}
```

To read more about `Lifecycle` please see [link](https://developer.android.com/topic/libraries/architecture/lifecycle#lc). 

On the other hand, when the consuming app manages resources manually or where it has more control on the lifecycle of views `MiniAppDisplay.destroyView` should be called upon e.g. when removing a view from the view system, yet within the same state of parent's lifecycle.

### #2 Navigating inside a mini app

For a common usage pattern, the navigation inside a mini app can be attached to the Android back key navigation as shown:

```kotlin
override fun onBackPressed() {
    if(!miniAppDisplay.navigateBackward()) {
        super.onBackPressed()
    }
}
```

### #3 External url loader

The mini app is loaded with the specific custom scheme and custom domain in mini app view.

In default, the external link is also loaded in mini app view.
It is possible for hostapp to load this external link with its own webview / browser.

- Implement `MiniAppNavigator`.

```kotlin
miniAppNavigator = object : MiniAppNavigator {
    override fun openExternalUrl(url: String, externalResultHandler: ExternalResultHandler) {
        // Load external url with own webview.
    }
}
```

- Create mini app display
Using `MiniApp.instance().create("MINI_APP_ID", miniAppMessageBridge, miniAppNavigator)`.

- Return URL result to mini app view.
Some mini apps are loaded their services with external url but in the end that external url will
trigger callback or webhook to redirect to mini app custom scheme, mini app custom domain.
The external webview / browser cannot recognize mini app url so it is required the return of
that url to mini app view.

There are two approaches to return mini app url from host app webview to mini app view:

#### Automatic check in WebView which belongs to separated Activity
If the external webview Activity is different from the Activity running mini app, our SDK provide
the auto check and Activity closing by overriding the [WebViewClient](https://developer.android.com/reference/android/webkit/WebViewClient).

```kotlin
val miniAppExternalUrlLoader = MiniAppExternalUrlLoader(miniAppId, externalWebViewActivity)
```
```kotlin
class MyWebViewClient(private val miniAppExternalUrlLoader: MiniAppExternalUrlLoader): WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        return miniAppExternalUrlLoader.shouldOverrideUrlLoading(url)
    }
}
```

Return the url result to mini app view:

```kotlin
// externalResultHandler is from MiniAppNavigator implementation.
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == externalWebViewReqCode && resultCode == Activity.RESULT_OK) {
            data?.let { intent -> externalResultHandler.emitResult(intent) }
        }
}
```

#### Manual check by host app
Host app can take full control and transmit the url back to mini app view.

```kotlin
val miniAppExternalUrlLoader = MiniAppExternalUrlLoader(miniAppId, null)
```
Using `miniAppExternalUrlLoader.shouldClose(url)` which returns `Boolean` to check if it is
mini app scheme and should close external webview.

Using `#ExternalResultHandler.emitResult(String)` to transmit the url string to mini app view.

## Troubleshooting

### Exception: "Network requests must not be performed on the main thread."

Some of the suspending functions in this SDK will perform network requests (`MiniApp.create`, `MiniApp.fetchInfo`, `MiniApp.listMiniApp`). Network requests should not be performed on the main thread, so the above exception will occur if your Coroutine is running in the `Dispatchers.Main` CoroutineContext. To avoid this exception, please use the `Dispatchers.IO` or `Dispatchers.Default` context instead. You can use [`withContext`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-context.html) to make sure your code is running in the appropriate CoroutineContext.

```kotlin
CoroutineScope(Dispatchers.Main).launch {
    withContext(Dispatchers.IO) {
        // Call MiniApp suspending function i.e. `MiniApp.create`
        // This runs in a background thread
    }
        
    // Update your UI - i.e. `setContentView(miniAppView)`
    // This runs on the main thread
}
```

## Changelog

See the full [CHANGELOG](https://github.com/rakutentech/android-miniapp/blob/master/CHANGELOG.md).
