package com.rakuten.tech.mobile.testapp.ui.deeplink

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.DeeplinksActivityBinding
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import com.rakuten.tech.mobile.testapp.ui.userdata.ContactInputDialog
import kotlin.collections.ArrayList

class DeeplinkListActivity : BaseActivity(), DeeplinkListener {
    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    private lateinit var settings: AppSettings
    private lateinit var binding: DeeplinksActivityBinding
    private val adapter = DeeplinkListAdapter(this)
    private var deeplinkListPrefs: SharedPreferences? = null
    private var isFirstLaunch: Boolean
        get() = deeplinkListPrefs?.getBoolean(IS_FIRST_TIME, true) ?: true
        set(value) {
            deeplinkListPrefs?.edit()?.putBoolean(IS_FIRST_TIME, value)?.apply()
        }

    private val fakeDeeplinks = arrayOf("miniappdemo://miniapp", "miniappdemo://miniapp2", "miniappdemo://miniapp3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deeplinkListPrefs = getSharedPreferences(
                "com.rakuten.tech.mobile.miniapp.sample.deeplinks", Context.MODE_PRIVATE
        )
        settings = AppSettings.instance
        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.deeplinks_activity)
        renderRandomDeeplinkList()
        binding.fabAddDeeplink.setOnClickListener { onAddAction() }
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
        settings.deeplinks = adapter.provideDeeplinkEntries()
        finish()
    }

    private fun onAddAction() {
        showDialog(isUpdate = false)
    }

    private fun showDialog(isUpdate: Boolean, position: Int? = null) {
        val deeplinkView = layoutInflater.inflate(R.layout.dialog_add_deeplink, null)
        val edtDeeplink = deeplinkView.findViewById<AppCompatEditText>(R.id.edtDeeplink)

        ContactInputDialog.Builder().build(this).apply {
            val randomDeeplink = createRandomDeeplinkList()
            edtDeeplink.setText("")

            setView(deeplinkView)

            if (isUpdate) {
                setPositiveButton(getString(R.string.action_update))
                setDialogTitle("Deeplink Update")
                position?.let {
                    val existingDeeplink = adapter.provideDeeplinkEntries()[it]
                    edtDeeplink.setText(existingDeeplink)
                }
            } else {
                setPositiveButton(getString(R.string.action_add))
                setDialogTitle("Deeplink Input")
            }

            setPositiveListener(View.OnClickListener {
                val deeplink: String = edtDeeplink.text.toString().trim()

                if (isVerifiedDeeplink(deeplink)) {
                    if (isUpdate) position?.let { adapter.updateDeeplink(it, deeplink) }
                    else adapter.addDeeplink(adapter.itemCount, deeplink)

                    this.dialog?.cancel()
                }
            })
        }.show()
    }

    private fun isVerifiedDeeplink(deeplink: String): Boolean {
        var isVerified = true

        if (deeplink.isEmpty()) {
            isVerified = false

            Toast.makeText(this@DeeplinkListActivity, "Empty deeplink", Toast.LENGTH_LONG)
                    .apply { setGravity(Gravity.TOP, 0, 100) }
                    .show()
        }

        return isVerified
    }

    override fun onResume() {
        super.onResume()
        if (isFirstLaunch) isFirstLaunch = false
    }

    private fun renderRandomDeeplinkList() {
        if (!isFirstLaunch && settings.isDeeplinksSaved) {
            if (settings.deeplinks.isEmpty()) {
                renderAdapter(arrayListOf())
            } else renderAdapter(settings.deeplinks)
        } else {
            val randomList = createRandomDeeplinkList()
            renderAdapter(randomList)
        }
    }

    private fun createRandomDeeplinkList(): ArrayList<String> = ArrayList<String>().apply {
        for (i in 1..10) {
            this.add("random deep link")
        }
    }

    private fun renderAdapter(deeplinks: ArrayList<String>) {
        adapter.addDeeplinkList(deeplinks)
        binding.listDeeplink.adapter = adapter
        binding.listDeeplink.layoutManager = LinearLayoutManager(applicationContext)
        binding.listDeeplink.addItemDecoration(
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            @SuppressLint("SyntheticAccessor")
            override fun onChanged() {
                super.onChanged()
                observeUIState()
            }

            @SuppressLint("SyntheticAccessor")
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                observeUIState()
            }

            @SuppressLint("SyntheticAccessor")
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                observeUIState()
            }
        })
        observeUIState()
    }

    private fun observeUIState() {
        when {
            adapter.itemCount == 0 -> {
                binding.viewEmptyDeeplink.visibility = View.VISIBLE
                binding.statusNoDeeplink.visibility = View.GONE
            }
            adapter.itemCount != 0 && !settings.isDeeplinksSaved -> {
                binding.viewEmptyDeeplink.visibility = View.GONE
                binding.statusNoDeeplink.visibility = View.VISIBLE
            }
            else -> {
                binding.viewEmptyDeeplink.visibility = View.GONE
                binding.statusNoDeeplink.visibility = View.GONE
            }
        }
    }

    companion object {
        private const val IS_FIRST_TIME = "is_first_time"
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DeeplinkListActivity::class.java))
        }
    }

    override fun onDeeplinkItemClick(position: Int) {
        showDialog(isUpdate = true, position = position)
    }
}
