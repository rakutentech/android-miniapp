package com.rakuten.tech.mobile.testapp.ui.userdata

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListContactBinding

class ContactListAdapter : RecyclerView.Adapter<ContactListAdapter.ViewHolder?>(),
    ContactAdapterPresenter {
    private var contactEntries = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemListContactBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.contactName.text = contactEntries[position]
        holder.contactRemoveButton.setOnClickListener { removeContactAt(position) }
    }

    override fun getItemCount(): Int = contactEntries.size

    override fun addContact(position: Int, contact: String) {
        contactEntries.add(position, contact)
        notifyItemInserted(position)
    }

    override fun addContactList(contacts: ArrayList<String>) {
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
        val contactName: AppCompatTextView = itemView.textContact
        val contactRemoveButton: ImageView = itemView.buttonRemoveContact
    }
}
