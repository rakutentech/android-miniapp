## CHANGELOG

### 2.1.0 (in progress)
**SDK**
- Support telephone hyperlink in miniapp. See [this](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/a).

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
