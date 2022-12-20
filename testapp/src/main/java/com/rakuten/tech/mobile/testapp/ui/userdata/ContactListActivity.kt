package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ContactsActivityBinding
import com.rakuten.tech.mobile.testapp.helper.getAdapterDataObserver
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import java.security.SecureRandom
import java.util.*

private const val CONTACT_LIST_REQUEST_CODE = 1

class ContactListActivity : BaseActivity(), ContactListener {
    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""
    private lateinit var settings: AppSettings
    private lateinit var binding: ContactsActivityBinding
    private val adapter = ContactListAdapter(this)
    private var contactListPrefs: SharedPreferences? = null
    private var isFirstLaunch: Boolean
        get() = contactListPrefs?.getBoolean(IS_FIRST_TIME, true) ?: true
        set(value) {
            contactListPrefs?.edit()?.putBoolean(IS_FIRST_TIME, value)?.apply()
        }

    private val fakeFirstNames = arrayOf(
        "Yvonne",
        "Jamie",
        "Leticia",
        "Priscilla",
        "Sidney",
        "Nancy",
        "Edmund",
        "Bill",
        "Megan"
    )
    private val fakeLastNames = arrayOf(
        "Andrews",
        "Casey",
        "Gross",
        "Lane",
        "Thomas",
        "Patrick",
        "Strickland",
        "Nicolas",
        "Freeman"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactListPrefs = getSharedPreferences(
            "com.rakuten.tech.mobile.miniapp.sample.contacts", Context.MODE_PRIVATE
        )
        settings = AppSettings.instance
        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.contacts_activity)
        renderRandomContactList()
        binding.fabAddContact.setOnClickListener { onAddAction() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onAddAction() {
        ContactAddActivity.start(
            this,
            requestCode = CONTACT_LIST_REQUEST_CODE
        )
    }

    override fun onResume() {
        super.onResume()
        if (isFirstLaunch) isFirstLaunch = false
    }

    private fun renderRandomContactList() {
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
        for (i in 1..10) {
            this.add(createRandomContact())
        }
    }

    private fun createRandomContact(): Contact {
        val firstName = fakeFirstNames[(SecureRandom().nextDouble() * fakeFirstNames.size).toInt()]
        val lastName = fakeLastNames[(SecureRandom().nextDouble() * fakeLastNames.size).toInt()]
        val email =
            firstName.toLowerCase(Locale.ROOT) + "." + lastName.toLowerCase(Locale.ROOT) + "@example.com"
        return Contact(UUID.randomUUID().toString().trimEnd(), "$firstName $lastName", email)
    }

    private fun renderAdapter(contactNames: ArrayList<Contact>) {
        adapter.addContactList(contactNames)
        binding.listContacts.adapter = adapter
        binding.listContacts.layoutManager = LinearLayoutManager(applicationContext)
        binding.listContacts.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        adapter.registerAdapterDataObserver(getAdapterDataObserver { observeUIState() })
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

    override fun onContactItemClick(position: Int) {
        val existingContact = adapter.provideContactEntries()[position]

        ContactAddActivity.start(
            this,
            CONTACT_LIST_REQUEST_CODE,
            existingContact,
            position
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == CONTACT_LIST_REQUEST_CODE) {
            data?.let { intent ->
                val contactJsonStr = intent.getStringExtra(ContactAddActivity.contactTag)
                val isUpdate = intent.getBooleanExtra(ContactAddActivity.isUpdateTag, false)
                val position = intent.getIntExtra(ContactAddActivity.positionTag, 0)
                contactJsonStr?.let {
                    val contact = Gson().fromJson(it, Contact::class.java)
                    if (isUpdate) {
                        adapter.updateContact(position, contact)
                    } else {
                        adapter.addContact(adapter.itemCount, contact)
                    }
                    settings.contacts = adapter.provideContactEntries()
                }
            }
        }
    }
}
