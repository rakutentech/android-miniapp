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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.ads.AdMobDisplayer
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.errors.MiniAppPointsError
import com.rakuten.tech.mobile.miniapp.file.MiniAppCameraPermissionDispatcher
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooserDefault
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileDownloaderDefault
import com.rakuten.tech.mobile.miniapp.js.MessageToContact
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.NativeEventType
import com.rakuten.tech.mobile.miniapp.js.chat.ChatBridgeDispatcher
import com.rakuten.tech.mobile.miniapp.js.userinfo.*
import com.rakuten.tech.mobile.miniapp.navigator.ExternalResultHandler
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope
import com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniAppDisplayActivityBinding
import com.rakuten.tech.mobile.testapp.helper.AppPermission
import com.rakuten.tech.mobile.testapp.helper.setResizableSoftInputMode
import com.rakuten.tech.mobile.testapp.helper.showAlertDialog
import com.rakuten.tech.mobile.testapp.helper.showErrorDialog
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.chat.ChatWindow
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import java.lang.NullPointerException
import java.util.*

class MiniAppDisplayActivity : BaseActivity() {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    private lateinit var miniAppMessageBridge: MiniAppMessageBridge
    private lateinit var miniAppNavigator: MiniAppNavigator
    private var miniappPermissionCallback: (isGranted: Boolean) -> Unit = {}
    private var miniappCameraPermissionCallback: (isGranted: Boolean) -> Unit = {}
    private lateinit var sampleWebViewExternalResultHandler: ExternalResultHandler
    private lateinit var binding: MiniAppDisplayActivityBinding

    private val externalWebViewReqCode = 100
    private val fileChoosingReqCode = 10101
    private val MINI_APP_FILE_DOWNLOAD_REQUEST_CODE = 10102
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
                AppPermission.getDevicePermissionRequest(miniAppPermissionType),
                AppPermission.getDeviceRequestCode(miniAppPermissionType)
            )
        }
    }
    private val miniAppFileChooser = MiniAppFileChooserDefault(
        requestCode = fileChoosingReqCode,
        miniAppCameraPermissionDispatcher = miniAppCameraPermissionDispatcher
    )

    private val miniAppFileDownloader = MiniAppFileDownloaderDefault(
        activity = this,
        requestCode = MINI_APP_FILE_DOWNLOAD_REQUEST_CODE
    )

    private var appInfo: MiniAppInfo? = null

    companion object {
        private const val appIdTag = "app_id_tag"
        private const val miniAppTag = "mini_app_tag"
        private const val appUrlTag = "app_url_tag"
        private const val sdkConfigTag = "sdk_config_tag"
        private const val updateTypeTag = "update_type_tag"

        fun start(context: Context, appId: String, miniAppSdkConfig: MiniAppSdkConfig? = null, updatetype: Boolean = false) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(appIdTag, appId)
                putExtra(updateTypeTag, updatetype)
                miniAppSdkConfig?.let { putExtra(sdkConfigTag, it) }
            })
        }

        fun startUrl(context: Context, appUrl: String, miniAppSdkConfig: MiniAppSdkConfig? = null, updatetype: Boolean = false) {
            context.startActivity(Intent(context, MiniAppDisplayActivity::class.java).apply {
                putExtra(appUrlTag, appUrl)
                putExtra(updateTypeTag, updatetype)
                miniAppSdkConfig?.let { putExtra(sdkConfigTag, it) }
            })
        }

        fun start(context: Context, miniAppInfo: MiniAppInfo, miniAppSdkConfig: MiniAppSdkConfig? = null, updatetype: Boolean = false) {
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
                    MiniAppShareWindow.getInstance(this).show(miniAppInfo = it)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        val appId = intent.getStringExtra(appIdTag) ?: appInfo?.id
        val appUrl = intent.getStringExtra(appUrlTag)
        var miniAppSdkConfig = intent.getParcelableExtra<MiniAppSdkConfig>(sdkConfigTag)
        val updateType = intent.getBooleanExtra(updateTypeTag, false)

        if(miniAppSdkConfig == null)
            miniAppSdkConfig = AppSettings.instance.newMiniAppSdkConfig

        binding = DataBindingUtil.setContentView(this, R.layout.mini_app_display_activity)

        val factory = MiniAppDisplayViewModelFactory(MiniApp.instance(miniAppSdkConfig, updateType))
        viewModel = ViewModelProvider(this, factory).get(MiniAppDisplayViewModel::class.java).apply {
            miniAppView.observe(this@MiniAppDisplayActivity) {
                if (ApplicationInfo.FLAG_DEBUGGABLE == 2)
                    WebView.setWebContentsDebuggingEnabled(true)

                //action: display webview
                addLifeCycleObserver(lifecycle)
                setContentView(it)
            }

            errorData.observe(this@MiniAppDisplayActivity) {
                Toast.makeText(this@MiniAppDisplayActivity, it, Toast.LENGTH_LONG).show()
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

        setupMiniAppMessageBridge()

        miniAppNavigator = object : MiniAppNavigator {

            override fun openExternalUrl(url: String, externalResultHandler: ExternalResultHandler) {
                if (AppSettings.instance.dynamicDeeplinks.contains(url)) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) })
                    } catch (e: Exception) {
                        showAlertDialog(this@MiniAppDisplayActivity, "Warning!", e.message.toString())
                    }
                } else {
                    sampleWebViewExternalResultHandler = externalResultHandler
                    WebViewActivity.startForResult(this@MiniAppDisplayActivity, url,
                            appId, appUrl, externalWebViewReqCode)
                }
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
        // setup MiniAppMessageBridge
        miniAppMessageBridge = object : MiniAppMessageBridge() {

            override fun getUniqueId(
                    onSuccess: (uniqueId: String) -> Unit,
                    onError: (message: String) -> Unit
            ) {
                val errorMsg = AppSettings.instance.uniqueIdError
                if (errorMsg.isNotEmpty()) onError(errorMsg)
                else onSuccess(AppSettings.instance.uniqueId)
            }

            override fun getMessagingUniqueId(
                onSuccess: (uniqueId: String) -> Unit,
                onError: (message: String) -> Unit
            ) {
                val errorMsg = AppSettings.instance.uniqueIdError
                if (errorMsg.isNotEmpty()) onError(errorMsg)
                else onSuccess("TEST-MESSAGE_UNIQUE-ID-01234")
            }

            override fun getMauid(
                onSuccess: (mauid: String) -> Unit,
                onError: (message: String) -> Unit
            ) {
                val errorMsg = AppSettings.instance.mauIdError
                if (errorMsg.isNotEmpty()) onError(errorMsg)
                else onSuccess("TEST-MAUID-01234-56789")
            }

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

        // setup UserInfoBridgeDispatcher
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
                    onError: (tokenError: MiniAppAccessTokenError) -> Unit
            ) {
                if (AppSettings.instance.accessTokenError != null) {
                    onError(AppSettings.instance.accessTokenError!!)
                } else {
                    onSuccess(AppSettings.instance.tokenData)
                }
            }

            override fun getContacts(
                onSuccess: (contacts: ArrayList<Contact>) -> Unit,
                onError: (message: String) -> Unit
            ) {
                if (AppSettings.instance.isContactsSaved)
                    onSuccess(AppSettings.instance.contacts)
                else
                    onError("There is no contact found in HostApp.")
            }

            override fun getPoints(
                onSuccess: (points: Points) -> Unit,
                onError: (pointsError: MiniAppPointsError) -> Unit
            ) {
                val points = AppSettings.instance.points
                if (points != null) onSuccess(points)
                else onError(MiniAppPointsError.custom("There is no points found in HostApp."))
            }
        }
        miniAppMessageBridge.setUserInfoBridgeDispatcher(userInfoBridgeDispatcher)

        // setup ChatBridgeDispatcher
        val chatWindow = ChatWindow(this@MiniAppDisplayActivity)
        val chatBridgeDispatcher = object : ChatBridgeDispatcher {

            override fun sendMessageToContact(
                message: MessageToContact,
                onSuccess: (contactId: String?) -> Unit,
                onError: (message: String) -> Unit
            ) {
                chatWindow.openSingleContactSelection(message, onSuccess, onError)
            }

            override fun sendMessageToContactId(
                contactId: String,
                message: MessageToContact,
                onSuccess: (contactId: String?) -> Unit,
                onError: (message: String) -> Unit
            ) {
                chatWindow.openSpecificContactIdSelection(contactId, message, onSuccess, onError)
            }

            override fun sendMessageToMultipleContacts(
                message: MessageToContact,
                onSuccess: (contactIds: List<String>?) -> Unit,
                onError: (message: String) -> Unit
            ) {
                chatWindow.openMultipleContactSelections(message, onSuccess, onError)
            }
        }
        miniAppMessageBridge.setChatBridgeDispatcher(chatBridgeDispatcher)

        miniAppMessageBridge.setMiniAppFileDownloader(miniAppFileDownloader)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val isGranted = !grantResults.contains(PackageManager.PERMISSION_DENIED)
        when(requestCode){
            AppPermission.ReqCode.CAMERA -> miniappCameraPermissionCallback.invoke(isGranted)
            else -> miniappPermissionCallback.invoke(isGranted)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (Activity.RESULT_OK != resultCode) {
            miniAppFileChooser.onCancel()
            miniAppFileDownloader.onCancel()
        }

        if (requestCode == externalWebViewReqCode && resultCode == Activity.RESULT_OK) {
            data?.let { intent ->
                val isClosedByBackPressed = intent.getBooleanExtra("isClosedByBackPressed", false)
                miniAppMessageBridge.dispatchNativeEvent(NativeEventType.EXTERNAL_WEBVIEW_CLOSE, "External webview closed")
                if(!isClosedByBackPressed)
                    sampleWebViewExternalResultHandler.emitResult(intent)
            }
        } else if (requestCode == fileChoosingReqCode && resultCode == Activity.RESULT_OK) {
            miniAppFileChooser.onReceivedFiles(data)
        } else if (requestCode == MINI_APP_FILE_DOWNLOAD_REQUEST_CODE) {
            data?.data?.let { destinationUri ->
                miniAppFileDownloader.onReceivedResult(destinationUri)
            }
        }
    }

    private fun toggleProgressLoading(isOn: Boolean) = when (isOn) {
        true -> binding.pb.visibility = View.VISIBLE
        false -> binding.pb.visibility = View.GONE
    }

    private fun checkCloseAlert() {
        try {
            val closeAlertInfo = miniAppMessageBridge.miniAppShouldClose()
            if (closeAlertInfo?.shouldDisplay!!) {
                val dialogClickListener =
                    DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                finish()
                            }
                        }
                    }

                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MiniAppDisplayActivity)
                builder.setTitle(closeAlertInfo.title)
                    .setMessage(closeAlertInfo.description)
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
            } else finish()
        } catch (e: NullPointerException) {
            finish()
        }
    }

    override fun onBackPressed() {
        if (!viewModel.canGoBackwards()) {
            checkCloseAlert()
        }
    }

    override fun onPause() {
        super.onPause()
        miniAppMessageBridge.dispatchNativeEvent(NativeEventType.MINIAPP_ON_PAUSE, "MiniApp Paused")
    }

    override fun onResume() {
        super.onResume()
        miniAppMessageBridge.dispatchNativeEvent(NativeEventType.MINIAPP_ON_RESUME, "MiniApp Resumed")
    }
}
