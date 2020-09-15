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
import com.rakuten.tech.mobile.testapp.helper.clearWhiteSpaces
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsMenuActivity
import kotlinx.android.synthetic.main.contacts_activity.*
import java.util.UUID

class ContactsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contacts_activity)
        initializeActionBar()
        createContactList()
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

    private fun initializeActionBar() {
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)
        showBackIcon()
    }

    private fun createContactList() {
        // prepare random contact list
        val randomContacts = arrayListOf<String>()
        for (i in 1..10)
            randomContacts.add(clearWhiteSpaces((UUID.randomUUID().toString())))

        // add contacts in adapter
        val adapter = ContactsAdapter()
        adapter.addContactList(randomContacts)
        listContacts.adapter = adapter
        listContacts.layoutManager = LinearLayoutManager(applicationContext)
        listContacts.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

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
}
