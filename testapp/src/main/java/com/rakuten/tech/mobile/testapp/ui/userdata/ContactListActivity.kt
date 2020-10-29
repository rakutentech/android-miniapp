package com.rakuten.tech.mobile.testapp.ui.userdata

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
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
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ContactsActivityBinding
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
                    adapter.addContact(adapter.itemCount, name.trimEnd())
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

    override fun onResume() {
        super.onResume()
        if (isFirstLaunch) isFirstLaunch = false
    }

    private fun renderContactList() {
        if (!isFirstLaunch && settings.isContactsSaved) {
            if (settings.contactNames.isEmpty()) {
                renderAdapter(arrayListOf())
                checkEmpty()
            } else renderAdapter(settings.contactNames)
        } else {
            val randomList = createRandomUUIDList()
            renderAdapter(randomList)
            settings.contactNames = randomList
        }
    }

    private fun createRandomUUIDList(): ArrayList<String> {
        val randomUUIDs = ArrayList<String>()
        for (i in 1..10)
            randomUUIDs.add(UUID.randomUUID().toString().trimEnd())

        return randomUUIDs
    }

    private fun renderAdapter(contactNames: ArrayList<String>) {
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
                checkEmpty()
            }

            @SuppressLint("SyntheticAccessor")
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkEmpty()
            }

            @SuppressLint("SyntheticAccessor")
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkEmpty()
            }
        })
    }

    private fun checkEmpty() {
        binding.viewEmptyContact.visibility =
            if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    companion object {
        private const val IS_FIRST_TIME = "is_first_time"
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, ContactListActivity::class.java))
        }
    }
}
