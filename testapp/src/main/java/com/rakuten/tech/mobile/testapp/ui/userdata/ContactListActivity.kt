package com.rakuten.tech.mobile.testapp.ui.userdata

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.AppScreen
import com.rakuten.tech.mobile.testapp.helper.clearWhiteSpaces
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import com.rakuten.tech.mobile.testapp.ui.settings.SettingsMenuActivity
import kotlinx.android.synthetic.main.contacts_activity.*
import java.util.UUID

class ContactListActivity : BaseActivity() {

    private lateinit var settings: AppSettings
    private val adapter = ContactListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings.instance
        setContentView(R.layout.contacts_activity)
        initializeActionBar()
        renderContactList()
        fabAddContact.setOnClickListener { onAddAction() }
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
        settings.contactNames = adapter.provideContactEntries()
        finish()
    }

    private fun onAddAction() {
        val contactView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        val editNewContact: AppCompatEditText = contactView.findViewById(R.id.editNewContact)
        ContactInputDialog.Builder().build(this).apply {
            setView(contactView)
            setPositiveListener(DialogInterface.OnClickListener { dialog, _ ->
                if (editNewContact.text.toString().isNotEmpty()) {
                    val name = editNewContact.text.toString()
                    // add contact in the last position
                    adapter.addContact(adapter.itemCount, clearWhiteSpaces(name))
                } else {
                    Toast.makeText(
                        this@ContactListActivity,
                        getString(R.string.userdata_error_invalid_contact),
                        Toast.LENGTH_LONG
                    ).apply {
                        setGravity(Gravity.BOTTOM, 0, 100)
                    }.show()
                }
                dialog.cancel()
            })
        }.show()
    }

    private fun initializeActionBar() {
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)
        showBackIcon()
    }

    private fun renderContactList() {
        var contactNames = ArrayList<String>()
        if (settings.contactNames.isEmpty())
            contactNames = createRandomUUIDList()
        else
            contactNames.addAll(settings.contactNames)

        configureAdapter(contactNames)
    }

    private fun createRandomUUIDList(): ArrayList<String> {
        val randomUUIDs = ArrayList<String>()
        for (i in 1..10)
            randomUUIDs.add(clearWhiteSpaces((UUID.randomUUID().toString())))

        return randomUUIDs
    }

    private fun configureAdapter(contactNames: ArrayList<String>) {
        adapter.addContactList(contactNames)
        listContacts.adapter = adapter
        listContacts.layoutManager = LinearLayoutManager(applicationContext)
        listContacts.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkEmpty()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkEmpty()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkEmpty()
            }

            @SuppressLint("SyntheticAccessor")
            private fun checkEmpty() {
                viewEmptyContact.visibility =
                    if (adapter.itemCount == 0) View.VISIBLE else View.GONE
            }
        })
    }

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, ContactListActivity::class.java)
            intent.putExtra(
                SettingsMenuActivity.SETTINGS_SCREEN_NAME,
                AppScreen.MINI_APP_SETTINGS_ACTIVITY
            )
            activity.startActivity(intent)
        }
    }
}
