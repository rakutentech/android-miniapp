package com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.widget.TableLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.ads.AdMobDisplayer
import com.rakuten.tech.mobile.miniapp.file.MiniAppCameraPermissionDispatcher
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooserDefault
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileDownloaderDefault
import com.rakuten.tech.mobile.miniapp.iap.InAppPurchaseProviderDefault
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.NativeEventType
import com.rakuten.tech.mobile.miniapp.navigator.ExternalResultHandler
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.FragmentMiniAppDisplayBinding
import com.rakuten.tech.mobile.miniapp.view.MiniAppConfig
import com.rakuten.tech.mobile.miniapp.view.MiniAppParameters
import com.rakuten.tech.mobile.miniapp.view.MiniAppView
import com.rakuten.tech.mobile.testapp.helper.*
import com.rakuten.tech.mobile.testapp.ui.base.BaseFragment
import com.rakuten.tech.mobile.testapp.ui.chat.ChatWindow
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppShareWindow
import com.rakuten.tech.mobile.testapp.ui.display.WebViewActivity
import com.rakuten.tech.mobile.testapp.ui.display.preload.PreloadMiniAppWindow
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.DemoAppMainActivity
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.miniAppIdAndViewMap
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val MINI_APP_FILE_DOWNLOAD_REQUEST_CODE = 1010
private const val EXTERNAL_WEBVIEW_REQUEST_CODE = 100


@Suppress("TooManyFunctions", "MagicNumber", "VariableNaming")
class MiniAppDisplayFragment : BaseFragment(), PreloadMiniAppWindow.PreloadMiniAppLaunchListener {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    private val args by navArgs<MiniAppDisplayFragmentArgs>()
    private lateinit var miniAppMessageBridge: MiniAppMessageBridge
    private lateinit var miniAppNavigator: MiniAppNavigator
    private var miniappPermissionCallback: (isGranted: Boolean) -> Unit = {}
    private var miniappCameraPermissionCallback: (isGranted: Boolean) -> Unit = {}
    private lateinit var sampleWebViewExternalResultHandler: ExternalResultHandler
    private lateinit var binding: FragmentMiniAppDisplayBinding
    private val FILE_CHOOSING_REQUEST_CODE = 10101
    private var appInfo: MiniAppInfo? = null
    private var isloadNew = false
    private lateinit var miniAppDisplay: MiniAppDisplay
    private lateinit var appId: String
    private lateinit var miniAppFileChooser: MiniAppFileChooserDefault
    private lateinit var miniAppFileDownloader: MiniAppFileDownloaderDefault
    private val preloadMiniAppWindow by lazy { PreloadMiniAppWindow(requireActivity(), this) }
    private var previousClickTimeMillis = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ApplicationInfo.FLAG_DEBUGGABLE == 2)
            WebView.setWebContentsDebuggingEnabled(true)
        isloadNew = true
    }

    override fun onDetach() {
        super.onDetach()
        context?.let {
            FileUtils.miniAppCloseLogs(it, appId)
        }
        if (this::miniAppDisplay.isInitialized) {
            miniAppDisplay.destroyView()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        appInfo = args.miniAppInfo
        appId = args.miniAppInfo.id
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_mini_app_display,
            container,
            false
        )
        loadMiniApp()
        return binding.root
    }

    private fun loadMiniApp() {
        val activity = requireActivity()
        if (isloadNew) {
            isloadNew = false
            initializeMiniAppDisplay(activity)
        } else {
            if (this::miniAppDisplay.isInitialized && activity is DemoAppMainActivity) {
                addMiniAppChildView(
                    activity,
                    miniAppView = miniAppIdAndViewMap[Pair(activity.getCurrentSelectedId(), appId)],
                    miniAppDisplay = miniAppDisplay
                )
            } else {
                initializeMiniAppDisplay(activity)
            }
        }
    }

    private fun removeCachedMiniAppViewIfExists(activity: Activity) {
        if (activity is DemoAppMainActivity) {
            val keyPair = Pair(activity.getCurrentSelectedId(), appId)
            if (miniAppIdAndViewMap.containsKey(keyPair)) {
                miniAppIdAndViewMap.remove(keyPair)
            }
        }
    }

    @Suppress("LongMethod", "ComplexMethod")
    private fun initializeMiniAppDisplay(activity: Activity) {
        toggleProgressLoading(true)
        setUpFileChooserAndDownloader(activity)
        setUpNavigator(activity)
        setupMiniAppMessageBridge(requireActivity(), miniAppFileDownloader)
        removeCachedMiniAppViewIfExists(activity)
        val miniAppView = MiniAppView.init(createMiniAppInfoParam(activity, args.miniAppInfo))
        miniAppView.load { miniAppDisplay, miniAppSdkException ->
            activity.runOnUiThread {
                miniAppDisplay?.let {
                    this.miniAppDisplay = miniAppDisplay
                    addMiniAppChildView(activity, miniAppView, miniAppDisplay)
                } ?: kotlin.run {
                    miniAppSdkException?.let {
                        toggleProgressLoading(false)
                        it.printStackTrace()
                        when (it) {
                            is MiniAppHasNoPublishedVersionException ->
                                Toast.makeText(
                                    activity,
                                    "No published version for the provided Mini App ID.",
                                    Toast.LENGTH_LONG
                                ).show()
                            is MiniAppNotFoundException ->
                                Toast.makeText(
                                    activity,
                                    "No Mini App found for the provided Project ID.",
                                    Toast.LENGTH_LONG
                                ).show()
                            is MiniAppTooManyRequestsError ->
                                showErrorDialog(
                                    activity,
                                    getString(R.string.error_desc_miniapp_too_many_request)
                                )
                            else -> {
                                // try to load miniapp from cache.
                                toggleProgressLoading(true)
                                miniAppView.load(fromCache = true) { miniAppDisplay, miniAppSdkException ->
                                    activity.runOnUiThread {
                                        miniAppDisplay?.let {
                                            this.miniAppDisplay = miniAppDisplay
                                            addMiniAppChildView(
                                                activity,
                                                miniAppView,
                                                miniAppDisplay
                                            )
                                        } ?: kotlin.run {
                                            toggleProgressLoading(false)

                                            miniAppSdkException?.let { e ->
                                                when (e) {
                                                    is MiniAppNotFoundException ->
                                                        Toast.makeText(
                                                            activity,
                                                            "No Mini App found for the provided Project ID.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    else -> Toast.makeText(
                                                        activity,
                                                        it.message,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // logs for MiniApp is launched.
        context?.let {
            FileUtils.miniAppOpenLogs(it, appId)
        }
    }

    private fun addMiniAppChildView(
        activity: Activity,
        miniAppView: MiniAppView?,
        miniAppDisplay: MiniAppDisplay
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            val miniAppChildView = miniAppDisplay.getMiniAppView(activity)
            activity.runOnUiThread {
                miniAppChildView?.layoutParams = TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f
                )
                (miniAppChildView?.parent as? ViewGroup)?.removeView(miniAppChildView)
                binding.linRoot.addView(miniAppChildView)
                toggleProgressLoading(false)
            }
            miniAppView?.let {
                with(miniAppIdAndViewMap) {
                    if (activity is DemoAppMainActivity) {
                        val pairTabAndMiniAppId = Pair(activity.getCurrentSelectedId(), appId)
                        if (!containsKey(pairTabAndMiniAppId)) {
                            this[pairTabAndMiniAppId] = it
                        }
                    }
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun setUpNavigator(activity: Activity) {
        miniAppNavigator = object : MiniAppNavigator {
            override fun openExternalUrl(
                url: String,
                externalResultHandler: ExternalResultHandler
            ) {
                if (AppSettings.instance.dynamicDeeplinks.contains(url)) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(url)
                        })
                    } catch (e: Exception) {
                        showAlertDialog(requireActivity(), "Warning!", e.message.toString())
                    }
                } else {
                    sampleWebViewExternalResultHandler = externalResultHandler
                    WebViewActivity.startForResult(
                        activity, url,
                        appId, null, EXTERNAL_WEBVIEW_REQUEST_CODE
                    )
                }
            }
        }
    }

    private fun setUpFileChooserAndDownloader(activity: Activity) {
        val miniAppCameraPermissionDispatcher = object : MiniAppCameraPermissionDispatcher {
            override fun getCameraPermission(permissionCallback: (isGranted: Boolean) -> Unit) {
                if (ContextCompat.checkSelfPermission(
                        requireActivity(),
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
                    activity,
                    AppDevicePermission.getDevicePermissionRequest(miniAppPermissionType),
                    AppDevicePermission.getDeviceRequestCode(miniAppPermissionType)
                )
            }
        }
        miniAppFileChooser = MiniAppFileChooserDefault(
            requestCode = FILE_CHOOSING_REQUEST_CODE,
            miniAppCameraPermissionDispatcher = miniAppCameraPermissionDispatcher
        )
        miniAppFileDownloader = MiniAppFileDownloaderDefault(
            activity = activity,
            requestCode = MINI_APP_FILE_DOWNLOAD_REQUEST_CODE
        )
    }

    private fun toggleProgressLoading(isOn: Boolean) = when (isOn) {
        true -> binding.pb.visibility = View.VISIBLE
        false -> binding.pb.visibility = View.GONE
    }

    @Suppress("OverridingDeprecatedMember")
    private fun setupMiniAppMessageBridge(
        activity: Activity,
        miniAppFileDownloader: MiniAppFileDownloaderDefault
    ) {
        // setup MiniAppMessageBridge
        miniAppMessageBridge = getMessageBridge(activity) { onDevicePermissionResultCallback ->
            miniappPermissionCallback = onDevicePermissionResultCallback
        }
        miniAppMessageBridge.setMiniAppCloseListener { withConfirmationAlert ->
            if (withConfirmationAlert) checkCloseAlert() else findNavController().navigateUp()
        }
        miniAppMessageBridge.setAdMobDisplayer(AdMobDisplayer(activity))
        miniAppMessageBridge.allowScreenOrientation(true)

        // setup UserInfoBridgeDispatcher
        val userInfoBridgeDispatcher = getUserInfoBridgeDispatcher()
        miniAppMessageBridge.setUserInfoBridgeDispatcher(userInfoBridgeDispatcher)

        // setup ChatBridgeDispatcher
        val chatWindow = ChatWindow(activity)
        val chatBridgeDispatcher = getChatBridgeDispatcher(chatWindow)
        miniAppMessageBridge.setChatBridgeDispatcher(chatBridgeDispatcher)
        miniAppMessageBridge.setMiniAppFileDownloader(miniAppFileDownloader)

        // setup InAppPurchaseProvider
        miniAppMessageBridge.setInAppPurchaseProvider(InAppPurchaseProviderDefault(requireActivity()))
    }

    fun handleOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Activity.RESULT_OK != resultCode) {
            miniAppFileChooser.onCancel()
            miniAppFileDownloader.onCancel()
        }

        when {
            requestCode == EXTERNAL_WEBVIEW_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                data?.let { intent ->
                    val isClosedByBackPressed =
                        intent.getBooleanExtra("isClosedByBackPressed", false)
                    miniAppMessageBridge.dispatchNativeEvent(
                        NativeEventType.EXTERNAL_WEBVIEW_CLOSE,
                        "External webview closed"
                    )
                    if (!isClosedByBackPressed)
                        sampleWebViewExternalResultHandler.emitResult(intent)
                }
            }
            requestCode == FILE_CHOOSING_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                miniAppFileChooser.onReceivedFiles(data)
            }
            requestCode == MINI_APP_FILE_DOWNLOAD_REQUEST_CODE -> {
                data?.data?.let { destinationUri ->
                    miniAppFileDownloader.onReceivedResult(destinationUri)
                }
            }
        }
    }

    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        val isGranted = !grantResults.contains(PackageManager.PERMISSION_DENIED)
        when (requestCode) {
            AppDevicePermission.ReqCode.CAMERA -> miniappCameraPermissionCallback.invoke(isGranted)
            else -> miniappPermissionCallback.invoke(isGranted)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.miniapp_display_menu, menu)
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
                        context = requireActivity(),
                        onShow = miniAppMessageBridge::dispatchOnPauseEvent,
                        onDismiss = miniAppMessageBridge::dispatchOnResumeEvent
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

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun checkCloseAlert() {
        try {
            val closeAlertInfo = miniAppMessageBridge.miniAppShouldClose()
            if (closeAlertInfo?.shouldDisplay!!) {
                val dialogClickListener =
                    DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                requireActivity().runOnUiThread {
                                    findNavController().navigateUp()
                                }
                            }
                        }
                    }

                val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
                builder.setTitle(closeAlertInfo.title)
                    .setMessage(closeAlertInfo.description)
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
            } else findNavController().navigateUp()
        } catch (e: NullPointerException) {
            findNavController().navigateUp()
        }
    }

    fun onBackPressed() {
        if (!::miniAppDisplay.isInitialized || !miniAppDisplay.navigateBackward()) {
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

    private fun createMiniAppInfoParam(
        activity: Activity,
        miniAppInfo: MiniAppInfo
    ): MiniAppParameters {
        return MiniAppParameters.InfoParams(
            context = activity,
            config = MiniAppConfig(
                miniAppSdkConfig = AppSettings.instance.newMiniAppSdkConfig,
                miniAppMessageBridge = miniAppMessageBridge,
                miniAppNavigator = miniAppNavigator,
                miniAppFileChooser = miniAppFileChooser,
                queryParams = AppSettings.instance.urlParameters,
            ),
            miniAppInfo = miniAppInfo,
            fromCache = false
        )
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

    override fun onPreloadMiniAppResponse(isAccepted: Boolean) {
        //intent
    }

}
