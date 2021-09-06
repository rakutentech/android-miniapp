package com.rakuten.tech.mobile.testapp.ui.deeplink

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.preview.PreviewMiniApp
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppDisplayActivity
import com.rakuten.tech.mobile.testapp.ui.display.error.QRErrorWindow
import com.rakuten.tech.mobile.testapp.ui.display.preload.PreloadMiniAppWindow
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * This activity will be the gateway of all deeplink scheme.
 */
class SchemeActivity : BaseActivity(), PreloadMiniAppWindow.PreloadMiniAppLaunchListener {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    private val preloadMiniAppWindow by lazy { PreloadMiniAppWindow(this, this) }
    private var miniAppInfo: MiniAppInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        miniAppInfo = null
        intent?.data?.let { data ->
            if (data.pathSegments.size > 1) {
                //token value to get miniAppInfo.
                val code = data.pathSegments[1]
                CoroutineScope(Dispatchers.IO).launch{
                    try {
                        miniAppInfo = PreviewMiniApp.instance().getMiniAppInfoByPreviewCode(previewCode = code)
                        //miniAppInfo = MiniApp.instance().getMiniAppInfoByPreviewCode(previewCode = code)
                        //TODO: if get the miniapp successfully open the app in demo app.
                        Log.e("MiniAppInfo", miniAppInfo.toString())
                        CoroutineScope(Dispatchers.Main).launch {
                            miniAppInfo?.let {
                                preloadMiniAppWindow.initiate(
                                    it,
                                    it.id,
                                    it.version.versionId,
                                    this@SchemeActivity
                                )
                            }
                        }
                    }catch (e: MiniAppNotFoundException){
                        Log.e("error", e.localizedMessage)
                        CoroutineScope(Dispatchers.Main).launch {
                            QRErrorWindow.getInstance(this@SchemeActivity).showQRCodeExpiredError{
                                finish()
                            }
                        }
                    }
                    catch (e: MiniAppHostException){
                        Log.e("error", e.localizedMessage)
                        CoroutineScope(Dispatchers.Main).launch {
                            QRErrorWindow.getInstance(this@SchemeActivity).showMiniAppPermissionError{
                                finish()
                            }
                        }
                    }
                    catch (e: Exception){
                        Log.e("error", e.localizedMessage)
                        CoroutineScope(Dispatchers.Main).launch {
                            QRErrorWindow.getInstance(this@SchemeActivity).showMiniAppNoLongerExistError(){
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPreloadMiniAppResponse(isAccepted: Boolean) {
        if (isAccepted)
            miniAppInfo?.let { MiniAppDisplayActivity.start(this, it) }
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
