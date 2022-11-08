package com.rakuten.tech.mobile.testapp.ui.settings.cache

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.google.gson.Gson


class MiniAppConfigCache(
    private val key: String,
) {

    fun getData(
        gson: Gson,
        prefs: SharedPreferences
    ): MiniAppConfigData? {
        val miniAppConfigData = prefs.getString(key, null) ?: return null
        return gson.fromJson(miniAppConfigData)
    }

    /**
     * no OnSharedPreferenceChangeListener added, thus requires immediate value
     */

    fun setData(
        editor: Editor,
        configData: MiniAppConfigData,
    ) {
        editor.putString(key, configData.toJsonString()).commit()
    }

    fun clear(editor: Editor) {
        editor.remove(key).commit()
    }

}
