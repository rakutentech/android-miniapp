package com.rakuten.tech.mobile.testapp.ui.display.preload

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.helper.setIcon
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import java.lang.Exception

private const val DEFAULT_ACCEPTANCE = false

class PreloadMiniAppWindow(private val context: Context, private val preloadMiniAppLaunchListener: PreloadMiniAppLaunchListener) {
    private val miniApp = MiniApp.instance(AppSettings.instance.miniAppSettings)
    private lateinit var preloadMiniAppAlertDialog: AlertDialog
    private lateinit var preloadMiniAppLayout: View
    private var miniAppInfo: MiniAppInfo? = null
    private var miniAppId: String = ""

    private var prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.sample.first_time.launch", Context.MODE_PRIVATE
    )

    fun initiate(appInfo: MiniAppInfo?, miniAppId: String) {
        if (appInfo != null) {
            this.miniAppInfo = appInfo
            this.miniAppId = miniAppInfo!!.id
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
        val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()

        // TODO: support "required" and "optional" custom permissions
        val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()

        miniApp.getManifest(miniAppId, "").requiredPermissions?.forEach {
            namesForAdapter.add(it)
            resultsForAdapter.add(MiniAppCustomPermissionResult.ALLOWED)
        }

        miniApp.getManifest(miniAppId, "").optionalPermissions?.forEach {
            namesForAdapter.add(it)
            resultsForAdapter.add(MiniAppCustomPermissionResult.DENIED)
        }

        // TODO: cleanup ui with mentioning required and optional permissions
        // TODO: set custom permission while accepting to download

        permissionAdapter.addPermissionList(
            namesForAdapter,
            resultsForAdapter,
            arrayListOf() // TODO: put reason here
        )

        // set action listeners
        preloadMiniAppLayout.findViewById<TextView>(R.id.preloadAccept).setOnClickListener {
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
            if (doesDataExist()) return prefs.getBoolean(miniAppId,
                DEFAULT_ACCEPTANCE
            )
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
