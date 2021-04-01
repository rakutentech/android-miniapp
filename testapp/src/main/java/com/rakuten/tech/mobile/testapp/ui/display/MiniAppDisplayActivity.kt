package com.rakuten.tech.mobile.testapp.ui.display

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.ads.AdMobDisplayer
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooserDefault
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.js.userinfo.TokenData
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridgeDispatcher
import com.rakuten.tech.mobile.miniapp.navigator.ExternalResultHandler
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope
import com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniAppDisplayActivityBinding
import com.rakuten.tech.mobile.testapp.helper.AppPermission
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import java.util.*

class MiniAppDisplayActivity : BaseActivity() {

    private lateinit var miniAppMessageBridge: MiniAppMessageBridge
    private lateinit var miniAppNavigator: MiniAppNavigator
    private var miniappPermissionCallback: (isGranted: Boolean) -> Unit = {}
    private lateinit var sampleWebViewExternalResultHandler: ExternalResultHandler
    private lateinit var binding: MiniAppDisplayActivityBinding

    private val externalWebViewReqCode = 100
    private val fileChoosingReqCode = 10101
    private val miniAppFileChooser = MiniAppFileChooserDefault(requestCode = fileChoosingReqCode)

    companion object {
        private val appIdTag = "app_id_tag"
        private val miniAppTag = "mini_app_tag"
        private val appUrlTag = "app_url_tag"

        fun start(context: Context, appId: String) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(appIdTag, appId)
            })
        }

        fun startUrl(context: Context, appUrl: String) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(appUrlTag, appUrl)
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

        if (!(intent.hasExtra(miniAppTag) || intent.hasExtra(appIdTag) || intent.hasExtra(appUrlTag))) {
            return
        }

        //Three different ways to get miniapp.
        val appInfo = intent.getParcelableExtra<MiniAppInfo>(miniAppTag)
        val appId = intent.getStringExtra(appIdTag) ?: appInfo?.id
        val appUrl = intent.getStringExtra(appUrlTag)

        binding = DataBindingUtil.setContentView(this, R.layout.mini_app_display_activity)

        viewModel = ViewModelProvider.NewInstanceFactory()
            .create(MiniAppDisplayViewModel::class.java).apply {

                miniAppView.observe(this@MiniAppDisplayActivity, Observer {
                    if (ApplicationInfo.FLAG_DEBUGGABLE == 2)
                        WebView.setWebContentsDebuggingEnabled(true)
                    //action: display webview
                    addLifeCycleObserver(lifecycle)
                    setContentView(it)
                })

                errorData.observe(this@MiniAppDisplayActivity, Observer {
                    Toast.makeText(this@MiniAppDisplayActivity, it, Toast.LENGTH_LONG).show()
                })

                isLoading.observe(this@MiniAppDisplayActivity, Observer {
                    toggleProgressLoading(it)
                })
            }

        setupMiniAppMessageBridge()

        miniAppNavigator = object : MiniAppNavigator {

            override fun openExternalUrl(url: String, externalResultHandler: ExternalResultHandler) {
                sampleWebViewExternalResultHandler = externalResultHandler
                WebViewActivity.startForResult(this@MiniAppDisplayActivity, url,
                    appId, appUrl, externalWebViewReqCode)
            }
        }

        if (appUrl != null) {
            viewModel.obtainMiniAppDisplayUrl(
                this@MiniAppDisplayActivity,
                appUrl,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser,
                AppSettings.instance.urlParameters
            )
        } else
            viewModel.obtainMiniAppDisplay(
                this@MiniAppDisplayActivity,
                appInfo,
                appId!!,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser,
                AppSettings.instance.urlParameters
            )
    }

    private fun setupMiniAppMessageBridge() {
        miniAppMessageBridge = object : MiniAppMessageBridge() {
            override fun getUniqueId() = AppSettings.instance.uniqueId

            override fun requestDevicePermission(
                miniAppPermissionType: MiniAppDevicePermissionType,
                callback: (isGranted: Boolean) -> Unit
            ) {
                miniappPermissionCallback = callback
                ActivityCompat.requestPermissions(
                    this@MiniAppDisplayActivity,
                    AppPermission.getDevicePermissionRequest(miniAppPermissionType),
                    AppPermission.getDeviceRequestCode(miniAppPermissionType)
                )
            }
        }

        miniAppMessageBridge.setAdMobDisplayer(AdMobDisplayer(this@MiniAppDisplayActivity))
        miniAppMessageBridge.allowScreenOrientation(true)

        val userInfoBridgeDispatcher = object : UserInfoBridgeDispatcher {

            override fun getUserName(
                onSuccess: (userName: String) -> Unit,
                onError: (message: String) -> Unit
            ) {
                val name = AppSettings.instance.profileName
                if (name.isNotEmpty()) onSuccess(name)
                else onError("User name is not found.")
            }

            override fun getProfilePhoto(
                onSuccess: (profilePhoto: String) -> Unit,
                onError: (message: String) -> Unit
            ) {
                val photo = AppSettings.instance.profilePictureUrlBase64
                if (photo.isNotEmpty()) onSuccess(photo)
                else onError("Profile photo is not found.")
            }

            override fun getAccessToken(
                    miniAppId: String,
                    accessTokenScope: AccessTokenScope,
                    onSuccess: (tokenData: TokenData) -> Unit,
                    onError: (message: String) -> Unit
            ) = onSuccess(AppSettings.instance.tokenData)

            override fun getContacts(
                onSuccess: (contacts: ArrayList<Contact>) -> Unit,
                onError: (message: String) -> Unit
            ) {
                if (AppSettings.instance.isContactsSaved)
                    onSuccess(AppSettings.instance.contacts)
                else
                    onError("There is no contact found in HostApp.")
            }
        }
        miniAppMessageBridge.setUserInfoBridgeDispatcher(userInfoBridgeDispatcher)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val isGranted = !grantResults.contains(PackageManager.PERMISSION_DENIED)
        miniappPermissionCallback.invoke(isGranted)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == externalWebViewReqCode && resultCode == Activity.RESULT_OK) {
            data?.let { intent -> sampleWebViewExternalResultHandler.emitResult(intent) }
        } else if (requestCode == fileChoosingReqCode && resultCode == Activity.RESULT_OK) {
            data?.let { intent ->
                miniAppFileChooser.onReceivedFiles(intent)
            }
        }
    }

    private fun toggleProgressLoading(isOn: Boolean) {
        when (isOn) {
            true -> binding.pb.visibility = View.VISIBLE
            false -> binding.pb.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        if (!viewModel.canGoBackwards()) {
            super.onBackPressed()
        }
    }
}
