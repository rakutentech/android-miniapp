package com.rakuten.tech.mobile.testapp.ui.permission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ListCustomPermissionBinding
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class MiniAppPermissionSettingsActivity(private val miniapp: MiniApp) : BaseActivity() {

    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    constructor() : this(MiniApp.instance(AppSettings.instance.newMiniAppSdkConfig))

    private lateinit var permissionSettingsAdapter: MiniAppPermissionSettingsAdapter
    private lateinit var miniAppId: String
    private lateinit var binding: ListCustomPermissionBinding
    private val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()

    companion object {
        const val REQ_CODE_PERMISSIONS_UPDATE = 10101
        private const val miniAppIdTag = "mini_app_id_tag"

        fun getStartIntent(context: Context, miniAppId: String): Intent =
            Intent(context, MiniAppPermissionSettingsActivity::class.java).apply {
                putExtra(miniAppIdTag, miniAppId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.list_custom_permission)

        renderScreen()
    }

    private fun renderScreen() {
        if (!intent.hasExtra(miniAppIdTag)) return

        miniAppId = intent.getStringExtra(miniAppIdTag) ?: ""

        initAdapter()
        val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()

        miniapp.getCustomPermissions(miniAppId).pairValues.forEach {
            namesForAdapter.add(it.first)
            resultsForAdapter.add(it.second)
        }

        permissionSettingsAdapter.addPermissionList(
            namesForAdapter,
            resultsForAdapter,
            arrayListOf()
        )

        if (namesForAdapter.isEmpty()) binding.emptyView.visibility = View.VISIBLE
        else binding.emptyView.visibility = View.GONE
    }

    private fun initAdapter() {
        permissionSettingsAdapter = MiniAppPermissionSettingsAdapter()
        binding.listCustomPermission.layoutManager = LinearLayoutManager(applicationContext)
        binding.listCustomPermission.adapter = permissionSettingsAdapter
        binding.listCustomPermission.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.settings_menu_save)?.isEnabled = namesForAdapter.isNotEmpty()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.settings_menu_save -> {
                onSaveAction()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onSaveAction() {
        // store values in SDK cache
        val miniAppCustomPermission = MiniAppCustomPermission(
            miniAppId = miniAppId,
            pairValues = permissionSettingsAdapter.permissionPairs
        )
        miniapp.setCustomPermissions(miniAppCustomPermission)

        // echo a result that permissions has been saved
        setResult(REQ_CODE_PERMISSIONS_UPDATE, Intent())
        finish()
    }
}
