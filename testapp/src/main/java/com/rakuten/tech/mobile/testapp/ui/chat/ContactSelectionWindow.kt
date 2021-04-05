package com.rakuten.tech.mobile.testapp.ui.chat

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.js.MessageToContact
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.DialogContactMessageContentBinding
import com.rakuten.tech.mobile.miniapp.testapp.databinding.WindowContactSelectionBinding
import com.rakuten.tech.mobile.testapp.helper.load
import com.rakuten.tech.mobile.testapp.helper.showAlertDialog
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ContactSelectionWindow(private val activity: Activity) :
    ContactSelectionAdapter.ContactSelectionListener {

    lateinit var contactSelectionAlertDialog: AlertDialog
    lateinit var messageSentAlertDialog: AlertDialog
    private val layoutInflater = LayoutInflater.from(activity)
    private lateinit var contactSelectionAdapter: ContactSelectionAdapter

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
        val contactView = WindowContactSelectionBinding.inflate(layoutInflater, null, false)
        contactView.listContactSelection.layoutManager = LinearLayoutManager(activity)
        contactView.listContactSelection.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )

        contactSelectionAdapter = ContactSelectionAdapter(this)
        contactView.listContactSelection.adapter = contactSelectionAdapter

        contactSelectionAlertDialog =
            AlertDialog.Builder(activity, R.style.AppTheme_DefaultWindow).create()
        contactSelectionAlertDialog.setView(contactView.root)
        contactView.contactCloseWindow.setOnClickListener {
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
        val messageView = DialogContactMessageContentBinding.inflate(layoutInflater, null, false)

        GlobalScope.launch(Dispatchers.Main) {
            message.apply {
                messageView.messageImage.load(activity, image)
                messageView.messageText.text = text
                messageView.messageTitle.text = miniAppTitle
                messageView.messageCaption.text = caption
                messageView.messageAction.text = action
            }
            messageView.messageGeneric.text = "The message has been sent to contact id: ${contactId}"

            // set dialog
            messageSentAlertDialog = AlertDialog.Builder(activity).create()
            messageSentAlertDialog.setView(messageView.root)
            messageView.dialogDismiss.setOnClickListener {
                messageSentAlertDialog.dismiss()
            }
            messageSentAlertDialog.show()
        }
    }
}
