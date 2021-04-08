package com.rakuten.tech.mobile.testapp.ui.chat

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rakuten.tech.mobile.miniapp.js.MessageToContact
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.WindowChatBinding
import com.rakuten.tech.mobile.testapp.helper.load
import com.rakuten.tech.mobile.testapp.helper.showAlertDialog
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChatWindow(private val activity: Activity) {

    lateinit var contactSelectionAlertDialog: AlertDialog
    private val layoutInflater = LayoutInflater.from(activity)
    private lateinit var contactSelectionAdapter: ContactSelectionAdapter

    private val hasContact =
        AppSettings.instance.isContactsSaved && !AppSettings.instance.contacts.isNullOrEmpty()

    private lateinit var message: MessageToContact
    private var specificContactId: String? = null
    private lateinit var onSuccessSingleContact: (contactId: String?) -> Unit
    private lateinit var onSuccessMultipleContacts: (contactIds: List<String>) -> Unit
    private lateinit var onSuccessSpecificContactId: () -> Unit
    private lateinit var onErrorContact: (message: String) -> Unit

    fun openSingleContactSelection(
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        checkContactAvailability()
        this.message = message
        this.onSuccessSingleContact = onSuccess
        this.onErrorContact = onError

        prepareWindow(ContactSelectionMode.SINGLE)
        prepareDataForAdapter(ContactSelectionMode.SINGLE)

        // preview dialog
        contactSelectionAlertDialog.show()
    }

    fun openMultipleContactSelections(
        message: MessageToContact,
        onSuccess: (contactIds: List<String>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        checkContactAvailability()
        this.message = message
        this.onSuccessMultipleContacts = onSuccess
        this.onErrorContact = onError

        prepareWindow(ContactSelectionMode.MULTIPLE)
        prepareDataForAdapter(ContactSelectionMode.MULTIPLE)

        // preview dialog
        contactSelectionAlertDialog.show()
    }

    fun openSpecificContactIdSelection(
        contactId: String?,
        message: MessageToContact,
        onSuccess: () -> Unit,
        onError: (message: String) -> Unit
    ) {
        checkContactAvailability()
        this.specificContactId = contactId
        this.message = message
        this.onSuccessSpecificContactId = onSuccess
        this.onErrorContact = onError

        prepareWindow(ContactSelectionMode.OTHER)
        prepareDataForAdapter(ContactSelectionMode.OTHER)

        // preview dialog
        contactSelectionAlertDialog.show()
    }

    private fun checkContactAvailability() {
        if (!hasContact) {
            showAlertDialog(
                activity, "Warning", "There is no contact found saved in HostApp!"
            )
            return
        }
    }

    private fun prepareWindow(mode: ContactSelectionMode) {
        val rootView = WindowChatBinding.inflate(layoutInflater, null, false)

        // set message content
        GlobalScope.launch(Dispatchers.Main) {
            message.apply {
                rootView.messageImage.load(activity, image)
                rootView.messageText.text = text
                rootView.messageCaption.text = caption
                rootView.messageCaption.setOnClickListener {
                    openActionUrl(action)
                }
                if (caption.isEmpty()) rootView.messageCaption.visibility = View.GONE
            }
        }

        // set list of contacts to select
        rootView.listContactSelection.layoutManager = LinearLayoutManager(activity)

        if (mode != ContactSelectionMode.OTHER)
            rootView.listContactSelection.addItemDecoration(
                DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
            )

        contactSelectionAdapter = ContactSelectionAdapter()
        rootView.listContactSelection.adapter = contactSelectionAdapter

        contactSelectionAlertDialog =
            AlertDialog.Builder(activity, R.style.AppTheme_DefaultWindow).create()
        contactSelectionAlertDialog.setView(rootView.root)

        rootView.chatActionSend.setOnClickListener {
            when (mode) {
                ContactSelectionMode.SINGLE -> onSingleMessageSend()
                ContactSelectionMode.MULTIPLE -> onMultipleMessageSend()
                ContactSelectionMode.OTHER -> onSendToSpecificContactId()
            }
        }
        rootView.chatActionCancel.setOnClickListener {
            contactSelectionAlertDialog.dismiss()
        }
    }

    private fun prepareDataForAdapter(mode: ContactSelectionMode) {
        val storedContacts = AppSettings.instance.contacts
        if (hasContact) {
            if (mode != ContactSelectionMode.OTHER)
                contactSelectionAdapter.addContactList(mode, storedContacts)
            else {
                specificContactId?.let { contactId ->
                    val contact: Contact? = storedContacts.find {
                        it.id == contactId
                    }
                    contactSelectionAdapter.addContactList(
                        mode,
                        arrayListOf(contact ?: Contact(contactId, null, null))
                    )
                }
            }
        }
    }

    private fun onSingleMessageSend() {
        when {
            message.isEmpty -> onErrorContact("The message sent was empty.")
            contactSelectionAdapter.singleContact?.contact?.id?.isEmpty()!! -> {
                onErrorContact("There is no contact found in HostApp!")
            }
            else -> {
                val contactId = contactSelectionAdapter.singleContact?.contact?.id
                onSuccessSingleContact(contactId)
                onMessageSent()
            }
        }
    }

    private fun onMultipleMessageSend() {
        when {
            message.isEmpty -> onErrorContact("The message sent was empty.")
            contactSelectionAdapter.multipleContacts.isEmpty() -> {
                onErrorContact("There is no contact found in HostApp!")
            }
            else -> {
                val contactIds = arrayListOf<String>()
                contactSelectionAdapter.multipleContacts.forEach {
                    contactIds.add(it.contact.id)
                }

                onSuccessMultipleContacts(contactIds)
                onMessageSent()
            }
        }
    }

    private fun onSendToSpecificContactId() {
        when {
            message.isEmpty -> onErrorContact("The message sent was empty.")
            specificContactId.isNullOrEmpty() -> {
                onErrorContact("There is no specific contact id found from MiniApp!")
            }
            else -> {
                onSuccessSpecificContactId.invoke()
                onMessageSent()
            }
        }
    }

    private fun openActionUrl(action: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(action)
            activity.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                activity, "The action data is not a valid url.", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun onMessageSent() {
        // Note: Doesn't need to actually send a message because we don't have an interface for this in the demo app.
        Toast.makeText(
            activity, "The message has been sent successfully!", Toast.LENGTH_LONG
        ).show()
        contactSelectionAlertDialog.dismiss()
    }
}