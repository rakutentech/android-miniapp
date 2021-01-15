package com.rakuten.tech.mobile.testapp.ui.chat

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.testapp.helper.showAlertDialog
import com.rakuten.tech.mobile.testapp.ui.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ContactSelectionWindow(private val activity: Activity) : CoroutineScope,
    ContactSelectionAdapter.ContactSelectionListener {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    lateinit var contactSelectionAlertDialog: AlertDialog

    private lateinit var contactSelectionAdapter: ContactSelectionAdapter
    private lateinit var contactSelectionLayout: View

    private val hasContact = AppSettings.instance.isContactsSaved
    private var selectedContactId: String = ""

    fun getSingleContactId(): String {
        if (!hasContact) {
            showAlertDialog(activity, "There is no contact found in HostApp.")
            return ""
        }

        launch {
            // initialize contact selection view
            initDefaultWindow()
            prepareDataForAdapter()

            // preview dialog
            contactSelectionAlertDialog.show()
        }

        return selectedContactId
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
            AlertDialog.Builder(activity, R.style.AppTheme_ContactSelectionDialog).create()
        contactSelectionAlertDialog.setView(contactSelectionLayout)

        contactSelectionLayout.findViewById<ImageView>(R.id.contactCloseWindow).setOnClickListener {
            contactSelectionAlertDialog.dismiss()
        }
    }

    private fun prepareDataForAdapter() {
        if (hasContact) contactSelectionAdapter.addContactList(AppSettings.instance.contactNames)
    }

    override fun onContactSelect(contactId: String) {
        selectedContactId = contactId
    }
}
