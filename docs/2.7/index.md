---
layout: userguide
---

# Mini App SDK for Android

Provides functionality to show a Mini App in Android Applications. The SDK offers features like downloading, caching, updating, and displaying of a Mini App.
Mini App SDK also facilitates communication between a mini app and the host app via a message bridge.

## Table of Contents
{:.no_toc}

* Table of contents
{:toc}

## Requirements

- **Minimum Android Version**: This SDK supports Android 6.0+ (API level 23+).
- **Base URL, App ID, Subscription Key**: We don't currently provide a public API for use with this SDK. You must provide a URL for your API as well as an App ID and Subscription Key for the API.

## Getting Started

This section will guide you through setting up the Mini App SDK in your App and you will learn how to display your first mini app.

### #1 Add dependency to your App

Add the following to your `build.gradle` file:

```groovy
repositories {
    jcenter()
}

dependency {
    implementation 'com.rakuten.tech.mobile.miniapp:miniapp:${version}'
}
```

### #2 Configure SDK settings in AndroidManifest.xml

The SDK is configured via `meta-data` tags in your `AndroidManifest.xml`. The following table lists the configurable values.

| Field                        | Datatype| Manifest Key                                           | Optional   | Default  |
|------------------------------|---------|--------------------------------------------------------|----------- |--------- |
| Base URL                     | String  | `com.rakuten.tech.mobile.miniapp.BaseUrl`              | ❌         | 🚫        |
| Is Preview Mode              | Boolean | `com.rakuten.tech.mobile.miniapp.IsPreviewMode`        | ❌         | 🚫        |
| RAS Project ID               | String  | `com.rakuten.tech.mobile.ras.ProjectId`                | ❌         | 🚫        |
| RAS Project Subscription Key | String  | `com.rakuten.tech.mobile.ras.ProjectSubscriptionKey`   | ❌         | 🚫        |
| Host App User Agent Info     | String  | `com.rakuten.tech.mobile.miniapp.HostAppUserAgentInfo` | ✅         | 🚫        |

**Note:**  
* We don't currently host a public API, so you will need to provide your own Base URL for API requests.
* The "Host App User Agent Info" is the string which is appended to user-agent of webview. It should be a meaningful keyword such as host app name to differentiate other host apps.

<details><summary markdown="span"><b>Click to expand example AndroidManifest.xml</b>
</summary>

```xml
<manifest>
    <application>

        <!-- Base URL used for retrieving a Mini App -->
        <meta-data
            android:name="com.rakuten.tech.mobile.miniapp.BaseUrl"
            android:value="https://www.example.com" />

        <!-- Preview mode used for retrieving the Mini Apps -->
        <meta-data
            android:name="com.rakuten.tech.mobile.miniapp.IsPreviewMode"
            android:value="${isPreviewMode}" />

        <!-- Project ID for the Platform API -->
        <meta-data
            android:name="com.rakuten.tech.mobile.ras.ProjectId"
            android:value="your_project_id" />

        <!-- Subscription Key for the Platform API -->
        <meta-data
            android:name="com.rakuten.tech.mobile.ras.ProjectSubscriptionKey"
            android:value="your_subscription_key" />

        <!-- Optional User Agent Information relating to the host app -->
        <meta-data
            android:name="com.rakuten.tech.mobile.miniapp.HostAppUserAgentInfo"
            android:value="app_name/version_info" />

    </application>
</manifest>
```
</details>

### #3 Create and display a Mini App
**API Docs:** [MiniApp.create](api/com.rakuten.tech.mobile.miniapp/-mini-app/create.html), [MiniAppDisplay](api/com.rakuten.tech.mobile.miniapp/-mini-app-display/), [MiniAppMessageBridge](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge)

`MiniApp.create` is used to create a `View` for displaying a specific mini app. You must provide the mini app ID which you wish to create (you can get the mini app ID by [Fetching Mini App Info](#fetching-mini-app-info) first). Calling `MiniApp.create` will do the following:

- Check what is the latest, published version of the mini app.
- Check if the latest version of the mini app has been downloaded.
    - If yes, return the already downloaded mini app.
    - If no, download the latest version and then return the downloaded version.
- If the device is disconnected from the internet and the device already has a version of the mini app downloaded, then the already downloaded version will be returned.

After calling `MiniApp.create`, you will obtain an instance of `MiniAppDisplay` which represents the downloaded mini app. You can call `MiniAppDisplay.getMiniAppView` to obtain a `View` for displaying the mini app.

The following is a simplified example:

```kotlin
try {
    val miniAppMessageBridge = object : MiniAppMessageBridge() {
        // implement methods for mini app bridge
    }

    val miniAppDisplay = withContext(Dispatchers.IO) {
        MiniApp.instance().create("MINI_APP_ID", miniAppMessageBridge)
    }
    val miniAppView = miniAppDisplay.getMiniAppView(this@YourActivity)

    // Add the view to your Activity
} catch (e: MiniAppSdkException) {
    // Handle exception
}
```

Note that this is a simplified example. See [Mini App Features](#mini-app-features) for the full functionality which you can provide to the mini app. The following is a more complete example:

<details><summary markdown="span"><b>Click here to expand a full code example</b>
</summary>

```kotlin
class MiniAppActivity : Activity(), CoroutineScope {

    override val coroutineContext = Dispatchers.Main

    private var miniAppDisplay: MiniAppDisplay

    override fun onCreate(savedInstanceState: Bundle?) {
        super(savedInstanceState)
        setContentView(R.layout.loading)

        launch {
            try {
                miniAppDisplay = withContext(Dispatchers.IO) {
                    MiniApp.instance().create("MINI_APP_ID", createMessageBridge())
                }
                val miniAppView = miniAppDisplay.getMiniAppView(this@MiniAppActivity)

                setContentView(miniAppView)
            } catch (e: MiniAppSdkException) {
                setContentView(R.layout.error_screen)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        miniAppDisplay.destroyView()
    }

    fun createMessageBridge() = object : MiniAppMessageBridge() {
        override fun getUniqueId() {
            // Implementation details to generate a Unique ID

            return "your-unique-id"
        }

        override fun requestPermission(
            miniAppPermissionType: MiniAppPermissionType,
            callback: (isGranted: Boolean) -> Unit
        ) {
            // Implementation details to request device permissions

            callback.invoke(true)
        }
        
        // You can additionally implement other MiniAppMessageBridge methods
    }
}
```
</details>

**Note:** 
* **Clean-up:** 
Clearing up the mini app display is essential. `MiniAppDisplay.destroyView` is required to be called when exit miniapp.
* **Suspend functions:** 
This SDK uses `suspend` functions, so you should use [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) when calling the functions. These examples use `Dispatchers.IO`, but you can use whichever `CoroutineContext` and `CouroutineScope` that is appropriate for your App. However, you MUST NOT use `Dispatchers.Main` because network requests cannot be performed on the main thread.
* **Preview Mode:** 
In preview mode, you can have multiple versions of single miniapp so you can load the specific version with MiniAppInfo object by using `MiniApp.instance().create(MINI_APP_INFO, miniAppMessageBridge)`.
* **Exceptions:** 
There are several different types of exceptions which could be thrown by `MiniApp.create`, but all are sub-classes of `MiniAppSdkException`.
You can handle each exception type differently if you would like different behavior for different cases.
For example you may wish to display a different error message when the server contains no published versions of a mini app.
See the full list of exceptions in the [API docs](api/com.rakuten.tech.mobile.miniapp/-mini-app/create.html).

## Mini App Features
**API Docs:** [MiniAppMessageBridge](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge)

The `MiniAppMessageBridge` is used for passing messages between the Mini App (JavaScript) and the Host App (your native Android App) and vice versa. Your App must provide the implementation for these functions and pass this implementation to the `MiniApp.create` function.

There are some methods have a default implementation but the host app can override them to fully control.

| Method                       | Default  |
|------------------------------|----------|
| getUniqueId                  | 🚫       |
| requestPermission            | 🚫       |
| requestCustomPermissions     | ✅       |
| shareContent                 | ✅       |

The `UserInfoBridgeDispatcher`:

| Method                       | Default  |
|------------------------------|----------|
| getUserName                  | 🚫       |
| getProfilePhoto              | 🚫       |
| getAccessToken               | 🚫       |
| getContacts                  | 🚫       |

The sections below explain each feature in more detail. 

The following is a full code example of using `MiniAppMessageBridge`.

<details><summary markdown="span"><b>Click here to expand full code example of MiniAppMessageBridge</b>
</summary>

```kotlin
val miniAppMessageBridge = object: MiniAppMessageBridge() {
    override fun getUniqueId() {
        var id: String = ""
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

    // Implement requestCustomPermissions if HostApp wants to show their own UI for managing permissions
    override fun requestCustomPermissions(
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) {
        // Implementation details to request custom permissions
        // .. .. ..
        // pass a list of Pair of MiniAppCustomPermissionType and MiniAppCustomPermissionResult in callback 
        callback.invoke(list) 
    }

    override fun shareContent(
        content: String,
        callback: (isSuccess: Boolean, message: String?) -> Unit
    ) {
        // Share content implementation.
        // .. .. ..
        
        callback.invoke(true, null) // or callback.invoke(false, "error message")
    }
}

val userInfoBridgeDispatcher = object : UserInfoBridgeDispatcher() {
    override fun getUserName(): String {
        var name: String = ""
        // Implementation details to get user name
        // .. .. ..
        return name
    }

    override fun getProfilePhoto(): String {
        var profilePhotoUrl: String = ""
        // Implementation details to get profile photo url
        // .. .. ..
        return profilePhotoUrl
    }

    override fun getAccessToken(
        miniAppId: String,
        onSuccess: (tokenData: TokenData) -> Unit,
        onError: (message: String) -> Unit
    ){
        var allowToken: Boolean = false
        // Check if you want to allow this Mini App ID to use the Access Token
        // .. .. ..
        if (allowToken)
            onSuccess(tokenData) // allow miniapp to get token and return TokenData value.
        else
            onError(message)    // reject miniapp to get token and with message explanation.
    }

     override fun getContacts(
        onSuccess: (contacts: ArrayList<Contact>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        // Check if there is any contact id in HostApp
        // .. .. ..
        if (hasContact)
            onSuccess(contacts) // invoke the list of contact IDs
        else
            onError("There is no contact found in HostApp.")
    }
}

// set UserInfoBridgeDispatcher object to miniAppMessageBridge
miniAppMessageBridge.setUserInfoBridgeDispatcher(userInfoBridgeDispatcher)
```
</details>

### Unique ID
**API Docs:** [MiniAppMessageBridge.getUniqueId](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/get-unique-id.html)

Your App should provide an ID to the mini app which is unique to each user or device. The mini app can use this ID for storing session information for each user.

### Device Permission Requests

**API Docs:** [MiniAppMessageBridge.requestPermission](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/request-permission.html)

The mini app is able to request some device permissions. Your App should be able to handle requests from the mini app for the following device permissions by ensuring that the Android permission dialog is displayed. Alternatively, if your App is not able to request certain device permissions, you can just deny that permission to all mini apps.

- Location (`MiniAppPermissionType.LOCATION`)

### Custom Permission Requests
**API Docs:** [MiniAppMessageBridge.requestCustomPermissions](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/request-custom-permissions.html)

Mini apps are able to make requests for custom permission types which are defined by the Mini App SDK. These permissions include things like user data. When a mini app requests a permission, your App should display a dialog to the user asking them to accept or deny the permissions. You can also choose to always deny some permissions if your App is not capable of providing that type of data. The following custom permission types are supported:

- User name (`MiniAppCustomPermissionType.USER_NAME`)
- Profile photo (`MiniAppCustomPermissionType.PROFILE_PHOTO`)
- Contact list (`MiniAppCustomPermissionType.CONTACT_LIST`)

**Note:** The Mini App SDK has a default UI built-in for the custom permission dialog, but you can choose to override this and use a custom UI. The Mini App SDK will handle caching the permission accept/deny state, and your `requestCustomPermission` function will only receive permissions which have not yet been granted to the mini app.

### Share Content
**API Docs:** [MiniAppMessageBridge.shareContent](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/request-custom-permissions.html)

The mini app can share text content to either your App or another App. The default functionality for this will create a `text` type `Intent` which shows the Android chooser and allows the user to share the content to any App which accepts text.

You can also choose to override the default functionality and instead share the text content to some feature within your own App.

### User Info
**API Docs:** [MiniAppMessageBridge.setUserInfoBridgeDispatcher](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/set-user-info-bridge-dispatcher.html)

The mini app is able to request data about the current user from your App. Each of these types of data is associated with a `MiniAppCustomPermissionType`  (except where noted). The mini app should have requested the permission before requesting the user data type. Note that the Mini App SDK will handle making sure that the permission has been granted, so if the permission has not been granted, then these functions will not be called within your App.

The following user data types are supported. If your App does not support a certain type of data, you do not have to implement the function for it.

- User name: string representing the user's name. See [UserInfoBridgeDispatcher.getUserName](api/com.rakuten.tech.mobile.miniapp.js.userinfo/-user-info-bridge-dispatcher/get-user-name.html)
- Profile photo: URL pointing to a photo. This can also be a Base64 data string. See [UserInfoBridgeDispatcher.getProfilePhoto](api/com.rakuten.tech.mobile.miniapp.js.userinfo/-user-info-bridge-dispatcher/get-profile-photo.html)
- Access Token (does not currenlty have a custom permission type): OAuth 1.0 token including token data and expiration date. Your App will be provided with the ID of the mini app which is requesting the Access Token, so you should verify that this mini app is allowed to use the access token. See See [UserInfoBridgeDispatcher.getAccessToken](api/com.rakuten.tech.mobile.miniapp.js.userinfo/-user-info-bridge-dispatcher/get-access-token.html)


### Ads Integration
**API Docs:** [MiniAppMessageBridge.setAdMobDisplayer](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/set-ad-mob-displayer.html)

It is optional to set AdMob for mini apps to show advertisement.
The below implementation will allow ads to be shown when mini apps trigger a request.

Configure the Android Ads SDK from [here](https://developers.google.com/admob/android/quick-start). Don't forget to [initialize the Ads SDK](https://developers.google.com/admob/android/quick-start#initialize_the_mobile_ads_sdk).

#### AdMob
**API Docs:** [AdMobDisplayer](api/com.rakuten.tech.mobile.miniapp.ads/-ad-mob-displayer/)

Set the `AdMobDisplayer` provided by MiniApp SDK. This controller will handle the display of ad so no work is required from host app.
```kotlin
miniAppMessageBridge.setAdMobDisplayer(AdMobDisplayer(activityContext))
``` 

#### Custom Ads Provider
**API Docs:** [MiniAppAdDisplayer](com.rakuten.tech.mobile.miniapp.ads/-mini-app-ad-displayer/)

In case the host app wants to take control of the ad display, there is the interface `MiniAppAdDisplayer` to implement.
```kotlin
class CustomAdDisplayer: MiniAppAdDisplayer { 

    override fun loadInterstitialAd(adUnitId: String, onLoaded: () -> Unit, onFailed: (String) -> Unit) {
      // load the ad
    }
    
    override fun showInterstitialAd(adUnitId: String, onClosed: () -> Unit, onFailed: (String) -> Unit) {
      // show the ad
    }
    //...more ad implementations.
}

miniAppMessageBridge.setAdMobDisplayer(CustomAdDisplayer())
```

### Screen Orientation
**API Docs:** [MiniAppMessageBridge.allowScreenOrientation](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/)

The default setting does not allow miniapp to change hostapp screen orientation.
Hostapp can allow miniapp to control the screen orientation for better experience by calling 

```kotlin
miniAppMessageBridge.allowScreenOrientation(true)
```

In case miniapp is allowed to control, please ensure that your activity handles screen orientation.
There are several ways to prevent the view from being reset.
In our Demo App, we set the config on activity `android:configChanges="orientation|screenSize"`.
See [here](https://developer.android.com/guide/topics/resources/runtime-changes#HandlingTheChange).

## Fetching Mini App Info
**API Docs:** [MiniApp.listMiniApp](api/com.rakuten.tech.mobile.miniapp/-mini-app/list-mini-app.html), [MiniApp.fetchInfo](api/com.rakuten.tech.mobile.miniapp/-mini-app/fetch-info.html), [MiniAppInfo](api/com.rakuten.tech.mobile.miniapp/-mini-app-info/)

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

## Advanced Features

### Clearing up mini app display
**API Docs:** [MiniAppDisplay.destroyView](api/com.rakuten.tech.mobile.miniapp/-mini-app-display/destroy-view.html)

For a mini app, it is required to destroy necessary view state and any services registered with.
The automatic way can be used only if we want to end the `Activity` container along with mini app display.  `MiniAppDisplay` complies to Android's `LifecycleObserver` contract. It is quite easy to setup for automatic clean up of resources.

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

### Navigating inside a mini app
**API Docs:** [MiniAppDisplay.navigateBackward](api/com.rakuten.tech.mobile.miniapp/-mini-app-display/navigate-backward.html), [MiniAppDisplay.navigateForward](api/com.rakuten.tech.mobile.miniapp/-mini-app-display/navigate-forward.html)

`MiniAppDisplay.navigateBackward` and `MiniAppDisplay.navigateForward` facilitates the navigation inside a mini app if the history stack is available in it. A common usage pattern could be to link it up to the Android Back Key navigation.

```kotlin
override fun onBackPressed() {
    if(!miniAppDisplay.navigateBackward()) {
        super.onBackPressed()
    }
}
```

### External url loader
**API Docs:** [MiniAppNavigator](api/com.rakuten.tech.mobile.miniapp.navigator/), [MiniAppExternalUrlLoader](api/com.rakuten.tech.mobile.miniapp.navigator/-mini-app-external-url-loader/), [ExternalResultHandler](api/com.rakuten.tech.mobile.miniapp.navigator/-external-result-handler/)

The mini app is loaded with the specific custom scheme and custom domain in mini app view.

In default, the external link will be opened in custom tab. See [this](https://developers.google.com/web/android/custom-tabs).

HostApp also can implement their own way by passing `MiniAppNavigator` object to `MiniApp.create(appId: String, miniAppMessageBridge: MiniAppMessageBridge, miniAppNavigator: MiniAppNavigator)`.

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
In preview mode, using `MiniApp.instance().create(MINI_APP_INFO, miniAppMessageBridge, miniAppNavigator)`.

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

### Custom Permissions
**API Docs:** [MiniApp.getCustomPermissions](api/com.rakuten.tech.mobile.miniapp/-mini-app/get-custom-permissions.html), [MiniApp.setCustomPermissions](api/com.rakuten.tech.mobile.miniapp/-mini-app/set-custom-permissions.html), [MiniApp.listDownloadedWithCustomPermissions](api/com.rakuten.tech.mobile.miniapp/-mini-app/list-downloaded-with-custom-permissions.html)

MiniApp Android SDK supports list of Custom Permissions (`MiniAppCustomPermissionType`) and these can be stored and retrieved using the following public interfaces.

#### Retrieving the Mini App Custom Permissions using MiniAppID
Custom permissions and its status can be retrieved using the following interface. `getCustomPermissions` will return `MiniAppCustomPermission` that contains the meta-info as a `Pair` of 
name and grant result (`ALLOWED` or `DENIED`). The custom permissions are stored per each miniAppId.
```kotlin
val permissions = miniapp.getCustomPermissions(miniAppId)
```

#### Store the Mini App Custom Permissions
Custom permissions for a mini app are cached by the SDK and you can use the following interface to store permissions when needed.
```kotlin
var permissionPairs = listOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()
// .. .. ..
val permissionsToSet = MiniAppCustomPermission(miniAppId, permissionPairs)
miniapp.setCustomPermissions(permissionsToSet)
```

#### List of downloaded Mini apps with Custom Permissions
```kotlin
val downloadedMiniApps = miniapp.listDownloadedWithCustomPermissions()
downloadedMiniApps.forEach {
    val miniApp = it.first
    val permissions = it.second
    // Display permissions in view, etc....
}
```

## Troubleshooting & FAQs

<details><summary markdown="span"><b>Exception: "Network requests must not be performed on the main thread."</b>
</summary>

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
</details>

<details><summary markdown="span"><b>How do I use snapshot versions of this SDK?</b>
</summary>

We may periodically publish snapshot versions for testing pre-release features. These versions will always end in `-SNAPSHOT`, for example `1.0.0-SNAPSHOT`. If you wish to use a snapshot version, you will need to add the snapshot repo to your Gradle configuration.

```
repositories {
    maven { url 'http://oss.jfrog.org/artifactory/simple/libs-snapshot/' }
}

dependency {
    implementation 'com.rakuten.tech.mobile.miniapp:miniapp:X.X.X-SNAPSHOT'
}
```
</details>

## Changelog

See the full [CHANGELOG](https://github.com/rakutentech/android-miniapp/blob/master/CHANGELOG.md).
