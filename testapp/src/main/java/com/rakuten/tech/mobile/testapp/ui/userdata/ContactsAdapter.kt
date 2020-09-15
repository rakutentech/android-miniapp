package com.rakuten.tech.mobile.testapp.ui.userdata

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListContactsBinding
import kotlinx.android.synthetic.main.item_list_contacts.view.*

class ContactsAdapter : RecyclerView.Adapter<ContactsAdapter.ViewHolder?>() {
    private var contactNames = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemListContactsBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.contactName.text = contactNames[position]
    }

    override fun getItemCount(): Int = contactNames.size

    fun addContactList(
        names: ArrayList<String>
    ) {
        contactNames = names
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactName: TextView = itemView.textContact
    }
}
