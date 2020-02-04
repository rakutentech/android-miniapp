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
    maven { url 'http://oss.jfrog.org/artifactory/simple/libs-snapshot/' } // If you want to use snapshot releases
}

dependency {
    implementation 'com.rakuten.tech.mobile.miniapp:miniapp:0.1.0-SNAPSHOT'
}
```

### #2 Set your App Id, Subscription Key, & Base URL

We don't currently host a public API, so you will need to provide your own Base URL for API requests.

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

### #3 Create and display a Mini App

Calling `MiniApp.create` will download the Mini App if it has not yet been downloaded, or it will download the latest version of the Mini App if an old version is already downloaded. A view will then be returned which will display the Mini App.

```kotlin
class MiniAppActivity : Activity(), CoroutineScope {

    override val coroutineContext = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super(savedInstanceState)
        setContentView(R.layout.loading)

        val context = this
        launch {
            val miniApp = withContext(Dispatchers.Default) {
                MiniApp.instance().create("mini_app_id")
            }
            val miniAppView = miniApp.obtainView(context)

            setContentView(miniAppView)
        }
    }
}
```

## Changelog
