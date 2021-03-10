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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.WindowPreloadMiniappBinding
import com.rakuten.tech.mobile.testapp.helper.setIcon

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

        initDefaultWindow()

        if (!prefs.contains(miniAppId)) {
            storeAcceptance(DEFAULT_ACCEPTANCE) // should be true only after accept.
            launchScreen()
        }
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
            setIcon(
                context, Uri.parse(miniAppInfo?.icon), binding.preloadAppIcon
            )
            binding.preloadMiniAppName.text = miniAppInfo?.displayName.toString()
            binding.preloadMiniAppVersion.text = LABEL_VERSION + miniAppInfo?.version?.versionTag.toString()
        } else {
            binding.preloadMiniAppName.text = ERR_NO_INFO
        }

        // set manifest/metadata to UI: permissions
        binding.listPreloadPermission.layoutManager = LinearLayoutManager(context)
        binding.listPreloadPermission.isNestedScrollingEnabled = false
        binding.listPreloadPermission.adapter = permissionAdapter

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

                    miniAppManifest.observe(lifecycleOwner,
                        Observer { apiManifest ->
                            val downloadedManifest = viewModel.miniApp.getDownloadedManifest(miniAppId)
                            if (downloadedManifest == apiManifest) {
                                storeAcceptance(true) // set true when accept
                                preloadMiniAppLaunchListener.onPreloadMiniAppResponse(true)
                            } else {
                                compareManifest(downloadedManifest, apiManifest)
                            }

                            binding.preloadMiniAppMetaData.text =
                                LABEL_CUSTOM_METADATA + toPrettyMetadata(apiManifest.customMetaData)
                        })

                    manifestErrorData.observe(lifecycleOwner, Observer {
                        // TODO: show screen again when required permissions are not granted
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    })
                }

        // retrieve version id when it's empty
        if (versionId.isEmpty())
            viewModel.getMiniAppVersionId(miniAppId)

        viewModel.getMiniAppManifest(miniAppId, versionId)

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

    private val changedPermissionsToShow = ArrayList<PreloadManifestPermission>()
    private val detectedRemovablePermissionsToShow = ArrayList<PreloadManifestPermission>()
    private val changedPermissionsToStore =
        arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

    private fun compareManifest(
        downloadedManifest: MiniAppManifest?,
        apiManifest: MiniAppManifest
    ) {
        if (downloadedManifest != null) {
            detectRequiredPermissions(downloadedManifest, apiManifest)
            detectOptionalPermission(downloadedManifest, apiManifest)
        }

        if (changedPermissionsToShow.isEmpty() && changedPermissionsToStore.isEmpty()) {
            // show latest manifest from api
            val manifestPermissions = ArrayList<PreloadManifestPermission>()

            apiManifest.requiredPermissions.forEach {
                val permission = PreloadManifestPermission(it.first, true, it.second, true)
                manifestPermissions.add(permission)
            }
            apiManifest.optionalPermissions.forEach {
                val permission = PreloadManifestPermission(it.first, false, it.second, true)
                manifestPermissions.add(permission)
            }

            permissionAdapter.addManifestPermissionList(manifestPermissions)
            launchScreen()
        } else if (changedPermissionsToShow.isNotEmpty() && changedPermissionsToStore.isEmpty()) {
            // show new permissions
            permissionAdapter.addManifestPermissionList(changedPermissionsToShow)
            launchScreen()
        } else if (changedPermissionsToShow.isEmpty() && changedPermissionsToStore.isNotEmpty()) {
            // detect removable permissions and save
            storeAcceptance(true) // set true when accept
            storeManifestPermission(changedPermissionsToStore)
            preloadMiniAppLaunchListener.onPreloadMiniAppResponse(true)
        } else if (changedPermissionsToShow.isNotEmpty() && changedPermissionsToStore.isNotEmpty()) {
            // detect removable permissions & new permissions and show ui
            val all: List<PreloadManifestPermission> = changedPermissionsToShow + detectedRemovablePermissionsToShow
            permissionAdapter.addManifestPermissionList(all.toMutableList())
            launchScreen()
        }
    }

    private fun detectRequiredPermissions(
        downloadedManifest: MiniAppManifest,
        apiManifest: MiniAppManifest
    ) {
        // detect required permissions changes
        val uncommonRequiredPerms =
            (apiManifest.requiredPermissions + downloadedManifest.requiredPermissions).groupBy { it.first.type }
                .filter { it.value.size == 1 }
                .flatMap { it.value }

        if (uncommonRequiredPerms.isNotEmpty()) {
            if (downloadedManifest.requiredPermissions.size < apiManifest.requiredPermissions.size) {
                // show the extra permission in ui comes from api
                uncommonRequiredPerms.forEach { (first, second) ->
                    val permission = PreloadManifestPermission(first, true, second, true)
                    changedPermissionsToShow.add(permission)
                }

                val cachedRequiredPerms = downloadedManifest.requiredPermissions.toMutableList()
                cachedRequiredPerms.removeAll { (first) ->
                    first.type in uncommonRequiredPerms.groupBy { it.first.type }
                }
                cachedRequiredPerms.forEach {
                    changedPermissionsToStore.add(
                        Pair(it.first, MiniAppCustomPermissionResult.ALLOWED)
                    )

                    val manifest = PreloadManifestPermission(it.first, true, it.second, false)
                    detectedRemovablePermissionsToShow.add(manifest)
                }
            } else {
                // nothing to show in ui, remove required permissions not included in manifest
                val cachedRequiredPerms = downloadedManifest.requiredPermissions.toMutableList()
                cachedRequiredPerms.removeAll { (first) ->
                    first.type in uncommonRequiredPerms.groupBy { it.first.type }
                }
                cachedRequiredPerms.forEach {
                    changedPermissionsToStore.add(
                        Pair(it.first, MiniAppCustomPermissionResult.ALLOWED)
                    )

                    val manifest = PreloadManifestPermission(it.first, true, it.second, false)
                    detectedRemovablePermissionsToShow.add(manifest)
                }
            }
        }
    }

    private fun detectOptionalPermission(downloadedManifest: MiniAppManifest, apiManifest: MiniAppManifest) {
        // detect optional permissions changes
        val uncommonOptionalPerms =
            (apiManifest.optionalPermissions + downloadedManifest.optionalPermissions).groupBy { it.first.type }
                .filter { it.value.size == 1 }
                .flatMap { it.value }

        if (uncommonOptionalPerms.isNotEmpty()) {
            if (downloadedManifest.optionalPermissions.size < apiManifest.optionalPermissions.size) {
                // show the extra permission in ui comes from api
                uncommonOptionalPerms.forEach { (first, second) ->
                    val permission = PreloadManifestPermission(first, false, second, true)
                    changedPermissionsToShow.add(permission)
                }

                val cachedOptionalPerms = downloadedManifest.optionalPermissions.toMutableList()
                cachedOptionalPerms.removeAll { (first) ->
                    first.type in uncommonOptionalPerms.groupBy { it.first.type }
                }
                cachedOptionalPerms.forEach {
                    changedPermissionsToStore.add(
                        Pair(it.first, MiniAppCustomPermissionResult.ALLOWED)
                    )

                    val manifest = PreloadManifestPermission(it.first, false, it.second, false)
                    detectedRemovablePermissionsToShow.add(manifest)
                }
            } else {
                // nothing to show in ui, remove optional permissions not included in manifest
                val cachedOptionalPerms = downloadedManifest.optionalPermissions.toMutableList()
                cachedOptionalPerms.removeAll { (first) ->
                    first.type in uncommonOptionalPerms.groupBy { it.first.type }
                }
                cachedOptionalPerms.forEach {
                    changedPermissionsToStore.add(
                        Pair(it.first, MiniAppCustomPermissionResult.ALLOWED)
                    )

                    val manifest = PreloadManifestPermission(it.first, false, it.second, false)
                    detectedRemovablePermissionsToShow.add(manifest)
                }
            }
        }
    }

    private fun toPrettyMetadata(metadata: Map<String, String>) =
        GsonBuilder().setPrettyPrinting().create().toJson(metadata)

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
    }
}
