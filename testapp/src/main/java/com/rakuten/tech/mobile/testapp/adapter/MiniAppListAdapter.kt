package com.rakuten.tech.mobile.testapp.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.Version
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemFooterMiniappBinding
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListMiniappBinding
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemSectionMiniappBinding
import com.rakuten.tech.mobile.testapp.helper.setIcon
import java.util.TreeSet

class MiniAppListAdapter(
    private val miniApps: ArrayList<MiniAppInfo>,
    private val miniAppListener: MiniAppListener
) : ListAdapter<MiniAppInfo, MiniAppsListViewHolder>(MiniAppDiffCallback()) {

    private val sectionPos = TreeSet<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiniAppsListViewHolder {
        val binding = when (viewType) {
            R.layout.item_section_miniapp -> ItemSectionMiniappBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            R.layout.item_footer_miniapp -> ItemFooterMiniappBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            else -> ItemListMiniappBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        }
        return MiniAppsListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MiniAppsListViewHolder, position: Int) {
        holder.itemView.tag = holder
        holder.bindTo(holder.binding, miniApps[position], miniAppListener)
    }

    override fun getItemCount() = miniApps.size

    override fun getItemViewType(position: Int): Int =
        when {
            sectionPos.contains(position) -> R.layout.item_section_miniapp
            (position == itemCount - 1) -> R.layout.item_footer_miniapp
            else -> R.layout.item_list_miniapp
        }

    fun addListWithSection(list: List<MiniAppInfo>) {
        miniApps.clear()
        sectionPos.clear()
        val sectionMiniApp = TreeSet<String>()

        for (item in list) {
            if (!sectionMiniApp.contains(item.id)) {
                miniApps.add(item)
                sectionPos.add(miniApps.size - 1)
                sectionMiniApp.add(item.id)
            }
            miniApps.add(item)
        }
        addFooter()

        notifyDataSetChanged()
    }

    private fun addFooter() {
        if (miniApps.size > 0) {
            val footerItem = MiniAppInfo("", "", "", Version("", ""))
            miniApps.add(itemCount, footerItem)
        }
    }
}

private class MiniAppDiffCallback : DiffUtil.ItemCallback<MiniAppInfo>() {

    override fun areItemsTheSame(oldItem: MiniAppInfo, newItem: MiniAppInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MiniAppInfo, newItem: MiniAppInfo): Boolean {
        return oldItem == newItem
    }
}

interface MiniAppListener {
    fun onMiniAppItemClick(miniAppInfo: MiniAppInfo)
}

class MiniAppsListViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bindTo(binding: ViewDataBinding, miniAppInfo: MiniAppInfo, miniAppListener: MiniAppListener) {
        if (binding is ItemSectionMiniappBinding)
            binding.miniapp = miniAppInfo
        else if (binding is ItemListMiniappBinding) {
            binding.miniapp = miniAppInfo
            setIcon(binding.root.context, Uri.parse(miniAppInfo.icon), binding.ivAppIcon)

            binding.tvVersion.isSelected = true

            binding.itemRoot.setOnClickListener {
                miniAppListener.onMiniAppItemClick(miniAppInfo)
            }
        }
    }
}
