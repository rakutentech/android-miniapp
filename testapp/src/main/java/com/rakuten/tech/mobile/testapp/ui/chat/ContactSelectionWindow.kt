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
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.helper.showAlertDialog
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings

class ContactSelectionWindow(private val activity: Activity) :
    ContactSelectionAdapter.ContactSelectionListener {

    lateinit var contactSelectionAlertDialog: AlertDialog

    private lateinit var contactSelectionAdapter: ContactSelectionAdapter
    private lateinit var contactSelectionLayout: View

    private val hasContact = AppSettings.instance.isContactsSaved

    private lateinit var message: MessageToContact
    private lateinit var onSuccessSingleContact: (contactId: String?) -> Unit
    private lateinit var onErrorSingleContact: (message: String) -> Unit

    fun openSingleContactSelection(
        message: MessageToContact,
        onSuccess: (contactId: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (!hasContact) {
            showAlertDialog(activity, "There is no contact found saved in HostApp.")
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
        if (hasContact) contactSelectionAdapter.addContactList(AppSettings.instance.contactNames)
    }

    override fun onContactSelect(contactId: String) {
        when {
            message.isEmpty -> onErrorSingleContact("The message sent was empty.")
            contactId.isEmpty() -> {
                onErrorSingleContact("There is no contact found in HostApp.")
            }
            else -> {
                onSuccessSingleContact(contactId)
                // Note: Doesn't need to actually send a message because we don't have an interface for this in the demo app.
                showAlertDialog(
                    activity,
                    "The message: ${message.title} has been sent to contact id: $contactId"
                )
            }
        }

        contactSelectionAlertDialog.dismiss()
    }
}
