package com.rakuten.tech.mobile.testapp.ui.permission

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniappDownloadedListActivityBinding
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class MiniAppDownloadedListActivity(private val miniApp: MiniApp) : BaseActivity(),
    MiniAppDownloadedListAdapter.MiniAppDownloadedListener {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private lateinit var adapter: MiniAppDownloadedListAdapter
    private lateinit var binding: MiniappDownloadedListActivityBinding

    private val startPermissionSettingsForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == MiniAppPermissionSettingsActivity.REQ_CODE_PERMISSIONS_UPDATE) {
                loadList()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.miniapp_downloaded_list_activity)
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
        loadList()
    }

    private fun initAdapter() {
        adapter = MiniAppDownloadedListAdapter(this)
        binding.listDownloadedMiniApp.layoutManager = LinearLayoutManager(applicationContext)
        binding.listDownloadedMiniApp.adapter = adapter
        binding.listDownloadedMiniApp.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun loadList() {
        val miniAppList: ArrayList<MiniAppInfo> = arrayListOf()
        val miniAppPermissions: ArrayList<String> = arrayListOf()
        miniApp.listDownloadedWithCustomPermissions().forEach {
            miniAppList.add(it.first)
            miniAppPermissions.add(parsePermissionText(it.second.pairValues))
        }
        adapter.addDownloadedList(miniAppList, miniAppPermissions)
        updateEmptyView()
    }

    override fun onMiniAppItemClick(miniAppInfo: MiniAppInfo) {
        val intent = MiniAppPermissionSettingsActivity.getStartIntent(this, miniAppInfo.id)
        startPermissionSettingsForResult.launch(intent)
    }

    private fun updateEmptyView() {
        if (adapter.itemCount == 0)
            binding.emptyView.visibility = View.VISIBLE
        else
            binding.emptyView.visibility = View.GONE
    }

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, MiniAppDownloadedListActivity::class.java))
        }
    }
}
