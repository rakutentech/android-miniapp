package com.rakuten.tech.mobile.testapp.ui.display.preload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rakuten.tech.mobile.miniapp.MiniApp

@Suppress("UNCHECKED_CAST")
class PreloadMiniAppFactory(
    private val miniApp: MiniApp,
    private val shouldShowDialog: Boolean
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PreloadMiniAppViewModel(miniApp = miniApp, shouldShowDialog) as T
    }
}
