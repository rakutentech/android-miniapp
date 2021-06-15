## CHANGELOG

### 3.4.0 (2021-06-XX)
**SDK**
- **Fix:** Prevent exception during calling `onError` asynchronously in `getUniqueId`.

### 3.3.0 (2021-05-19)
**SDK**
- **Deprecated:** Old `getUniqueId`interface.
- **Feature:** Added `getUniqueId` new interface for invoking data using `onSuccess` and `onError`.
- **Feature:** Support `mailto` uri address. See [this](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/a).
- **Feature:** Added `rakuten.miniapp.user.action.SEND_MESSAGE` custom permission and applied to `ChatBridgeDispatcher.sendMessageToContactId`.
- **Change:** Added `getAccessToken` result with new Error type i.e `MiniAppAccessTokenError` to support predefined error types, previous `getAccessToken` is deprecated and can not be used anymore.
- **Feature:** Update `MiniAppSdkConfig` class to accept list of `MiniAppAnalyticsConfig` to send analytics in multiple account.

**Sample App**
- **Change:** Replaced the implementation of `getUniqueId` using new interface.
- **Feature:** Added QA option in settings screen to test `authorizationFailureError` and `custom` error type.

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
