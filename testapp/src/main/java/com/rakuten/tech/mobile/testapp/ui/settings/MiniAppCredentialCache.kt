package com.rakuten.tech.mobile.testapp.ui.settings

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.rakuten.tech.mobile.miniapp.testapp.BuildConfig
import com.rakuten.tech.mobile.testapp.BuildVariant


class MiniAppCredentialCache(
    private val isproductionKey: String,
    private val isPreviewModeKey: String,
    private val isSignatureVerificationRequiredKey: String,
    private val projectidKey: String,
    private val subscriptionKey: String,
) {

    fun getData(prefs: SharedPreferences): MiniAppCredentialData {
        return MiniAppCredentialData(
            prefs.getBoolean(isproductionKey, BuildConfig.BUILD_TYPE == BuildVariant.RELEASE.value),
            prefs.getBoolean(isPreviewModeKey, false),
            prefs.getBoolean(isSignatureVerificationRequiredKey, false),
            prefs.getString(projectidKey, null) ?: "",
            prefs.getString(subscriptionKey, null) ?: ""
        )
    }

    /**
     * no OnSharedPreferenceChangeListener added, thus requires immediate value
     */

    fun setIsProduction(
        editor: Editor,
        isProduction: Boolean
    ) {
        editor.putBoolean(isproductionKey, isProduction).commit()
    }

    fun setData(
        editor: Editor,
        credentialData: MiniAppCredentialData,
    ) {
        editor.putBoolean(isproductionKey, credentialData.isProduction).commit()
        editor.putBoolean(isPreviewModeKey, credentialData.isPreviewMode).commit()
        editor.putBoolean(
            isSignatureVerificationRequiredKey,
            credentialData.requireSignatureVerification
        ).commit()
        editor.putString(projectidKey, credentialData.projectId).commit()
        editor.putString(subscriptionKey, credentialData.subscriptionId).commit()
    }

    fun isValid(prefs: SharedPreferences): Boolean {
        val data = getData(prefs)
        return data.projectId.isNotBlank() && data.subscriptionId.isNotBlank()
    }

    fun clear(editor: Editor) {
        editor.remove(isproductionKey).commit()
        editor.remove(isPreviewModeKey).commit()
        editor.remove(isSignatureVerificationRequiredKey).commit()
        editor.remove(projectidKey).commit()
        editor.remove(subscriptionKey).commit()
    }

}
