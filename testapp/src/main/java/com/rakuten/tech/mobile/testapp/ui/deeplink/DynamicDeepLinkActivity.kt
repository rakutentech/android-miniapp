package com.rakuten.tech.mobile.testapp.ui.deeplink

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.DynamicDeeplinkActivityBinding
import com.rakuten.tech.mobile.testapp.helper.getAdapterDataObserver
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import com.rakuten.tech.mobile.testapp.ui.userdata.ContactInputDialog
import kotlin.collections.ArrayList

class DynamicDeepLinkActivity : BaseActivity(), DeepLinkListener {
    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    private lateinit var settings: AppSettings
    private lateinit var binding: DynamicDeeplinkActivityBinding
    private val adapter = DeepLinkListAdapter(this)
    private var deepLinksPrefs: SharedPreferences? = null
    private var isFirstLaunch: Boolean
        get() = deepLinksPrefs?.getBoolean(IS_FIRST_TIME, true) ?: true
        set(value) {
            deepLinksPrefs?.edit()?.putBoolean(IS_FIRST_TIME, value)?.apply()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinksPrefs = getSharedPreferences(
                "com.rakuten.tech.mobile.miniapp.sample.dynamic_deeplinks", Context.MODE_PRIVATE
        )
        settings = AppSettings.instance
        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.dynamic_deeplink_activity)
        binding.fabAddDeepLink.setOnClickListener { onAddAction() }
        renderAdapter(settings.dynamicDeeplinks)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
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
        settings.dynamicDeeplinks = adapter.provideDeepLinkEntries()
        finish()
    }

    private fun onAddAction() {
        showDialog(isUpdate = false)
    }

    private fun showDialog(isUpdate: Boolean, position: Int? = null) {
        val deepLinkView = layoutInflater.inflate(R.layout.dialog_add_dynamic_deeplink, null)
        val edtDeepLink = deepLinkView.findViewById<AppCompatEditText>(R.id.edtDeepLink)

        ContactInputDialog.Builder().build(this).apply {
            setView(deepLinkView)

            if (isUpdate) {
                setPositiveButton(getString(R.string.action_update))
                setDialogTitle("Update Deeplink")
                position?.let {
                    val existingDeepLink = adapter.provideDeepLinkEntries()[it]
                    edtDeepLink.setText(existingDeepLink)
                }
            } else {
                setPositiveButton(getString(R.string.action_add))
                setDialogTitle("Add New Deeplink")
            }

            setPositiveListener(View.OnClickListener {
                val deepLink: String = edtDeepLink.text.toString().trim()

                if (isVerifiedDeepLink(deepLink)) {
                    if (isUpdate) position?.let { adapter.updateDeepLink(it, deepLink) }
                    else adapter.addDeepLink(adapter.itemCount, deepLink)

                    this.dialog?.cancel()
                }
            })
        }.show()
    }

    private fun isVerifiedDeepLink(deepLink: String): Boolean {
        var isVerified = true

        if (deepLink.isEmpty()) {
            isVerified = false
            showWarning(getString(R.string.deeplink_activity_error_empty))
        }

        return isVerified
    }

    private fun showWarning(message: String) {
        Toast.makeText(this@DynamicDeepLinkActivity, message, Toast.LENGTH_LONG)
                .apply { setGravity(Gravity.TOP, 0, 100) }
                .show()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstLaunch) isFirstLaunch = false
    }

    private fun renderAdapter(deepLinks: ArrayList<String>) {
        adapter.addDeepLinkList(deepLinks)
        binding.listDeepLink.adapter = adapter
        binding.listDeepLink.layoutManager = LinearLayoutManager(applicationContext)
        binding.listDeepLink.addItemDecoration(
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        adapter.registerAdapterDataObserver(getAdapterDataObserver { observeUIState() })
        observeUIState()
    }

    private fun observeUIState() {
        when {
            adapter.itemCount == 0 -> {
                binding.viewEmptyDeepLink.visibility = View.VISIBLE
                binding.statusNoDeepLink.visibility = View.GONE
            }
            adapter.itemCount != 0 && !settings.isDynamicDeeplinksSaved -> {
                binding.viewEmptyDeepLink.visibility = View.GONE
                binding.statusNoDeepLink.visibility = View.VISIBLE
            }
            else -> {
                binding.viewEmptyDeepLink.visibility = View.GONE
                binding.statusNoDeepLink.visibility = View.GONE
            }
        }
    }

    companion object {
        private const val IS_FIRST_TIME = "is_first_time"
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DynamicDeepLinkActivity::class.java))
        }
    }

    override fun onDeepLinkItemClick(position: Int) {
        showDialog(isUpdate = true, position = position)
    }
}
