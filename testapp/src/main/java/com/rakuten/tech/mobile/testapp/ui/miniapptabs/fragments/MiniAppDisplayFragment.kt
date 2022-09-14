package com.rakuten.tech.mobile.testapp.ui.miniapptabs.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TableLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.ads.AdMobDisplayer
import com.rakuten.tech.mobile.miniapp.errors.MiniAppAccessTokenError
import com.rakuten.tech.mobile.miniapp.errors.MiniAppPointsError
import com.rakuten.tech.mobile.miniapp.file.MiniAppCameraPermissionDispatcher
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooserDefault
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileDownloaderDefault
import com.rakuten.tech.mobile.miniapp.js.MessageToContact
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.NativeEventType
import com.rakuten.tech.mobile.miniapp.js.chat.ChatBridgeDispatcher
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.js.userinfo.Points
import com.rakuten.tech.mobile.miniapp.js.userinfo.TokenData
import com.rakuten.tech.mobile.miniapp.js.userinfo.UserInfoBridgeDispatcher
import com.rakuten.tech.mobile.miniapp.navigator.ExternalResultHandler
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope
import com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.FragmentMiniAppDisplayBinding
import com.rakuten.tech.mobile.miniapp.view.MiniAppConfig
import com.rakuten.tech.mobile.miniapp.view.MiniAppParameters
import com.rakuten.tech.mobile.miniapp.view.MiniAppView
import com.rakuten.tech.mobile.testapp.helper.AppPermission
import com.rakuten.tech.mobile.testapp.helper.showAlertDialog
import com.rakuten.tech.mobile.testapp.ui.base.BaseFragment
import com.rakuten.tech.mobile.testapp.ui.chat.ChatWindow
import com.rakuten.tech.mobile.testapp.ui.display.MiniAppShareWindow
import com.rakuten.tech.mobile.testapp.ui.display.WebViewActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.NullPointerException
import java.util.ArrayList

class MiniAppDisplayFragment : BaseFragment() {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    private val args by navArgs<MiniAppDisplayFragmentArgs>()
    private lateinit var miniAppMessageBridge: MiniAppMessageBridge
    private lateinit var miniAppNavigator: MiniAppNavigator
    private var miniappPermissionCallback: (isGranted: Boolean) -> Unit = {}
    private var miniappCameraPermissionCallback: (isGranted: Boolean) -> Unit = {}
    private lateinit var sampleWebViewExternalResultHandler: ExternalResultHandler
    private lateinit var binding: FragmentMiniAppDisplayBinding
    private val externalWebViewReqCode = 100
    private val fileChoosingReqCode = 10101
    private val MINI_APP_FILE_DOWNLOAD_REQUEST_CODE = 1010
    private var appInfo: MiniAppInfo? = null
    private var isloadNew = false
    private var miniappview: View? = null
    private lateinit var miniAppDisplay: MiniAppDisplay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("MiniAppDisplay", "fragment onCreate")
        isloadNew = true

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e("MiniAppDisplay", "fragment onAttach")
    }

    override fun onDetach() {
        super.onDetach()
        Log.e("MiniAppDisplay", "fragment onDetach")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("MiniAppDisplay", "fragment onDestroyView")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("MiniAppDisplay", "fragment onViewCreated")
    }

    private lateinit var miniAppFileChooser: MiniAppFileChooserDefault
    private lateinit var miniAppFileDownloader: MiniAppFileDownloaderDefault

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("MiniAppDisplay", "fragment onViewCreated")

        setHasOptionsMenu(true)
        appInfo = args.miniAppInfo
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_mini_app_display,
            container,
            false
        )
        if (isloadNew) {
            isloadNew = false
            Log.e("MiniAppDisplay", "load miniapp")
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
                        requireActivity(),
                        AppPermission.getDevicePermissionRequest(miniAppPermissionType),
                        AppPermission.getDeviceRequestCode(miniAppPermissionType)
                    )
                }
            }
            miniAppFileChooser = MiniAppFileChooserDefault(
                requestCode = fileChoosingReqCode,
                miniAppCameraPermissionDispatcher = miniAppCameraPermissionDispatcher
            )
            miniAppFileDownloader = MiniAppFileDownloaderDefault(
                activity = requireActivity(),
                requestCode = MINI_APP_FILE_DOWNLOAD_REQUEST_CODE
            )
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
                            requireActivity(), url,
                            "appId", "appUrl", externalWebViewReqCode
                        )
                    }
                }
            }
            setupMiniAppMessageBridge(requireActivity(), miniAppFileDownloader)

            val param = MiniAppParameters.DefaultParams(
                context = requireActivity(),
                config = MiniAppConfig(
                    miniAppSdkConfig = AppSettings.instance.miniAppSettings,
                    miniAppMessageBridge = miniAppMessageBridge,
                    miniAppNavigator = miniAppNavigator,
                    miniAppFileChooser = miniAppFileChooser,
                    queryParams = AppSettings.instance.urlParameters
                ),
                miniAppId = args.miniAppInfo.id,
                miniAppVersion = args.miniAppInfo.version.versionId,
                fromCache = false
            )
            val activiy = activity
            val miniapp = MiniAppView.init(param)
            toggleProgressLoading(true)
            miniapp.load { miniAppDisplay ->
                this.miniAppDisplay = miniAppDisplay
                CoroutineScope(Dispatchers.Default).launch {
                    activity?.let {
                        miniappview = miniAppDisplay.getMiniAppView(it)
                        activiy?.runOnUiThread {
                            miniappview?.layoutParams = TableLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                1f
                            )
                            miniappview?.setPadding(5, 30, 0, 30)
                            binding.linRoot.addView(miniappview)
                            toggleProgressLoading(false)
                        }
                    }
                }
            }
        } else {
            val activiy = activity
            activiy?.runOnUiThread {
                miniappview?.layoutParams = TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    800, 1f
                )
                miniappview?.setPadding(5, 30, 0, 30)
                (miniappview?.parent as? ViewGroup)?.removeView(miniappview)
                miniappview?.let {
                    binding.linRoot.addView(it)
                    toggleProgressLoading(false)
                }
            }
        }
        return binding.root
    }

    private fun toggleProgressLoading(isOn: Boolean) = when (isOn) {
        true -> binding.pb.visibility = View.VISIBLE
        false -> binding.pb.visibility = View.GONE
    }

    private fun setupMiniAppMessageBridge(
        activity: Activity,
        miniAppFileDownloader: MiniAppFileDownloaderDefault
    ) {
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
                    activity,
                    AppPermission.getDevicePermissionRequest(miniAppPermissionType),
                    AppPermission.getDeviceRequestCode(miniAppPermissionType)
                )
            }
        }
        miniAppMessageBridge.setAdMobDisplayer(AdMobDisplayer(activity))
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
        val chatWindow = ChatWindow(activity)
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


    fun handleOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Activity.RESULT_OK != resultCode) {
            miniAppFileChooser.onCancel()
            miniAppFileDownloader.onCancel()
        }

        if (requestCode == externalWebViewReqCode && resultCode == Activity.RESULT_OK) {
            data?.let { intent ->
                val isClosedByBackPressed = intent.getBooleanExtra("isClosedByBackPressed", false)
                miniAppMessageBridge.dispatchNativeEvent(
                    NativeEventType.EXTERNAL_WEBVIEW_CLOSE,
                    "External webview closed"
                )
                if (!isClosedByBackPressed)
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

    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        val isGranted = !grantResults.contains(PackageManager.PERMISSION_DENIED)
        when (requestCode) {
            AppPermission.ReqCode.CAMERA -> miniappCameraPermissionCallback.invoke(isGranted)
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
                    MiniAppShareWindow.getInstance(requireActivity()).show(miniAppInfo = it)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkCloseAlert() {
        try {
            val closeAlertInfo = miniAppMessageBridge.miniAppShouldClose()
            if (closeAlertInfo?.shouldDisplay!!) {
                val dialogClickListener =
                    DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                findNavController().navigateUp()
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

    private fun canGoBackwards(): Boolean =
        if (::miniAppDisplay.isInitialized)
            miniAppDisplay.navigateBackward()
        else
            false

    fun onBackPressed() {
        if (!canGoBackwards()) {
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
