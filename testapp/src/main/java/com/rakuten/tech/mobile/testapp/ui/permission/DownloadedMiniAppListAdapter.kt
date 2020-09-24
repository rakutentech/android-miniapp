package com.rakuten.tech.mobile.testapp.ui.permission

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
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListDownloadedMiniappBinding

// TODO: rename
class DownloadedMiniAppListAdapter(val miniAppList: DownloadedMiniAppList) :
    ListAdapter<MiniAppInfo, DownloadedMiniAppViewHolder>(MiniAppDiffCallback()) {

    // TODO: Pair
    private var miniApps: ArrayList<MiniAppInfo> = arrayListOf()
    private var miniAppPermissions: ArrayList<String> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadedMiniAppViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemListDownloadedMiniappBinding.inflate(layoutInflater, parent, false)
        return DownloadedMiniAppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadedMiniAppViewHolder, position: Int) {
        holder.itemView.tag = holder
        holder.bindTo(holder.binding, miniApps[position], miniAppList)
    }

    override fun getItemCount(): Int = miniApps.size

    fun addList(miniApps: List<MiniAppInfo>, miniAppPermissions: ArrayList<String>) {
        this.miniApps.clear()
        this.miniApps.addAll(miniApps)
        this.miniAppPermissions = miniAppPermissions
        notifyDataSetChanged()
    }
}

interface DownloadedMiniAppList {
    fun onMiniAppItemClick(miniAppInfo: MiniAppInfo)
}

class DownloadedMiniAppViewHolder(val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bindTo(
        binding: ViewDataBinding,
        miniAppInfo: MiniAppInfo,
        miniAppList: DownloadedMiniAppList
    ) {
        if (binding is ItemListDownloadedMiniappBinding) {
            binding.miniapp = miniAppInfo
            setIcon(
                binding.root.context,
                Uri.parse(miniAppInfo.icon),
                binding.appIcon
            )

            binding.itemRoot.setOnClickListener {
                miniAppList.onMiniAppItemClick(miniAppInfo)
            }
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

fun setIcon(context: Context, uri: Uri, view: ImageView) {
    Glide.with(context)
        .load(uri).apply(RequestOptions().circleCrop())
        .placeholder(R.drawable.ic_default)
        .into(view)
}
