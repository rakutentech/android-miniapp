package com.rakuten.tech.mobile.testapp.ui.display.firsttime

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
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

    private fun initDefaultWindow() {
        // set ui
        val layoutInflater = LayoutInflater.from(activity)
        firstTimeLayout = layoutInflater.inflate(R.layout.window_first_time_miniapp, null)
        firstTimeAlertDialog =
            AlertDialog.Builder(activity, R.style.AppTheme_DefaultWindow).create()
        firstTimeAlertDialog.setView(firstTimeLayout)

        // set action listeners
        firstTimeLayout.findViewById<ImageView>(R.id.firstTimeCloseWindow).setOnClickListener {
            firstTimeLaunchListener.onFirstTimeAccept(false, miniAppInfo, miniAppId)
            firstTimeAlertDialog.dismiss()
        }
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

    private fun isAlreadyDownloaded(appId: String): Boolean {
        return false
    }

    private fun isFirstTime(): Boolean {
        try {
            if (doesDataExist()) {
                val firstTimeLaunch: FirstTimeLaunch = Gson().fromJson(
                    prefs.getString(miniAppId, ""),
                    object : TypeToken<FirstTimeLaunch>() {}.type
                )

                return firstTimeLaunch.isFirstTime
            }
        } catch (e: Exception) {
            return true
        }
        return true
    }

    private fun storeFirstTime(isFirstTime: Boolean) {
        val firstTimeLaunch = FirstTimeLaunch(miniAppId, isFirstTime)
        val jsonToStore: String = Gson().toJson(firstTimeLaunch)
        prefs.edit()?.putString(miniAppId, jsonToStore)?.apply()
    }

    interface FirstTimeLaunchListener {
        fun onFirstTimeAccept(isAccepted: Boolean, appInfo: MiniAppInfo?, miniAppId: String)
    }

    private companion object {
        const val DEFAULT_FIRST_TIME = true
    }
}
