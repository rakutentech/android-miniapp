package com.rakuten.tech.mobile.testapp.ui.permission

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.MiniappDownloadedListActivityBinding
import com.rakuten.tech.mobile.testapp.helper.MiniAppListStore
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.viewModel.MiniAppListViewModel
import com.rakuten.tech.mobile.testapp.ui.miniapptabs.viewModel.MiniAppListViewModelFactory
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsProgressDialog

class MiniAppDownloadedListActivity(private val miniApp: MiniApp) : BaseActivity(),
    MiniAppDownloadedListAdapter.MiniAppDownloadedListener {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    constructor() : this(MiniApp.instance(AppSettings.instance.newMiniAppSdkConfig))

    private lateinit var adapter: MiniAppDownloadedListAdapter
    private lateinit var binding: MiniappDownloadedListActivityBinding
    private lateinit var viewModel: MiniAppListViewModel
    private lateinit var settingsProgressDialog: SettingsProgressDialog

    private val startPermissionSettingsForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == MiniAppPermissionSettingsActivity.REQ_CODE_PERMISSIONS_UPDATE) {
                loadList()
            }
        }

    override fun onStart() {
        super.onStart()
        executeLoadingList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.miniapp_downloaded_list_activity)
        settingsProgressDialog = SettingsProgressDialog(this)
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
        settingsProgressDialog.show()
        val factory = MiniAppListViewModelFactory(MiniApp.instance(AppSettings.instance.newMiniAppSdkConfig))
        viewModel =
                ViewModelProvider(this, factory).get(MiniAppListViewModel::class.java).apply {
                settingsProgressDialog.cancel()
                miniAppListData.observe(
                    this@MiniAppDownloadedListActivity,
                    Observer { miniAppListAvailable ->
                        checkAndUpdateList(miniAppListAvailable)
                    })
                errorData.observe(this@MiniAppDownloadedListActivity, Observer {
                    val list = MiniAppListStore.instance.getMiniAppList()
                    if (list.isNotEmpty()) {
                        checkAndUpdateList(list)
                    }
                })
            }
    }
    /** Check MiniApp List Available in RAS and Update the downloaded miniapp list*/
    private fun checkAndUpdateList(availableMiniAppList: List<MiniAppInfo>) {
        val miniAppList: ArrayList<MiniAppInfo> = arrayListOf()
        val miniAppPermissions: ArrayList<String> = arrayListOf()
        miniApp.listDownloadedWithCustomPermissions().forEach {
            if (availableMiniAppList.contains(it.first)) {
                miniAppList.add(it.first)
                miniAppPermissions.add(parsePermissionText(it.second.pairValues))
            }
        }
        adapter.addDownloadedList(miniAppList, miniAppPermissions)
        updateEmptyView()
    }

    private fun executeLoadingList() {
        viewModel.getMiniAppList()
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
