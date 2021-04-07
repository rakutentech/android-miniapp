package com.rakuten.tech.mobile.testapp.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.js.userinfo.Contact
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListContactSelectionBinding

internal class ContactSelectionAdapter(private val contactSelectionListener: ContactSelectionListener) :
    RecyclerView.Adapter<ContactSelectionAdapter.ViewHolder?>() {
    private var contactEntries = ArrayList<Contact>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemListContactSelectionBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.contactId.text =
            holder.contactId.context.getString(
                R.string.prefix_placeholder,
                "Id: ",
                contactEntries[position].id
            )
        holder.contactId.setOnClickListener {
            contactSelectionListener.onContactSelect(contactEntries[position])
        }
    }

    override fun getItemCount(): Int = contactEntries.size

    fun addContactList(contacts: ArrayList<Contact>) {
        contactEntries = contacts
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: ItemListContactSelectionBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val contactId: AppCompatTextView = itemView.textContact
    }

    interface ContactSelectionListener {
        fun onContactSelect(contact: Contact)
    }
}
