package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context

internal class MiniAppLocal(val context: Context) {
    private var miniAppStatus: MiniAppStatus? = null
    private var miniAppStorage: MiniAppStorage? = null
    private var verifier: CachedMiniAppVerifier? = null

    fun getMiniAppStatus(): MiniAppStatus {
        if (miniAppStatus == null)
            miniAppStatus = MiniAppStatus(context)
        return miniAppStatus!!
    }

    fun getMiniAppStorage(): MiniAppStorage {
        if (miniAppStorage == null)
            miniAppStorage = MiniAppStorage(FileWriter(), context.filesDir)
        return miniAppStorage!!
    }

    fun getVerifier(): CachedMiniAppVerifier {
        if (verifier == null)
            verifier = CachedMiniAppVerifier(context)
        return verifier!!
    }
}
