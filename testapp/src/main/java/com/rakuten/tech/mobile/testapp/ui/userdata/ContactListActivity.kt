package com.rakuten.tech.mobile.testapp.ui.userdata

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
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ContactsActivityBinding
import com.rakuten.tech.mobile.testapp.helper.getAdapterDataObserver
import com.rakuten.tech.mobile.testapp.helper.isEmailValid
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import kotlin.collections.ArrayList

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

    private val fakeFirstNames = arrayOf("Yvonne", "Jamie", "Leticia", "Priscilla", "Sidney", "Nancy", "Edmund", "Bill", "Megan")
    private val fakeLastNames = arrayOf("Andrews", "Casey", "Gross", "Lane", "Thomas", "Patrick", "Strickland", "Nicolas", "Freeman")

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
        settings.contacts = adapter.provideContactEntries()
        finish()
    }

    private fun onAddAction() {
        showDialog(isUpdate = false)
    }

    private fun showDialog(isUpdate: Boolean, position: Int? = null){
        val contactView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        val edtContactId = contactView.findViewById<AppCompatEditText>(R.id.edtContactId)
        val edtContactName = contactView.findViewById<AppCompatEditText>(R.id.edtContactName)
        val edtContactEmail = contactView.findViewById<AppCompatEditText>(R.id.edtContactEmail)

        ContactInputDialog.Builder().build(this).apply {
            val randomContact = createRandomContact()
            edtContactId.setText(randomContact.id)
            edtContactName.setText(randomContact.name)
            edtContactEmail.setText(randomContact.email)

            setView(contactView)

            if (isUpdate) {
                setPositiveButton(getString(R.string.action_update))
                setDialogTitle("Contact Update")
                position?.let {
                    val existingContact = adapter.provideContactEntries()[it]
                    edtContactId.setText(existingContact.id)
                    edtContactName.setText(existingContact.name)
                    edtContactEmail.setText(existingContact.email)
                }
            } else {
                setPositiveButton(getString(R.string.action_add))
                setDialogTitle("Contact Input")
            }

            setPositiveListener {
                val id: String = edtContactId.text.toString().trim()
                val name: String = edtContactName.text.toString().trim()
                val email: String = edtContactEmail.text.toString().trim()

                if (isVerifiedContact(id, name, email)) {
                    val contact = Contact(id = id, name = name, email = email)
                    if (isUpdate) position?.let { adapter.updateContact(it, contact) }
                    else adapter.addContact(adapter.itemCount, contact)

                    this.dialog?.cancel()
                }
            }
        }.show()
    }

    private fun isVerifiedContact(id: String, name: String, email: String): Boolean {
        var isVerified = true

        if (id.isEmpty()) {
            isVerified = false
            showContactInputWarning(getString(R.string.userdata_error_empty_contact_id))
        } else if (name.isEmpty() && email.isNotEmpty()) {
            isVerified = false
            showContactInputWarning(getString(R.string.userdata_error_empty_contact_name))
        } else if (email.isEmpty() && name.isNotEmpty()) {
            isVerified = false
            showContactInputWarning(getString(R.string.userdata_error_empty_contact_email))
        } else if (name.isEmpty() && email.isEmpty()) {
            isVerified = false
            showContactInputWarning(getString(R.string.userdata_error_empty_contact_name_email))
        }

        if (email.isNotEmpty() && !email.isEmailValid()) {
            isVerified = false
            showContactInputWarning(getString(R.string.userdata_error_invalid_contact_email))
        }

        return isVerified
    }

    private fun showContactInputWarning(message: String) {
        Toast.makeText(this@ContactListActivity, message, Toast.LENGTH_LONG)
                .apply { setGravity(Gravity.TOP, 0, 100) }
                .show()
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
        val email = firstName.toLowerCase(Locale.ROOT) + "." + lastName.toLowerCase(Locale.ROOT) + "@example.com"
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
        showDialog(isUpdate = true, position = position)
    }
}
