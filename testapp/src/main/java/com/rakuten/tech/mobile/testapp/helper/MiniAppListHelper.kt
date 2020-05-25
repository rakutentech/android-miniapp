package com.rakuten.tech.mobile.testapp.helper

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import java.lang.IllegalStateException

class MiniAppListHelper(context: Context) {

    companion object {
        lateinit var instance: MiniAppListHelper

        fun init(context: Context) {
            instance = MiniAppListHelper(context)
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.testapp.helper.miniapplist",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val MINI_APP_LIST = "mini_app_list"

    fun saveMiniAppListToStorage(list: List<MiniAppInfo>): Boolean = when {
        list.isEmpty() -> false
        else -> {
            prefs.edit().putString(MINI_APP_LIST, gson.toJson(list)).apply()
            true
        }
    }

    fun getMiniAppListFromStorage(): List<MiniAppInfo> = try {
        gson.fromJson<List<MiniAppInfo>>(
            prefs.getString(MINI_APP_LIST, ""),
            object : TypeToken<List<MiniAppInfo>>() {}.type
        )
    } catch (error: IllegalStateException) {
        emptyList()
    }
}
