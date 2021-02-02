package com.rakuten.tech.mobile.testapp.ui.display.firsttime

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.helper.setIcon
import java.lang.Exception

class FirstTimeWindow(
    private val activity: Activity,
    private val firstTimeLaunchListener: FirstTimeLaunchListener
) {
    private lateinit var firstTimeAlertDialog: AlertDialog
    private lateinit var firstTimeLayout: View
    private var miniAppInfo: MiniAppInfo? = null
    private var miniAppId: String = ""

    private var prefs: SharedPreferences = activity.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.sample.first_time.launch", Context.MODE_PRIVATE
    )

    fun initiate(appInfo: MiniAppInfo?, miniAppId: String) {
        if (appInfo != null) {
            this.miniAppInfo = appInfo
            this.miniAppId = miniAppInfo!!.id
        } else this.miniAppId = miniAppId

        if (isFirstTime()) {
            launchScreen()
        } else if (!doesDataExist()) {
            storeFirstTime(DEFAULT_FIRST_TIME) // should be false only after accept.
            launchScreen()
        } else {
            firstTimeLaunchListener.onFirstTimeAccept(true, miniAppInfo, miniAppId)
        }
    }

    private fun launchScreen() {
        initDefaultWindow()
        firstTimeAlertDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun initDefaultWindow() {
        // set ui components
        val layoutInflater = LayoutInflater.from(activity)
        firstTimeLayout = layoutInflater.inflate(R.layout.window_first_time_miniapp, null)
        firstTimeAlertDialog =
            AlertDialog.Builder(activity, R.style.AppTheme_DefaultWindow).create()
        firstTimeAlertDialog.setView(firstTimeLayout)

        // set data to ui
        val nameView = firstTimeLayout.findViewById<TextView>(R.id.firstTimeMiniAppName)
        val versionView = firstTimeLayout.findViewById<TextView>(R.id.firstTimeMiniAppVersion)
        if (miniAppInfo != null) {
            setIcon(
                activity,
                Uri.parse(miniAppInfo?.icon),
                firstTimeLayout.findViewById(R.id.firstTimeAppIcon)
            )

            nameView.text = miniAppInfo?.displayName.toString()
            versionView.text = "Version: " + miniAppInfo?.version?.versionTag.toString()
        } else {
            nameView.text = "No info found for this miniapp!"
        }

        // set action listeners
        firstTimeLayout.findViewById<Button>(R.id.firstTimeAccept).setOnClickListener {
            storeFirstTime(false) // set false when accept
            firstTimeLaunchListener.onFirstTimeAccept(true, miniAppInfo, miniAppId)
            firstTimeAlertDialog.dismiss()
        }
        firstTimeLayout.findViewById<Button>(R.id.firstTimeCancel).setOnClickListener {
            firstTimeLaunchListener.onFirstTimeAccept(false, miniAppInfo, miniAppId)
            firstTimeAlertDialog.dismiss()
        }
    }

    private fun doesDataExist() = prefs.contains(miniAppId)

    private fun isFirstTime(): Boolean {
        try {
            if (doesDataExist()) return prefs.getBoolean(miniAppId, DEFAULT_FIRST_TIME)
        } catch (e: Exception) {
            return true
        }
        return true
    }

    private fun storeFirstTime(isFirstTime: Boolean) =
        prefs.edit()?.putBoolean(miniAppId, isFirstTime)?.apply()

    interface FirstTimeLaunchListener {
        fun onFirstTimeAccept(isAccepted: Boolean, appInfo: MiniAppInfo?, miniAppId: String)
    }

    private companion object {
        const val DEFAULT_FIRST_TIME = true
    }
}
