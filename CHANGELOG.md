## CHANGELOG

### 5.x.x (xxxx-xx-xx)
**SDK**
- **Feature:** Added `MiniAppView` to create multiple MiniApp views.
- **Feature:** Added `MiniAppConfig` data class to hold configuration settings for the `MiniAppView`.
- **Deprecated:** Old `create` and `createWithUrl` interfaces.
- **Fix:** Made the `MaxStorageSize` Limit in Bytes.
- **Fix:** Returning `onSuccess` for the Bulk Delete even if no items could be deleted.
- **Update:** Refactored the `maxStorageSizeLimitInMB` parameter to `maxStorageSizeLimitInBytes` in `MiniAppSdkConfig`.

**Sample App**
- **Feature:** Demo App will now use a TabBar instead of a single screen.
- **Feature:** Adding List I and List II for displaying multiple MiniApps at the same time and configurable through settings.
- **Feature:** Features tab will start with URL feature to run MiniApps locally.
- **Feature:** Settings is moved to it's own tab.

### 4.3.0 (2022-08-09)
**SDK**
- **Feature:** Added `languageCode` in cached manifest to support localization of manifest.
- **Fix:** Added bug fixes related to Secure storage feature.

**Sample App**
- **Feature:** Added bluetooth paired device detection on Android 12+ devices in QA settings screen.
- **Feature:** Added UI in QA settings screen to upgrade the max storage size limit on runtime.
- **Fix:** Displayed close confirmation dialog when MiniApp is closed using physical back button.
- **Fix:** Added proper error message when name and email both inputs are empty in contact settings page.

### 4.2.0 (2022-06-24)
**SDK**
- **Feature:** Added `MiniAppMessageBridge.getMessagingUniqueId` for supporting MAUID v2 and `MiniAppMessageBridge.getMauid` in MiniAppMessageDelegate for retrieving MAUID.
- **Feature:** Added support for base64 `data:` URIs to the File Download feature in `MiniAppFileDownloader`.
- **Feature:** Added secure storage support for storing, getting and removing data for MiniApps safely. HostApp can clear secured data using `MiniApp.clearSecureStorage`. Also, `MiniAppSdkConfig` is extended with `storageMaxSizeKB` to set the maximum available space in bytes for secure storage.
- **Feature:** Added `MiniAppMessageBridge.miniAppShouldClose()` function which would help the host app to check if any alert need to be displayed before closing the MiniApp.
- **Feature:** Added `MiniAppTooManyRequestsError` which will be thrown from SDK if any API from platform sends `429` status code.

**Sample App**
- **Feature:** Added implementation of `MiniAppMessageBridge.getMessagingUniqueId` and `MiniAppMessageBridge.getMauid`.
- **Feature:** Added a `Clear All` button to Settings/QA to remove all secure storages.
- **Feature:** Added support to display error popup when there is `MiniAppTooManyRequestsError`.

### 4.1.2 (2022-06-20)
**SDK**
- **Fix:** Removed `//` from API calls to generate a valid URL in `MetaDataApi` and `ManifestApi`.

### 4.1.1 (2022-05-16)
**SDK**
- **Remove:** Removed `io.github.rakutentech.sdkutils` dependency and adopted the related changes to MiniApp SDK.

### 4.1.0 (2022-04-11)
**SDK**
- **Feature:** Added interface `MiniAppFileDownloader` to download files in device from miniapp.
- **Update:** Updated `MiniApp.create` method with optional `fromCache` variable that helps to load the mini-app from cache.

### 4.0.0 (2022-03-01)
**SDK**
- **Upgraded:** Target SDK is now Android 12 (API level 31)
- **Upgraded:** SDK dependencies to new versions
    - androidx.core:core-ktx:1.6.0
    - androidx.webkit:webkit:1.4.0
    - com.google.android.gms:play-services-ads:20.5.0
    - org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.31
    - org.jetbrains.kotlin:kotlin-test:1.5.31
    - org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2
    - org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2
    - com.squareup.okhttp3:okhttp:4.9.1
    - androidx.appcompat:appcompat:1.3.0
    - androidx.recyclerview:recyclerview:1.2.1
- **Feature:** Added interface for requesting Camera permission e.g. `MiniAppCameraPermissionDispatcher`. Please see user-guide.
- **Removed:** Functions which were deprecated in previous releases have been removed:
  - `MiniAppMessageBridge.getUniqueId()`
  - `UserInfoBridgeDispatcher.getAccessToken(miniAppId: String, onSuccess: (tokenData: TokenData) -> Unit, onError: (message: String) -> Unit)`
  - `UserInfoBridgeDispatcher.getAccessToken(miniAppId: String, accessTokenScope: AccessTokenScope, onSuccess: (tokenData: TokenData) -> Unit, onError: (message: String) -> Unit)`
- **Removed:** Classes which were deprecated in this releases have been removed:
  - `AdMobDisplayer19`
- **Update:** Added a new constructor to pass `hostLocale` info in `HostEnvironmentInfo` data class.
- **Update:** Removed admob 19 support.
- **Feature:** Added [Ad placement beta](https://developers.google.com/ad-placement) support. Please see user-guide.
- **Update:** Renamed `AdMobDisplayer20` to `AdMobDisplayer`.

**Sample App**
- **Upgraded:** Target SDK is now Android 12 (API level 31)
- **Upgraded:** Sample App dependencies to new versions
    - androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1
    - androidx.activity:activity-ktx:1.2.4
    - com.github.bumptech.glide:glide:4.12.0
    - com.google.android.material:material:1.4.0
    - com.google.code.gson:gson:2.8.9

### 3.9.1 (2022-01-20)
**SDK**
- **Fix:** Unable to launch a mini app while the device is offline

### 3.9.0 (2021-12-17)
**SDK**
- **Feature:** Added `languageCode` parameter in `MiniApp.getMiniAppManifest` to support for internationalized manifest.
- **Feature:** Added `hostLocale` in `HostEnvironmentInfo` to provide default language value from Host App.
- **Feature:** Added `promotionalImageUrl` and `promotionalText` in MiniAppInfo model.
- **Feature:** Added support for taking picture from camera in Mini App.

**Sample App**
- **Feature:** Added production and staging toggle to change environments.
- **Feature:** Adding Sharing option to display the promotional content via `MiniAppDisplay`.

### 3.8.0 (2021-11-09)
**SDK**
- **Feature:** Added `getHostEnvironmentInfo` function in `MiniAppMessageBridge` to provide environment information to Mini App.
- **Feature:** Added `dispatchNativeEvent` function in `MiniAppMessageBridge` to send events to Mini App.
- **Feature:** Added `rakuten.miniapp.device.FILE_DOWNLOAD` custom permission before downloading file attachment in MiniApp.

**Sample App**
- **Feature:** Added dynamic deeplink support to test deeplink urls from Mini App.

### 3.7.0 (2021-10-07)
**SDK**
- **Feature:** Added `admob-latest` module to support latest admob sdk.
- **Feature:** Added `getMiniAppInfoByPreviewCode` interface to get MiniAppInfo by preview code.
- **Feature:** Added optional public key pinning through `MiniAppSdkConfig`.
- **Feature:** Added additional rat events to track sdk feature usage.
- **Feature:** Added signature verification process before downloading a MiniApp. HostApp also can enable the settings of SDK to verify signature. Please see user-guide.

**Sample App**
- **Feature:** User can edit contact name and email in App Settings.
- **Feature:** User can see access token scopes requested for the RAE audience in first-time screen.
- **Feature:** User can scan qr code and preview miniapp in demo app.
- **Feature:** Added deeplink support to open miniapp in demo app by qrcode scan.
- **Feature:** Added additional rat events to track the demo app by adding custom views.
- **Feature:** Added signature verification requirement enabling option in App Settings.

### 3.6.1 (2021-09-17)
- **Fix:** Prevent manifest merger failure when Host App already has implemented a FileProvider.

### 3.6.0 (2021-09-16)
- **Feature:** Added support for downloading files in a Mini App, and added `MiniAppDownloadNavigator` interface for overriding the default file download behavior.

### 3.5.0 (2021-07-27)
**SDK**
- **Feature:** Added `getPoints` interface in `UserInfoBridgeDispatcher` to request for user's point with checking `rakuten.miniapp.user.POINTS` custom permission.
- **Feature:** Added support for using `".jpg"` and multiple types e.g. `".jpg,.png"` in the accept field of the file input.
- **Update:** Updated `MessageToContact` to support an optional message to the user for the contact picker capability.
- **Update:** Included technical improvement in manifest file verification process.

**Sample App**
- **Feature:** Added a screen to input user's points which can be invoked using `getPoints` interface.
- **Update:** Display a banner message on contact picker ui while sending message to single/multiple contacts.

### 3.4.0 (2021-06-29)
**SDK**
- **Feature:** Update `MiniAppSdkConfig` class to accept list of `MiniAppAnalyticsConfig` to send analytics in multiple account.
- **Change:** Added `getAccessToken` result with new Error type i.e `MiniAppAccessTokenError` to support predefined error types, previous `getAccessToken` is deprecated and can not be used anymore.
- **Fix:** Prevent exception during calling `onError` asynchronously in `getUniqueId`.
- **Update:** Supported "Update codebase" functionality for preview mode.
- **Update:** Included technical improvement in custom permission and manifest information caching.

**Sample App**
- **Feature:** Added QA option in settings screen to test `authorizationFailureError` and `custom` error type.

### 3.3.0 (2021-05-19)
**SDK**
- **Deprecated:** Old `getUniqueId`interface.
- **Feature:** Added `getUniqueId` new interface for invoking data using `onSuccess` and `onError`.
- **Feature:** Support `mailto` uri address. See [this](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/a).
- **Feature:** Added `rakuten.miniapp.user.action.SEND_MESSAGE` custom permission and applied to `ChatBridgeDispatcher.sendMessageToContactId`.

**Sample App**
- **Change:** Replaced the implementation of `getUniqueId` using new interface.

### 3.2.0 (2021-04-23)
**SDK**
- **Feature:** Added several chatting interfaces e.g. `sendMessageToContact`, `sendMessageToContactId` and `sendMessageToMultipleContacts` in `ChatBridgeDispatcher` for sending message to single or multiple contacts, even to a specific contact id. HostApp can get the message using `MessageToContact` object.

**Sample App**
- **Feature:** Added implementations of `ChatBridgeDispatcher.sendMessageToContact`, `ChatBridgeDispatcher.sendMessageToContactId` and `ChatBridgeDispatcher.sendMessageToMultipleContacts` with the demo UI.

### 3.1.0 (2021-04-02)
**SDK**
- **Feature:** Support name and email as optional in Contact.
- **Deprecated:** `getAccessToken` with only miniapp id verfication.
- **Feature:** Add a new `getAccessToken` function under custom permission and support audience/scope validation.
- **Feature:** Added `MiniAppFileChooser` to choose a file which is requested using HTML forms with 'file' input type within a miniapp, HostApp can also use `MiniAppFileChooserDefault` when there is nothing to customize during file choosing.
- **Change:** Updated `MiniApp.create` & `MiniApp.createWithUrl` to include `MiniAppFileChooser` to choose a file within a miniapp.

**Sample App**
- **Feature:** Input and display name, email of Contact.

### 3.0.0 (2021-03-22)
**SDK**
- **Removed:** Cleanup deprecated components before v3.0.0. Please replace usages in your code as follows:

Before v3.0.0  |  v3.0.0
------------- | -------------
`isTestMode`  | `isPreviewMode`
`rasAppId`  | `rasProjectId`
`MiniAppMessageBridge.requestPermission` | `MiniAppMessageBridge.requestDevicePermission`
`getUserName(): String` | `getUserName(onSuccess, onError)`
`getProfilePhoto(): String` | `getProfilePhoto(onSuccess, onError)`

- **Change:** Support Android 7 - API 24 as minimum version.
- **Change:** Convert `UserInfoBridgeDispatcher` into interface. Usages in your code of `object : UserInfoBridgeDispatcher()` should be changed to `object : UserInfoBridgeDispatcher`.
- **Change:** Update `MiniApp.create` to check the required permissions in Mini App's manifest have been granted or not before creating the Mini App.
- **Change:** Maven Group ID changed to `io.github.rakutentech.miniapp`. You must update your dependency declaration to `io.github.rakutentech.miniapp:miniapp:3.0.0`.
- **Change:** Migrated publishing to Maven Central due to Bintray/JCenter being [shutdown](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/). You must add `mavenCentral()` to your `repositories`.
- **Fix:** Load ad error when do re-try loading.
- **Fix:** Failure when simultaneous custom permission requests are received.
- **Feature:** Added `MiniApp.getMiniAppManifest` interface to retrieve the manifest of a MiniApp.
- **Feature:** Added `MiniApp.getDownloadedManifest` interface to retrieve currently downloaded manifest of a MiniApp.

**Sample App**
- **Feature:** Added first-time launching screen to show the manifest information before downloading/launching a MiniApp.

### 2.8.0 (2021-01-25)
**SDK**
- **Feature:** Added `getUserName`, `getProfilePhoto` new interfaces for invoking data using `onSuccess` and `onError`.
- **Feature:** Support analytics SDK with event tracking.
- **Deprecated:** Old `getUserName`, `getProfilePhoto` interfaces.
- **Feature:** Added `queryParams: String` using `MiniApp.create` and `MiniApp.createWithUrl` for appending it with the miniapp's url.
- **Feature:** Added `MiniAppMessageBridge.requestDevicePermission` for requesting device permission e.g. Location
- **Change:** Deprecated `MiniAppMessageBridge.requestPermission` and changed to be optional to implement.
- **Fix:** Dialog cancel event when touch outside area.

**Sample App**
- **Change:** Replaced the implementation of `getUserName`, `getProfilePhoto` using new interfaces.
- **Feature:** Added input option in settings screen to keep query parameters to be passed using `MiniApp.create` and `MiniApp.createWithUrl`.
- **Feature:** Added crash reports integration with [app-center diagnostics](https://docs.microsoft.com/en-us/appcenter/diagnostics/).
- **Fix:** Correct the group and order display of miniapp list.
- **Change:** Added the usage of `MiniAppMessageBridge.requestDevicePermission`.

### 2.7.2 (2021-03-03)
- **Fix:** Exception for miniapp verification failed. See [this](miniapp/USERGUIDE.md#troubleshooting--faqs).

### 2.7.1 (2020-12-23)
**SDK**
- **Fix:** MiniApp view did not display due to obfuscation code guard.

### 2.7.0 (2020-12-18)
**SDK**
- **Feature:** Added `rakuten.miniapp.device.LOCATION` custom permission.
- **Feature:** Added `getContacts()` interface in `UserInfoBridgeDispatcher` for receiving list of contact IDs.
- **Feature:** Support loading miniapp by url. This feature is only for QA purpose to let developers have a quick look at their miniapps.
- **Change:** Remove `app_name` property.

**Sample App**
- **Feature:** `rakuten.miniapp.device.LOCATION` permission as "Location" in custom permissions settings screen is visible now.
- **Feature:** Support loading miniapp by url. This enables developers to test while their miniapps are still in development.
- **Change:** Added `getContacts()` implementation for sending the list of contact IDs.
- **Change:** Disable Google backup.

### 2.6.0 (2020-11-27)
**SDK**
- **Feature:** Mini App can call media execution play/pause programmatically.
- **Feature:** Added `MiniApp#create(appInfo: MiniAppInfo, miniAppMessageBridge: MiniAppMessageBridge, miniAppNavigator: MiniAppNavigator? = null)`.
- **Feature:** Officially support Preview Mode.
- **Change:** Added the default implementation for external link handler. Using [custom tab](https://developers.google.com/web/android/custom-tabs).
- **Change:** `isTestMode` has been deprecated and replaced with `isPreviewMode` to adopt the Preview mode. See [this](miniapp/USERGUIDE.md#2-configure-sdk-settings-in-androidmanifestxml).
- **Change:** `com.rakuten.tech.mobile.ras.AppId` has been deprecated, use `com.rakuten.tech.mobile.ras.ProjectId` instead. See [this](miniapp/USERGUIDE.md#2-configure-sdk-settings-in-androidmanifestxml).

**Sample App**
- **Change:** Updated setting of external webview.
- **Change:** Used `isPreviewMode` in sample app. "Preview Mode" switch in the settings screen is visible now.
- **Change:** Updated sample app Manifest configuration with replacing RAS App ID by Project ID.
- **Change:** Updated setting permission screen with permission preview.

### 2.5.0 (2020-11-13)
**SDK**
- **Feature:** Provide the access token to miniapp. You can implement `MiniAppMessageBridge.getAccessToken` to use this feature.
- **Feature:** Added default UI support for managing custom permissions in case `requestCustomPermissions` hasn't been implemented in Host App.
- **Feature:** Added `MiniAppHasNoPublishedVersionException` and `MiniAppNotFoundException` exception types to `MiniApp.create` and `MiniApp.fetchInfo`.

**Sample App**
- **Feature:** Added "Access Token" configuration options to settings screen.
- **Feature:** Show error dialog instead of Toast for errors in settings screen.

### 2.4.0 (2020-10-30)
**SDK**
- **Feature:** Handle the screen orientation change request from miniapp.
- **Fix:** Clean up fullscreen view when exit miniapp. `MiniAppDisplay.destroyView` is mandatory for single activity architecture.

**Sample App**
- **Fix:** Disable pull to refresh while searching.

### 2.3.0 (2020-10-15)
**SDK**
- **Feature:** Load and display ads from miniapp. See [this](miniapp/USERGUIDE.md#5-ads-integration).
- **Feature:** Added `getUserName()` interface in `UserInfoBridgeDispatcher` for receiving user name if the required permission has been granted e.g. "rakuten.miniapp.user.USER_NAME".
- **Feature:** Added `getProfilePhoto()` interface in `UserInfoBridgeDispatcher` for receiving profile photo url if the required permission has been granted e.g. "rakuten.miniapp.user.PROFILE_PHOTO".
- **Fix:** `tel:` links weren't functioning correctly on API 23.
- **Fix:** `tel:` links weren't functioning in the external WebView.

**Sample App**
- **Feature:** Added sample implementation for receiving username and profile photo to Mini app.
- **Fix:** Made the name field optional on the Profile settings screen.

### 2.2.0 (2020-10-02)
**SDK**
- **Feature:** Added public interface to read and store the custom permissions inside Android SDK. See [this](miniapp/USERGUIDE.md#4-custom-permissions).
- **Feature:** Added interface for sharing content from a mini app. See [this](miniapp/USERGUIDE.md#4-implement-the-miniappmessagebridge).
- **Feature:** Added `listDownloadedWithCustomPermissions()` public interface that enables the host app to retrieve the list of downloaded mini-apps and their respective custom permissions. See [this](miniapp/USERGUIDE.md#4-custom-permissions).
- **Feature:** Added capability to open external links in an external browser or WebView. By default, all external links should open in an external WebView. See [this](miniapp/USERGUIDE.md#3-external-url-loader).
- **Fix:** Added header to all API requests to accept content encoding. This is to support caching which is done by the API.

**Sample App**
- **Feature:** Added example for showing list of custom permissions (on request from Mini app) and response back to Mini app.
- **Feature:** User name, profile photo, and contact list can be configured in the settings screen.
- **Feature:** Added sample app implementation to revoke/manage the custom permissions for the list of downloaded mini apps.

### 2.1.0 (2020-09-03)
**SDK**
- **Feature:** Support telephone hyperlink in miniapp. See [this](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/a).
- **Feature:** Support webview video fullscreen.
- **Fix:** `MiniAppSdkException` had a null message in some cases.
- **Fix:** Some Mini Apps which use sub-directories were failing to unzip.

### 2.0.0 (2020-08-07)
**SDK**
- **Feature:** Mini App is now downloaded as a ZIP archive and extracted. This should improve the initial launch time on a Mini App with many files.
- **Feature:** Added custom dialog for `window.alert`, `window.prompt`, and `window.confirm`. These look and behave the same as the default WebView dialogs with the exception that the dialog title is now set to the Mini App's name.
- **Upgraded:** `minSdkVersion` has been bumped up to 23 (Android 6.0).
- **Removed:** `MiniAppDisplay#getMiniAppView()`, `MiniApp#create(info: MiniAppInfo)` has been removed.
- **Removed:** `MiniApp#create(info: MiniAppInfo, miniAppMessageBridge: MiniAppMessageBridge)` has been removed.

**Sample App**
- **Feature:** Added search by Mini App name to listing view.
- **Fix:** Display warning message when an invalid value is input for "Host App Id" or "Subscription Key".

### 1.2.0 (2020-07-21)
**SDK**
- Upgraded build setup and some internal dependencies
- Added support for customization of user agent information. See [this](miniapp/USERGUIDE.md#2-configure-sdk-settings-in-androidmanifestxml).
- Added feature to support backward & forward navigation support in a mini app.
- Added support for javascript of type module e.g. `<script src="some_module.js" type="module"></script>` would render just fine now even though the mimetype is missed out by the web technologies for this scenario.
- Added feature to obtain geolocation data in a mini app
- Hotfix for redirection over custom scheme and http

**Sample App**
- Updated styling of the mini app list
- Updated App's setting screen with build information
- Demo usage of customized user agent information
- Add navigation into mini app. See [this](miniapp/USERGUIDE.md#navigating-inside-a-mini-app).

### 1.1.1 (2020-06-11)

**SDK**
- *Bugfix:* `select` and `date` input elements weren't working correctly.
- Deprecated `MiniAppDisplay#getMiniAppView()` and added `MiniAppDisplay#getMiniAppView(activityContext: Context)`. You now must provide an Activity Context when retrieving the `View` for the Mini App. This is related to the bugfix for `select` and `date` inputs - if you use the deprecated method, then these elements will not work correctly.

**Sample App**
- Display first time setup instructions on first launch of App.

### 1.1.0 (2020-06-02)

- Added JavaScript bridge for passing data between Mini App and Host App. Your App now must implement `MiniAppMessageBridge` and provide the implementation when calling `MiniApp#create`. See [this](miniapp/USERGUIDE.md#4-implement-the-miniappmessagebridge)
- Deprecated `MiniApp#create(info: MiniAppInfo)`. Your App should instead use `MiniApp#create(info: MiniAppInfo, miniAppMessageBridge: MiniAppMessageBridge)`.
- Added `getUniqueId` function to `MiniAppMessageBridge`. This function should provide a unique identifier (unique to the user and device) to Mini Apps.
- Added support for custom scheme URL redirect. The URL `mscheme.MINI_APP_ID://miniapp/index.html` can be used by a Mini App for redirection. This matches the URL used in the iOS Mini App SDK.

### 1.0.0 (2020-04-21)

- Initial release
