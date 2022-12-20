package com.rakuten.tech.mobile.testapp.ui.userdata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ActivityContactAddBinding
import com.rakuten.tech.mobile.testapp.helper.hideSoftKeyboard
import com.rakuten.tech.mobile.testapp.helper.isEmailValid
import com.rakuten.tech.mobile.testapp.ui.base.BaseActivity
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import java.security.SecureRandom
import java.util.*

class ContactAddActivity : BaseActivity() {
    override val pageName: String = this::class.simpleName ?: ""
    override val siteSection: String = this::class.simpleName ?: ""

    companion object {
        const val isUpdateTag = "isUpdate"
        const val contactTag = "contactTag"
        const val positionTag = "position"

        fun start(
            activity: Activity,
            requestCode: Int,
            contact: Contact? = null,
            position: Int = 0
        ) {
            activity.startActivityForResult(
                Intent(activity, ContactAddActivity::class.java).apply {
                    contact?.let {
                        putExtra(contactTag, Gson().toJson(it))
                        putExtra(isUpdateTag, true)
                        putExtra(positionTag, position)
                    }
                },
                requestCode
            )
        }
    }

    private lateinit var settings: AppSettings
    private lateinit var binding: ActivityContactAddBinding
    private var contact: Contact? = null
    private var isUpdate = false
    private var position = 0

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
        settings = AppSettings.instance
        showBackIcon()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact_add)
        val contactJsonStr = intent.getStringExtra(contactTag)
        isUpdate = intent.getBooleanExtra(isUpdateTag, false)
        position = intent.getIntExtra(positionTag, 0)
        title = getString(
            if (isUpdate) R.string.action_contact_update
            else R.string.action_contact_add
        )
        contactJsonStr?.let {
            contact = Gson().fromJson(it, Contact::class.java)
        }
        renderScreen()

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
        if (!isVerifiedContact()) {
            return
        }
        contact = Contact(
            id = binding.edtContactId.text.toString(),
            email = binding.edtContactEmail.text.toString(),
            name = binding.edtContactName.text.toString()
        )
        val returnIntent = Intent().apply {
            putExtra(contactTag, Gson().toJson(contact))
            putExtra(isUpdateTag, isUpdate)
            putExtra(positionTag, position)
        }
        setResult(Activity.RESULT_OK, returnIntent)
        hideSoftKeyboard(binding.root)
        finish()
    }

    private fun renderScreen() {
        (contact ?: createRandomContact()).apply {
            binding.edtContactId.setText(id)
            binding.edtContactName.setText(name)
            binding.edtContactEmail.setText(email)
        }
    }

    private fun isVerifiedContact(): Boolean {
        var isVerified = true

        val id = binding.edtContactId.text.toString()
        val name = binding.edtContactName.text.toString()
        val email = binding.edtContactEmail.text.toString()

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
        Toast.makeText(this@ContactAddActivity, message, Toast.LENGTH_LONG)
            .apply { setGravity(Gravity.TOP, 0, 100) }
            .show()
    }

    private fun createRandomContact(): Contact {
        val firstName = fakeFirstNames[(SecureRandom().nextDouble() * fakeFirstNames.size).toInt()]
        val lastName = fakeLastNames[(SecureRandom().nextDouble() * fakeLastNames.size).toInt()]
        val email =
            firstName.toLowerCase(Locale.ROOT) + "." + lastName.toLowerCase(Locale.ROOT) + "@example.com"
        return Contact(UUID.randomUUID().toString().trimEnd(), "$firstName $lastName", email)
    }

}
