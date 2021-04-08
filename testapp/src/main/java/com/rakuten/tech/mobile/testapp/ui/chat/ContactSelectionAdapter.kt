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
    private var contactSelectionMode = ""
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
        if (contactSelectionMode == "single") holder.contactSingleSelector.visibility = View.VISIBLE
        else if (contactSelectionMode == "multiple") holder.contactMultipleSelector.visibility =
            View.VISIBLE

        holder.contactSingleSelector.isChecked = contactEntries[position].isSelected
        holder.contact.setOnClickListener {
            if (contactSelectionMode == "single") {
                contactEntries.forEach {
                    it.isSelected = false
                }
                contactEntries[position].isSelected = true

                if (selectedSingleView != null && holder.contactSingleSelector != selectedSingleView)
                    selectedSingleView?.isChecked = false

                selectedSingleView = holder.contactSingleSelector
                selectedSingleView?.isChecked = true
                singleContact = contactEntries[position]
            } else if (contactSelectionMode == "multiple") {

            }

//            val isSingle = contactSelectionMode == "single" && entry.isSelected
//            holder.contactSingleSelector.isChecked = isSingle
//            if (isSingle) singleContact = entry
//
//            val isMultiple = contactSelectionMode == "multiple" && entry.isSelected
//            holder.contactMultipleSelector.isChecked = isMultiple
//            if (isMultiple) multipleContacts.add(entry)
//            else multipleContacts.removeAt(position)
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

    fun addContactList(mode: String, contacts: ArrayList<Contact>) {
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
