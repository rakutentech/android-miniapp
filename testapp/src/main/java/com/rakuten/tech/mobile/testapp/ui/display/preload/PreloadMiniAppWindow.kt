package com.rakuten.tech.mobile.testapp.ui.display.preload

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.WindowPreloadMiniappBinding
import com.rakuten.tech.mobile.testapp.helper.isAvailable
import com.rakuten.tech.mobile.testapp.helper.load
import com.rakuten.tech.mobile.testapp.helper.registerOnShowAndOnDismissEvent
import com.rakuten.tech.mobile.testapp.helper.showErrorDialog
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class PreloadMiniAppWindow(
    private val context: Context,
    private val preloadMiniAppLaunchListener: PreloadMiniAppLaunchListener
) {
    private lateinit var preloadMiniAppAlertDialog: AlertDialog
    private lateinit var binding: WindowPreloadMiniappBinding
    private lateinit var viewModel: PreloadMiniAppViewModel
    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var miniApp: MiniApp
    private var miniAppInfo: MiniAppInfo? = null
    private var miniAppId: String = ""
    private var versionId: String = ""
    private val permissionAdapter = PreloadMiniAppPermissionAdapter()

    @Suppress("LongParameterList")
    fun initiate(
        appInfo: MiniAppInfo?,
        miniAppIdAndVersionIdPair: Pair<String, String>,
        lifecycleOwner: LifecycleOwner,
        miniApp: MiniApp = MiniApp.instance(AppSettings.instance.newMiniAppSdkConfig),
        shouldShowDialog: Boolean = false,
        onShow: () -> Unit = {},
        onDismiss: () -> Unit = {}
    ) {
        this.lifecycleOwner = lifecycleOwner
        this.miniApp = miniApp

        if (appInfo != null) {
            this.miniAppInfo = appInfo
            this.miniAppId = miniAppInfo!!.id
            this.versionId = miniAppInfo!!.version.versionId
        } else {
            this.miniAppId = miniAppIdAndVersionIdPair.first
            this.versionId = miniAppIdAndVersionIdPair.second
        }

        if (miniAppId.isNotEmpty() && versionId.isNotEmpty()) initDefaultWindow(
            shouldShowDialog = shouldShowDialog,
            onShow = onShow,
            onDismiss = onDismiss
        )
    }

    private fun launchScreen() {
        preloadMiniAppAlertDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun initDefaultWindow(
        shouldShowDialog: Boolean,
        onShow: () -> Unit,
        onDismiss: () -> Unit
    ) {
        // set ui components
        val layoutInflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.window_preload_miniapp, null, false
        )

        if (shouldShowDialog) {
            binding.preloadTutorial.visibility = View.INVISIBLE
        }

        preloadMiniAppAlertDialog =
            AlertDialog.Builder(context, R.style.AppTheme_DefaultWindow).create()
        preloadMiniAppAlertDialog.setView(binding.root)
        preloadMiniAppAlertDialog.registerOnShowAndOnDismissEvent(onShow = onShow, onDismiss = onDismiss)

        // set data to ui
        if (miniAppInfo != null) {
            miniAppInfo?.icon?.let {
                if (context.isAvailable) {
                    binding.preloadAppIcon.load(context, it, R.drawable.r_logo)
                }
            }
            binding.preloadMiniAppName.text = miniAppInfo?.displayName.toString()
            binding.preloadMiniAppVersion.text =
                LABEL_VERSION + miniAppInfo?.version?.versionTag.toString()
        } else {
            binding.preloadMiniAppName.text = ERR_NO_INFO
        }

        // set manifest/metadata to UI: permissions
        binding.listPreloadPermission.layoutManager = LinearLayoutManager(context)
        binding.listPreloadPermission.isNestedScrollingEnabled = false
        binding.listPreloadPermission.adapter = permissionAdapter
        binding.listPreloadPermission.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        val factory = PreloadMiniAppFactory(miniApp = miniApp, shouldShowDialog)
        viewModel = factory.create(PreloadMiniAppViewModel::class.java)
            .apply {
                miniAppManifest.observe(lifecycleOwner) { apiManifest ->
                    if (apiManifest != null) onShowManifest(apiManifest)
                    else onAccept()
                }

                manifestErrorData.observe(lifecycleOwner) {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }

                containTooManyRequestsError.observe(lifecycleOwner) {
                    showErrorDialog(
                        context,
                        context.getString(R.string.error_desc_miniapp_too_many_request)
                    )
                }
            }

        viewModel.checkMiniAppManifest(miniAppId, versionId)

        // set action listeners
        binding.preloadAccept.setOnClickListener {
            viewModel.storeManifestPermission(miniAppId, permissionAdapter.manifestPermissionPairs)
            onAccept()
        }
        binding.preloadCancel.setOnClickListener {
            onCancel()
        }

        preloadMiniAppAlertDialog.setOnCancelListener {
            onCancel()
        }
    }

    private fun onShowManifest(manifest: MiniAppManifest) {
        // show latest manifest from api
        val manifestPermissions = ArrayList<PreloadManifestPermission>()

        manifest.requiredPermissions.forEach {
            val permission = PreloadManifestPermission(it.first, true, it.second)
            manifestPermissions.add(permission)
        }
        manifest.optionalPermissions.forEach {
            val permission = PreloadManifestPermission(it.first, false, it.second)
            manifestPermissions.add(permission)
        }

        // add scopes requested for the RAE audience
        var scope: String
        manifestPermissions.find {
            it.type == MiniAppCustomPermissionType.ACCESS_TOKEN
        }.let {
            if (manifest.accessTokenPermissions.isNotEmpty()) {
                manifest.accessTokenPermissions.find { scope ->
                    scope.audience == "rae"
                }.let {
                    val scopes: List<String> = it?.scopes ?: emptyList()
                    scope = scopes.joinToString(
                        separator = ", ",
                        prefix = "",
                        postfix = "",
                        truncated = "",
                        limit = scopes.size
                    )
                }

                it?.optionalInfo = "(rae scopes): $scope"
            }
        }

        permissionAdapter.addManifestPermissionList(
            manifestPermissions = manifestPermissions,
            miniApp = miniApp,
            miniAppId = miniAppId,
            shouldShowDialog = viewModel.shouldshowDialog
        )

        prepareBottomText(binding.preloadMiniAppMetaData, manifest)

        launchScreen()
    }

    private fun prepareBottomText(
        bottomTextView: TextView,
        manifest: MiniAppManifest
    ) {
        val bottomText = StringBuilder()
        val metaData = toPrettyMetadata(manifest.customMetaData)
        if (!metaData.contentEquals("{}"))
            bottomText.append(LABEL_CUSTOM_METADATA + toPrettyMetadata(manifest.customMetaData))

        if (bottomText.toString().isNotEmpty()) bottomTextView.text = bottomText.toString()
        else bottomTextView.visibility = View.GONE
    }

    private fun onAccept() {
        preloadMiniAppLaunchListener.onPreloadMiniAppResponse(true)
        preloadMiniAppAlertDialog.dismiss()
    }

    private fun onCancel() {
        preloadMiniAppLaunchListener.onPreloadMiniAppResponse(false)
        preloadMiniAppAlertDialog.dismiss()
    }

    private fun toPrettyMetadata(metadata: Map<String, String>) =
        GsonBuilder().setPrettyPrinting().create().toJson(metadata).toString()

    interface PreloadMiniAppLaunchListener {
        fun onPreloadMiniAppResponse(isAccepted: Boolean)
    }

    private companion object {
        const val LABEL_VERSION = "Version: "
        const val LABEL_CUSTOM_METADATA = "Custom MetaData: "
        const val ERR_NO_INFO = "No info found for this miniapp!"
    }
}
