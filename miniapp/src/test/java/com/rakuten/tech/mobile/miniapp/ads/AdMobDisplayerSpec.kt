package com.rakuten.tech.mobile.miniapp.ads

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.TEST_AD_UNIT_ID
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class AdMobDisplayerSpec {
    private val adDisplayer = Mockito.spy(AdMobDisplayer(ApplicationProvider.getApplicationContext()))

    @Test
    fun `should show interstitial ads when it is ready`() {
        adDisplayer.loadInterstitial(TEST_AD_UNIT_ID, {}, {})
        When calling adDisplayer.isAdReady(TEST_AD_UNIT_ID) itReturns true
        adDisplayer.showInterstitial(TEST_AD_UNIT_ID, {}, {})
    }

    @Test
    fun `should invoke error when interstitial ads when is not ready`() {
        val onError: (msg: String) -> Unit = {
            it shouldBe "Ad is not loaded yet"
        }
        adDisplayer.showInterstitial(TEST_AD_UNIT_ID, {}, onError)
    }
}
