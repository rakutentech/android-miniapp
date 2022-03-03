package com.rakuten.tech.mobile.miniapp.js.hostenvironment

import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.TestActivity
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HostEnvironmentInfoSpec {

    @Test
    fun `HostEnvironmentInfo constructor should work properly with activity and hostLocale parameters`() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val hostEnvironmentInfo = HostEnvironmentInfo(activity, "en")
            val hostVersion = activity.packageManager.getPackageInfo(
                activity.packageName, 0
            ).versionName
            hostEnvironmentInfo.platformVersion shouldBeEqualTo Build.VERSION.RELEASE
            hostEnvironmentInfo.hostVersion shouldBeEqualTo hostVersion
        }
    }
}
