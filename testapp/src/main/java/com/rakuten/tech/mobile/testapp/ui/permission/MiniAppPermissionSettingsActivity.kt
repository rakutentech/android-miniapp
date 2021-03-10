package com.rakuten.tech.mobile.testapp.ui.permission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ListCustomPermissionBinding
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity

class MiniAppPermissionSettingsActivity : BaseActivity() {

    private lateinit var permissionSettingsAdapter: MiniAppPermissionSettingsAdapter
    private lateinit var miniAppId: String
    private lateinit var binding: ListCustomPermissionBinding
    private lateinit var viewModel: PermissionSettingsViewModel

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
        viewModel =
            ViewModelProvider.NewInstanceFactory().create(PermissionSettingsViewModel::class.java)
                .apply {
                    val namesForAdapter: ArrayList<MiniAppCustomPermissionType> = arrayListOf()
                    val resultsForAdapter: ArrayList<MiniAppCustomPermissionResult> = arrayListOf()

                    miniAppCustomPermission.observe(this@MiniAppPermissionSettingsActivity,
                        Observer { perm ->
                            perm.pairValues.forEach {
                                namesForAdapter.add(it.first)
                                resultsForAdapter.add(it.second)
                            }
                            permissionSettingsAdapter.addPermissionList(
                                namesForAdapter,
                                resultsForAdapter,
                                arrayListOf()
                            )
                        }
                    )
                }

        viewModel.getCustomPermission(miniAppId)
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
        viewModel.miniApp.setCustomPermissions(miniAppCustomPermission)

        // echo a result that permissions has been saved
        setResult(REQ_CODE_PERMISSIONS_UPDATE, Intent())
        finish()
    }
}
