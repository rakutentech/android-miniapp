package com.rakuten.tech.mobile.miniapp.ads

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import com.rakuten.tech.mobile.miniapp.TEST_AD_UNIT_ID
import com.rakuten.tech.mobile.miniapp.TestActivity
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class AdMobDisplayerSpec {
    private lateinit var context: Activity
    private lateinit var adDisplayer: AdMobDisplayer

    @Before
    fun setup() {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            context = activity
            adDisplayer = Mockito.spy(AdMobDisplayer(context))
        }
    }

    @Test
    fun `should show interstitial ads when it is ready`() {
        adDisplayer.loadInterstitialAd(TEST_AD_UNIT_ID, {}, {})

        val map = HashMap<String, InterstitialAd>()
        val ad = Mockito.spy(InterstitialAd(context))
        ad.adUnitId = TEST_AD_UNIT_ID
        When calling ad.isLoaded itReturns true
        map[TEST_AD_UNIT_ID] = ad
        adDisplayer.initAdMap(interstitialAdMap = map)

        adDisplayer.showInterstitialAd(TEST_AD_UNIT_ID, {}, {})
        verify(ad).show()
    }

    @Test
    fun `should invoke error when interstitial ads when is not ready`() {
        val onError: (msg: String) -> Unit = {
            it shouldBe "Ad is not loaded yet"
        }
        adDisplayer.showInterstitialAd(TEST_AD_UNIT_ID, {}, onError)

        adDisplayer.loadInterstitialAd(TEST_AD_UNIT_ID, {}, {})
        adDisplayer.showInterstitialAd(TEST_AD_UNIT_ID, {}, onError)
    }

    @Test
    fun `should invoke error when interstitial ads is already on queue`() {
        val onError: (msg: String) -> Unit = {
            it shouldBeEqualTo "Previous $TEST_AD_UNIT_ID is still in progress"
        }
        adDisplayer.loadInterstitialAd(TEST_AD_UNIT_ID, {}, {})

        adDisplayer.loadInterstitialAd(TEST_AD_UNIT_ID, {}, onError)
    }

    @Test
    fun `should show rewarded ads when it is ready`() {
        adDisplayer.loadRewardedAd(TEST_AD_UNIT_ID, {}, {})

        val map = HashMap<String, RewardedAd>()
        val ad = Mockito.spy(RewardedAd(context, TEST_AD_UNIT_ID))
        When calling ad.isLoaded itReturns true
        map[TEST_AD_UNIT_ID] = ad
        adDisplayer.initAdMap(rewardedAdMap = map)

        val onClosed: (reward: Reward?) -> Unit = {}
        val onError: (msg: String) -> Unit = {}
        val rewardedAdCallback = adDisplayer.createRewardedAdShowCallback(TEST_AD_UNIT_ID, onClosed, onError)
        When calling adDisplayer.createRewardedAdShowCallback(TEST_AD_UNIT_ID, onClosed, onError) itReturns
                rewardedAdCallback

        adDisplayer.showRewardedAd(TEST_AD_UNIT_ID, onClosed, onError)
        verify(ad).show(context, rewardedAdCallback)
    }

    @Test
    fun `should invoke sdk callbacks without error`() {
        val rewardedAdLoadCallback = adDisplayer.createRewardedAdLoadCallback(TEST_AD_UNIT_ID, spy(), spy())
        val rewardedAdShowCallback = adDisplayer.createRewardedAdShowCallback(TEST_AD_UNIT_ID, spy(), spy())
        val rewardItem = object : RewardItem {
            override fun getType(): String = ""

            override fun getAmount(): Int = 0
        }

        rewardedAdLoadCallback.onRewardedAdLoaded()
        rewardedAdLoadCallback.onRewardedAdFailedToLoad(LoadAdError(0, "", "", null, null))

        rewardedAdShowCallback.onUserEarnedReward(rewardItem)
        rewardedAdShowCallback.onRewardedAdClosed()
    }

    @Test
    fun `should invoke error when rewarded ads when is not ready`() {
        val onError: (msg: String) -> Unit = {
            it shouldBe "Ad is not loaded yet"
        }
        adDisplayer.showRewardedAd(TEST_AD_UNIT_ID, {}, onError)

        adDisplayer.loadRewardedAd(TEST_AD_UNIT_ID, {}, {})
        adDisplayer.showRewardedAd(TEST_AD_UNIT_ID, {}, onError)
    }

    @Test
    fun `should invoke error when rewarded ads is already on queue`() {
        val onError: (msg: String) -> Unit = {
            it shouldBeEqualTo "Previous $TEST_AD_UNIT_ID is still in progress"
        }
        adDisplayer.loadRewardedAd(TEST_AD_UNIT_ID, {}, {})

        adDisplayer.loadRewardedAd(TEST_AD_UNIT_ID, {}, onError)
    }

    @Test
    fun `should not have ad in queue when it loads failed`() {
        val map = HashMap<String, RewardedAd>()
        val ad = Mockito.spy(RewardedAd(context, TEST_AD_UNIT_ID))
        map[TEST_AD_UNIT_ID] = ad
        adDisplayer.initAdMap(interstitialAdMap = mutableMapOf(), rewardedAdMap = map)

        val rewardedAdLoadCallback = adDisplayer.createRewardedAdLoadCallback(TEST_AD_UNIT_ID, spy(), spy())
        rewardedAdLoadCallback.onRewardedAdLoaded()
        rewardedAdLoadCallback.onRewardedAdFailedToLoad(LoadAdError(0, "", "", null, null))

        adDisplayer.rewardedAdMap.containsKey(TEST_AD_UNIT_ID) shouldBe false
    }
}
