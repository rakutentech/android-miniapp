package com.rakuten.tech.mobile.testapp.ui.display

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.ads.AdMobDisplayer
import com.rakuten.tech.mobile.miniapp.file.MiniAppCameraPermissionDispatcher
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooserDefault
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileDownloaderDefault
import com.rakuten.tech.mobile.miniapp.iap.*
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.NativeEventType
import com.rakuten.tech.mobile.miniapp.navigator.ExternalResultHandler
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniAppDisplayActivityBinding
import com.rakuten.tech.mobile.testapp.helper.*
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.chat.ChatWindow
import com.rakuten.tech.mobile.testapp.ui.display.preload.PreloadMiniAppWindow
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

private const val MINI_APP_EXTERNAL_WEBVIEW_REQUEST_CODE = 100
private const val MINI_APP_FILE_CHOOSING_REQUEST_CODE = 10101
private const val MINI_APP_FILE_DOWNLOAD_REQUEST_CODE = 10102

class MiniAppDisplayActivity : BaseActivity(), PreloadMiniAppWindow.PreloadMiniAppLaunchListener {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    private lateinit var miniAppMessageBridge: MiniAppMessageBridge
    private lateinit var miniAppNavigator: MiniAppNavigator
    private var miniappPermissionCallback: (isGranted: Boolean) -> Unit = {}
    private var miniappCameraPermissionCallback: (isGranted: Boolean) -> Unit = {}
    private lateinit var sampleWebViewExternalResultHandler: ExternalResultHandler
    private lateinit var binding: MiniAppDisplayActivityBinding
    private var isFromMiniAppByUrlActivity = false
    private val preloadMiniAppWindow by lazy { PreloadMiniAppWindow(this, this) }
    private val miniAppCameraPermissionDispatcher = object : MiniAppCameraPermissionDispatcher {
        override fun getCameraPermission(permissionCallback: (isGranted: Boolean) -> Unit) {
            if (ContextCompat.checkSelfPermission(
                    this@MiniAppDisplayActivity,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permissionCallback(true)
            } else {
                permissionCallback(false)
            }
        }

        override fun requestCameraPermission(
            miniAppPermissionType: MiniAppDevicePermissionType,
            permissionRequestCallback: (isGranted: Boolean) -> Unit
        ) {
            miniappCameraPermissionCallback = permissionRequestCallback
            ActivityCompat.requestPermissions(
                this@MiniAppDisplayActivity,
                AppDevicePermission.getDevicePermissionRequest(miniAppPermissionType),
                AppDevicePermission.getDeviceRequestCode(miniAppPermissionType)
            )
        }
    }
    private val miniAppFileChooser = MiniAppFileChooserDefault(
        requestCode = MINI_APP_FILE_CHOOSING_REQUEST_CODE,
        miniAppCameraPermissionDispatcher = miniAppCameraPermissionDispatcher
    )

    private val miniAppFileDownloader = MiniAppFileDownloaderDefault(
        activity = this,
        requestCode = MINI_APP_FILE_DOWNLOAD_REQUEST_CODE
    )

    private var appInfo: MiniAppInfo? = null
    private var appId: String? = null
    private var versionId: String? = null
    private var fromBundle = false
    private var appUrl: String? = null
    private var previousClickTimeMillis = 0L

    companion object {
        private const val appIdTag = "app_id_tag"
        private const val versionIdTag = "version_id_tag"
        private const val miniAppTag = "mini_app_tag"
        private const val appUrlTag = "app_url_tag"
        private const val sdkConfigTag = "sdk_config_tag"
        private const val updateTypeTag = "update_type_tag"
        private const val fromBundleTypeTag = "from_bundle_type_tag"
        private const val isFromMiniAppByUrlActivityTag = "is_from_miniapp_by_url_tag"

        fun start(
            context: Context,
            appId: String,
            versionId: String = "",
            miniAppSdkConfig: MiniAppSdkConfig? = null,
            updatetype: Boolean = false,
            fromBundle: Boolean = false
        ) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(appIdTag, appId)
                putExtra(versionIdTag, versionId)
                putExtra(updateTypeTag, updatetype)
                putExtra(fromBundleTypeTag, fromBundle)
                miniAppSdkConfig?.let { putExtra(sdkConfigTag, it) }
            })
        }

        fun startUrl(
            context: Context,
            appUrl: String,
            miniAppSdkConfig: MiniAppSdkConfig? = null,
            updatetype: Boolean = false,
            isFromMiniAppActivity: Boolean = true,
        ) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(appUrlTag, appUrl)
                putExtra(updateTypeTag, updatetype)
                putExtra(isFromMiniAppByUrlActivityTag, isFromMiniAppActivity)
                miniAppSdkConfig?.let { putExtra(sdkConfigTag, it) }
            })
        }

        fun start(
            context: Context,
            miniAppInfo: MiniAppInfo,
            miniAppSdkConfig: MiniAppSdkConfig? = null,
            updatetype: Boolean = false
        ) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(miniAppTag, miniAppInfo)
                putExtra(updateTypeTag, updatetype)
                miniAppSdkConfig?.let { putExtra(sdkConfigTag, it) }
            })
        }
    }

    private lateinit var viewModel: MiniAppDisplayViewModel

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.miniapp_display_menu, menu)
        menu.findItem(R.id.share_mini_app).isVisible = !fromBundle
        menu.findItem(R.id.settings_permission_mini_app).isVisible = !fromBundle
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            android.R.id.home -> {
                checkCloseAlert()
                true
            }
            R.id.share_mini_app -> {
                appInfo?.let {
                    MiniAppShareWindow.getInstance(
                        this,
                        onShow = miniAppMessageBridge::dispatchOnPauseEvent,
                        onDismiss = miniAppMessageBridge::dispatchOnResumeEvent,
                    ).show(miniAppInfo = it)
                }
                true
            }
            R.id.settings_permission_mini_app -> {
                singleSafeClick(previousClickTimeMillis) { tappedTime ->
                    previousClickTimeMillis = tappedTime
                    launchCustomPermissionDialog()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun launchCustomPermissionDialog() {
        appInfo?.let {
            preloadMiniAppWindow.initiate(
                appInfo = appInfo,
                miniAppIdAndVersionIdPair = Pair(
                    it.id,
                    it.version.versionId
                ),
                this,
                shouldShowDialog = true,
                onShow = miniAppMessageBridge::dispatchOnPauseEvent,
                onDismiss = miniAppMessageBridge::dispatchOnResumeEvent
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        setResizableSoftInputMode(activity = this)
        if (!(intent.hasExtra(miniAppTag) || intent.hasExtra(appIdTag) || intent.hasExtra(appUrlTag))) {
            return
        }

        //Three different ways to get miniapp.
        appInfo = intent.getParcelableExtra(miniAppTag)
        appId = intent.getStringExtra(appIdTag) ?: appInfo?.id
        versionId = intent.getStringExtra(versionIdTag)
        appUrl = intent.getStringExtra(appUrlTag)
        var miniAppSdkConfig = intent.getParcelableExtra<MiniAppSdkConfig>(sdkConfigTag)
        val updateType = intent.getBooleanExtra(updateTypeTag, false)
        fromBundle = intent.getBooleanExtra(fromBundleTypeTag, false)

        if (miniAppSdkConfig == null)
            miniAppSdkConfig = AppSettings.instance.newMiniAppSdkConfig
        binding = DataBindingUtil.setContentView(this, R.layout.mini_app_display_activity)

        val factory = MiniAppDisplayViewModelFactory(MiniApp.instance(miniAppSdkConfig, updateType))
        viewModel =
            ViewModelProvider(this, factory).get(MiniAppDisplayViewModel::class.java).apply {
                miniAppView.observe(this@MiniAppDisplayActivity) {
                    if (ApplicationInfo.FLAG_DEBUGGABLE == 2)
                        WebView.setWebContentsDebuggingEnabled(true)

                    //action: display webview

                    addLifeCycleObserver(lifecycle)
                    setContentView(it)
                }

                errorData.observe(this@MiniAppDisplayActivity) {
                    Toast.makeText(this@MiniAppDisplayActivity, it, Toast.LENGTH_LONG).show()
                    delayUIThread {
                        finish()
                    }
                }

                isLoading.observe(this@MiniAppDisplayActivity) {
                    toggleProgressLoading(it)
                }

                containTooManyRequestsError.observe(this@MiniAppDisplayActivity) {
                    showErrorDialog(
                        this@MiniAppDisplayActivity,
                        getString(R.string.error_desc_miniapp_too_many_request)
                    )
                }
            }

        FileUtils.miniAppOpenLogs(this, appId ?: appInfo?.id ?: appUrl ?: "")

        setupMiniAppMessageBridge()

        miniAppNavigator = object : MiniAppNavigator {

            override fun openExternalUrl(
                url: String,
                externalResultHandler: ExternalResultHandler
            ) {
                isFromMiniAppByUrlActivity =
                    intent.getBooleanExtra(isFromMiniAppByUrlActivityTag, false)
                if (AppSettings.instance.dynamicDeeplinks.contains(url)) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) })
                    } catch (e: Exception) {
                        showAlertDialog(
                            this@MiniAppDisplayActivity,
                            "Warning!",
                            e.message.toString()
                        )
                    }
                } else {
                    sampleWebViewExternalResultHandler = externalResultHandler
                    WebViewActivity.startForResult(
                        activity = this@MiniAppDisplayActivity,
                        url = url,
                        appId = appId,
                        appUrl = appUrl,
                        externalWebViewReqCode = MINI_APP_EXTERNAL_WEBVIEW_REQUEST_CODE,
                    )
                }
            }
        }

        loadMiniApp()
    }

    private fun loadMiniApp() {
        appUrl?.let {
            if (it.isNotBlank()) {
                viewModel.obtainNewMiniAppDisplayUrl(
                    this@MiniAppDisplayActivity,
                    it,
                    miniAppMessageBridge,
                    miniAppNavigator,
                    miniAppFileChooser,
                    AppSettings.instance.urlParameters
                )
            }
            return
        }
        appInfo?.let {
            viewModel.obtainMiniAppDisplay(
                this@MiniAppDisplayActivity,
                it,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser,
                AppSettings.instance.urlParameters
            )
        }
        if(fromBundle){
            supportActionBar?.title = "MiniApp"
            viewModel.obtainMiniAppDisplayFromBundle(
                context = this@MiniAppDisplayActivity,
                appId = appId!!,
                versionId = versionId!!,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser,
            )
        }
    }

    @Suppress("OverridingDeprecatedMember")
    private fun setupMiniAppMessageBridge() {
        miniAppMessageBridge = getMessageBridge(this) { onDevicePermissionResultCallback ->
            miniappPermissionCallback = onDevicePermissionResultCallback
        }
        miniAppMessageBridge.setAdMobDisplayer(AdMobDisplayer(this@MiniAppDisplayActivity))
        miniAppMessageBridge.allowScreenOrientation(true)

        // setup UserInfoBridgeDispatcher
        val userInfoBridgeDispatcher = getUserInfoBridgeDispatcher()
        miniAppMessageBridge.setUserInfoBridgeDispatcher(userInfoBridgeDispatcher)

        // setup ChatBridgeDispatcher
        val chatWindow = ChatWindow(this@MiniAppDisplayActivity)
        val chatBridgeDispatcher = getChatBridgeDispatcher(chatWindow)
        miniAppMessageBridge.setChatBridgeDispatcher(chatBridgeDispatcher)
        miniAppMessageBridge.setMiniAppFileDownloader(miniAppFileDownloader)

        // setup InAppPurchaseProvider
        miniAppMessageBridge.setInAppPurchaseProvider(InAppPurchaseProviderDefault(this@MiniAppDisplayActivity))
        miniAppMessageBridge.setMiniAppCloseListener { withConfirmationAlert ->
            if (withConfirmationAlert) checkCloseAlert() else finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val isGranted = !grantResults.contains(PackageManager.PERMISSION_DENIED)
        when (requestCode) {
            AppDevicePermission.ReqCode.CAMERA -> miniappCameraPermissionCallback.invoke(isGranted)
            else -> miniappPermissionCallback.invoke(isGranted)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (Activity.RESULT_OK != resultCode) {
            miniAppFileChooser.onCancel()
            miniAppFileDownloader.onCancel()
        }

        when {
            requestCode == MINI_APP_EXTERNAL_WEBVIEW_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                data?.let { intent ->
                    val isClosedByBackPressed =
                        intent.getBooleanExtra("isClosedByBackPressed", false)
                    miniAppMessageBridge.dispatchNativeEvent(
                        NativeEventType.EXTERNAL_WEBVIEW_CLOSE,
                        "External webview closed"
                    )
                    if (!isClosedByBackPressed) {
                        sampleWebViewExternalResultHandler.emitResult(intent)
                    }

                    handleRedirectUrlPage()
                }
            }
            requestCode == MINI_APP_FILE_CHOOSING_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                miniAppFileChooser.onReceivedFiles(data)
            }
            requestCode == MINI_APP_FILE_DOWNLOAD_REQUEST_CODE -> {
                data?.data?.let { destinationUri ->
                    miniAppFileDownloader.onReceivedResult(destinationUri)
                }
            }
        }
    }

    /**
     * Handles a blank page caused by a redirect url.
     * The valid state is whenever either this page is entered from deeplink or by url,
     * would not navigate to external webview right away,
     */
    private fun handleRedirectUrlPage() {
        if (isFromMiniAppByUrlActivity) {
            finish()
        }
    }

    private fun toggleProgressLoading(isOn: Boolean) = when (isOn) {
        true -> binding.pb.visibility = View.VISIBLE
        false -> binding.pb.visibility = View.GONE
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun checkCloseAlert() {
        try {
            val closeAlertInfo = miniAppMessageBridge.miniAppShouldClose()
            if (closeAlertInfo?.shouldDisplay!!) {
                val dialogClickListener =
                    DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                FileUtils.miniAppCloseLogs(this, appId ?: appInfo?.id ?: "")
                                finish()
                            }
                        }
                    }

                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MiniAppDisplayActivity)
                builder.setTitle(closeAlertInfo.title)
                    .setMessage(closeAlertInfo.description)
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
            } else {
                FileUtils.miniAppCloseLogs(this, appId ?: appInfo?.id ?: appUrl ?: "")
                finish()
            }
        } catch (e: NullPointerException) {
            FileUtils.miniAppCloseLogs(this, appId ?: appInfo?.id ?: appUrl ?: "")
            finish()
        }
    }

    override fun onBackPressed() {
        viewModel.onBackPressed {
            checkCloseAlert()
        }
    }

    override fun onPause() {
        super.onPause()
        miniAppMessageBridge.dispatchOnPauseEvent()
    }

    override fun onResume() {
        super.onResume()
        miniAppMessageBridge.dispatchOnResumeEvent()
    }

    override fun onPreloadMiniAppResponse(isAccepted: Boolean) {
        // Implementation not needed
    }
}
