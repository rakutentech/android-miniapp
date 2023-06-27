package com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalyticsConfig
import com.rakuten.tech.mobile.miniapp.testapp.BuildConfig
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.FeaturesFragmentBinding
import com.rakuten.tech.mobile.testapp.ui.base.BaseFragment
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.input.MiniAppByUrlActivity
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.BUNDLE_MINI_APP_ID
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.BUNDLE_MINI_APP_VERSION_ID
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class FeaturesFragment : BaseFragment() {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    private lateinit var binding: FeaturesFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.features_fragment,
            container,
            false
        )
        binding.fragment = this
        return binding.root
    }

    fun switchToInput() {
        raceExecutor.run {
            startActivity(Intent(requireActivity(), MiniAppByUrlActivity::class.java))
        }
    }

    fun openMiniAppFromBundle() {
        context?.let {
            MiniAppDisplayActivity.start(
                context = it,
                appId = BUNDLE_MINI_APP_ID+" ",
                versionId = BUNDLE_MINI_APP_VERSION_ID,
                miniAppSdkConfig = createSdkConfig(
                    AppSettings.instance.newMiniAppSdkConfig.rasProjectId,
                    AppSettings.instance.newMiniAppSdkConfig.subscriptionKey
                ),
                updatetype = true,
                fromBundle = true
            )
        }
    }

    private fun createSdkConfig(hostId: String, subscriptionKey: String): MiniAppSdkConfig {
        return MiniAppSdkConfig(
            baseUrl = AppSettings.instance.newMiniAppSdkConfig.baseUrl,
            rasProjectId = hostId,
            subscriptionKey = subscriptionKey,
            hostAppUserAgentInfo = AppSettings.instance.newMiniAppSdkConfig.hostAppUserAgentInfo,
            isPreviewMode = AppSettings.instance.newMiniAppSdkConfig.isPreviewMode,
            requireSignatureVerification = AppSettings.instance.newMiniAppSdkConfig.requireSignatureVerification,
            // temporarily taking values from buildConfig, we may add UI for this later.
            miniAppAnalyticsConfigList = listOf(
                MiniAppAnalyticsConfig(
                    BuildConfig.ADDITIONAL_ANALYTICS_ACC,
                    BuildConfig.ADDITIONAL_ANALYTICS_AID
                )
            ),
            sslPinningPublicKeyList = getSSlKeyList()
        )
    }

    private fun getSSlKeyList(): List<String> {
        return if (AppSettings.instance.miniAppSettings1.baseUrl != getString(R.string.prodBaseUrl)) listOf(
            getString(R.string.sslPublicKey), getString(R.string.sslPublicKeyBackup)
        ) else listOf(
            getString(R.string.sslPublicKeyProd), getString(R.string.sslPublicKeyProdBackup)
        )
    }
}
