package com.rakuten.tech.mobile.testapp.ui.settings.cache

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniAppInfo


class MiniAppListCache(private val key: String) {

    fun getData(
        prefs: SharedPreferences
    ): List<MiniAppInfo>? {
        val miniAppList = prefs.getString(key, null) ?: return null
        return Gson().fromJson(miniAppList)
    }

    fun setData(
        editor: Editor,
        data: List<MiniAppInfo>
    ){
        editor.putString(key, data.toJsonString())
    }

    fun clear(editor: Editor) {
        editor.remove(key).commit()
    }

    private fun List<MiniAppInfo>.toJsonString() = Gson().toJson(this)

    private fun Gson.fromJson(key: String) = fromJson(key, Array<MiniAppInfo>::class.java).toList()
}


