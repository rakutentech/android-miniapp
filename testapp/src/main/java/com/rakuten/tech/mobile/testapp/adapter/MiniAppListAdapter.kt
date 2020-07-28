package com.rakuten.tech.mobile.testapp.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListMiniappBinding
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemSectionMiniappBinding
import java.util.*

class MiniAppListAdapter(val miniapps: ArrayList<MiniAppInfo>, val miniAppList: MiniAppList) :
    ListAdapter<MiniAppInfo, MiniAppsListViewHolder>(MiniAppDiffCallback()) {

    private val sectionPos = TreeSet<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiniAppsListViewHolder {
        val binding = when (viewType) {
            R.layout.item_section_miniapp -> ItemSectionMiniappBinding.inflate(
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
        holder.bindTo(holder.binding, miniapps[position], miniAppList)
    }

    override fun getItemCount() = miniapps.size

    override fun getItemViewType(position: Int): Int =
        if (sectionPos.contains(position)) R.layout.item_section_miniapp
        else R.layout.item_list_miniapp

    fun addListWithSection(list: List<MiniAppInfo>) {
        miniapps.clear()
        sectionPos.clear()
        val sectionMiniApp = TreeSet<String>()

        for (item in list) {
            if (!sectionMiniApp.contains(item.id)) {
                miniapps.add(item)
                sectionPos.add(miniapps.size - 1)
                sectionMiniApp.add(item.id)
            }
            miniapps.add(item)
        }

        notifyDataSetChanged()
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

interface MiniAppList {
    fun onMiniAppItemClick(miniAppInfo: MiniAppInfo)
}

class MiniAppsListViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bindTo(binding: ViewDataBinding, miniAppInfo: MiniAppInfo, miniAppList: MiniAppList) {
        if (binding is ItemSectionMiniappBinding)
            binding.miniapp = miniAppInfo
        else if (binding is ItemListMiniappBinding) {
            binding.miniapp = miniAppInfo
            setIcon(binding.root.context, Uri.parse(miniAppInfo.icon), binding.ivAppIcon)

            binding.tvVersion.isSelected = true

            binding.itemRoot.setOnClickListener {
                miniAppList.onMiniAppItemClick(miniAppInfo)
            }
        }
    }
}

fun setIcon(context: Context, uri: Uri, view: ImageView) {
    Glide.with(context)
        .load(uri).apply(RequestOptions().circleCrop())
        .placeholder(R.drawable.ic_default)
        .into(view)
}
