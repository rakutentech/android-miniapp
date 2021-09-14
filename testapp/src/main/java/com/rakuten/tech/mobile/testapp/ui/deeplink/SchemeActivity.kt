package com.rakuten.tech.mobile.testapp.ui.deeplink

import android.os.Bundle
import android.util.Log
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalyticsConfig
import com.rakuten.tech.mobile.miniapp.testapp.BuildConfig
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.display.error.QRCodeErrorType
import com.rakuten.tech.mobile.testapp.ui.display.error.QRErrorWindow
import com.rakuten.tech.mobile.testapp.ui.display.preload.PreloadMiniAppWindow
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

/**
 * This activity will be the gateway of all deeplink scheme.
 */
class SchemeActivity : BaseActivity(), PreloadMiniAppWindow.PreloadMiniAppLaunchListener {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    private val preloadMiniAppWindow by lazy { PreloadMiniAppWindow(this, this) }
    private var miniAppInfo: MiniAppInfo? = null
    private var previewMiniAppInfo: PreviewMiniAppInfo? = null
    private var miniAppSdkConfig: MiniAppSdkConfig = AppSettings.instance.miniAppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        miniAppInfo = null
        intent?.data?.let { data ->
            if (data.pathSegments.size > 1) {
                val code = data.pathSegments[1]
                Coroutines.IO {
                    try {
                        previewMiniAppInfo =
                            MiniApp.instance().getMiniAppInfoByPreviewCode(previewCode = code)
                        miniAppInfo = previewMiniAppInfo?.miniapp
                        previewMiniAppInfo?.host?.let {
                            miniAppSdkConfig = createSdkConfig(
                                hostId = it.id,
                                subscriptionKey = it.subscriptionKey
                            )
                        }
                        Coroutines.main {
                            miniAppInfo?.let {
                                preloadMiniAppWindow.initiate(
                                    it,
                                    it.id,
                                    it.version.versionId,
                                    this@SchemeActivity,
                                    MiniApp.instance(miniAppSdkConfig, setConfigAsDefault = false)
                                )
                            }
                        }
                    } catch (e: MiniAppNotFoundException) {
                        showErrorDialog(QRCodeErrorType.MiniAppNoLongerExist)
                    } catch (e: MiniAppHostException) {
                        showErrorDialog(QRCodeErrorType.MiniAppNoPermission)
                    } catch (e: SSLCertificatePinnigException) {
                        Log.e("SSLCertificatePinnigException", e.message ?: "")
                        finish()
                    } catch (e: MiniAppSdkException) {
                        showErrorDialog(QRCodeErrorType.MiniAppNoLongerExist)
                    }
                }
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

    private fun showErrorDialog(type: QRCodeErrorType, miniAppVersion: String = ""){
        Coroutines.main {
            QRErrorWindow.getInstance(this@SchemeActivity).showMiniAppQRCodeError(errorType = type){
                finish()
            }
        }
    }

    private fun createSdkConfig(hostId: String, subscriptionKey: String): MiniAppSdkConfig {
        return MiniAppSdkConfig(
            baseUrl = AppSettings.instance.miniAppSettings.baseUrl,
            rasProjectId = hostId,
            subscriptionKey = subscriptionKey,
            hostAppUserAgentInfo = AppSettings.instance.miniAppSettings.hostAppUserAgentInfo,
            isPreviewMode = AppSettings.instance.miniAppSettings.isPreviewMode,
            // temporarily taking values from buildConfig, we may add UI for this later.
            miniAppAnalyticsConfigList = listOf(
                MiniAppAnalyticsConfig(
                    BuildConfig.ADDITIONAL_ANALYTICS_ACC,
                    BuildConfig.ADDITIONAL_ANALYTICS_AID
                )
            ),
            sslPinningPublicKey = AppSettings.instance.miniAppSettings.sslPinningPublicKey
        )
    }
}
