package com.rakuten.tech.mobile.admob

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class RealAdmobDisplayerLatestSpec {

    private lateinit var context: Activity
    private lateinit var adDisplayer: RealAdmobDisplayerLatest
    private var onCallback: (loadStatus: AdStatus, errorMessage: String?) -> Unit = mock()

    @Before
    fun setup() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            context = activity
            adDisplayer = Mockito.spy(RealAdmobDisplayerLatest(context))
        }
    }

    @Test
    fun `should show interstitial ads when it is ready`() {
    }
}
