package com.rakuten.tech.mobile.testapp.ui.permission

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.helper.MiniAppListStore
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import kotlinx.android.synthetic.main.downloaded_miniapp_list_activity.*
import kotlinx.android.synthetic.main.downloaded_miniapp_list_activity.swipeRefreshLayout

class DownloadedMiniAppListActivity : BaseActivity(), DownloadedMiniAppList {

    private lateinit var adapter: DownloadedMiniAppListAdapter
    private lateinit var viewModel: DownloadedMiniAppListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        setContentView(R.layout.downloaded_miniapp_list_activity)
        renderScreen()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        swipeRefreshLayout.post {
            swipeRefreshLayout.isRefreshing = true
            executeLoadingList()
        }
    }

    private fun renderScreen() {
        renderAdapter()

        viewModel = ViewModelProvider.NewInstanceFactory()
            .create(DownloadedMiniAppListViewModel::class.java).apply {
                miniAppListData.observe(this@DownloadedMiniAppListActivity, Observer {
                    swipeRefreshLayout.isRefreshing = false
                    addMiniAppList(it)
                    // MiniAppListStore.instance.saveMiniAppList(it)
                })
                errorData.observe(this@DownloadedMiniAppListActivity, Observer {
                    val list = MiniAppListStore.instance.getMiniAppList()
                    if (list.isEmpty())
                        checkEmpty()
                    else {
                        addMiniAppList(list)
                        swipeRefreshLayout.isRefreshing = false
                    }
                })
            }

        swipeRefreshLayout.setOnRefreshListener {
            executeLoadingList()
        }
    }

    private fun renderAdapter() {
        adapter = DownloadedMiniAppListAdapter(this)
        listDownloadedMiniApp.adapter = adapter
        listDownloadedMiniApp.layoutManager = LinearLayoutManager(applicationContext)
        listDownloadedMiniApp.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun checkEmpty() {
        viewEmptyContact.visibility =
            if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun addMiniAppList(list: List<MiniAppInfo>) {
        adapter.addList(list, arrayListOf())
    }

    private fun executeLoadingList() {
        viewModel.getMiniAppList()
    }

    override fun onMiniAppItemClick(miniAppInfo: MiniAppInfo) {
        raceExecutor.run {
            PermissionSettingsActivity.start(this@DownloadedMiniAppListActivity, miniAppInfo)
        }
    }

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DownloadedMiniAppListActivity::class.java))
        }
    }
}
