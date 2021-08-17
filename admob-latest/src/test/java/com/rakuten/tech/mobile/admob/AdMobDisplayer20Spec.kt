package com.rakuten.tech.mobile.admob

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.rewarded.RewardedAd
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class AdMobDisplayer20Spec {

    private lateinit var context: Activity
    private lateinit var adDisplayer20: AdMobDisplayer20
    private var mockInterstitialAd: InterstitialAd = mock()
    private var mockRewardedAd: RewardedAd = mock()

    @Before
    fun setup() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            context = activity
            adDisplayer20 = Mockito.spy(AdMobDisplayer20(context))
        }
    }

    @Test
    fun `should show interstitial ads when it is ready`() {
        adDisplayer20.loadInterstitialAd(TEST_AD_UNIT_ID, {}, {})
        val map = HashMap<String, InterstitialAd>()
        map[TEST_AD_UNIT_ID] = mockInterstitialAd
        adDisplayer20.initAdMap(interstitialAdMap = map)

        adDisplayer20.showInterstitialAd(TEST_AD_UNIT_ID, {}, {})
        verify(mockInterstitialAd).show(context)
    }

    @Test
    fun `should invoke error when interstitial ads when is not ready`() {
        val onError: (msg: String) -> Unit = {
            it shouldBe "Ad is not loaded yet"
        }
        adDisplayer20.showInterstitialAd(TEST_AD_UNIT_ID, {}, onError)

        adDisplayer20.loadInterstitialAd(TEST_AD_UNIT_ID, {}, {})
        adDisplayer20.showInterstitialAd(TEST_AD_UNIT_ID, {}, onError)
    }

    @Test
    fun `should invoke error when interstitial ads is already loaded`() {
        val onError: (msg: String) -> Unit = {
            it shouldBeEqualTo "Previous $TEST_AD_UNIT_ID already loaded."
        }
        adDisplayer20.loadInterstitialAd(TEST_AD_UNIT_ID, {}, {})

        val map = HashMap<String, InterstitialAd>()
        map[TEST_AD_UNIT_ID] = mockInterstitialAd
        adDisplayer20.initAdMap(interstitialAdMap = map)

        adDisplayer20.loadInterstitialAd(TEST_AD_UNIT_ID, {}, onError)
    }

    @Test
    fun `should show rewarded ads when it is ready`() {
        adDisplayer20.loadRewardedAd(TEST_AD_UNIT_ID, {}, {})

        val map = HashMap<String, RewardedAd>()
        map[TEST_AD_UNIT_ID] = mockRewardedAd
        adDisplayer20.initAdMap(rewardedAdMap = map)

        val rewardedAdCallback = adDisplayer20.createRewardedAdShowCallback()
        whenever(adDisplayer20.createRewardedAdShowCallback()) doReturn rewardedAdCallback

        adDisplayer20.showRewardedAd(TEST_AD_UNIT_ID, {}, {})
        verify(mockRewardedAd).show(context, rewardedAdCallback)
    }

    @Test
    fun `should invoke error when rewarded ads when is not ready`() {
        val onError: (msg: String) -> Unit = {
            it shouldBe "Ad is not loaded yet"
        }
        adDisplayer20.showRewardedAd(TEST_AD_UNIT_ID, {}, onError)

        adDisplayer20.loadRewardedAd(TEST_AD_UNIT_ID, {}, {})
        adDisplayer20.showRewardedAd(TEST_AD_UNIT_ID, {}, onError)
    }

    @Test
    fun `should invoke error when rewarded ads is already loaded`() {
        val onError: (msg: String) -> Unit = {
            it shouldBeEqualTo "Previous $TEST_AD_UNIT_ID already loaded."
        }
        adDisplayer20.loadRewardedAd(TEST_AD_UNIT_ID, {}, {})

        val map = HashMap<String, InterstitialAd>()
        map[TEST_AD_UNIT_ID] = mockInterstitialAd
        adDisplayer20.initAdMap(interstitialAdMap = map)

        adDisplayer20.loadRewardedAd(TEST_AD_UNIT_ID, {}, onError)
    }
}
