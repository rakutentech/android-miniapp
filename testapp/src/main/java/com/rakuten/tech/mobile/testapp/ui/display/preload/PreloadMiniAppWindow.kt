package com.rakuten.tech.mobile.testapp.ui.display.preload

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.WindowPreloadMiniappBinding
import com.rakuten.tech.mobile.testapp.helper.setIcon
import java.lang.Exception

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

    private var prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.sample.first_time.launch", Context.MODE_PRIVATE
    )

    fun initiate(appInfo: MiniAppInfo?, miniAppId: String, lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner

        if (appInfo != null) {
            this.miniAppInfo = appInfo
            this.miniAppId = miniAppInfo!!.id
            this.versionId = miniAppInfo!!.version.versionId
        } else this.miniAppId = miniAppId

        if (!isAccepted()) {
            launchScreen()
        } else if (!prefs.contains(miniAppId)) {
            storeAcceptance(DEFAULT_ACCEPTANCE) // should be true only after accept.
            launchScreen()
        } else {
            preloadMiniAppLaunchListener.onPreloadMiniAppResponse(true)
        }
    }

    private fun launchScreen() {
        initDefaultWindow()
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
            setIcon(
                context, Uri.parse(miniAppInfo?.icon), binding.preloadAppIcon
            )
            binding.preloadMiniAppName.text = miniAppInfo?.displayName.toString()
            binding.preloadMiniAppVersion.text = LABEL_VERSION + miniAppInfo?.version?.versionTag.toString()
        } else {
            binding.preloadMiniAppName.text = ERR_NO_INFO
        }

        // set manifest/metadata to UI: permissions
        val permissionAdapter = PreloadMiniAppPermissionAdapter()
        binding.listPreloadPermission.layoutManager = LinearLayoutManager(context)
        binding.listPreloadPermission.isNestedScrollingEnabled = false
        binding.listPreloadPermission.adapter = permissionAdapter
        binding.listPreloadPermission.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )

        viewModel =
            ViewModelProvider.NewInstanceFactory().create(PreloadMiniAppViewModel::class.java)
                .apply {
                    // observe version id when it's empty
                    if (versionId.isEmpty()) {
                        miniAppVersionId.observe(lifecycleOwner, Observer { versionId = it })
                        versionIdErrorData.observe(lifecycleOwner, Observer {
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        })
                    }

                    miniAppManifestMetadata.observe(lifecycleOwner, Observer {
                        binding.preloadMiniAppMetaData.text = LABEL_CUSTOM_METADATA + it
                    })

                    miniAppManifest.observe(lifecycleOwner,
                        Observer { (requiredPermissions, optionalPermissions) ->
                            val manifestPermissions = ArrayList<PreloadManifestPermission>()

                            requiredPermissions.forEach {
                                val permission = PreloadManifestPermission(
                                    it.first,
                                    true,
                                    it.second
                                )
                                manifestPermissions.add(permission)
                            }
                            optionalPermissions.forEach {
                                val permission = PreloadManifestPermission(
                                    it.first,
                                    false,
                                    it.second
                                )
                                manifestPermissions.add(permission)
                            }

                            permissionAdapter.addManifestPermissionList(manifestPermissions)
                        })

                    manifestErrorData.observe(lifecycleOwner, Observer {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    })
                }

        // retrieve version id when it's empty
        if (versionId.isEmpty())
            viewModel.getMiniAppVersionId(miniAppId)

        viewModel.getMiniAppManifest(miniAppId, versionId, KEY_METADATA)

        // set action listeners
        binding.preloadAccept.setOnClickListener {
            storeAcceptance(true) // set true when accept
            storeManifestPermission(permissionAdapter.manifestPermissionPairs)
            preloadMiniAppLaunchListener.onPreloadMiniAppResponse(true)
            preloadMiniAppAlertDialog.dismiss()
        }
        binding.preloadCancel.setOnClickListener {
            preloadMiniAppLaunchListener.onPreloadMiniAppResponse(false)
            preloadMiniAppAlertDialog.dismiss()
        }
    }

    private fun isAccepted(): Boolean {
        try {
            if (prefs.contains(miniAppId)) return prefs.getBoolean(miniAppId, DEFAULT_ACCEPTANCE)
        } catch (e: Exception) {
            return false
        }
        return false
    }

    private fun storeManifestPermission(
        permissions: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
    ) {
        // store values in SDK cache
        val permissionsWhenAccept = MiniAppCustomPermission(
            miniAppId = miniAppId,
            pairValues = permissions
        )
        viewModel.miniApp.setCustomPermissions(permissionsWhenAccept)
    }

    private fun storeAcceptance(isAccepted: Boolean) = prefs.edit()?.putBoolean(miniAppId, isAccepted)?.apply()

    interface PreloadMiniAppLaunchListener {
        fun onPreloadMiniAppResponse(isAccepted: Boolean)
    }

    private companion object {
        const val DEFAULT_ACCEPTANCE = false
        const val LABEL_VERSION = "Version: "
        const val LABEL_CUSTOM_METADATA = "Custom MetaData: "
        const val ERR_NO_INFO = "No info found for this miniapp!"
        const val KEY_METADATA = "randomTestKey" // HostApp can set it's own key
    }
}
