package com.rakuten.tech.mobile.testapp.ui.permission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.android.synthetic.main.permission_settings_activity.*

// TODO: rename
class PermissionSettingsActivity(private val miniapp: MiniApp) : BaseActivity() {
    private lateinit var adapter: CustomPermissionAdapter
    private lateinit var appId: String

    companion object {
        private const val miniAppTag = "mini_app_tag"

        fun start(context: Context, miniAppInfo: MiniAppInfo) {
            context.startActivity(Intent(context, PermissionSettingsActivity::class.java).apply {
                putExtra(miniAppTag, miniAppInfo)
            })
        }
    }

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        setContentView(R.layout.permission_settings_activity)

        renderScreen()
    }

    private fun renderScreen() {
        if (intent.hasExtra(miniAppTag)) {
            appId = (intent.getParcelableExtra<MiniAppInfo>(miniAppTag) ?: return).id

            initAdapter()

            val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()
            val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()

            miniapp.getCustomPermissions(appId).pairValues.forEach {
                namesForAdapter.add(it.first)
                resultsForAdapter.add(it.second)
            }

            adapter.addPermissionList(namesForAdapter, resultsForAdapter, arrayListOf())
        }
    }

    private fun initAdapter() {
        adapter = CustomPermissionAdapter()
        listCustomPermission.adapter = adapter
        listCustomPermission.layoutManager = LinearLayoutManager(applicationContext)
        listCustomPermission.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onSaveAction()
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onSaveAction() {
        // store values in SDK cache
        val miniAppCustomPermission = MiniAppCustomPermission(
            miniAppId = appId,
            pairValues = adapter.permissionPairs
        )
        miniapp.setCustomPermissions(miniAppCustomPermission)
    }
}
