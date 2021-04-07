package com.rakuten.tech.mobile.testapp.ui.chat

import android.app.Activity
import android.view.LayoutInflater
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

class ChatWindow(private val activity: Activity) :
    ContactSelectionAdapter.ContactSelectionListener {

    lateinit var contactSelectionAlertDialog: AlertDialog
    private val layoutInflater = LayoutInflater.from(activity)
    private lateinit var contactSelectionAdapter: ContactSelectionAdapter

    private val hasContact =
        AppSettings.instance.isContactsSaved && !AppSettings.instance.contacts.isNullOrEmpty()

    private lateinit var miniAppTitle: String
    private lateinit var message: MessageToContact
    private lateinit var onSuccessSingleContact: (contactId: String?) -> Unit
    private lateinit var onErrorSingleContact: (message: String) -> Unit

    fun openSingleContactSelection(
        miniAppTitle: String,
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (!hasContact) {
            showAlertDialog(
                activity,
                "Warning",
                "There is no contact found saved in HostApp!"
            )
            return
        }

        this.miniAppTitle = miniAppTitle
        this.message = message
        this.onSuccessSingleContact = onSuccess
        this.onErrorSingleContact = onError

        initDefaultWindow()
        prepareDataForAdapter()

        // preview dialog
        contactSelectionAlertDialog.show()
    }

    private fun initDefaultWindow() {
        val rootView = WindowChatBinding.inflate(layoutInflater, null, false)

        // set message content
        GlobalScope.launch(Dispatchers.Main) {
            message.apply {
                rootView.messageImage.load(activity, image)
                rootView.messageText.text = text
                rootView.miniAppTitle.text = miniAppTitle
                rootView.messageCaption.text = caption // todo
                rootView.messageAction.text = action // todo
            }
        }

        // set list of contacts to select
        rootView.listContactSelection.layoutManager = LinearLayoutManager(activity)
        rootView.listContactSelection.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )

        contactSelectionAdapter = ContactSelectionAdapter(this)
        rootView.listContactSelection.adapter = contactSelectionAdapter

        contactSelectionAlertDialog =
            AlertDialog.Builder(activity, R.style.AppTheme_DefaultWindow).create()
        contactSelectionAlertDialog.setView(rootView.root)
        rootView.chatActionCancel.setOnClickListener {
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
                Toast.makeText(
                    activity, "The message has been sent to contact id: ${contact.id}", Toast.LENGTH_LONG
                ).show()
            }
        }

        contactSelectionAlertDialog.dismiss()
    }
}
