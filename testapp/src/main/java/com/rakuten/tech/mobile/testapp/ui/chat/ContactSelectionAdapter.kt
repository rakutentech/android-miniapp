package com.rakuten.tech.mobile.testapp.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListContactBinding

internal class ContactSelectionAdapter :
    RecyclerView.Adapter<ContactSelectionAdapter.ViewHolder?>() {
    private var contactEntries = ArrayList<SelectableContact>()
    private var contactSelectionMode: ContactSelectionMode? = null
    var singleContact: SelectableContact? = null
    var multipleContacts: ArrayList<SelectableContact> = arrayListOf()
    private var selectedSingleView: AppCompatRadioButton? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemListContactBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = contactEntries[position]
        bindView("Id: ", entry.contact.id, holder.contactId)
        bindView("Name: ", entry.contact.name, holder.contactName)
        bindView("Email: ", entry.contact.email, holder.contactEmail)

        holder.contactRemoveButton.visibility = View.GONE
        if (contactSelectionMode == ContactSelectionMode.SINGLE) holder.contactSingleSelector.visibility =
            View.VISIBLE
        else if (contactSelectionMode == ContactSelectionMode.MULTIPLE) holder.contactMultipleSelector.visibility =
            View.VISIBLE

        holder.contactSingleSelector.isChecked = contactEntries[position].isSelected
        holder.contact.setOnClickListener {
            if (contactSelectionMode == ContactSelectionMode.SINGLE) {
                contactEntries.forEach {
                    it.isSelected = false
                }
                contactEntries[position].isSelected = true

                if (selectedSingleView != null && holder.contactSingleSelector != selectedSingleView)
                    selectedSingleView?.isChecked = false

                selectedSingleView = holder.contactSingleSelector
                selectedSingleView?.isChecked = true
                singleContact = contactEntries[position]
            } else if (contactSelectionMode == ContactSelectionMode.MULTIPLE) {
                contactEntries[position].isSelected = !contactEntries[position].isSelected

                if (contactEntries[position].isSelected) {
                    multipleContacts.add(contactEntries[position])
                } else {
                    multipleContacts.remove(contactEntries[position])
                }
                holder.contactMultipleSelector.isChecked = contactEntries[position].isSelected
            }
        }
    }

    private fun bindView(prefix: String, text: String?, holderView: TextView) {
        if (text != null) {
            holderView.visibility = View.VISIBLE
            holderView.text = holderView.context.getString(R.string.prefix_placeholder, prefix, text)
        } else
            holderView.visibility = View.GONE
    }

    override fun getItemCount(): Int = contactEntries.size

    fun addContactList(mode: ContactSelectionMode, contacts: ArrayList<Contact>) {
        contactSelectionMode = mode
        contacts.forEach {
            contactEntries.add(SelectableContact(it))
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: ItemListContactBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val contact = itemView.root
        val contactId = itemView.tvId
        val contactName = itemView.tvName
        val contactEmail = itemView.tvEmail
        val contactRemoveButton = itemView.buttonRemoveContact
        val contactSingleSelector = itemView.singleSelectContact
        val contactMultipleSelector = itemView.multipleSelectContact
    }
}

internal enum class ContactSelectionMode {
    SINGLE, MULTIPLE, OTHER
}
