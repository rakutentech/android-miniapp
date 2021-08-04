package com.rakuten.tech.mobile.admob

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.rewarded.RewardedAd
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.verify
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class RealAdmobDisplayerLatestSpec {

    private lateinit var context: Activity
    private lateinit var ad20Displayer: RealAdmobDisplayerLatest
    private var mockOnCallback = mock<(AdStatus, String?) -> Unit>()
    private var mockInterstitialAd: InterstitialAd = mock()
    private var mockRewardedAd: RewardedAd = mock()

    @get:Rule
    var activityScenarioRule = activityScenarioRule<TestActivity>()

    @Before
    fun setup() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            context = activity
            activityScenarioRule.scenario
            ad20Displayer = Mockito.spy(RealAdmobDisplayerLatest(context))
        }
    }

    @Test
    fun `should show interstitial ads when it is ready`() {

        val loadCallback: (AdStatus, String?) -> Unit = { adStatus: AdStatus, errorMessage: String? ->
            adStatus shouldBe AdStatus.LOADED
            errorMessage shouldBe null
        }

        ad20Displayer.loadInterstitialAd(TEST_AD_UNIT_ID, loadCallback)
        val map = HashMap<String, InterstitialAd>()
        map[TEST_AD_UNIT_ID] = mockInterstitialAd
        ad20Displayer.initAdMap(interstitialAdMap = map)

        ad20Displayer.showInterstitialAd(TEST_AD_UNIT_ID, mockOnCallback)
        verify(mockInterstitialAd).show(context)
    }

    @Test
    fun `should invoke error when interstitial ads when is not ready`() {
        val callback: (AdStatus, String?) -> Unit = { adStatus: AdStatus, errorMessage: String? ->
            adStatus shouldBe AdStatus.FAILED
            errorMessage shouldBe "Ad is not loaded yet"
        }
        ad20Displayer.showInterstitialAd(TEST_AD_UNIT_ID, callback)
    }

    @Test
    fun `should invoke error when interstitial ads is already loaded`() {
        val map = HashMap<String, InterstitialAd>()
        map[TEST_AD_UNIT_ID] = mockInterstitialAd
        ad20Displayer.initAdMap(interstitialAdMap = map)

        ad20Displayer.loadInterstitialAd(TEST_AD_UNIT_ID, mockOnCallback)
        verify(ad20Displayer).createLoadReqError(TEST_AD_UNIT_ID)
    }

    @Test
    fun `should show rewarded ads when it is ready`() {
        val onReward: (Int, String) -> Unit = { amount: Int, type: String ->
            amount shouldBe 10
            type shouldBe "test_type"
        }

        ad20Displayer.loadRewardedAd(TEST_AD_UNIT_ID, mockOnCallback)

        val map = HashMap<String, RewardedAd>()
        map[TEST_AD_UNIT_ID] = mockRewardedAd
        ad20Displayer.initAdMap(rewardedAdMap = map)

        ad20Displayer.showRewardedAd(TEST_AD_UNIT_ID, onReward, mockOnCallback)
        verify(mockRewardedAd).show(any(), any())
    }

    @Test
    fun `should invoke error when rewarded ads when is not ready`() {
        val callback: (AdStatus, String?) -> Unit = { adStatus: AdStatus, errorMessage: String? ->
            adStatus shouldBe AdStatus.FAILED
            errorMessage shouldBe "Ad is not loaded yet"
        }
        val onReward: (Int, String) -> Unit = { amount: Int, type: String ->
            amount shouldBe 10
            type shouldBe "test_type"
        }
        ad20Displayer.showRewardedAd(TEST_AD_UNIT_ID, onReward, callback)
    }

    @Test
    fun `should invoke error when rewarded ads is already loaded`() {
        val map = HashMap<String, RewardedAd>()
        map[TEST_AD_UNIT_ID] = mockRewardedAd
        ad20Displayer.initAdMap(rewardedAdMap = map)

        ad20Displayer.loadRewardedAd(TEST_AD_UNIT_ID, mockOnCallback)
        verify(ad20Displayer).createLoadReqError(TEST_AD_UNIT_ID)
    }
}
