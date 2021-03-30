package com.rakuten.tech.mobile.testapp.ui.userdata

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
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ContactsActivityBinding
import com.rakuten.tech.mobile.testapp.helper.defaultContact
import com.rakuten.tech.mobile.testapp.helper.isEmailValid
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import java.util.UUID

class ContactListActivity : BaseActivity() {

    private lateinit var settings: AppSettings
    private lateinit var binding: ContactsActivityBinding
    private val adapter = ContactListAdapter()
    private var contactListPrefs: SharedPreferences? = null
    private var isFirstLaunch: Boolean
        get() = contactListPrefs?.getBoolean(IS_FIRST_TIME, true) ?: true
        set(value) {
            contactListPrefs?.edit()?.putBoolean(IS_FIRST_TIME, value)?.apply()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactListPrefs = getSharedPreferences(
            "com.rakuten.tech.mobile.miniapp.sample.contacts", Context.MODE_PRIVATE
        )
        settings = AppSettings.instance
        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.contacts_activity)
        renderContactList()
        binding.fabAddContact.setOnClickListener { onAddAction() }
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
        settings.contacts = adapter.provideContactEntries()
        finish()
    }

    private fun onAddAction() {
        val contactView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        val edtContactId = contactView.findViewById<AppCompatEditText>(R.id.edtContactId)
        val edtContactName = contactView.findViewById<AppCompatEditText>(R.id.edtContactName)
        val edtContactEmail = contactView.findViewById<AppCompatEditText>(R.id.edtContactEmail)

        ContactInputDialog.Builder().build(this).apply {
            setView(contactView)
            setPositiveListener(View.OnClickListener {
                var canSave = true
                val id: String = edtContactId.text.toString().trim()
                var name: String? = edtContactName.text.toString().trim()
                var email: String? = edtContactEmail.text.toString().trim()

                if (id.isNotEmpty()) {
                    if (name!!.isEmpty())
                        name = null

                    if (email!!.isEmpty())
                        email = null
                    else {
                        if (!email.isEmailValid()) {
                            canSave = false
                            Toast.makeText(this@ContactListActivity, getString(R.string.userdata_error_invalid_contact_email), Toast.LENGTH_LONG)
                                .apply { setGravity(Gravity.TOP, 0, 50) }
                                .show()
                        }
                    }
                } else {
                    canSave = false
                    Toast.makeText(this@ContactListActivity, getString(R.string.userdata_error_invalid_contact_id), Toast.LENGTH_LONG)
                        .apply { setGravity(Gravity.TOP, 0, 50) }
                        .show()
                }

                if (canSave) {
                    adapter.addContact(adapter.itemCount, Contact(id = id, name = name, email = email))
                    this.dialog?.cancel()
                }
            })
        }.show()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstLaunch) isFirstLaunch = false
    }

    private fun renderContactList() {
        if (!isFirstLaunch && settings.isContactsSaved) {
            if (settings.contacts.isEmpty()) {
                renderAdapter(arrayListOf())
            } else renderAdapter(settings.contacts)
        } else {
            val randomList = createRandomContactList()
            renderAdapter(randomList)
        }
    }

    private fun createRandomContactList(): ArrayList<Contact> = ArrayList<Contact>().apply {
        for (i in 1..10)
            this.add(defaultContact(UUID.randomUUID().toString().trimEnd()))
    }

    private fun renderAdapter(contactNames: ArrayList<Contact>) {
        adapter.addContactList(contactNames)
        binding.listContacts.adapter = adapter
        binding.listContacts.layoutManager = LinearLayoutManager(applicationContext)
        binding.listContacts.addItemDecoration(
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
                binding.viewEmptyContact.visibility = View.VISIBLE
                binding.statusNoContact.visibility = View.GONE
            }
            adapter.itemCount != 0 && !settings.isContactsSaved -> {
                binding.viewEmptyContact.visibility = View.GONE
                binding.statusNoContact.visibility = View.VISIBLE
            }
            else -> {
                binding.viewEmptyContact.visibility = View.GONE
                binding.statusNoContact.visibility = View.GONE
            }
        }
    }

    companion object {
        private const val IS_FIRST_TIME = "is_first_time"
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, ContactListActivity::class.java))
        }
    }
}
