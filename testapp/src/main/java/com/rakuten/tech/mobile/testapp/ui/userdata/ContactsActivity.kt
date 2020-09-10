package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.AppScreen
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsMenuActivity
import kotlinx.android.synthetic.main.contacts_activity.*

class ContactsActivity : BaseActivity() {

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, ContactsActivity::class.java)
            intent.putExtra(
                SettingsMenuActivity.SETTINGS_SCREEN_NAME,
                AppScreen.MINI_APP_SETTINGS_ACTIVITY
            )
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.contacts_activity)
        initializeActionBar()
        setContactList()
    }

    private fun initializeActionBar() {
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)
        showBackIcon()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateToPreviousScreen()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setContactList() {
        val adapter = ContactsAdapter()
        val dummyContacts = arrayListOf<String>()
        for (i in 1..10) dummyContacts.add("User Contact - $i")
        adapter.addContactList(dummyContacts)
        listContacts.adapter = adapter

        listContacts.layoutManager = LinearLayoutManager(applicationContext)
        listContacts.addItemDecoration(
            DividerItemDecoration(
                applicationContext,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun navigateToPreviousScreen() {
        when (intent.extras?.getString(SettingsMenuActivity.SETTINGS_SCREEN_NAME)) {
            AppScreen.MINI_APP_SETTINGS_ACTIVITY -> {
                val intent = Intent(this, SettingsMenuActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                finish()
            }
            else -> finish()
        }
    }

    override fun onBackPressed() {
        navigateToPreviousScreen()
        super.onBackPressed()
    }
}
