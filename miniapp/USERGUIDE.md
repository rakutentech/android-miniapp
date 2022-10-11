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

- **Minimum Android Version**: This SDK supports Android 9.0+ (API level 28+).
    - Note: Currently this SDK is set to `minSdkVersion 24`, however support for versions 24 to 27 is deprecated and could be removed in a later release.
- **Base URL, App ID, Subscription Key**: We don't currently provide a public API for use with this SDK. You must provide a URL for your API as well as an App ID and Subscription Key for the API.

## Getting Started

This section will guide you through setting up the Mini App SDK in your App and you will learn how to display your first mini app.

### #1 Add dependency to your App

Add the following to your `build.gradle` file:

```groovy
repositories {
    mavenCentral()
}

dependency {
    implementation 'io.github.rakutentech.miniapp:miniapp:${version}'
}
```

### #2 Configure SDK settings in AndroidManifest.xml

The SDK is configured via `meta-data` tags in your `AndroidManifest.xml`. The following table lists the configurable values.

| Field                        | Datatype| Manifest Key                                           | Optional   | Default  |
|------------------------------|---------|--------------------------------------------------------|----------- |--------- |
| Base URL                     | String  | `com.rakuten.tech.mobile.miniapp.BaseUrl`              | ❌         | 🚫        |
| Is Preview Mode              | Boolean | `com.rakuten.tech.mobile.miniapp.IsPreviewMode`        | ✅         | false      |
| Require Signature Verification      | Boolean | `com.rakuten.tech.mobile.miniapp.RequireSignatureVerification`    | ✅         | false      |
| RAS Project ID               | String  | `com.rakuten.tech.mobile.ras.ProjectId`                | ❌         | 🚫        |
| RAS Project Subscription Key | String  | `com.rakuten.tech.mobile.ras.ProjectSubscriptionKey`   | ❌         | 🚫        |
| Host App User Agent Info     | String  | `com.rakuten.tech.mobile.miniapp.HostAppUserAgentInfo` | ✅         | 🚫        |
| Enable [Ad Placement api](https://developers.google.com/ad-placement)      | Boolean | `com.rakuten.tech.mobile.miniapp.EnableH5Ads`          | ✅         | false     |
| Secure Storage Max Size     | String  | `com.rakuten.tech.mobile.miniapp.MaxStorageSizeLimitInBytes` | ✅         | ✅          |

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

        <!-- Using for enabling SDK's settings to verify the signature of the Mini Apps -->
        <meta-data
            android:name="com.rakuten.tech.mobile.miniapp.RequireSignatureVerification"
            android:value="${requireSignatureVerification}" />

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

`MiniApp.create` is used to create a `View` for displaying a specific mini app. Before calling `MiniApp.create`, the Host App should first get the manifest using `MiniApp.getMiniAppManifest`, show permission prompt to user, then set the result with `MiniApp.setCustomPermissions`.
If Host App wants to launch/download the miniapp without granting the required permissions, the SDK will throw `RequiredPermissionsNotGrantedException` to notify Host App.
You must provide the mini app ID which you wish to create (you can get the mini app ID by [Fetching Mini App Info](#fetching-mini-app-info) first). Calling `MiniApp.create` will do the following:

- Check what is the latest, published version of the mini app.
- Check if the latest version of the mini app has been downloaded.
    - If yes, return the already downloaded mini app.
    - If no, download the latest version and then return the downloaded version.
- If the device is disconnected from the internet and the device already has a version of the mini app downloaded, then the already downloaded version will be returned.
- If the host app needs the cached version of the miniapp, set `fromCache` value to `true` will return already downloaded version.

After calling `MiniApp.create` and all the "required" manifest permissions have been granted, you will obtain an instance of `MiniAppDisplay` which represents the downloaded mini app. You can call `MiniAppDisplay.getMiniAppView` to obtain a `View` for displaying the mini app.

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
        override fun getUniqueId(
            onSuccess: (uniqueId: String) -> Unit,
            onError: (message: String) -> Unit
        ) {
            if (...)
                onSuccess("your-unique-id")
            else
                onError("your-error-message")
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

### #4 Create and display multiple Mini App views
**API Docs:** [MiniAppView.init](api/com.rakuten.tech.mobile.miniapp.view/-mini-app-view/init.html), [MiniAppConfig](api/com.rakuten.tech.mobile.miniapp.view/-mini-app-config.html)
`MiniAppView.init` is used to initialize the mini app view for displaying a specific mini app using `MiniAppDisplay` when `MiniAppView.load` is being called.  `MiniAppParameters` needed to send with the `MiniAppView.init` call. 

Before calling `MiniAppView.init`, the Host App should first get the manifest using `MiniApp.getMiniAppManifest`, show permission prompt to user, then set the result with `MiniApp.setCustomPermissions`.
If Host App wants to launch the miniapp without granting the required permissions, the SDK will throw `RequiredPermissionsNotGrantedException` to notify Host App.
You must provide `MiniAppConfig` and `Context` while initializing `MiniAppView`. You must provide the mini app ID which you wish to create (you can get the mini app ID by [Fetching Mini App Info](#fetching-mini-app-info) first). 
Calling `MiniAppView.load` will do the following:
- Check what is the latest, published version of the mini app.
- Check if the latest version of the mini app has been downloaded.
  - If yes, return the already downloaded mini app.
  - If no, download the latest version and then return the downloaded version.
- If the device is disconnected from the internet and the device already has a version of the mini app downloaded, then the already downloaded version will be returned.
- If the host app needs the cached version of the miniapp, set `fromCache` value to `true` will return already downloaded version.

After calling `MiniAppView.load` and all the "required" manifest permissions have been granted, you will obtain an instance of `MiniAppDisplay` which represents the downloaded mini app. You can call `MiniAppDisplay.getMiniAppView` to obtain a `View` for displaying the mini app.

The following is a simplified example:
```kotlin
val param = MiniAppParameters.DefaultParams(
            context = this,
            config = MiniAppConfig(
                miniAppSdkConfig = miniAppSdkConfig,
                miniAppMessageBridge = miniAppMessageBridge,
                miniAppNavigator = miniAppNavigator,
                miniAppFileChooser = miniAppFileChooser,
                queryParams = ""
            ),
            miniAppId = "id",
            miniAppVersion = "version",
            fromCache = false
        )
        
val miniapp = MiniAppView.init(param)
```
To load the miniapp following load function needed to call
```kotlin
miniapp.load { miniAppDisplay ->
    val miniAppView = miniAppDisplay.getMiniAppView(this@MiniAppActivity)
    // view could be added to show the miniapp           
}
```

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
| requestDevicePermission      | 🚫       |
| requestCustomPermissions     | ✅       |
| shareContent                 | ✅       |
| getHostEnvironmentInfo       | ✅       |

The `UserInfoBridgeDispatcher`:

| Method                       | Default  |
|------------------------------|----------|
| getUserName                  | 🚫       |
| getProfilePhoto              | 🚫       |
| getAccessToken               | 🚫       |
| getContacts                  | 🚫       |
| getPoints                    | 🚫       |

The `ChatBridgeDispatcher`:

| Method                       | Default  |
|------------------------------|----------|
| sendMessageToContact         | 🚫       |
| sendMessageToContactId       | 🚫       |
| sendMessageToMultipleContacts| 🚫       |

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

    override fun requestDevicePermission(
        miniAppPermissionType: MiniAppDevicePermissionType,
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

    override fun getHostEnvironmentInfo(
        onSuccess: (info: HostEnvironmentInfo) -> Unit,
        onError: (infoError: HostEnvironmentInfoError) -> Unit
    ) {
        // Check if there is any environment info in HostApp
        if (hasInfo) {
            // allow miniapp to invoke the host environment info
            onSuccess(hostEnvironmentInfo)
        }
        else
            onError(hostEnvironmentError) // reject miniapp to send host environment info with message explanation.
    }
}

val userInfoBridgeDispatcher = object : UserInfoBridgeDispatcher {

    override fun getUserName(
        onSuccess: (userName: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val name: String?
        // Check if there is any valid username in HostApp
        // .. .. ..
        if (isNameValid) // Check if name is valid
            onSuccess(name) // allow miniapp to get user name.
        else
            onError(message) // reject miniapp to get user name with message explanation.
    }

    override fun getProfilePhoto(
        onSuccess: (profilePhoto: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val photoUrl: String?
        // Check if there is any valid photo url in HostApp
        // .. .. ..
        if (isPhotoUrlValid) // Check if photoUrl is valid
            onSuccess(photoUrl) // allow miniapp to get photo url.
        else
            onError(message) // reject miniapp to get photo url with message explanation.
    }

    override fun getAccessToken(
        miniAppId: String,
        accessTokenScope: AccessTokenScope,
        onSuccess: (tokenData: TokenData) -> Unit,
        onError: (tokenError: MiniAppAccessTokenError) -> Unit
    ) {
        var allowToken: Boolean = false
        // Check if you want to allow this Mini App ID to use the Access Token based on AccessTokenScope.
        // .. .. ..
        if (allowToken)
            onSuccess(tokenData) // allow miniapp to get token and return TokenData value.
        else
            onError(tokenError) // reject miniapp to get token with specific access token error type.
    }

    override fun getContacts(
        onSuccess: (contacts: ArrayList<Contact>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        // Check if there is any contact id in HostApp
        // .. .. ..
        if (hasContact)
            onSuccess(contacts) // allow miniapp to get contacts.
        else
            onError(message) // reject miniapp to get contacts with message explanation.
    }

    override fun getPoints(
        onSuccess: (points: Points) -> Unit,
        onError: (pointsError: MiniAppPointsError) -> Unit
    ) {
        // Check if there is any point in HostApp
        // .. .. ..
        if (hasPoints)
            onSuccess(points) // allow miniapp to get points.
        else
            onError(pointsError) // reject miniapp to get points with message explanation.
    }
}

// set UserInfoBridgeDispatcher object to miniAppMessageBridge
miniAppMessageBridge.setUserInfoBridgeDispatcher(userInfoBridgeDispatcher)

val chatBridgeDispatcher = object : ChatBridgeDispatcher {

    override fun sendMessageToContact(
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        // Check if there is any contact in HostApp
        // .. .. ..
        if (hasContact) {
            // You can show a contact selection UI for picking a single contact.
            // .. .. ..
            // allow miniapp to invoke after message has been sent,
            // user can invoke null when cancelling the operation.
            onSuccess(contactId)
        }
        else
            onError(message) // reject miniapp to send message with message explanation.
    }

    override fun sendMessageToContactId(
        contactId: String,
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (there is contact id) {
            // You can show a UI with the message content and the contactId.
            // .. .. ..
            // allow miniapp to invoke after message has been sent,
            // user can invoke null when cancelling the operation.
            onSuccess(contactId)
        }
        else
            onError(message) // reject miniapp to send message with message explanation.
    }

    override fun sendMessageToMultipleContacts(
        message: MessageToContact,
        onSuccess: (contactIds: List<String>?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        // Check if there is any contact in HostApp
        // .. .. ..
        if (hasContact) {
            // You can show a contact selection UI for picking a single contact.
            // .. .. ..
            // allow miniapp to invoke the contact ids where message has been sent,
            // user can invoke null when cancelling the operation.
            onSuccess(contactIds)
        }
        else
            onError(message) // reject miniapp to send message with message explanation.
    }
}

// set ChatBridgeDispatcher object to miniAppMessageBridge
miniAppMessageBridge.setChatBridgeDispatcher(chatBridgeDispatcher)
```
</details>

### Unique ID
**API Docs:** [MiniAppMessageBridge.getUniqueId](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/get-unique-id.html)

Your App should provide an ID to the mini app which is unique to each user or device. The mini app can use this ID for storing session information for each user.

### Device Permission Requests

**API Docs:** [MiniAppMessageBridge.requestPermission](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/request-permission.html)

The mini app is able to request some device permissions. Your App should be able to handle requests from the mini app for the following device permissions by ensuring that the Android permission dialog is displayed. Alternatively, if your App is not able to request certain device permissions, you can just deny that permission to all mini apps.

- Location (`MiniAppPermissionType.LOCATION`) (`MiniAppPermissionType` has been deprecated)
- Location (`MiniAppDevicePermissionType.LOCATION`)

### Custom Permission Requests
**API Docs:** [MiniAppMessageBridge.requestCustomPermissions](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/request-custom-permissions.html)

Mini apps are able to make requests for custom permission types which are defined by the Mini App SDK. These permissions include things like user data. When a mini app requests a permission, your App should display a dialog to the user asking them to accept or deny the permissions. You can also choose to always deny some permissions if your App is not capable of providing that type of data. The following custom permission types are supported:

- User name (`MiniAppCustomPermissionType.USER_NAME`)
- Profile photo (`MiniAppCustomPermissionType.PROFILE_PHOTO`)
- Contact list (`MiniAppCustomPermissionType.CONTACT_LIST`)

**Note:** The Mini App SDK has a default UI built-in for the custom permission dialog, but you can choose to override this and use a custom UI. The Mini App SDK will handle caching the permission accept/deny state, and your `requestCustomPermission` function will only receive permissions which have not yet been granted to the mini app.

### Share Content
**API Docs:** [MiniAppMessageBridge.shareContent](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/share-content.html)

The mini app can share text content to either your App or another App. The default functionality for this will create a `text` type `Intent` which shows the Android chooser and allows the user to share the content to any App which accepts text.

You can also choose to override the default functionality and instead share the text content to some feature within your own App.

### Host Environment Info
**API Docs:** [MiniAppMessageBridge.getHostEnvironmentInfo](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/get-host-environment.html)

The default functionality will provide information using `HostEnvironmentInfo` object to Mini App. Also, Host App can send it's environment information by implementing this function.

### User Info
**API Docs:** [MiniAppMessageBridge.setUserInfoBridgeDispatcher](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/set-user-info-bridge-dispatcher.html)

The mini app is able to request data about the current user from your App. Each of these types of data is associated with a `MiniAppCustomPermissionType`  (except where noted). The mini app should have requested the permission before requesting the user data type. Note that the Mini App SDK will handle making sure that the permission has been granted, so if the permission has not been granted, then these functions will not be called within your App.

The following user data types are supported. If your App does not support a certain type of data, you do not have to implement the function for it.

- User name: string representing the user's name. See [UserInfoBridgeDispatcher.getUserName](api/com.rakuten.tech.mobile.miniapp.js.userinfo/-user-info-bridge-dispatcher/get-user-name.html)
- Profile photo: URL pointing to a photo. This can also be a Base64 data string. See [UserInfoBridgeDispatcher.getProfilePhoto](api/com.rakuten.tech.mobile.miniapp.js.userinfo/-user-info-bridge-dispatcher/get-profile-photo.html)
- Access Token: OAuth 1.0 token including token data and expiration date. Your App will be provided with the ID of the mini app and [AccessTokenScope]((api/com.rakuten.tech.mobile.miniapp.permission/-access-token-scope)) which is requesting the Access Token, so you should verify that this mini app is allowed to use the access token. See [UserInfoBridgeDispatcher.getAccessToken](api/com.rakuten.tech.mobile.miniapp.js.userinfo/-user-info-bridge-dispatcher/get-access-token.html)

### Ads Integration
In case the host app wants to use the admob sdk, Add the following to your `build.gradle` file:
```groovy
dependency {
    implementation project(':admob-latest')
}
``` 

**Admob Version**

`admob-latest` module use Google Play Services Ads version 20.2.0.

**API Docs:** [MiniAppMessageBridge.setAdMobDisplayer](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/set-ad-mob-displayer.html)

It is optional to set AdMob for mini apps to show advertisement.
The below implementation will allow ads to be shown when mini apps trigger a request.

Configure the Android Ads SDK from [here](https://developers.google.com/admob/android/quick-start). Don't forget to [initialize the Ads SDK](https://developers.google.com/admob/android/quick-start#initialize_the_mobile_ads_sdk).

**Note:** We only support AdMob usage on Android 7.0+. Some ads from AdMob have inconsistent behavior on Android 6.0 due to the older webview implementation on those devices.

#### AdMob
**API Docs:** [AdMobDisplayer](api/com.rakuten.tech.mobile.miniapp.ads/-ad-mob-displayer/)

Set the `AdMobDisplayer` provided by `admob-latest`. This controller will handle the display of ad so no work is required from host app.
```kotlin
miniAppMessageBridge.setAdMobDisplayer(AdMobDisplayer(activityContext))
```

### Send Native Events
**API Docs:** [miniAppMessageBridge.dispatchNativeEvent](api/com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/dispatch-native-event.html)

Mini apps are able to get events for custom event types which are defined by the Mini App SDK. These events include things like external webview close, pause, resume.

- External webview close (`NativeEventType.EXTERNAL_WEBVIEW_CLOSE`)
- Pause (`NativeEventType.MINIAPP_ON_PAUSE`)
- Resume (`NativeEventType.MINIAPP_ON_RESUME`)

**Note:** Host app can send these events whenever these events occur and MiniApp will be able to get those events.

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
        Log.e("MiniApp", "There was an error when retrieving the Mini App info", e)
    }
}
```

## Fetching Mini App Meta data
**API Docs:** [MiniApp.getMiniAppManifest](api/com.rakuten.tech.mobile.miniapp/-mini-app/get-mini-app-manifest.html)

MiniApp developers need to add the following attributes in manifest.json. Host App can require Mini Apps to set meta data here using "customMetaData", such as the first time launch screen options.

```json
{
   "reqPermissions":[
      {
         "name":"rakuten.miniapp.user.USER_NAME",
         "reason":"Describe your reason here."
      },
      {
         "name":"rakuten.miniapp.user.PROFILE_PHOTO",
         "reason":"Describe your reason here."
      }
   ],
   "optPermissions":[
      {
         "name":"rakuten.miniapp.user.CONTACT_LIST",
         "reason":"Describe your reason here."
      },
      {
         "name":"rakuten.miniapp.device.LOCATION",
         "reason":"Describe your reason here."
      }
   ],
   "customMetaData":{
      "hostAppRandomTestKey":"metadata value"
   }
}
```

In Host App, we can get the manifest information as following:

```kotlin
CoroutineScope(Dispatchers.IO).launch {
    try {
        val miniAppManifest = MiniApp.instance().getMiniAppManifest(
                                    appId = "MINI_APP_ID",
                                    versionId = "VERSION_ID",
                                    languageCode = "ja"
                              )

        // Host App can set it's own metadata key in manifest.json to retrieve the value
        miniAppManifest.customMetaData["hostAppRandomTestKey"]
    } catch(e: MiniAppSdkException) {
        Log.e("MiniApp", "There was an error when retrieving the Mini App manifest", e)
    }
}
```
By passing the `languageCode` e.g. `en`, `ja` in the above `getMiniAppManifest` method, you can get the localized description/reason for the permission from the platform API.
If there is no localized description/reason is available, it will return the default value given in the `manifest.json`.

## Getting downloaded Mini App Meta data

In Host App, we can get the downloaded manifest information as following:

```kotlin
  val downloadedManifest = MiniApp.instance().getDownloadedManifest("MINI_APP_ID")
```

## Send message to contacts
**API Docs:** [ChatBridgeDispatcher](api/com.rakuten.tech.mobile.miniapp.js.chat/-chat-bridge-dispatcher/)

Send a message to a single contact, multiple contacts or to a specific contact id by using the following three methods can be triggered by the Mini App, and here are the recommended behaviors for each one:

| |`ChatBridgeDispatcher.sendMessageToContact`|`ChatBridgeDispatcher.sendMessageToContactId`|`ChatBridgeDispatcher.sendMessageToMultipleContacts`|
|---|---|---|---|
|**Triggered when**|Mini App wants to send a message to a contact.|Triggered when Mini App wants to send a message to a specific contact.|Triggered when Mini App wants to send a message to multiple contacts. |
| **Contact chooser needed** | single contact | None | multiple contacts |
| **Action** | send the message to the chosen contact | send a message to the specific contact id without any prompt to the user | send the message to multiple chosen contacts |
| **On success** | invoke onSuccess with the ID of the contact where the message was sent. | invoke onSuccess with the ID of the contact where the message was sent. | invoke onSuccess with a list of IDs of the contacts where the message was sent. |
| **On cancellation** | invoke onSuccess null value. | invoke onSuccess null value. | invoke onSuccess null value. |
| **On error** | invoke onError when there was an error. | invoke onError when there was an error. | invoke onError when there was an error. |

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

#### Downloading files in a Mini App

You can also optionally use your `MiniAppNavigator` to intercept file download requests from the Mini App. Note that this will only receive files that the Mini App downloaded with XHR, so the URL you receive will be a Base64 data URI. If you do not override the file download functionality, then the SDK will use default functionality to create an Intent for sharing the file to another App.

To use this, you must implement the `MiniAppDownloadNavigator` interface in your `MiniAppNavigator` implementation.

```kotlin
miniAppNavigator = object : MiniAppNavigator, MiniAppDownloadNavigator {
    // Override MiniAppNavigator functions here
    // ...

    // Override MiniAppDownloadNavigator functions
    fun onFileDownloadStart(url: String, userAgent: String, contentDisposition: String, mimetype: String, contentLength: Long) {
        // Decode URL as Base 64 and then use it somehow - i.e. save to device, share to another App, etc.
    }
}
```

### File choosing
**API Docs:** [MiniAppFileChooser](api/com.rakuten.tech.mobile.miniapp.file/-mini-app-file-chooser/)

The mini app is able to choose the file which is requested using HTML forms with 'file' input type whenever users press a "Select file" button.
HostApp can use a default class provided by the SDK e.g. `MiniAppFileChooserDefault` to choose the files.
- At first, HostApp needs to initiate `MiniAppFileChooserDefault` in the `Activity`.

```kotlin
val fileChoosingReqCode = REQUEST_CODE // define a request code in HostApp
val miniAppFileChooser = MiniAppFileChooserDefault(requestCode = fileChoosingReqCode)
```

- Then, HostApp activity can receive the files at `onActivityResult` as following:

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    // HostApp can cancel the file choosing operation when resultCode doesn't match
    if (Activity.RESULT_OK != resultCode) {
         miniAppFileChooser.onCancel()
    }

    if (requestCode == fileChoosingReqCode && resultCode == Activity.RESULT_OK) {
        data?.let { intent ->
            miniAppFileChooser.onReceivedFiles(intent)
        }
    }
}
```

Alternatively, HostApp can use `MiniAppFileChooser` interface to override `onShowFileChooser` for customizing file choosing mode and other options.

```kotlin
val miniAppFileChooser = object : MiniAppFileChooser {

        override fun onShowFileChooser(
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: WebChromeClient.FileChooserParams?,
            context: Context
        ): Boolean {
           // write own implementation here.
        }
    }
```

In both case, HostApp needs to pass `MiniAppFileChooser` through `MiniApp.create(appId: String, miniAppMessageBridge: MiniAppMessageBridge, miniAppFileChooser: MiniAppFileChooser)`.

In case, MiniApp needs to check camera permission or request camera access from HostApp.

```kotlin
val miniAppCameraPermissionDispatcher = object : MiniAppCameraPermissionDispatcher {
            
            override fun getCameraPermission(permissionCallback: (isGranted: Boolean) -> Unit) {
               // Check the camera permission of the Device and send it back - i.e. isGranted = true/false.
               if (DeviceCameraPermissionGranted)
                permissionCallback(true)
               else
                permissionCallback(false)
            }

            override fun requestCameraPermission(
                miniAppPermissionType: MiniAppDevicePermissionType,
                callback: (isGranted: Boolean) -> Unit
            ) {
                // Request the camera permission of the Device send send it back through callback.
                callback.invoke(true)
            }
        }
```

Dispatch the `miniAppCameraPermissionDispatcher` with the `MiniAppFileChooserDefault`.

```kotlin
val fileChoosingReqCode = REQUEST_CODE // define a request code in HostApp
val miniAppFileChooser = MiniAppFileChooserDefault(
        requestCode = fileChoosingReqCode,
        miniAppCameraPermissionDispatcher = miniAppCameraPermissionDispatcher
)
```
`miniAppCameraPermissionDispatcher` is optional, No need to implement this if HostApp doesn't have camera permission in manifest or miniapp doesn't need to access camera.

### File Downloading
**API Docs:** [MiniAppFileDownloader](api/com.rakuten.tech.mobile.miniapp.file/-mini-app-file-downloader/)

The mini app is able to downlad a file on the local storage. HostApp can use a default class provided by the SDK e.g. `MiniAppFileDownloaderDefault` to download the files.
- At first, HostApp needs to initiate `MiniAppFileDownloaderDefault` in the `Activity`.

```kotlin
val fileDownloadReqCode = REQUEST_CODE // define a request code in HostApp
val MiniAppFileDownloader = MiniAppFileDownloaderDefault(activity, requestCode = fileDownloadReqCode)
```

- Then set the `miniappFileDownloader` with the `miniAppMessageBridge`.
```kotlin
miniAppMessageBridge.setMiniAppFileDownloader(miniAppFileDownloader)
```
- Then, HostApp activity can receive the file Uri on `onActivityResult` and pass the Uri to download the file.
```kotlin
if (requestCode == fileDownloadReqCode) {
    intent?.data?.let { destinationUri ->
        miniAppFileDownloader.onReceivedResult(destinationUri)
    }
}
```
Host app can also implement their own `miniAppFileDownloader` by implementing the `MiniAppFileDownloader` interface.
```kotlin
val miniAppFileDownloader = object : MiniAppFileDownloader{
    override fun onStartFileDownload(
        fileName: String,
        url: String,
        headers: Map<String, String>,
        onDownloadSuccess: (String) -> Unit,
        onDownloadFailed: (MiniAppDownloadFileError) -> Unit
    ) {
           //.. Download the file
      }
}
```
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

### Passing parameters to a miniapp

For a mini app, you can pass query parameters as String using `MiniApp.create` to be appended with miniapp's url.
For example: `https://mscheme.1234/miniapp/index.html?param1=value1&param2=value2`

```kotlin
class MiniAppActivity : Activity(), CoroutineScope {

    override fun onCreate(savedInstanceState: Bundle?) {
    //...
        launch {
            val miniAppDisplay = withContext(Dispatchers.IO) {
                MiniApp.instance().create(
                    appId = "mini_app_id",
                    miniAppMessageBridge = miniAppMessageBridge,
                    miniAppNavigator = miniAppNavigator,
                    queryParams = "param1=value1&param2=value2"
                )
            }
    //...
        }
    }
}
```

### Analytics events

When [Analytics SDK](https://github.com/rakutentech/android-analytics) is integrated, MiniApp SDK sends analytics data from your app when some events are triggered:

- HostApp starts with MiniApp SDK.
- When miniapp is launched.
- When miniapp is closed.

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

<details><summary markdown="span"><b>Exception: MiniAppVerificationException</b>
</summary>

This exception will be thrown when the SDK cannot verify the security check on local storage using keystore which means that users are not allowed to use miniapp.
Some keystores within devices are tampered or OEM were shipped with broken keystore from the beginning.

</details>

<details><summary markdown="span"><b>Build Error: `java.lang.RuntimeException: Duplicate class com.rakuten.tech.mobile.manifestconfig.annotations.ManifestConfig`</b>
</summary>

This build error could occur if you are using older versions of other libraries from `com.rakuten.tech.mobile`.
Some of the dependencies in this SDK have changed to a new Group ID of `io.github.rakutentech` (due to the [JCenter shutdown](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/)).
This means that if you have another library in your project which depends on the older dependencies using the Group ID `com.rakuten.tech.mobile`, then you will have duplicate classes.

To avoid this, please add the following to your `build.gradle` in order to exclude the old `com.rakuten.tech.mobile` dependencies from your project.

```groovy
configurations.all {
    exclude group: 'com.rakuten.tech.mobile', module: 'manifest-config-processor'
    exclude group: 'com.rakuten.tech.mobile', module: 'manifest-config-annotations'
}

```

</details>

<details><summary markdown="span"><b>How do I use snapshot versions of this SDK?</b>
</summary>

We may periodically publish snapshot versions for testing pre-release features. These versions will always end in `-SNAPSHOT`, for example `1.0.0-SNAPSHOT`. If you wish to use a snapshot version, you will need to add the snapshot repo to your Gradle configuration.

```
repositories {
    maven { url 'https://s01.oss.sonatype.org/content/repositories/snapshots/' }
}

dependency {
    implementation 'io.github.rakutentech.miniapp:miniapp:X.X.X-SNAPSHOT'
}
```
</details>

<details><summary markdown="span"><b>How do I deep link to mini apps?</b>
</summary>

If you want to have deep links direclty to your mini apps, then you must implement deep link handling within your App. This can be done using either a custom deep link scheme (such as `myAppName://miniapp`) or an [App Link](https://developer.android.com/training/app-links) (such as `https://www.example.com/miniapp`). See the following Android Developer resources for more information on how to implement deep linking capabilities:

- [Create Deep Links to App Content](https://developer.android.com/training/app-links/deep-linking)
- [Handling App Links](https://developer.android.com/training/app-links)
- [Verify Android App Links](https://developer.android.com/training/app-links/verify-site-associations)

After you have implemented deep linking capabilities in your App, then you can configure your deep link to open and launch a Mini App. Note that your deep link should contain information about which mini app ID to open. Also, you can pass query parameters and a URL fragment to the mini app. The recommended deep link format is similar to `https://www.example.com/miniapp/MINI_APP_ID?myParam=myValue#myFragment` where the `myParam=myValue#myFragment` portion is optional and will be passed directly to the mini app. 

The following is an example which will parse the mini app ID and query string from a deep link intent:

```kotlin
// In your main Activity
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    val action: String? = intent?.action
    val uri: Uri? = intent?.data
    
    if (action == Intent.ACTION_VIEW && uri != null) {
        handleDeepLink(uri)
    }
}

fun handleDeepLink(uri: Uri) {
    if (uri.path?.startsWith("/miniapp") == true && uri.lastPathSegment != null) {
        val miniAppId = uri.lastPathSegment
        val queryParams = uri.query ?: ""
        val queryFragment = uri.fragment ?: ""
        val query = "$queryParams#$queryFragment"

        // Note that `MyMiniAppDisplayScreen` is just a placeholder example for your own class
        // Inside this class you should call `MiniApp.create` in order to create and display the mini app
        MyMiniAppDisplayScreen.launch(miniAppId, query)
    }
}
```

</details>

<details><summary markdown="span"><b>How do I clear the session data for Mini Apps?</b>
</summary>

In the case that a user logs out of your App, you should clear the session data for all of your Mini Apps. This will ensure that the next user does not have access to the stored sensitive information about the previous user such as Local Storage, IndexedDB, and Web SQL.

The session data can be cleared by using the following:

```kotlin
// Should be called after the User logs out
WebStorage.getInstance().deleteAllData()
CookieManager.getInstance().removeAllCookies {}
WebViewDatabase.getInstance(this).clearHttpAuthUsernamePassword()
```

**Note:** This will also clear the storage, cookies, and authentication data for ALL WebViews used by your App.
</details>

<details><summary markdown="span"><b>How can I use this SDK in a Java project?</b>
</summary>

We don't support usage of the Mini App SDK in a Java project. This is because this SDK uses Kotlin specific features such as `suspend` functions and `Coroutines`.

However, it is possible to use this SDK in a Java project if you wrap the calls to `suspend` functions into something that Java can use such as a `Future` or a normal callback interface.

To do this, you will need to integrate Kotlin and Kotlin Coroutines into your project:

```groovy
// In your top level "build.gradle" file:
buildscript {
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50"
    }
}

// In your project "myApp/build.gradle" file
apply plugin: 'kotlin-android'

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.50"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.0" // Needed if you want to use CoroutineScope.future
}
```

Then, you will need to create wrapper functions for the Mini App SDK functionality which you wish to use.

If your minimum Android API level is 24 or higher, then you can use Java [`CompletableFuture`](https://developer.android.com/reference/java/util/concurrent/CompletableFuture):

```kotlin
// MiniAppAsync.kt

private val coroutineScope = CoroutineScope(Dispatchers.Default)

fun createMiniAppAsync(
    appId: String,
    miniAppMessageBridge: MiniAppMessageBridge,
    miniAppNavigator: MiniAppNavigator?
): CompletableFuture<MiniAppDisplay> = coroutineScope.future {
    MiniApp.instance().create(appId, miniAppMessageBridge, miniAppNavigator)
}
```

If your minimum Android API level is lower than 24, then you can use a normal Java callback interface:

```kotlin
// MiniAppAsync.kt

interface MiniAppCallback<T> {
    fun onSuccess(result: T)
    fun onError(error: MiniAppSdkException)
}

val coroutineScope = CoroutineScope(Dispatchers.Default)

fun createMiniAppAsync(
    appId: String,
    miniAppMessageBridge: MiniAppMessageBridge,
    miniAppNavigator: MiniAppNavigator?,
    callback: MiniAppCallback<MiniAppDisplay>
) {
    coroutineScope.launch {
        try {
            val display = MiniApp.instance().create(appId, miniAppMessageBridge, miniAppNavigator)
            callback.onSuccess(display)
        } catch (error: MiniAppSdkException) {
            callback.onError(error)
        }
    }
}
```
</details>

<details><summary markdown="span"><b>How to override text for localization purpose?</b>
</summary>

The MiniApp SDK provides the default UI (i.e custom permission window) when your app does not have own UI implementation.

In case you want to use the default UI and only change text display, you can override the string values in [here](https://github.com/rakutentech/android-miniapp/blob/master/miniapp/src/main/res/values/strings.xml).
Just need to place them in your app `strings.xml` with the same key. You can also put them in different localization resource directory.

Example: We want to change `<string name="miniapp_sdk_android_save">Save</string>` in another locale text.

```xml
<!--src/main/res/values-ja/strings.xml-->

<string name="miniapp_sdk_android_save">セーブ</string>
```
</details>

## Changelog

See the full [CHANGELOG](https://github.com/rakutentech/android-miniapp/blob/master/CHANGELOG.md).
