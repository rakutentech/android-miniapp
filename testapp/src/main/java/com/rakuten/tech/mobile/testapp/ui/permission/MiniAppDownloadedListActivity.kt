package com.rakuten.tech.mobile.testapp.ui.permission

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.adapter.MiniAppList
import com.rakuten.tech.mobile.testapp.adapter.MiniAppListAdapter
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.android.synthetic.main.miniapp_downloaded_list_activity.*
import kotlinx.android.synthetic.main.miniapp_downloaded_list_activity.emptyView

class MiniAppDownloadedListActivity(private val miniapp: MiniApp) : BaseActivity(), MiniAppList {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private lateinit var miniAppListAdapter: MiniAppListAdapter
    private var miniAppList: ArrayList<MiniAppInfo> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        setContentView(R.layout.miniapp_downloaded_list_activity)
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
        miniAppListAdapter = MiniAppListAdapter(ArrayList(), this)
        listDownloadedMiniApp.layoutManager = LinearLayoutManager(applicationContext)
        listDownloadedMiniApp.adapter = miniAppListAdapter
    }

    private fun executeLoadingList() {
        miniAppList.clear()
        miniapp.listDownloadedWithCustomPermissions().forEach {
            miniAppList.add(it.first)
        }
    }

    private fun addMiniAppList(miniAppsInfo: List<MiniAppInfo>) {
        miniAppListAdapter.addListWithSection(miniAppsInfo)
        updateEmptyView()
    }

    override fun onMiniAppItemClick(miniAppInfo: MiniAppInfo) {
        raceExecutor.run {
            MiniAppPermissionSettingsActivity.start(
                this@MiniAppDownloadedListActivity,
                miniAppInfo.id
            )
        }
    }

    private fun updateEmptyView() {
        if (miniAppListAdapter.itemCount == 0)
            emptyView.visibility = View.VISIBLE
        else
            emptyView.visibility = View.GONE
    }

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, MiniAppDownloadedListActivity::class.java))
        }
    }
}
