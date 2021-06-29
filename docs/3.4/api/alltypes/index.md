

### All Types

|

##### [com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope](../com.rakuten.tech.mobile.miniapp.permission/-access-token-scope/index.md)

Contains the components which need to be validated when access token is granted.


|

##### [com.rakuten.tech.mobile.miniapp.ads.AdMobDisplayer](../com.rakuten.tech.mobile.miniapp.ads/-ad-mob-displayer/index.md)

The ad displayer.


|

##### [com.rakuten.tech.mobile.miniapp.js.chat.ChatBridgeDispatcher](../com.rakuten.tech.mobile.miniapp.js.chat/-chat-bridge-dispatcher/index.md)

Functionality related to Chatting with the contacts.


|

##### [com.rakuten.tech.mobile.miniapp.js.userinfo.Contact](../com.rakuten.tech.mobile.miniapp.js.userinfo/-contact/index.md)

Contact object for miniapp.


|

##### [com.rakuten.tech.mobile.miniapp.navigator.ExternalResultHandler](../com.rakuten.tech.mobile.miniapp.navigator/-external-result-handler/index.md)

The url transmitter from external factors to mini app view.


|

##### [com.rakuten.tech.mobile.miniapp.js.MessageToContact](../com.rakuten.tech.mobile.miniapp.js/-message-to-contact/index.md)

An object to prepare the message for sending to contacts.


|

##### [com.rakuten.tech.mobile.miniapp.api.MetadataPermissionObj](../com.rakuten.tech.mobile.miniapp.api/-metadata-permission-obj/index.md)

Metadata permission object includes name and reason.


|

##### [com.rakuten.tech.mobile.miniapp.api.MetadataResponse](../com.rakuten.tech.mobile.miniapp.api/-metadata-response/index.md)

Metadata response object includes required and optional permissions.


|

##### [com.rakuten.tech.mobile.miniapp.MiniApp](../com.rakuten.tech.mobile.miniapp/-mini-app/index.md)

This represents the contract between the consuming application and the SDK
by which operations in the mini app ecosystem are exposed.
Should be accessed via [MiniApp.instance](../com.rakuten.tech.mobile.miniapp/-mini-app/instance.md).


|

##### [com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError](../com.rakuten.tech.mobile.miniapp.errors/-mini-app-access-token-error/index.md)

A class to provide the custom errors specific for access token.


|

##### [com.rakuten.tech.mobile.miniapp.ads.MiniAppAdDisplayer](../com.rakuten.tech.mobile.miniapp.ads/-mini-app-ad-displayer/index.md)

Control ads load &amp; display when want to use AdMob.


|

##### [com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalyticsConfig](../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md)

Contains the components which need to add extra analytics credentials from host app.


|

##### [com.rakuten.tech.mobile.miniapp.errors.MiniAppBridgeError](../com.rakuten.tech.mobile.miniapp.errors/-mini-app-bridge-error/index.md)

Contains the components to use custom errors from host app.


|

##### [com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission](../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission/index.md)

A data class to hold the custom permission with grant results using Pair per MiniApp.


|

##### [com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResponse](../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-response/index.md)

A data class to prepare the json response of custom permissions to be sent from this SDK.


|

##### [com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult](../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-result/index.md)

Type of miniapp custom permission result.


|

##### [com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType](../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-type/index.md)

Type of miniapp custom permission.


|

##### [com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionType](../com.rakuten.tech.mobile.miniapp.permission/-mini-app-device-permission-type/index.md)

Type of miniapp device permission.


|

##### [com.rakuten.tech.mobile.miniapp.MiniAppDisplay](../com.rakuten.tech.mobile.miniapp/-mini-app-display/index.md)

This represents the contract by which the host app can interact with the
display unit of the mini app.
This contract complies to Android's [LifecycleObserver](#) contract, and when made to observe
the lifecycle, it automatically clears up the view state and any services registered with.


|

##### [com.rakuten.tech.mobile.miniapp.navigator.MiniAppExternalUrlLoader](../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-external-url-loader/index.md)

This support the scenario that external loader redirect to url which is only supported in mini app view,
close the external loader and emit that url to mini app view by [ExternalResultHandler.emitResult](../com.rakuten.tech.mobile.miniapp.navigator/-external-result-handler/emit-result.md).


|

##### [com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser](../com.rakuten.tech.mobile.miniapp.file/-mini-app-file-chooser/index.md)

The file chooser of a miniapp with `onShowFileChooser` function.


|

##### [com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooserDefault](../com.rakuten.tech.mobile.miniapp.file/-mini-app-file-chooser-default/index.md)

The default file chooser of a miniapp.


|

##### [com.rakuten.tech.mobile.miniapp.MiniAppHasNoPublishedVersionException](../com.rakuten.tech.mobile.miniapp/-mini-app-has-no-published-version-exception/index.md)

Exception which is thrown when the server returns no published
versions for the provided mini app ID.


|

##### [com.rakuten.tech.mobile.miniapp.MiniAppInfo](../com.rakuten.tech.mobile.miniapp/-mini-app-info/index.md)

This represents a Mini App entity.


|

##### [com.rakuten.tech.mobile.miniapp.MiniAppManifest](../com.rakuten.tech.mobile.miniapp/-mini-app-manifest/index.md)

A data class to represent data in the mini app's manifest.


|

##### [com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge](../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md)

Bridge interface for communicating with mini app.


|

##### [com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator](../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)

The navigation controller of sdk mini app view.


|

##### [com.rakuten.tech.mobile.miniapp.MiniAppNotFoundException](../com.rakuten.tech.mobile.miniapp/-mini-app-not-found-exception/index.md)

Exception which is thrown when the provided project ID
does not have any mini app exist on the server.


|

##### [com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig](../com.rakuten.tech.mobile.miniapp/-mini-app-sdk-config/index.md)

This represents the configuration settings for the Mini App SDK.


|

##### [com.rakuten.tech.mobile.miniapp.MiniAppSdkException](../com.rakuten.tech.mobile.miniapp/-mini-app-sdk-exception/index.md)

A custom exception class which treats the purpose of providing
error information to the consumer app in an unified way.


|

##### [com.rakuten.tech.mobile.miniapp.MiniAppVerificationException](../com.rakuten.tech.mobile.miniapp/-mini-app-verification-exception/index.md)

Exception which is thrown when cannot verify device keystore.


|

##### [com.rakuten.tech.mobile.miniapp.RequiredPermissionsNotGrantedException](../com.rakuten.tech.mobile.miniapp/-required-permissions-not-granted-exception/index.md)

Exception which is thrown when the required permissions of the manifest are not granted.


|

##### [com.rakuten.tech.mobile.miniapp.ads.Reward](../com.rakuten.tech.mobile.miniapp.ads/-reward/index.md)

Earn the reward from rewarded ad of AdMob.


|

##### [com.rakuten.tech.mobile.miniapp.js.userinfo.TokenData](../com.rakuten.tech.mobile.miniapp.js.userinfo/-token-data/index.md)

Access token object for miniapp.


|

##### [com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridgeDispatcher](../com.rakuten.tech.mobile.miniapp.js.userinfo/-user-info-bridge-dispatcher/index.md)

A class to provide the interfaces for getting user info e.g. user-name, profile-photo etc.


|

##### [com.rakuten.tech.mobile.miniapp.Version](../com.rakuten.tech.mobile.miniapp/-version/index.md)

This represents a version entity of a Mini App.


