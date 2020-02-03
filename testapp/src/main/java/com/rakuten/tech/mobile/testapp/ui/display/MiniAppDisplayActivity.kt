package com.rakuten.tech.mobile.testapp.ui.display

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import kotlinx.coroutines.launch

class MiniAppDisplayActivity: BaseActivity() {

    private lateinit var viewModel: MiniAppDwnlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)
            .get(MiniAppDwnlViewModel::class.java).apply {
                miniAppView.observe(this@MiniAppDisplayActivity, Observer {
                    //action: display webview
                    setContentView(it)
                })
            }

        val miniAppInfo = MiniAppInfo(
            id = "c028be14-4ded-4734-acc6-2d35d9a67630",
            versionId = "1ad5f15d-3fec-4872-8507-7c0f3fd54acb",
            description = "",
            icon = "",
            files = emptyList(),
            name = "")
        launch { viewModel.obtainMiniAppView(miniAppInfo, this@MiniAppDisplayActivity) }
    }
}
