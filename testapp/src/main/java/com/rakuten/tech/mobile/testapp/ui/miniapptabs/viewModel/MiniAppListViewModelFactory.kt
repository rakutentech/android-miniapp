package com.rakuten.tech.mobile.testapp.ui.miniapptabs.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rakuten.tech.mobile.miniapp.MiniApp

@Suppress("UNCHECKED_CAST")
class MiniAppListViewModelFactory(private val miniApp: MiniApp) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MiniAppListViewModel(miniapp = miniApp) as T
    }
}
