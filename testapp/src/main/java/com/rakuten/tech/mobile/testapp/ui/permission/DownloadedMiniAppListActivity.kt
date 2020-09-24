package com.rakuten.tech.mobile.testapp.ui.permission

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.android.synthetic.main.downloaded_miniapp_list_activity.*

// TODO: rename
class DownloadedMiniAppListActivity(private val miniapp: MiniApp) : BaseActivity(),
    DownloadedMiniAppList {

    private lateinit var adapter: DownloadedMiniAppListAdapter
    private var miniAppList: List<MiniAppInfo> = emptyList()

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

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

    private fun renderScreen() {
        initAdapter()
        executeLoadingList()
        addMiniAppList(miniAppList)
    }

    private fun initAdapter() {
        adapter = DownloadedMiniAppListAdapter(this)
        listDownloadedMiniApp.adapter = adapter
        listDownloadedMiniApp.layoutManager = LinearLayoutManager(applicationContext)
        listDownloadedMiniApp.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun addMiniAppList(list: List<MiniAppInfo>) {
        adapter.addList(list, arrayListOf())
        checkEmpty()
    }

    private fun executeLoadingList() {
        miniAppList = miniapp.listDownloadedWithCustomPermissions()
    }

    override fun onMiniAppItemClick(miniAppInfo: MiniAppInfo) {
        raceExecutor.run {
            PermissionSettingsActivity.start(this@DownloadedMiniAppListActivity, miniAppInfo)
        }
    }

    private fun checkEmpty() {
        viewEmptyContact.visibility =
            if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DownloadedMiniAppListActivity::class.java))
        }
    }
}
