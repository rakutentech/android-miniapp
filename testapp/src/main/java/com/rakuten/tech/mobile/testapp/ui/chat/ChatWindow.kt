package com.rakuten.tech.mobile.testapp.ui.chat

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.text.method.ScrollingMovementMethod
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
    private lateinit var contactSelectionAlertDialog: AlertDialog
    private lateinit var contactSelectionAdapter: ContactSelectionAdapter

    private val storedContacts = AppSettings.instance.contacts
    private val hasContact = AppSettings.instance.isContactsSaved && !storedContacts.isNullOrEmpty()

    private lateinit var message: MessageToContact
    private var specificContactId: String? = null
    private lateinit var onSuccessSingleContact: (contactId: String?) -> Unit
    private lateinit var onSuccessMultipleContacts: (contactIds: List<String>?) -> Unit
    private lateinit var onSuccessSpecificContactId: (contactId: String?) -> Unit
    private lateinit var onErrorContact: (message: String) -> Unit

    fun openSingleContactSelection(
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (hasContact) {
            this.message = message
            this.onSuccessSingleContact = onSuccess
            this.onErrorContact = onError
            launchScreen(ContactSelectionMode.SINGLE)
        } else warnNoContactSaved()
    }

    fun openMultipleContactSelections(
        message: MessageToContact,
        onSuccess: (contactIds: List<String>?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (hasContact) {
            this.message = message
            this.onSuccessMultipleContacts = onSuccess
            this.onErrorContact = onError
            launchScreen(ContactSelectionMode.MULTIPLE)
        } else warnNoContactSaved()
    }

    fun openSpecificContactIdSelection(
        contactId: String?,
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (hasContact) {
            this.specificContactId = contactId
            if (getSavedSpecificContact() != null) {
                this.message = message
                this.onSuccessSpecificContactId = onSuccess
                this.onErrorContact = onError
                launchScreen(ContactSelectionMode.OTHER)
            } else showInstruction("Provided contact id hasn't been saved in HostApp yet.")
        } else warnNoContactSaved()
    }

    private fun launchScreen(mode: ContactSelectionMode) = GlobalScope.launch(Dispatchers.Main) {
        prepareWindow(mode)
    }

    private fun prepareWindow(mode: ContactSelectionMode) {
        val layoutInflater = LayoutInflater.from(activity)
        val rootView = WindowChatBinding.inflate(layoutInflater, null, false)
        contactSelectionAlertDialog =
            AlertDialog.Builder(activity, R.style.AppTheme_DefaultWindow).create()
        contactSelectionAlertDialog.setView(rootView.root)

        message.apply {
            rootView.messageImage.load(activity, image, R.drawable.r_logo)
            rootView.messageText.text = text
            rootView.messageCaption.text = caption
            rootView.messageCaption.setOnClickListener {
                openActionUrl(action)
            }
            if (caption.isEmpty()) rootView.messageCaption.visibility = View.GONE
        }
        rootView.messageText.movementMethod = ScrollingMovementMethod()

        rootView.listContactSelection.layoutManager = LinearLayoutManager(activity)
        if (mode != ContactSelectionMode.OTHER)
            rootView.listContactSelection.addItemDecoration(
                DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
            )
        contactSelectionAdapter = ContactSelectionAdapter()
        rootView.listContactSelection.adapter = contactSelectionAdapter
        prepareDataForAdapter(mode)

        rootView.chatActionSend.setOnClickListener {
            when (mode) {
                ContactSelectionMode.SINGLE -> onSingleMessageSend()
                ContactSelectionMode.MULTIPLE -> onMultipleMessageSend()
                ContactSelectionMode.OTHER -> onSendToSpecificContactId()
            }
        }
        rootView.chatActionCancel.setOnClickListener {
            onCancel(mode)
            contactSelectionAlertDialog.dismiss()
        }

        contactSelectionAlertDialog.setOnCancelListener {
            onCancel(mode)
        }
        contactSelectionAlertDialog.show()
    }

    private fun onCancel(mode: ContactSelectionMode) {
        when (mode) {
            ContactSelectionMode.SINGLE -> onSuccessSingleContact(null)
            ContactSelectionMode.MULTIPLE -> onSuccessMultipleContacts(null)
            ContactSelectionMode.OTHER -> onSuccessSpecificContactId.invoke(null)
        }
    }

    private fun prepareDataForAdapter(mode: ContactSelectionMode) {
        if (hasContact) {
            if (mode != ContactSelectionMode.OTHER)
                contactSelectionAdapter.addContactList(mode, storedContacts)
            else {
                val storedContact = getSavedSpecificContact()
                if (storedContact != null)
                    contactSelectionAdapter.addContactList(mode, arrayListOf(storedContact))
                else showInstruction("Provided contact id hasn't been saved in HostApp yet.")
            }
        }
    }

    private fun onSingleMessageSend() {
        if (contactSelectionAdapter.singleContact == null) {
            showInstruction("Please select a single contact.")
            return
        }
        when {
            message.isEmpty -> onErrorContact("The message sent was empty.")
            contactSelectionAdapter.singleContact?.contact?.id?.isEmpty()!! -> {
                onErrorContact("Provided contact ID is invalid.")
            }
            else -> {
                val contactId = contactSelectionAdapter.singleContact?.contact?.id
                onSuccessSingleContact(contactId)
                onMessageSent()
            }
        }
        contactSelectionAlertDialog.dismiss()
    }

    private fun onMultipleMessageSend() {
        if (contactSelectionAdapter.multipleContacts.isEmpty()) {
            showInstruction("Please select at-least one contact.")
            return
        }
        when {
            message.isEmpty -> onErrorContact("The message sent was empty.")
            else -> {
                val contactIds = arrayListOf<String>()
                contactSelectionAdapter.multipleContacts.forEach {
                    contactIds.add(it.contact.id)
                }

                onSuccessMultipleContacts(contactIds)
                onMessageSent()
            }
        }
        contactSelectionAlertDialog.dismiss()
    }

    private fun onSendToSpecificContactId() {
        when {
            message.isEmpty -> onErrorContact("The message sent was empty.")
            specificContactId.isNullOrEmpty() -> {
                onErrorContact("Provided contact ID is invalid.")
            }
            else -> {
                onSuccessSpecificContactId.invoke(specificContactId)
                onMessageSent()
            }
        }
        contactSelectionAlertDialog.dismiss()
    }

    private fun getSavedSpecificContact(): Contact? {
        return specificContactId?.let { contactId ->
            storedContacts.find { it.id == contactId }
        }
    }

    private fun openActionUrl(action: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(action)
            activity.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            showInstruction("The action data is not a valid url.")
        }
    }

    private fun onMessageSent() {
        // Note: Doesn't need to actually send a message because we don't have an interface for this in the demo app.
        showInstruction("The message has been sent successfully!")
    }

    private fun warnNoContactSaved() {
        showAlertDialog(activity, "Warning", "There is no contact found saved in HostApp!")
    }

    private fun showInstruction(instruction: String) {
        Toast.makeText(activity, instruction, Toast.LENGTH_LONG).show()
    }
}
