package com.rakuten.tech.mobile.testapp.ui.userdata

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListContactBinding

class ContactListAdapter : RecyclerView.Adapter<ContactListAdapter.ViewHolder?>(),
    ContactAdapterPresenter {
    private var contactEntries = ArrayList<Contact>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemListContactBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contactEntries[position]
        bindNonVisibleView("Id: ", contact.id, holder.contactId)
        bindNonVisibleView("Name: ", contact.name, holder.contactName)
        bindNonVisibleView("Email: ", contact.email, holder.contactEmail)

        holder.contactRemoveButton.setOnClickListener { removeContactAt(position) }
    }

    private fun bindNonVisibleView(prefix: String, text: String?, holderView: TextView) {
        if (text != null) {
            holderView.visibility = View.VISIBLE
            holderView.text = holderView.context.getString(R.string.prefix_placeholder, prefix, text)
        } else
            holderView.visibility = View.GONE
    }

    override fun getItemCount(): Int = contactEntries.size

    override fun addContact(position: Int, contact: Contact) {
        contactEntries.add(position, contact)
        notifyItemInserted(position)
    }

    override fun addContactList(contacts: ArrayList<Contact>) {
        contactEntries = contacts
        notifyDataSetChanged()
    }

    override fun removeContactAt(position: Int) {
        contactEntries.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)
    }

    override fun provideContactEntries() = contactEntries

    inner class ViewHolder(itemView: ItemListContactBinding) : RecyclerView.ViewHolder(itemView.root) {
        val contactId = itemView.tvId
        val contactName = itemView.tvName
        val contactEmail = itemView.tvEmail
        val contactRemoveButton = itemView.buttonRemoveContact
    }
}
