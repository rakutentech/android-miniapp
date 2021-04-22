package com.rakuten.tech.mobile.testapp.ui.display.preload

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.WindowPreloadMiniappBinding
import com.rakuten.tech.mobile.testapp.helper.load

class PreloadMiniAppWindow(
    private val context: Context,
    private val preloadMiniAppLaunchListener: PreloadMiniAppLaunchListener
) {
    private lateinit var preloadMiniAppAlertDialog: AlertDialog
    private lateinit var binding: WindowPreloadMiniappBinding
    private lateinit var viewModel: PreloadMiniAppViewModel
    private lateinit var lifecycleOwner: LifecycleOwner
    private var miniAppInfo: MiniAppInfo? = null
    private var miniAppId: String = ""
    private var versionId: String = ""
    private val permissionAdapter = PreloadMiniAppPermissionAdapter()

    fun initiate(appInfo: MiniAppInfo?, miniAppId: String, versionId: String, lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner

        if (appInfo != null) {
            this.miniAppInfo = appInfo
            this.miniAppId = miniAppInfo!!.id
            this.versionId = miniAppInfo!!.version.versionId
        } else {
            this.miniAppId = miniAppId
            this.versionId = versionId
        }

        if (miniAppId.isNotEmpty() && versionId.isNotEmpty()) initDefaultWindow()
    }

    private fun launchScreen() {
        preloadMiniAppAlertDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun initDefaultWindow() {
        // set ui components
        val layoutInflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.window_preload_miniapp, null, false
        )
        preloadMiniAppAlertDialog = AlertDialog.Builder(context, R.style.AppTheme_DefaultWindow).create()
        preloadMiniAppAlertDialog.setView(binding.root)

        // set data to ui
        if (miniAppInfo != null) {
            miniAppInfo?.icon?.let { binding.preloadAppIcon.load(context, it, R.drawable.r_logo) }
            binding.preloadMiniAppName.text = miniAppInfo?.displayName.toString()
            binding.preloadMiniAppVersion.text = LABEL_VERSION + miniAppInfo?.version?.versionTag.toString()
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

        viewModel =
            ViewModelProvider.NewInstanceFactory().create(PreloadMiniAppViewModel::class.java)
                .apply {
                    miniAppManifest.observe(lifecycleOwner, Observer { apiManifest ->
                        if (apiManifest != null)
                            onShowManifest(apiManifest)
                        else
                            preloadMiniAppLaunchListener.onPreloadMiniAppResponse(true)
                    })

                    manifestErrorData.observe(lifecycleOwner, Observer {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    })
                }

        viewModel.checkMiniAppManifest(miniAppId, versionId)

        // set action listeners
        binding.preloadAccept.setOnClickListener {
            viewModel.storeManifestPermission(miniAppId, permissionAdapter.manifestPermissionPairs)
            preloadMiniAppLaunchListener.onPreloadMiniAppResponse(true)
            preloadMiniAppAlertDialog.dismiss()
        }
        binding.preloadCancel.setOnClickListener {
            preloadMiniAppLaunchListener.onPreloadMiniAppResponse(false)
            preloadMiniAppAlertDialog.dismiss()
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

        permissionAdapter.addManifestPermissionList(manifestPermissions)
        binding.preloadMiniAppMetaData.text =
            LABEL_CUSTOM_METADATA + toPrettyMetadata(manifest.customMetaData)

        launchScreen()
    }

    private fun toPrettyMetadata(metadata: Map<String, String>) =
        GsonBuilder().setPrettyPrinting().create().toJson(metadata)

    interface PreloadMiniAppLaunchListener {
        fun onPreloadMiniAppResponse(isAccepted: Boolean)
    }

    private companion object {
        const val LABEL_VERSION = "Version: "
        const val LABEL_CUSTOM_METADATA = "Custom MetaData: "
        const val ERR_NO_INFO = "No info found for this miniapp!"
    }
}
