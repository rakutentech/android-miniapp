package com.rakuten.tech.mobile.testapp.ui.display

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.permission.MiniAppPermissionManager
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.MiniAppPermissionPlatform
import com.rakuten.tech.mobile.miniapp.permission.MiniAppPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.helper.AppPermission
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.android.synthetic.main.mini_app_display_activity.*

class MiniAppDisplayActivity : BaseActivity(),
    MiniAppPermissionManager.OnRequestPermissionResultCallback {

    private lateinit var appId: String
    private lateinit var miniAppMessageBridge: MiniAppMessageBridge
    private var miniappPermissionCallback: (isGranted: Boolean) -> Unit = {}

    companion object {
        private val appIdTag = "app_id_tag"
        private val miniAppTag = "mini_app_tag"

        fun start(context: Context, appId: String) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(appIdTag, appId)
            })
        }

        fun start(context: Context, miniAppInfo: MiniAppInfo) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(miniAppTag, miniAppInfo)
            })
        }
    }

    private lateinit var viewModel: MiniAppDisplayViewModel

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()

        if (intent.hasExtra(miniAppTag) || intent.hasExtra(appIdTag)) {
            appId = intent.getStringExtra(appIdTag) ?: ""

            setContentView(R.layout.mini_app_display_activity)

            viewModel = ViewModelProvider.NewInstanceFactory()
                .create(MiniAppDisplayViewModel::class.java).apply {

                    setHostLifeCycle(lifecycle)
                    miniAppView.observe(this@MiniAppDisplayActivity, Observer {
                        if (ApplicationInfo.FLAG_DEBUGGABLE == 2)
                            WebView.setWebContentsDebuggingEnabled(true)
                        //action: display webview
                        setContentView(it)
                    })

                    errorData.observe(this@MiniAppDisplayActivity, Observer {
                        Toast.makeText(this@MiniAppDisplayActivity, it, Toast.LENGTH_LONG).show()
                    })

                    isLoading.observe(this@MiniAppDisplayActivity, Observer {
                        toggleProgressLoading(it)
                    })
                }

            miniAppMessageBridge = object : MiniAppMessageBridge() {
                override fun getUniqueId() = AppSettings.instance.uniqueId

                override fun requestPermission(
                    miniAppPermissionType: MiniAppPermissionType,
                    callback: (isGranted: Boolean) -> Unit
                ) {
                    miniappPermissionCallback = callback

                    when (miniAppPermissionType.platform) {
                        MiniAppPermissionPlatform.Android.name -> {
                            // requesting Android specific permission
                            ActivityCompat.requestPermissions(
                                this@MiniAppDisplayActivity,
                                AppPermission.getPermissionRequest(miniAppPermissionType),
                                AppPermission.getRequestCode(miniAppPermissionType)
                            )
                        }
                        MiniAppPermissionPlatform.MiniApp.name -> {
                            // requesting MiniApp SDK specific permission
                            MiniApp.requestPermission(
                                this@MiniAppDisplayActivity,
                                AppPermission.getSinglePermissionRequest(miniAppPermissionType)
                            )
                        }
                    }
                }
            }

            if (appId.isEmpty())
                viewModel.obtainMiniAppDisplay(
                    this@MiniAppDisplayActivity,
                    intent.getParcelableExtra<MiniAppInfo>(miniAppTag)!!.id,
                    miniAppMessageBridge
                )
            else
                viewModel.obtainMiniAppDisplay(
                    this@MiniAppDisplayActivity,
                    appId,
                    miniAppMessageBridge
                )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        val isGranted = !grantResults.contains(PackageManager.PERMISSION_DENIED)
        miniappPermissionCallback.invoke(isGranted)
    }

    override fun onRequestPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        miniappPermissionCallback.invoke(isGranted)
    }

    private fun toggleProgressLoading(isOn: Boolean) {
        if (findViewById<View>(R.id.pb) != null) {
            when (isOn) {
                true -> pb.visibility = View.VISIBLE
                false -> pb.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed() {
        if (!viewModel.canGoBackwards()) {
            super.onBackPressed()
        }
    }
}
