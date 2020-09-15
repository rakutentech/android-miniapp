package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
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

    private val adapter = ContactsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contacts_activity)
        initializeActionBar()
        createContactList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.contact_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.contact_menu_add -> {
                onAddAction()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onAddAction() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Please enter the custom ID you would like to add in Contacts")
        val contactView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        val editNewContact: AppCompatEditText = contactView.findViewById(R.id.editNewContact)
        builder.setView(contactView)
        builder.setPositiveButton("Add") { dialog, _ ->
            if (editNewContact.text.toString().isNotEmpty()) {
                val name = editNewContact.text.toString()
                // add name in the last position
                adapter.addContact(adapter.itemCount, clearWhiteSpaces(name))
            } else {
                val toast =
                    Toast.makeText(
                        this@ContactsActivity,
                        getString(R.string.userdata_error_invalid_contact),
                        Toast.LENGTH_LONG
                    )
                toast.setGravity(Gravity.BOTTOM, 0, 100)
                toast.show()
            }
            dialog.cancel()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun initializeActionBar() {
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)
        showBackIcon()
    }

    private fun createContactList() {
        // prepare random contact list
        val contactNames = ArrayList<String>()
        for (i in 1..10)
            contactNames.add(clearWhiteSpaces((UUID.randomUUID().toString())))

        // add contacts in adapter
        adapter.addContactList(contactNames)
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
