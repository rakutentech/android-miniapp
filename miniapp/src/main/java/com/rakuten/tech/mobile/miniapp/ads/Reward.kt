package com.rakuten.tech.mobile.miniapp.ads

import androidx.annotation.Keep

/** Earn the reward from rewarded ad of AdMob. **/
@Keep
data class Reward(val type: String, val amount: Int)
