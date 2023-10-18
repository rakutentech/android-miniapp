//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.file](../index.md)/[MiniAppCameraPermissionDispatcher](index.md)

# MiniAppCameraPermissionDispatcher

[androidJvm]\
interface [MiniAppCameraPermissionDispatcher](index.md)

A class to provide the interfaces for getting and requesting camera permission.

## Functions

| Name | Summary |
|---|---|
| [getCameraPermission](get-camera-permission.md) | [androidJvm]<br>open fun [getCameraPermission](get-camera-permission.md)(permissionCallback: (isGranted: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>Get camera permission from host app. You can also throw an [Exception](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html) from this method. |
| [requestCameraPermission](request-camera-permission.md) | [androidJvm]<br>open fun [requestCameraPermission](request-camera-permission.md)(miniAppPermissionType: [MiniAppDevicePermissionType](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-device-permission-type/index.md), permissionRequestCallback: (isGranted: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>Request camera permission from host app. You can also throw an [Exception](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html) from this method. |
