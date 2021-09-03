package com.rakuten.tech.mobile.testapp.ui.deeplink

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.display.error.QRErrorWindow
import com.rakuten.tech.mobile.testapp.ui.miniapplist.MiniAppListActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * This activity will be the gateway of all deeplink scheme.
 */
class SchemeActivity : BaseActivity() {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.let { data ->
            if (data.pathSegments.size > 1) {
                //token value to get miniAppInfo.
                val code = data.pathSegments[1]
                CoroutineScope(Dispatchers.IO).launch{
                    try {
                        val miniAppInfo = MiniApp.instance().getMiniAppInfoByPreviewCode(previewCode = code)
                        //TODO: if get the miniapp successfully open the app in demo app.
                    }catch (e: Exception){
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
}
