package com.rakuten.tech.mobile.testapp.ui.deeplink

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListDeeplinkBinding

class DeeplinkListAdapter(private val deeplinkListener: DeeplinkListener) : RecyclerView.Adapter<DeeplinkListAdapter.ViewHolder?>(),
        DeeplinkAdapterPresenter {
    private var deeplinkEntries = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemListDeeplinkBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.deeplink.text = deeplinkEntries[position]
        holder.deeplinkRemoveButton.setOnClickListener { removeDeeplinkAt(position) }
        holder.rootView.setOnClickListener {
            deeplinkListener.onDeeplinkItemClick(position)
        }
    }

    override fun getItemCount(): Int = deeplinkEntries.size

    override fun addDeeplink(position: Int, deeplink: String) {
        deeplinkEntries.add(position, deeplink)
        notifyItemInserted(position)
    }

    override fun updateDeeplink(position: Int, deeplink: String) {
        deeplinkEntries[position] = deeplink
        notifyItemChanged(position)
    }

    override fun addDeeplinkList(deeplinks: ArrayList<String>) {
        deeplinkEntries = deeplinks
        notifyDataSetChanged()
    }

    override fun removeDeeplinkAt(position: Int) {
        deeplinkEntries.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)
    }

    override fun provideDeeplinkEntries() = deeplinkEntries

    inner class ViewHolder(itemView: ItemListDeeplinkBinding) : RecyclerView.ViewHolder(itemView.root) {
        val deeplink = itemView.tvDeeplink
        val deeplinkRemoveButton = itemView.buttonRemoveDeeplink
        val rootView = itemView.root
    }
}

interface DeeplinkListener {
    fun onDeeplinkItemClick(position: Int)
}

interface DeeplinkAdapterPresenter {
    fun addDeeplink(position: Int, deeplink: String)
    fun updateDeeplink(position: Int, deeplink: String)
    fun addDeeplinkList(deeplinks: ArrayList<String>)
    fun removeDeeplinkAt(position: Int)
    fun provideDeeplinkEntries(): ArrayList<String>
}
