package com.rakuten.tech.mobile.testapp.ui.chat

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.js.MessageToContact
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.DialogContactMessageContentBinding
import com.rakuten.tech.mobile.testapp.helper.load
import com.rakuten.tech.mobile.testapp.helper.showAlertDialog
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class ContactSelectionWindow(private val activity: Activity) :
    ContactSelectionAdapter.ContactSelectionListener {

    lateinit var contactSelectionAlertDialog: AlertDialog

    private lateinit var contactSelectionAdapter: ContactSelectionAdapter
    private lateinit var contactSelectionLayout: View

    private val hasContact =
        AppSettings.instance.isContactsSaved && !AppSettings.instance.contacts.isNullOrEmpty()

    private lateinit var message: MessageToContact
    private lateinit var onSuccessSingleContact: (contactId: String?) -> Unit
    private lateinit var onErrorSingleContact: (message: String) -> Unit

    fun openSingleContactSelection(
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (!hasContact) {
            showAlertDialog(
                activity,
                "Warning",
                "There is no contact found saved in HostApp."
            )
            return
        }

        this.message = message
        this.onSuccessSingleContact = onSuccess
        this.onErrorSingleContact = onError

        // initialize contact selection view
        initDefaultWindow()
        prepareDataForAdapter()

        // preview dialog
        contactSelectionAlertDialog.show()
    }

    private fun initDefaultWindow() {
        val layoutInflater = LayoutInflater.from(activity)
        contactSelectionLayout = layoutInflater.inflate(R.layout.window_contact_selection, null)
        val recyclerView =
            contactSelectionLayout.findViewById<RecyclerView>(R.id.listContactSelection)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )

        contactSelectionAdapter = ContactSelectionAdapter(this)
        recyclerView.adapter = contactSelectionAdapter

        contactSelectionAlertDialog =
            AlertDialog.Builder(activity, R.style.AppTheme_DefaultWindow).create()
        contactSelectionAlertDialog.setView(contactSelectionLayout)
        contactSelectionLayout.findViewById<ImageView>(R.id.contactCloseWindow).setOnClickListener {
            contactSelectionAlertDialog.dismiss()
        }
    }

    private fun prepareDataForAdapter() {
        if (hasContact) contactSelectionAdapter.addContactList(AppSettings.instance.contacts)
    }

    override fun onContactSelect(contact: Contact) {
        when {
            message.isEmpty -> onErrorSingleContact("The message sent was empty.")
            contact.id.isEmpty() -> {
                onErrorSingleContact("There is no contact found in HostApp.")
            }
            else -> {
                onSuccessSingleContact(contact.id)
                // Note: Doesn't need to actually send a message because we don't have an interface for this in the demo app.
                showMessageDialog(contact.id)
            }
        }

        contactSelectionAlertDialog.dismiss()
    }

    private fun showMessageDialog(contactId: String) {
        // set message attributes to views
        val layoutInflater = LayoutInflater.from(activity)
        val mainContent = DialogContactMessageContentBinding.inflate(layoutInflater, null, false)

        mainContent.messageText.text = this.message.text
        mainContent.messageTitle.text = this.message.miniAppTitle
        mainContent.messageCaption.text = this.message.caption
        mainContent.messageAction.text = this.message.action
        mainContent.messageGeneric.text = "The message has been sent to contact id: ${contactId}"

        // set dialog
        val alertDialog = android.app.AlertDialog.Builder(activity, R.style.AppTheme_DefaultWindow)
        alertDialog.setView(mainContent.root)
        alertDialog.setNegativeButton("Close") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.create().show()
    }
}
