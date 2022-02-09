package com.rakuten.tech.mobile.testapp.ui.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rakuten.tech.mobile.miniapp.MiniApp

@Suppress("UNCHECKED_CAST")
class MiniAppDisplayViewModelFactory(private val miniApp: MiniApp) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MiniAppDisplayViewModel(miniapp = miniApp) as T
    }
}
