//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.file](../index.md)/[MiniAppFileChooserDefault](index.md)/[MiniAppFileChooserDefault](-mini-app-file-chooser-default.md)

# MiniAppFileChooserDefault

[androidJvm]\
fun [MiniAppFileChooserDefault](-mini-app-file-chooser-default.md)(requestCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), miniAppCameraPermissionDispatcher: [MiniAppCameraPermissionDispatcher](../-mini-app-camera-permission-dispatcher/index.md)? = null)

## Parameters

androidJvm

| | |
|---|---|
| requestCode | of file choosing using an intent inside sdk, which will also be used to retrieve the data by [Activity.onActivityResult](https://developer.android.com/reference/kotlin/android/app/Activity.html#onactivityresult) in the HostApp. |
| miniAppCameraPermissionDispatcher | needs to be implemented if HostApp want to access camera from miniapp and HostApp has camera permission in manifest.xml |
