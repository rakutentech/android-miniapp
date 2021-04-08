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
    private lateinit var onSuccessSingleContact: (contactId: String?) -> Unit
    private lateinit var onSuccessMultipleContacts: (contactIds: List<String>) -> Unit
    private lateinit var onSuccessSpecificContactId: () -> Unit
    private lateinit var onErrorSingleContact: (message: String) -> Unit

    fun openSingleContactSelection(
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        checkContactAvailability()
        this.message = message
        this.onSuccessSingleContact = onSuccess
        this.onErrorSingleContact = onError

        initDefaultWindow("single")
        prepareDataForAdapter("single")

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
        this.onErrorSingleContact = onError

        initDefaultWindow("multiple")
        prepareDataForAdapter("multiple")

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
        this.message = message
        this.onSuccessSpecificContactId = onSuccess
        this.onErrorSingleContact = onError

        initDefaultWindow("")
        prepareDataForAdapter("")

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

    private fun initDefaultWindow(mode: String) {
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
        rootView.listContactSelection.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )

        contactSelectionAdapter = ContactSelectionAdapter()
        rootView.listContactSelection.adapter = contactSelectionAdapter

        contactSelectionAlertDialog =
            AlertDialog.Builder(activity, R.style.AppTheme_DefaultWindow).create()
        contactSelectionAlertDialog.setView(rootView.root)

        when (mode) {
            "single" -> {
                rootView.chatActionSend.setOnClickListener {
                    onSingleMessageSend()
                }
            }
            "multiple" -> {
                rootView.chatActionSend.setOnClickListener {
                    onMultipleMessageSend()
                }
            }
            else -> {
                rootView.chatActionSend.setOnClickListener {
                    onSuccessSpecificContactId
                }
            }
        }
        rootView.chatActionCancel.setOnClickListener {
            contactSelectionAlertDialog.dismiss()
        }
    }

    private fun prepareDataForAdapter(mode: String) {
        if (hasContact) contactSelectionAdapter.addContactList(mode, AppSettings.instance.contacts)
    }

    private fun onSingleMessageSend() {
        when {
            message.isEmpty -> onErrorSingleContact("The message sent was empty.")
            contactSelectionAdapter.singleContact?.contact?.id?.isEmpty()!! -> {
                onErrorSingleContact("There is no contact found in HostApp!")
            }
            else -> {
                val contactId = contactSelectionAdapter.singleContact?.contact?.id
                onSuccessSingleContact(contactId)
                // Note: Doesn't need to actually send a message because we don't have an interface for this in the demo app.
                Toast.makeText(
                    activity, "The message has been sent to contact id: ${contactId}", Toast.LENGTH_LONG
                ).show()
                contactSelectionAlertDialog.dismiss()
            }
        }
    }

    private fun onMultipleMessageSend() {
        when {
            message.isEmpty -> onErrorSingleContact("The message sent was empty.")
            contactSelectionAdapter.multipleContacts.isEmpty() -> {
                onErrorSingleContact("There is no contact found in HostApp!")
            }
            else -> {
                val contactIds = arrayListOf<String>()
                contactSelectionAdapter.multipleContacts.forEach {
                    contactIds.add(it.contact.id)
                }

                onSuccessMultipleContacts(contactIds)
                // Note: Doesn't need to actually send a message because we don't have an interface for this in the demo app.
                Toast.makeText(
                    activity, "The message has been sent.", Toast.LENGTH_LONG
                ).show()
                contactSelectionAlertDialog.dismiss()
            }
        }
    }

    private fun onSendToSpecificContactId() {

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
}
