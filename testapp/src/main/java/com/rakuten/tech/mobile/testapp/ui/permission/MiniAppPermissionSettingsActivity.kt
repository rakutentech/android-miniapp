package com.rakuten.tech.mobile.testapp.ui.permission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.android.synthetic.main.list_custom_permission.*

class MiniAppPermissionSettingsActivity(private val miniapp: MiniApp) : BaseActivity() {

    constructor() : this(MiniApp.instance(AppSettings.instance.miniAppSettings))

    private lateinit var permissionSettingsAdapter: MiniAppPermissionSettingsAdapter
    private lateinit var miniAppId: String

    companion object {
        private const val miniAppIdTag = "mini_app_id_tag"

        fun start(context: Context, miniAppId: String) {
            context.startActivity(
                Intent(context, MiniAppPermissionSettingsActivity::class.java).apply {
                    putExtra(miniAppIdTag, miniAppId)
                })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showBackIcon()
        setContentView(R.layout.window_custom_permission)

        renderScreen()
    }

    private fun renderScreen() {
        if (!intent.hasExtra(miniAppIdTag)) return

        miniAppId = intent.getStringExtra(miniAppIdTag) ?: ""

        initAdapter()
        val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()
        val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()

        miniapp.getCustomPermissions(miniAppId).pairValues.forEach {
            namesForAdapter.add(it.first)
            resultsForAdapter.add(it.second)
        }

        permissionSettingsAdapter.addPermissionList(
            namesForAdapter,
            resultsForAdapter
        )
    }

    private fun initAdapter() {
        permissionSettingsAdapter = MiniAppPermissionSettingsAdapter()
        listCustomPermission.layoutManager = LinearLayoutManager(applicationContext)
        listCustomPermission.adapter = permissionSettingsAdapter
        listCustomPermission.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.settings_menu_save -> {
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
            miniAppId = miniAppId,
            pairValues = permissionSettingsAdapter.permissionPairs
        )
        miniapp.setCustomPermissions(miniAppCustomPermission)
    }
}
