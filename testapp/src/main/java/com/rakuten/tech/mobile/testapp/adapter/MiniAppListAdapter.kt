package com.rakuten.tech.mobile.testapp.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.R
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListMiniappBinding

class MiniAppListAdapter(var miniapps: List<MiniAppInfo>, val miniAppList: MiniAppList) :
    ListAdapter<MiniAppInfo, MiniAppsListViewHolder>(MiniAppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiniAppsListViewHolder {
        val binding = ItemListMiniappBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MiniAppsListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MiniAppsListViewHolder, position: Int) {
        holder.itemView.tag = holder
        holder.bindTo(holder.binding, miniapps[position], miniAppList)
    }

    override fun getItemCount() = miniapps.size

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
    fun onMiniAppItemClick(miniapp: MiniAppInfo)
}

class MiniAppsListViewHolder(val binding: ItemListMiniappBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bindTo(binding: ItemListMiniappBinding, miniapp: MiniAppInfo, miniAppList: MiniAppList) {
        binding.miniapp = miniapp
        setIcon(binding.root.context, Uri.parse(miniapp.icon), binding.ivAppIcon)

        binding.tvVersion.isSelected = true

        binding.itemRoot.setOnClickListener {
            miniAppList.onMiniAppItemClick(miniapp)
        }
    }

}

fun setIcon(context: Context, uri: Uri, view: ImageView) {
    Glide.with(context)
        .load(uri).apply(RequestOptions().circleCrop())
        .placeholder(R.drawable.ic_default)
        .into(view)
}
