package com.rakuten.tech.mobile.miniapp.ads

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.rakuten.tech.mobile.miniapp.TEST_AD_UNIT_ID
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class AdMobDisplayer20Spec {

    private var context: Activity = mock()
    private lateinit var adDisplayer20: AdMobDisplayer20
    private var mockInterstitialAd: InterstitialAd = mock()
    private var mockRewardedAd: RewardedAd = mock()
    private var testLoadInterstitialAd: (
        context: Activity,
        adUnitId: String,
        adRequest: AdRequest,
        adLoadCallback: InterstitialAdLoadCallback
    ) -> Unit = mock()
    private val testShowInterstitialAd: (context: Activity, ad: InterstitialAd) -> Unit = mock()
    private val testLoadRewardedAd: (context: Activity, adUnitId: String, adRequest: AdRequest, RewardedAdLoadCallback) -> Unit = mock()
    private val testShowRewardedAd: (context: Activity, ad: RewardedAd, rewardListener: OnUserEarnedRewardListener) -> Unit = mock()

    @Before
    fun setup() {
        adDisplayer20 = spy(AdMobDisplayer20(context))
        whenever(adDisplayer20.loadInterstitialAd).thenReturn(testLoadInterstitialAd)
        whenever(adDisplayer20.showInterstitialAd).thenReturn(testShowInterstitialAd)
        whenever(adDisplayer20.loadRewardedAd).thenReturn(testLoadRewardedAd)
        whenever(adDisplayer20.showRewardedAd).thenReturn(testShowRewardedAd)
    }

    @Test
    fun `should show interstitial ads when it is ready`() {
        adDisplayer20.loadInterstitialAd(TEST_AD_UNIT_ID, {}, {})
        val map = HashMap<String, InterstitialAd>()
        map[TEST_AD_UNIT_ID] = mockInterstitialAd
        adDisplayer20.initAdMap(interstitialAdMap = map)

        adDisplayer20.showInterstitialAd(TEST_AD_UNIT_ID, {}, {})
        verify(adDisplayer20).showInterstitialAd
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
        verify(adDisplayer20).showRewardedAd
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
