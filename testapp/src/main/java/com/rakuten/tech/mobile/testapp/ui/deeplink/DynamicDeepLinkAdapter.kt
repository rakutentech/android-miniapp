package com.rakuten.tech.mobile.testapp.ui.deeplink

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListDynamicDeeplinkBinding

class DeepLinkListAdapter(private val listener: DeepLinkListener) :
        RecyclerView.Adapter<DeepLinkListAdapter.ViewHolder?>(),
        DeepLinkAdapterPresenter {
    private var deepLinkEntries = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemListDynamicDeeplinkBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.deepLink.text = deepLinkEntries[position]
        holder.deepLinkRemoveButton.setOnClickListener { removeDeepLinkAt(position) }
        holder.rootView.setOnClickListener {
            listener.onDeepLinkItemClick(position)
        }
    }

    override fun getItemCount(): Int = deepLinkEntries.size

    override fun addDeepLink(position: Int, deepLink: String) {
        deepLinkEntries.add(position, deepLink)
        notifyItemInserted(position)
    }

    override fun updateDeepLink(position: Int, deepLink: String) {
        deepLinkEntries[position] = deepLink
        notifyItemChanged(position)
    }

    override fun addDeepLinkList(deepLinks: ArrayList<String>) {
        deepLinkEntries = deepLinks
        notifyDataSetChanged()
    }

    private fun removeDeepLinkAt(position: Int) {
        deepLinkEntries.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)
    }

    override fun provideDeepLinkEntries() = deepLinkEntries

    inner class ViewHolder(itemView: ItemListDynamicDeeplinkBinding) : RecyclerView.ViewHolder(itemView.root) {
        val deepLink = itemView.tvDeepLink
        val deepLinkRemoveButton = itemView.buttonRemoveDeepLink
        val rootView = itemView.root
    }
}

interface DeepLinkListener {
    fun onDeepLinkItemClick(position: Int)
}

interface DeepLinkAdapterPresenter {
    fun addDeepLink(position: Int, deepLink: String)
    fun updateDeepLink(position: Int, deepLink: String)
    fun addDeepLinkList(deepLinks: ArrayList<String>)
    fun provideDeepLinkEntries(): ArrayList<String>
}
