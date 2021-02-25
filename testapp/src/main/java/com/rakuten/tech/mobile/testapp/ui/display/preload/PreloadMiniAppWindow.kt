package com.rakuten.tech.mobile.testapp.ui.display.preload

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.helper.setIcon
import java.lang.Exception

private const val DEFAULT_ACCEPTANCE = false

class PreloadMiniAppWindow(private val context: Context, private val preloadMiniAppLaunchListener: PreloadMiniAppLaunchListener) {
    private lateinit var preloadMiniAppAlertDialog: AlertDialog
    private lateinit var preloadMiniAppLayout: View
    private lateinit var viewModel: PreloadMiniAppViewModel
    private lateinit var lifecycleOwner: LifecycleOwner
    private var miniAppInfo: MiniAppInfo? = null
    private var miniAppId: String = ""
    private var versionId: String = ""
    private val metadataKey = "randomTestKey" // HostApp can set it's own key

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
        } else if (!doesDataExist()) {
            storeAcceptance(DEFAULT_ACCEPTANCE) // should be false only after accept.
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
        preloadMiniAppLayout = layoutInflater.inflate(R.layout.window_preload_miniapp, null)
        preloadMiniAppAlertDialog = AlertDialog.Builder(context, R.style.AppTheme_DefaultWindow).create()
        preloadMiniAppAlertDialog.setView(preloadMiniAppLayout)

        // set data to ui
        val nameView = preloadMiniAppLayout.findViewById<TextView>(R.id.preloadMiniAppName)
        val versionView = preloadMiniAppLayout.findViewById<TextView>(R.id.preloadMiniAppVersion)
        if (miniAppInfo != null) {
            setIcon(
                context,
                Uri.parse(miniAppInfo?.icon),
                preloadMiniAppLayout.findViewById(R.id.preloadAppIcon)
            )

            nameView.text = miniAppInfo?.displayName.toString()
            versionView.text = "Version: " + miniAppInfo?.version?.versionTag.toString()
        } else {
            nameView.text = "No info found for this miniapp!"
        }

        // set manifest/metadata to UI: permissions
        val permissionAdapter = PreloadMiniAppPermissionAdapter()
        preloadMiniAppLayout.findViewById<RecyclerView>(R.id.listPreloadPermission).layoutManager =
            LinearLayoutManager(context)
        preloadMiniAppLayout.findViewById<RecyclerView>(R.id.listPreloadPermission).adapter =
            permissionAdapter
        preloadMiniAppLayout.findViewById<RecyclerView>(R.id.listPreloadPermission)
            .addItemDecoration(
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
                        preloadMiniAppLayout.findViewById<TextView>(R.id.preloadMiniAppMetaData).text =
                            "Custom MetaData: " + it
                    })

                    miniAppManifest.observe(lifecycleOwner,
                        Observer { (requiredPermissions, optionalPermissions) ->
                            // TODO: inflate UI from MiniApp.getRequiredCustomPermissions
                            // TODO: inflate UI from MiniApp.getOptionalCustomPermissions
                            val namesForAdapter: ArrayList<MiniAppCustomPermissionType> =
                                arrayListOf()
                            val reasonsForAdapter: ArrayList<String> = arrayListOf()
                            val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> =
                                arrayListOf()

                            requiredPermissions.forEach {
                                namesForAdapter.add(it.first)
                                reasonsForAdapter.add(it.second)
                                resultsForAdapter.add(MiniAppCustomPermissionResult.ALLOWED)
                            }
                            optionalPermissions.forEach {
                                namesForAdapter.add(it.first)
                                reasonsForAdapter.add(it.second)
                                resultsForAdapter.add(MiniAppCustomPermissionResult.ALLOWED)
                            }
                            permissionAdapter.addManifestPermissionList(
                                namesForAdapter,
                                resultsForAdapter,
                                reasonsForAdapter
                            )
                        })

                    manifestErrorData.observe(lifecycleOwner, Observer {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    })
                }

        // retrieve version id when it's empty
        if (versionId.isEmpty())
            viewModel.getMiniAppVersionId(miniAppId)

        viewModel.getMiniAppManifest(miniAppId, versionId, metadataKey)

        // set action listeners
        preloadMiniAppLayout.findViewById<TextView>(R.id.preloadAccept).setOnClickListener {
            // TODO: MiniApp.setCustomPermissions
            storeAcceptance(true) // set false when accept
            preloadMiniAppLaunchListener.onPreloadMiniAppResponse(true)
            preloadMiniAppAlertDialog.dismiss()
        }
        preloadMiniAppLayout.findViewById<TextView>(R.id.preloadCancel).setOnClickListener {
            preloadMiniAppLaunchListener.onPreloadMiniAppResponse(false)
            preloadMiniAppAlertDialog.dismiss()
        }
    }

    private fun doesDataExist() = prefs.contains(miniAppId)

    private fun isAccepted(): Boolean {
        try {
            if (doesDataExist()) return prefs.getBoolean(miniAppId, DEFAULT_ACCEPTANCE)
        } catch (e: Exception) {
            return false
        }
        return false
    }

    private fun storeAcceptance(isAccepted: Boolean) = prefs.edit()?.putBoolean(miniAppId, isAccepted)?.apply()

    interface PreloadMiniAppLaunchListener {
        fun onPreloadMiniAppResponse(isAccepted: Boolean)
    }
}
