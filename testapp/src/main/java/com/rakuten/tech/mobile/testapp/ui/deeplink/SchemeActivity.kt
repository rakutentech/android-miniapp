package com.rakuten.tech.mobile.testapp.ui.deeplink

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalyticsConfig
import com.rakuten.tech.mobile.miniapp.testapp.BuildConfig
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.helper.AppCoroutines
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.display.error.QRCodeErrorType
import com.rakuten.tech.mobile.testapp.ui.display.error.QRErrorWindow
import com.rakuten.tech.mobile.testapp.ui.display.preload.PreloadMiniAppWindow
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.DemoAppMainActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import com.rakuten.tech.mobile.testapp.ui.settings.cache.MiniAppConfigData

/**
 * This activity will be the gateway of all deeplink scheme.
 */
const val INTENT_EXTRA_DEEPLINK = "isFromDeeplink"
class SchemeActivity : BaseActivity(), PreloadMiniAppWindow.PreloadMiniAppLaunchListener {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    private val preloadMiniAppWindow by lazy { PreloadMiniAppWindow(this, this) }
    private var miniAppInfo: MiniAppInfo? = null
    private var previewMiniAppInfo: PreviewMiniAppInfo? = null
    private var miniAppSdkConfig: MiniAppSdkConfig? = null
    private var miniApp: MiniApp? = null

    @Suppress("TooGenericExceptionCaught")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.let { data ->
            if (data.pathSegments.isNotEmpty() && data.pathSegments[0].equals(getString(R.string.settings_pathprefix).replace("/",""))) {
                // setup settings
                val tab = data.getQueryParameter("tab") ?: ""
                val projectId = data.getQueryParameter("projectid") ?: ""
                val subscriptionKey = data.getQueryParameter("subscription") ?: ""
                val isProduction = data.getBooleanQueryParameter("isProduction", false)
                val isPreviewMode = data.getBooleanQueryParameter("isPreviewMode", false)

                // Save the keys to prefs
                if(tab == "1") {
                    AppSettings.instance.setTempTab1ConfigData(
                        MiniAppConfigData(
                            isProduction = isProduction,
                            isPreviewMode = isPreviewMode,
                            isVerificationRequired =AppSettings.instance.getCurrentTab1ConfigData().isVerificationRequired,
                            projectId = projectId,
                            subscriptionId = subscriptionKey
                        )
                    )

                } else if (tab == "2") {
                    AppSettings.instance.setTempTab2ConfigData(
                        MiniAppConfigData(
                            isProduction = isProduction,
                            isPreviewMode = isPreviewMode,
                            isVerificationRequired =AppSettings.instance.getCurrentTab1ConfigData().isVerificationRequired,
                            projectId = projectId,
                            subscriptionId = subscriptionKey
                        )
                    )
                }
                startActivity(
                    Intent(this,
                        DemoAppMainActivity::class.java
                    ).putExtra(INTENT_EXTRA_DEEPLINK, true)
                )
                finish()
            } else if (data.pathSegments.size > 1) {
                miniAppInfo = null
                miniAppSdkConfig = createSdkConfig(
                    AppSettings.instance.newMiniAppSdkConfig.rasProjectId,
                    AppSettings.instance.newMiniAppSdkConfig.subscriptionKey
                )
                miniAppSdkConfig?.let { config ->
                    miniApp = MiniApp.instance(config, setConfigAsDefault = false)
                }

                val code = data.pathSegments[1]
                AppCoroutines.io {
                    try {
                        previewMiniAppInfo =
                            miniApp?.getMiniAppInfoByPreviewCode(previewCode = code)
                        miniAppInfo = previewMiniAppInfo?.miniapp
                        previewMiniAppInfo?.host?.let {
                            miniAppSdkConfig = createSdkConfig(
                                hostId = it.id,
                                subscriptionKey = it.subscriptionKey
                            )
                            miniApp =
                                MiniApp.instance(miniAppSdkConfig!!, setConfigAsDefault = false)
                        }
                        AppCoroutines.main {
                            miniAppInfo?.let { miniAppInfo ->
                                miniApp?.let { miniApp ->
                                    preloadMiniAppWindow.initiate(
                                        appInfo = miniAppInfo,
                                        miniAppIdAndVersionIdPair = Pair(
                                            miniAppInfo.id,
                                            miniAppInfo.version.versionId
                                        ),
                                        this@SchemeActivity,
                                        miniApp = miniApp
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        handleException(e)
                    }
                }
            }
        }
    }

    private fun handleException(e: Exception) {
        when (e) {
            is MiniAppNotFoundException -> {
                showErrorDialog(QRCodeErrorType.MiniAppNoLongerExist)
            }
            is MiniAppHostException -> {
                showErrorDialog(QRCodeErrorType.MiniAppNoPermission)
            }
            is SSLCertificatePinningException -> {
                Log.e("SSLCertificatePinningException", e.message ?: "")
                finish()
            }
            is MiniAppSdkException -> {
                showErrorDialog(QRCodeErrorType.MiniAppNoLongerExist)
            }
            else -> {
                e.printStackTrace()
            }
        }
    }

    override fun onPreloadMiniAppResponse(isAccepted: Boolean) {
        if (isAccepted)
            miniAppInfo?.let { MiniAppDisplayActivity.start(this, it, miniAppSdkConfig, true) }
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun showErrorDialog(type: QRCodeErrorType) {
        AppCoroutines.main {
            QRErrorWindow.getInstance(this).showMiniAppQRCodeError(errorType = type) {
                finish()
            }
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
